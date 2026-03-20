<h1 align="center">📦 Product Management Service 📦</h1>

<p align="center">
  <strong>Product Management Service is part of the Software Security Suite — manages software products, source code repositories, dependencies, security scan results, digital signing artifacts, and approval workflows. Serves both the Electron Desktop App and Web Portal.</strong>
</p>

---

## High-Level Architecture

**Spring Boot 3.3.4 | Java 21 | MongoDB | JWT Authentication**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                       PRODUCT MANAGEMENT SERVICE                             │
│                              (Port 9090)                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────────────────────────┐ │
│  │   Clients   │───>│   Security   │───>│         REST Controllers         │ │
│  │  (Desktop   │    │    Filter    │    │                                  │ │
│  │   & Web     │    │    Chain     │    │  • ProductController             │ │
│  │   Portal)   │    │              │    │  • RepoController                │ │
│  └─────────────┘    │  ┌────────┐  │    │  • DependencyController          │ │
│                     │  │  JWT   │  │    │                                  │ │
│                     │  │ Filter │  │    │                                  │ │
│                     │  └────────┘  │    │                                  │ │
│                     └──────────────┘    └───────────────┬──────────────────┘ │
│                                                          │                   │
│                            ┌─────────────────────────────┤                   │
│                            │                             │                   │
│                  ┌─────────▼──────────┐     ┌─────────────▼────────────────┐ │
│                  │   Service Layer     │    │      Security Layer          │ │
│                  │                     │    │                              │ │
│                  │  • ProductService   │    │  • JwtService                │ │
│                  │  • RepoService      │    │  • JwtAuthenticationFilter   │ │
│                  │  • DependencyService│    │  • SecurityConfig            │ │
│                  │                     │    │                              │ │
│                  └─────────┬──────────┘    └──────────────────────────────┘  │
│                            │                                                 │
│                  ┌─────────▼──────────┐                                      │
│                  │  Repository Layer   │                                     │
│                  │ (Spring Data Mongo) │                                     │
│                  │                     │                                     │
│                  │  • ProductRepository│                                     │
│                  │  • RepoRepository   │                                     │
│                  │  • DependencyRepo   │                                     │
│                  └─────────┬──────────┘                                      │
│                            │                                                 │
├────────────────────────────┼─────────────────────────────────────────────────┤
│                            │                                                 │
│                  ┌─────────▼──────────┐                                      │
│                  │      MongoDB        │                                     │
│                  │                     │                                     │
│                  │  Collections:       │                                     │
│                  │   - products        │                                     │
│                  │   - repos           │                                     │
│                  │   - dependencies    │                                     │
│                  └─────────────────────┘                                     │
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
│  JWT Auth Filter │  (Validates shared JWT secret with User Service)
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

## Tech Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Framework          | Spring Boot 3.3.4                   |
| Language           | Java 21                             |
| Database           | MongoDB (Cloud URI or Standalone)   |
| ODM                | Spring Data MongoDB                 |
| Authentication     | JWT (JJWT 0.12.6) — shared secret  |
| Authorization      | Spring Security + @PreAuthorize     |
| Mapping            | ModelMapper 3.2.0                   |
| API Docs           | SpringDoc OpenAPI 2.6 (Swagger UI)  |
| Validation         | Jakarta Bean Validation             |
| Build              | Maven                               |

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

# JWT (must match User Management Service)
JWT_SECRET=
JWT_EXPIRATION=86400000
```

---

## Running

```bash
# Prerequisites: Java 21, Maven, MongoDB

# 1. Set environment variables (or create .env file)

# 2. Build and run
./mvnw spring-boot:run

# Service starts on http://localhost:9090
# Swagger UI: http://localhost:9090/swagger-ui/index.html