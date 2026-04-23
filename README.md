<h1 align="center">🛡️ Software Security Suite — Backend Services 🛡️</h1>

<p align="center">
  <strong>Backend API layer for the Sherlock Security Suite. Serves both the Electron Desktop App and Web Portal. Two independent Spring Boot microservices connected through asymmetric RSA JWT authentication.</strong>
</p>

---

## System Architecture

**Spring Boot 3.3.4 | Java 21 | Microservices | RS256 Asymmetric JWT | RSA-4096**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        SHERLOCK SECURITY SUITE                          │
│                 (Desktop App  &  Web Portal)                            │
│                          Frontend — Port 5173                           │
└───────────┬─────────────────────────────────────────────┬───────────────┘
            │                                             │
            │  User Authentication                        │  Product, Repo &
            │  Authorization, Registration                │  Dependency Management
            │  OTP, Password Recovery                     │  Security Scans
            │                                             │
            ▼                                             ▼
┌───────────────────────────────┐       ┌───────────────────────────────────┐
│                               │       │                                   │
│  USER MANAGEMENT SERVICE      │       │  PRODUCT MANAGEMENT SERVICE       │
│  Port 8181                    │       │  Port 9090                        │
│                               │       │                                   │
│  • AuthController             │       │  • ProductController              │
│  • UserController             │       │  • RepoController                 │
│                               │       │  • DependencyController           │
│  JWT Issuer (signs tokens)    │       │  JWT Validator (verify only)      │
│  (encrypted RSA private key)  │       │  (RSA public key only)            │
│                               │       │                                   │
│         PostgreSQL            │       │           MongoDB                 │
│      USER_MANAGEMENT_DB       │       │     product_management_db         │
│                               │       │                                   │
└───────────────────────────────┘       └───────────────────────────────────┘
```

---

## Services Overview

| Service                    | Port   | Database         | Purpose                                            |
|----------------------------|--------|------------------|----------------------------------------------------|
| **User Management**        | `8181` | PostgreSQL       | Auth, registration, roles, license, OTP, email     |
| **Product Management**     | `9090` | MongoDB          | Products, repos, dependencies, scan results        |

---

## Asymmetric RSA JWT Authentication

The services use **RS256 asymmetric JWT authentication**. The User Management Service holds the **encrypted private key** and signs tokens. The Product Management Service holds only the **public key** and can verify tokens but **cannot forge them** — even if compromised.

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                          ASYMMETRIC RSA JWT (RS256)                           │
│                                                                               │
│   Private Key (encrypted, password-protected)  -->  User Service ONLY         │
│   Public Key                                   -->  Both Services             │
└──────────────────┬────────────────────────────────────┬───────────────────────┘
                   │                                    │
         Used to SIGN                         Used to VERIFY
       (encrypted private key)                 (public key only)
                   │                                    │
┌──────────────────▼────────────────────┐  ┌────────────▼───────────────────────┐
│                                       │  │                                    │
│    USER MANAGEMENT SERVICE            │  │    PRODUCT MANAGEMENT SERVICE      │
│           (Port 8181)                 │  │           (Port 9090)              │
│                                       │  │                                    │
│  1. Client sends credentials          │  │  1. Client sends JWT in header     │
│     POST /api/auth/login              │  │     Authorization: Bearer <token>  │
│                                       │  │                                    │
│  2. Validates email + password        │  │  2. JwtAuthenticationFilter        │
│     against PostgreSQL                │  │     extracts token from request    │
│                                       │  │                                    │
│  3. JwtService.generateToken()        │  │  3. JwtService.validateToken()     │
│     Signs JWT with private key        │  │     Verifies with public key       │
│     Includes license expiration       │  │                                    │
│                                       │  │  4. Validates token expiry (1 hr)  │
│  4. Returns JWT to client             │  │     Validates license expiration   │
│                                       │  │     Extracts user email & roles    │
│                                       │  │     Sets SecurityContext           │
│    ROLE: TOKEN ISSUER                 │  │                                    │
│                                       │  │    ROLE: TOKEN VALIDATOR           │
│                                       │  │    (cannot forge tokens)           │
└──────────────────┬────────────────────┘  └────────────┬───────────────────────┘
                   │                                    │
                   │       ┌──────────────────┐         │
                   │       │                  │         │
                   └──────>│     Client       │<────────┘
                   JWT     │  (Desktop App /  │  Sends JWT
                   issued  │   Web Portal)    │  with every
                           │                  │  API request
                           └──────────────────┘
```

### JWT Token Claims

```json
{
  "sub": "user@email.com",
  "roles": ["ROLE_Admin"],
  "isInternal": true,
  "licenseExpiredOn": "2025-12-31T23:59:59",
  "iat": 1711100000,
  "exp": 1711103600
}
```

| Claim              | Description                                                   |
|--------------------|---------------------------------------------------------------|
| `sub`              | User email (subject)                                          |
| `roles`            | User roles                                                    |                                             
| `isInternal`       | Whether user is internal (bypasses license check)             |
| `licenseExpiredOn` | License expiration timestamp (null for Admin/Internal users)  |
| `iat`              | Issued at timestamp                                           |
| `exp`              | Expiration timestamp                                          |

### Validation Flow

Both services validate:
- ✅ RSA signature verification (RS256)
- ✅ Token expiration (1 hour)
- ✅ License expiration (from `licenseExpiredOn` claim — no DB call needed)
                     |

---

## RSA Key Generation

Generate the RSA key pair using the **Software-Crypto-Shield** KeyGenerator tool (one-time setup):

> 🔗 **Tool:** [Software-Crypto-Shield](https://github.com/DevJayantaGhosh/Software-Crypto-Shield) — KeyGenerator

```bash
# Generate RSA-4096 key pair (encrypted private key + public key)
KeyGenerator.exe generate rsa -s 4096 -o ./mykeys -p MyPassword
```

This generates:
- `encrypted-private.pem` — RSA-4096 private key encrypted with **PBES2 + AES-256-CBC** (password-protected)
- `public.pem` — RSA-4096 public key in PKCS#1 PEM format

Place the keys in `src/main/resources/keys/`:

```
user-management-service/src/main/resources/keys/
  ├── encrypted-private.pem   ← 🔒 Encrypted private key (password-protected)
  └── public.pem              ← 🔓 Public key

product-management-service/src/main/resources/keys/
  └── public.pem              ← 🔓 Public key ONLY (copy from user-service)
```

> ⚠️ **Important:** The `encrypted-private.pem` file must NEVER be placed in the product-management-service. Only the `public.pem` is shared.

---

## Quick Start

```bash
# Prerequisites: Java 21, Maven, PostgreSQL, MongoDB, Software-Crypto-Shield KeyGenerator

# 1. Generate RSA-4096 keys using KeyGenerator (see above)

# 2. Set environment variables for User Management Service:
#    DB_USERNAME, DB_PASSWORD, JWT_PRIVATE_KEY_PASSPHRASE, JWT_EXPIRATION,
#    MAIL_USERNAME, MAIL_PASSWORD

# 3. Set environment variables for Product Management Service:
#    IS_CLOUD_DB, MONGODB_HOST, MONGODB_PORT, MONGODB_DATABASE (or MONGODB_URI)
#    (No JWT secret needed — uses public key from resources)

# 4. Start User Management Service
cd user-management-service
./mvnw spring-boot:run          # → http://localhost:8181

# 5. Start Product Management Service
cd product-management-service
./mvnw spring-boot:run          # → http://localhost:9090

# Swagger UI:
#   User Service:    http://localhost:8181/swagger-ui/index.html
#   Product Service: http://localhost:9090/swagger-ui/index.html
