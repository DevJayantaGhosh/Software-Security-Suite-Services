<h1 align="center">🔐 User Management Service 🔐</h1>

<p align="center">
  <strong>User Management Service is part of the Software Security Suite — handles user registration, authentication, role-based authorization, license activation, OTP-based password recovery, and email notifications. Serves both the Electron Desktop App and Web Portal.</strong>
</p>

---

## High-Level Architecture

**Spring Boot 3.3.4 | Java 21 | PostgreSQL | RS256 Asymmetric JWT (RSA-4096)**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          USER MANAGEMENT SERVICE                             │
│                              (Port 8181)                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────────────────────────┐ │
│  │   Clients   │───>│   Security   │───>│         REST Controllers         │ │
│  │  (Desktop   │    │    Filter    │    │                                  │ │
│  │   & Web     │    │    Chain     │    │  • AuthController                │ │
│  │   Portal)   │    │              │    │  • UserController                │ │
│  └─────────────┘    │  ┌────────┐  │    │                                  │ │
│                     │  │  JWT   │  │    │                                  │ │
│                     │  │ Filter │  │    │                                  │ │
│                     │  └────────┘  │    │                                  │ │
│                     └──────────────┘    └───────────────┬──────────────────┘ │
│                                                         │                    │
│                            ┌────────────────────────────┤                    │
│                            │                            │                    │
│                  ┌─────────▼──────────┐     ┌─────────────▼────────────────┐ │
│                  │   Service Layer     │    │      Security Layer          │ │
│                  │                     │    │                              │ │
│                  │  • UserService      │    │  • JwtService                │ │
│                  │  • OtpService       │    │  • JwtAuthenticationFilter   │ │
│                  │  • EmailService     │    │  • CustomUserDetailsService  │ │
│                  │                     │    │  • SecurityConfig            │ │
│                  └─────────┬──────────┘     └──────────────────────────────┘ │
│                            │                                                 │
│                  ┌─────────▼──────────┐     ┌──────────────────────────────┐ │
│                  │  Repository Layer  │     │      External Services       │ │
│                  │  (Spring Data JPA) │     │                              │ │
│                  │                    │     │  • Gmail SMTP                │ │
│                  │  • UserRepository  │     │    (OTP Email Delivery)      │ │
│                  │  • RoleRepository  │     │                              │ │
│                  │  • OtpRepository   │     └──────────────────────────────┘ │
│                  └─────────┬──────────┘                                      │
│                            │                                                 │
│                  ┌─────────▼──────────┐     ┌──────────────────────────────┐ │
│                  │    PostgreSQL      │     │    RSA Keys (resources)      │ │
│                  │ USER_MANAGEMENT_DB │     │                              │ │
│                  │                    │     │  encrypted-private.pem       │ │
│                  │  Tables:           │     │  public.pem                  │ │
│                  │   - users          │     │                              │ │
│                  │   - roles          │     │  Stored in:                  │ │
│                  │   - otps           │     │  src/main/resources/keys/    │ │
│                  └────────────────────┘     └──────────────────────────────┘ │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Request Flow

```
Client Request (Desktop App / Web Portal)
      │
      ▼
┌──────────────────┐
│   CORS Filter    │  (Allows Desktop, Web Portal, localhost origins)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  JWT Auth Filter │  (Extracts & validates JWT — RS256 signature + expiration + license)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  SecurityConfig  │  (Role-based: Admin, Internal, External)
│  @PreAuthorize   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐     ┌──────────────┐     ┌──────────────┐
│   Controller     │────>│   Service    │────>│  Repository  │
│  (REST Endpoint) │     │  (Business   │     │  (JPA /      │
│                  │<────│   Logic)     │<────│  PostgreSQL) │
└──────────────────┘     └──────────────┘     └──────────────┘
```

---

## JWT Authentication — Token Issuer 🔒

This service is the **JWT issuer**. It holds the **encrypted RSA-4096 private key** and signs all JWT tokens using the **RS256** algorithm.

### Token Generation

On successful login, `JwtService.generateToken()` creates a JWT signed with the encrypted private key containing:

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

| Claim              | Source                    | Description                              |
|--------------------|---------------------------|------------------------------------------|
| `sub`              | `AppUser.email`           | User email (subject)                     |
| `roles`            | `AppUser.getAuthorities()`| User roles                               |
| `isInternal`       | `AppUser.isInternal`      | Internal user flag (bypasses license)    |
| `licenseExpiredOn` | `AppUser.licenseExpiredOn`| License expiry (null for Admin/Internal) |
| `exp`              | Current time + 1 hour     | Token expiration                         |

