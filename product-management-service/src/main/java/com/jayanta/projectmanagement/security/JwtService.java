package com.jayanta.projectmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Service - Token Verifier (RS256 with RSA-4096 public key)
 *
 * Verifies JWT tokens signed by user-management-service using the shared RSA public key.
 * Extracts roles, license claims, and validates token expiration + license expiration.
 * This service never signs tokens — it only verifies.
 */
@Service
@Slf4j
public class JwtService {

    /*
     * Register BouncyCastle as a JCA security provider.
     */
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;


    // ─── Key Loading (fresh load on every call) ─────────────────────────────────

    /**
     * Load the RSA public key from PEM file.
     * Handles:
     *   - BEGIN PUBLIC KEY       (X.509/SPKI)
     *   - BEGIN RSA PUBLIC KEY   (PKCS#1)
     *   - BEGIN RSA PRIVATE KEY  (extracts public component from key pair)
     */
    private PublicKey loadPublicKey() {
        try (PEMParser parser = new PEMParser(new InputStreamReader(
                resolveKeyPath(publicKeyPath), StandardCharsets.UTF_8))) {

            Object pem = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (pem instanceof SubjectPublicKeyInfo spki) {
                return converter.getPublicKey(spki);
            }
            if (pem instanceof PEMKeyPair pair) {
                return converter.getPublicKey(pair.getPublicKeyInfo());
            }

            throw new IllegalStateException("Unknown PEM public key type: " + pem.getClass().getName());
        } catch (Exception e) {
            log.error("Failed to load public key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load RSA public key", e);
        }
    }

    /**
     * Resolve a key path that can be either a classpath resource (classpath:...) or a file system path.
     */
    private InputStream resolveKeyPath(String path) {
        try {
            if (path.startsWith("classpath:")) {
                ResourceLoader loader = new DefaultResourceLoader();
                return loader.getResource(path).getInputStream();
            }
            return Files.newInputStream(Paths.get(path));
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve key path: " + path, e);
        }
    }

    // ─── Token Parsing ──────────────────────────────────────────────────────────

    public String extractUsername(String token) {
        log.debug("Extracting username from JWT");
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("JWT parse failed: {}", e.getMessage());
            throw e;
        }
    }

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        log.debug("Extracting authorities from JWT");
        Claims claims = extractAllClaims(token);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        PublicKey publicKey = loadPublicKey();

        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ─── Token Validation ───────────────────────────────────────────────────────

    /**
     * Validate token: checks token expiration and license expiration.
     * Internal users and Admins bypass the license check.
     */
    public boolean isTokenValid(String token) {
        log.debug("Validating JWT token");
        try {
            Claims claims = extractAllClaims(token);

            // Check token expiration
            boolean notExpired = !claims.getExpiration().before(new Date());
            if (!notExpired) {
                log.debug("Token is expired (expiry: {})", claims.getExpiration());
                return false;
            }

            // Check license validity
            boolean licenseValid = isLicenseValid(claims);
            log.debug("Token valid: true, License valid: {}", licenseValid);
            return licenseValid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the license is valid based on token claims.
     * Internal users and Admins bypass the license check.
     * External users must have a licenseExpiredOn in the future.
     */
    private boolean isLicenseValid(Claims claims) {
        Boolean isInternal = claims.get("isInternal", Boolean.class);
        if (Boolean.TRUE.equals(isInternal)) {
            return true; // Internal users bypass license check
        }

        // Check roles - Admins bypass license check
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles != null && roles.contains("ROLE_Admin")) {
            return true;
        }

        String licenseExpStr = claims.get("licenseExpiredOn", String.class);
        if (licenseExpStr == null) {
            return false; // No license = denied
        }

        LocalDateTime licenseExpiry = LocalDateTime.parse(licenseExpStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return licenseExpiry.isAfter(LocalDateTime.now());
    }
}