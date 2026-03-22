<h1 align="center">📦 Product Management Service 📦</h1>

<p align="center">
  <strong>Product Management Service is part of the Software Security Suite — manages software products, source code repositories, dependencies, security scan results, digital signing artifacts, and approval workflows. Serves both the Electron Desktop App and Web Portal.</strong>
</p>

---

## High-Level Architecture

**Spring Boot 3.3.4 | Java 21 | MongoDB | RS256 Asymmetric JWT (RSA-4096 Public Key Verification)**

```
┌────────────────────────────────────────────────────────────────────────┐
│                     PRODUCT MANAGEMENT SERVICE                         │
│                            (Port 9090)                                 │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│ ┌─────────────┐  ┌──────────────┐  ┌──────────────────────────────┐    │
│ │   Clients   │─>│   Security   │─>│      REST Controllers        │    │
│ │  (Desktop   │  │    Filter    │  │                              │    │
│ │   & Web     │  │    Chain     │  │  • ProductController         │    │
│ │   Portal)   │  │              │  │  • RepoController            │    │
│ └─────────────┘  │  ┌────────┐  │  │  • DependencyController      │    │
│                  │  │  JWT   │  │  │                              │    │
│                  │  │ Filter │  │  │                              │    │
│                  │  └────────┘  │  │                              │    │
│                  └──────────────┘  └──────────────┬───────────────┘    │
│                                                   │                    │
│                       ┌───────────────────────────┤                    │
│                       │                           │                    │
│             ┌─────────▼────────┐   ┌───────────────▼──────────────┐    │
│             │  Service Layer    │  │     Security Layer           │    │
│             │                   │  │                              │    │
│             │ • ProductService  │  │  • JwtService                │    │
│             │ • RepoService     │  │  • JwtAuthenticationFilter   │    │
│             │ • DependencyServ. │  │  • SecurityConfig            │    │
│             │                   │  │                              │    │
│             └─────────┬────────┘   └──────────────────────────────┘    │
│                       │                                                │
│             ┌─────────▼────────┐   ┌──────────────────────────────┐    │
│             │ Repository Layer  │  │  RSA Key (resources)         │    │
│             │(Spring Data Mongo)|  │                              │    │
│             │                   │  │  public.pem ONLY             │    │
│             │ • ProductRepo     │  │                              │    │
│             │ • RepoRepository  │  │  No private key              │    │
│             │ • DependencyRepo  │  │  Cannot forge tokens         │    │
│             └─────────┬────────┘   │                              │    │
│                       │            │  Stored in:                  │    │
│                       │            │  src/main/resources/keys/    │    │
│                       │            └──────────────────────────────┘    │
├───────────────────────┼────────────────────────────────────────────────┤
│                       │                                                │
│             ┌─────────▼────────┐                                       │
│             │     MongoDB      │                                       │
│             │                  │                                       │
│             │ Collections:     │                                       │
│             │  - products      │                                       │
│             │  - repos         │                                       │
│             │  - dependencies  │                                       │
│             └──────────────────┘                                       │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
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
│  JWT Auth Filter │  (Validates JWT using RSA public key — RS256 signature + expiration + license)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  SecurityConfig  │  (Role-based: Admin for write ops)
│  @PreAuthorize   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐     ┌──────────────┐     ┌──────────────┐
│   Controller     │────>│   Service    │────>│  Repository  │
│  (REST Endpoint) │     │  (Business   │     │  (MongoDB)   │
│                  │<────│   Logic)     │<────│              │
└──────────────────┘     └──────────────┘     └──────────────┘
```

---

## JWT Authentication — Token Validator 🔓

This service is the **JWT validator**. It holds only the **RSA-4096 public key** and can verify tokens but **cannot sign or forge them**.

### Token Validation

`JwtService.isTokenValid()` checks:
- ✅ RSA signature verification (RS256 with public key)
- ✅ Token not expired (1 hour lifetime)
- ✅ License not expired (from `licenseExpiredOn` JWT claim — **no DB call, no inter-service call**)

### License Validation from Token Claims

The JWT token issued by the User Management Service contains a `licenseExpiredOn` claim. This service reads it directly from the token:

```java
// No database call needed — license info is embedded in the JWT
String licenseExp = claims.get("licenseExpiredOn", String.class);
boolean isInternal = claims.get("isInternal", Boolean.class);
// Internal users and Admins bypass license check
// External users: licenseExpiredOn must be in the future
```

### RSA Key Storage

```
src/main/resources/keys/
  └── public.pem       ← 🔓 RSA public key ONLY (copied from user-management-service)
```

> ⚠️ **Security:** This service has NO private key. Even if this service is compromised, attackers cannot forge JWT tokens.