### Token Validation

`isTokenValid()` checks:
- RSA signature verification (RS256 with public key)
- Username matches UserDetails
- Token not expired (1 hour lifetime)
- License not expired (from `licenseExpiredOn` claim)

### RSA Key Storage

```
src/main/resources/keys/
  ├── encrypted-private.pem   ← 🔒 Encrypted RSA-4096 private key (AES-256-CBC, password-protected)
  └── public.pem              ← 🔓 RSA public key (shared with product-management-service)
```

The private key passphrase is provided via the `JWT_PRIVATE_KEY_PASSPHRASE` environment variable.

---

## Tech Stack

| Layer              | Technology                                        |
|--------------------|---------------------------------------------------|
| Framework          | Spring Boot 3.3.4                                 |
| Language           | Java 21                                           |
| Database           | PostgreSQL                                        |
| ORM                | Spring Data JPA / Hibernate                       |
| Authentication     | JWT RS256 (JJWT 0.12.6) — RSA-4096 asymmetric    |
| Authorization      | Spring Security + @PreAuthorize                   |
| Email              | Spring Boot Mail (Gmail SMTP)                     |
| API Docs           | SpringDoc OpenAPI 2.6 (Swagger UI)                |
| Validation         | Jakarta Bean Validation                           |
| Build              | Maven                                             |

---

## API Endpoints

### Auth (`/api/auth`) — Public

| Method | Endpoint              | Description               |
|--------|-----------------------|---------------------------|
| POST   | `/register`           | Register new user         |
| POST   | `/login`              | Login (returns JWT)       |
| POST   | `/forgot-password`    | Send OTP to email         |
| POST   | `/verify-otp`         | Verify OTP code           |
| POST   | `/reset-password`     | Reset password after OTP  |

### Users (`/api/users`) — Authenticated

| Method | Endpoint              | Access       | Description                 |
|--------|-----------------------|--------------|-----------------------------|
| GET    | `/`                   | Any auth     | List all users              |
| GET    | `/internal`           | Any auth     | List internal users         |
| PUT    | `/{userId}`           | Admin only   | Update user                 |
| DELETE | `/{userId}`           | Admin only   | Delete single user          |
| DELETE | `/bulk`               | Admin only   | Bulk delete users           |

---

## Environment Variables

```env
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_PRIVATE_KEY_PASSPHRASE=your_rsa_key_passphrase
JWT_EXPIRATION=3600000
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

| Variable                     | Description                                          |
|------------------------------|------------------------------------------------------|
| `DB_USERNAME`                | PostgreSQL username                                  |
| `DB_PASSWORD`                | PostgreSQL password                                  |
| `JWT_PRIVATE_KEY_PASSPHRASE` | Passphrase to decrypt the encrypted RSA private key  |
| `JWT_EXPIRATION`             | Token lifetime in milliseconds (3600000 = 1 hour)    |
| `MAIL_USERNAME`              | Gmail address for OTP emails                         |
| `MAIL_PASSWORD`              | Gmail app password                                   |

---

## RSA Key Generation

Generate the RSA key pair using the **Software-Crypto-Shield** KeyGenerator tool:

> 🔗 **Tool:** [Software-Crypto-Shield](https://github.com/DevJayantaGhosh/Software-Crypto-Shield) — KeyGenerator

```bash
# 1. Generate RSA-4096 key pair (encrypted private key + public key)
KeyGenerator.exe generate rsa -s 4096 -o ./mykeys -p MyPassword

# 2. Place keys in resources
mkdir -p src/main/resources/keys
mv ./mykeys/encrypted-private.pem src/main/resources/keys/
mv ./mykeys/public.pem src/main/resources/keys/

# 3. Copy public key to product-management-service
mkdir -p ../product-management-service/src/main/resources/keys
cp src/main/resources/keys/public.pem ../product-management-service/src/main/resources/keys/
```


---

## Running

```bash
# Prerequisites: Java 21, Maven, PostgreSQL running on port 5432

# 1. Create database
psql -U postgres -c "CREATE DATABASE USER_MANAGEMENT_DB;"

# 2. Generate RSA keys and place in src/main/resources/keys/

# 3. Set environment variables (or create .env file)

# 4. Build and run
./mvnw spring-boot:run

# Service starts on http://localhost:8181
# Swagger UI: http://localhost:8181/swagger-ui/index.html
