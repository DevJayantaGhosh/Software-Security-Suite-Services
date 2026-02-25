package com.jayanta.projectmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     *  EXACT SAME as User Service - UTF-8 encoding (RECOMMENDED)
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        log.debug(" Extracting username from JWT");
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.debug("✅ Username extracted: {}", username);
            return username;
        } catch (Exception e) {
            log.error("❌ JWT Parse FAILED: {}", e.getMessage());
            throw e;
        }
    }

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        log.debug(" Extracting authorities from JWT");
        Claims claims = extractAllClaims(token);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        log.debug("✅ Roles found: {}", roles);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        log.debug("✅ Authorities created: {}", authorities);
        return authorities;
    }

    public boolean isTokenValid(String token) {
        log.debug(" Validating JWT token");
        try {
            Claims claims = extractAllClaims(token);
            boolean notExpired = !claims.getExpiration().before(new Date());
            log.debug("✅ Token valid: {} (expires: {})", notExpired, claims.getExpiration());
            return notExpired;
        } catch (Exception e) {
            log.error("❌ Token validation FAILED: {}", e.getMessage());
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        log.debug(" Parsing JWT claims");
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
