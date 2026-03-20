<h1 align="center">🛡️ Software Security Suite — Backend Services 🛡️</h1>

<p align="center">
  <strong>Backend API layer for the Sherlock Security Suite. Serves both the Electron Desktop App and Web Portal. Two independent Spring Boot microservices connected through a shared JWT authentication mechanism.</strong>
</p>

---

## System Architecture

**Spring Boot 3.3.4 | Java 21 | Microservices**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        SHERLOCK SECURITY SUITE                           │
│                 (Electron Desktop App  &  Web Portal)                    │
│                          Frontend — Port 5173                            │
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
│  Port 8080                    │       │  Port 9090                        │
│                               │       │                                   │
│  • AuthController             │       │  • ProductController              │
│  • UserController             │       │  • RepoController                 │
│                               │       │  • DependencyController           │
│  JWT Issuer (generates token) │       │  JWT Validator (shared secret)    │
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
| **User Management**        | `8080` | PostgreSQL       | Auth, registration, roles, license, OTP, email     |
| **Product Management**     | `9090` | MongoDB          | Products, repos, dependencies, scan results        |

---

## Shared JWT Authentication

Both services share the **same `JWT_SECRET`**. The User Management Service is the **issuer** — it generates JWT tokens on successful login. The Product Management Service is the **validator** — it verifies incoming tokens using the same secret, enabling cross-service authentication without inter-service API calls.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SHARED JWT_SECRET                                  │
│                       (Same secret configured in both)                          │
└──────────────────┬──────────────────────────────────────┬───────────────────────┘
                   │                                      │
         Used to SIGN                           Used to VERIFY
                   │                                      │
┌──────────────────▼──────────────────┐  ┌────────────────▼───────────────────────┐
│                                     │  │                                        │
│    USER MANAGEMENT SERVICE          │  │    PRODUCT MANAGEMENT SERVICE          │
│           (Port 8080)               │  │           (Port 9090)                  │
│                                     │  │                                        │
│  1. Client sends credentials        │  │  1. Client sends JWT in header         │
│     POST /api/auth/login            │  │     Authorization: Bearer <token>      │
│                                     │  │                                        │
│  2. Validates email + password      │  │  2. JwtAuthenticationFilter extracts   │
│     against PostgreSQL              │  │     token from request                 │
│                                     │  │                                        │
│  3. JwtService.generateToken()      │  │  3. JwtService.validateToken()         │
│     Signs JWT with JWT_SECRET       │  │     Verifies signature with JWT_SECRET │
│                                     │  │                                        │
│  4. Returns JWT to client           │  │  4. Extracts user email & roles        │
│                                     │  │     Sets SecurityContext               │
│                                     │  │                                        │
│    ROLE: TOKEN ISSUER               │  │      ROLE: TOKEN VALIDATOR             │
│                                     │  │                                        │
└──────────────────┬──────────────────┘  └────────────────┬───────────────────────┘
                   │                                      │
                   │        ┌──────────────────┐          │
                   │        │                  │          │
                   └───────>│     Client       │<─────────┘
                   JWT      │  (Desktop App /  │   Sends JWT
                   issued   │   Web Portal)    │   with every
                            │                  │   API request
                            └──────────────────┘
```

**Flow:**
1. **Login** → Client sends credentials to User Service (`POST /api/auth/login`)
2. **JWT Issued** → User Service validates credentials, signs a JWT with `JWT_SECRET`, returns token
3. **API Calls** → Client includes JWT in `Authorization: Bearer <token>` header for all subsequent requests
4. **JWT Validated** → Product Service verifies the token signature using the same `JWT_SECRET` — no call to User Service needed

---

## Quick Start

```bash
# Prerequisites: Java 21, Maven, PostgreSQL, MongoDB

# 1. Set environment variables for both services:
#    DB_USERNAME, DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION,
#    MAIL_USERNAME, MAIL_PASSWORD,
#    IS_CLOUD_DB, MONGODB_HOST, MONGODB_PORT, MONGODB_DATABASE (or MONGODB_URI)

# 2. Start User Management Service
cd user-management-service
./mvnw spring-boot:run          # → http://localhost:8080

# 3. Start Product Management Service
cd product-management-service
./mvnw spring-boot:run          # → http://localhost:9090

# Swagger UI:
#   User Service:    http://localhost:8080/swagger-ui/index.html
#   Product Service: http://localhost:9090/swagger-ui/index.html