---

## Tech Stack

| Layer              | Technology                                               |
|--------------------|----------------------------------------------------------|
| Framework          | Spring Boot 3.3.4                                        |
| Language           | Java 21                                                  |
| Database           | MongoDB (Cloud URI or Standalone)                        |
| ODM                | Spring Data MongoDB                                      |
| Authentication     | JWT RS256 (JJWT 0.12.6) — RSA-4096 public key verify     |
| Authorization      | Spring Security + @PreAuthorize                          |
| Mapping            | ModelMapper 3.2.0                                        |
| API Docs           | SpringDoc OpenAPI 2.6 (Swagger UI)                       |
| Validation         | Jakarta Bean Validation                                  |
| Build              | Maven                                                    |

---

## MongoDB Connection Modes

Controlled by the `IS_CLOUD_DB` flag in environment variables:

| Flag                | Mode                     | Connection Style                         |
|---------------------|--------------------------|------------------------------------------|
| `IS_CLOUD_DB=true`  | Cloud (URI-based)        | Uses `MONGODB_URI` connection string     |
| `IS_CLOUD_DB=false` | Standalone (Credentials) | Uses `MONGODB_HOST`, `PORT`, `USERNAME`, `PASSWORD` |

---

## API Endpoints

### Products (`/api/products`)

| Method | Endpoint          | Access     | Description                       |
|--------|-------------------|------------|-----------------------------------|
| POST   | `/`               | Auth       | Create product                    |
| GET    | `/`               | Auth       | List all (paginated, newest first)|
| GET    | `/opensource`     | Auth       | List open-source (paginated)      |
| GET    | `/{id}`           | Auth       | Get single product                |
| PUT    | `/{id}`           | Auth       | Update product                    |
| DELETE | `/{id}`           | Auth       | Delete product                    |
| GET    | `/stats`          | Auth       | Product statistics                |

### Repos (`/api/repos`)

| Method | Endpoint          | Access     | Description                       |
|--------|-------------------|------------|-----------------------------------|
| GET    | `/`               | Auth       | List all repos (paginated)        |
| GET    | `/opensource`     | Auth       | List open-source repos            |
| GET    | `/{id}`           | Auth       | Get single repo                   |
| POST   | `/`               | Admin      | Create repo                       |
| PUT    | `/{id}`           | Admin      | Update repo                       |
| DELETE | `/{id}`           | Admin      | Delete repo                       |

### Dependencies (`/api/dependencies`)

| Method | Endpoint          | Access     | Description                       |
|--------|-------------------|------------|-----------------------------------|
| GET    | `/`               | Auth       | List all (paginated)              |
| GET    | `/{id}`           | Auth       | Get single dependency             |
| POST   | `/`               | Admin      | Create dependency                 |
| PUT    | `/{id}`           | Admin      | Update dependency                 |
| DELETE | `/{id}`           | Admin      | Delete dependency                 |

---

## Environment Variables

```env
# MongoDB Connection Mode
IS_CLOUD_DB=false

# Option 1: URI-based (when IS_CLOUD_DB=true)
MONGODB_URI=mongodb+srv://<user>:<password>@cluster.mongodb.net/product_management_db

# Option 2: Host/Port/Credentials (when IS_CLOUD_DB=false)
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=product_management_db
MONGODB_USERNAME=
MONGODB_PASSWORD=
MONGODB_AUTH_DB=admin
```

> **Note:** No `JWT_SECRET` or `JWT_PRIVATE_KEY_PASSPHRASE` is needed. This service uses only the public key from `src/main/resources/keys/public.pem` to verify tokens.

---

## RSA Public Key Setup

This service requires the RSA public key generated by the **Software-Crypto-Shield** KeyGenerator tool.

> 🔗 **Tool:** [Software-Crypto-Shield](https://github.com/DevJayantaGhosh/Software-Crypto-Shield) — KeyGenerator

The key pair is generated in the user-management-service using:
```bash
KeyGenerator.exe generate rsa -s 4096 -o ./mykeys -p MyPassword
```

Then copy the public key to this service:
```bash
mkdir -p src/main/resources/keys
cp ../user-management-service/src/main/resources/keys/public.pem src/main/resources/keys/
```

> ⚠️ **Security:** Only `public.pem` should be placed here. The encrypted private key must NEVER be copied to this service.

---

## Running

```bash
# Prerequisites: Java 21, Maven, MongoDB

# 1. Ensure public.pem is in src/main/resources/keys/ (see above)

# 2. Set environment variables (or create .env file)

# 3. Local build and run
./mvnw spring-boot:run

# Service starts on http://localhost:9090
# Swagger UI: http://localhost:9090/swagger-ui/index.html