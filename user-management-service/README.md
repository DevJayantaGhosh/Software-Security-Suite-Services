<h1 align="center">🔐 User Management Service 🔐</h1>

<p align="center">
  <strong>User Management Service is part of the Software Security Suite — handles user registration, authentication, role-based authorization, license activation, OTP-based password recovery, and email notifications. Serves both the Electron Desktop App and Web Portal.</strong>
</p>

---

## High-Level Architecture

**Spring Boot 3.3.4 | Java 21 | PostgreSQL | JWT Authentication**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          USER MANAGEMENT SERVICE                             │
│                              (Port 8080)                                     │
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
│                            ┌─────────────────────────────┤                   │
│                            │                             │                   │
│                  ┌─────────▼──────────┐    ┌─────────────▼────────────────┐  │
│                  │   Service Layer     │    │      Security Layer          │ │
│                  │                     │    │                              │ │
│                  │  • UserService      │    │  • JwtService                │ │
│                  │  • OtpService       │    │  • JwtAuthenticationFilter   │ │
│                  │  • EmailService     │    │  • CustomUserDetailsService  │ │
│                  │                     │    │  • SecurityConfig            │ │
│                  └─────────┬──────────┘    └──────────────────────────────┘  │
│                            │                                                 │
│                  ┌─────────▼──────────┐    ┌──────────────────────────────┐  │
│                  │  Repository Layer   │    │      External Services       │ │
│                  │  (Spring Data JPA)  │    │                              │ │
│                  │                     │    │  • Gmail SMTP                │ │
│                  │  • UserRepository   │    │    (OTP Email Delivery)      │ │
│                  │  • RoleRepository   │    │                              │ │
│                  │  • OtpRepository    │    └──────────────────────────────┘ │
│                  └─────────┬──────────┘                                      │
│                            │                                                 │
├────────────────────────────┼─────────────────────────────────────────────────┤
│                            │                                                 │
│                  ┌─────────▼──────────┐                                      │
│                  │    PostgreSQL       │                                     │
│                  │  USER_MANAGEMENT_DB │                                     │
│                  │                     │                                     │
│                  │  Tables:            │                                     │
│                  │   - users           │                                     │
│                  │   - roles           │                                     │
│                  │   - otps            │                                     │
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
│  JWT Auth Filter │  (Extracts & validates JWT from Authorization header)
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

## Tech Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Framework          | Spring Boot 3.3.4                   |
| Language           | Java 21                             |
| Database           | PostgreSQL                          |
| ORM                | Spring Data JPA / Hibernate         |
| Authentication     | JWT (JJWT 0.12.6)                   |
| Authorization      | Spring Security + @PreAuthorize     |
| Email              | Spring Boot Mail (Gmail SMTP)       |
| API Docs           | SpringDoc OpenAPI 2.6 (Swagger UI)  |
| Validation         | Jakarta Bean Validation             |
| Build              | Maven                               |

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
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

---

## Running

```bash
# Prerequisites: Java 21, Maven, PostgreSQL running on port 5432

# 1. Create database
psql -U postgres -c "CREATE DATABASE USER_MANAGEMENT_DB;"

# 2. Set environment variables (or create .env file)

# 3. Build and run
./mvnw spring-boot:run

# Service starts on http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html