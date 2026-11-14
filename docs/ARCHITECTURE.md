# Architecture Overview

## System Architecture

Giftly follows a **layered architecture** with clear separation of concerns. Each business domain is organized as a module with its own controller, service, repository, entity, and DTO layers.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser/Mobile)                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                      │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Security Layer                          │   │
│  │  ┌──────────────┐  ┌──────────────────────────────┐      │   │
│  │  │  JWT Filter  │→│  Spring Security Filter Chain  │      │   │
│  │  └──────────────┘  └──────────────────────────────┘      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                   Controller Layer                        │   │
│  │  Auth │ Products │ Cart │ Orders │ Reviews │ GiftBox     │   │
│  │  User │ Admin (Products, Categories, Orders, Users,      │   │
│  │         Reviews, Dashboard)                               │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌───────────────────────────▼─────────────────────────────────┐│
│  │                   Service Layer                              ││
│  │  Business logic, validation, orchestration                   ││
│  └───────────────────────────┬─────────────────────────────────┘│
│                              │                                   │
│  ┌───────────────────────────▼─────────────────────────────────┐│
│  │                   Repository Layer                           ││
│  │  Spring Data JPA repositories                                ││
│  └───────────────────────────┬─────────────────────────────────┘│
│                              │                                   │
│  ┌───────────────────────────▼─────────────────────────────────┐│
│  │                   Entity Layer                               ││
│  │  JPA entities mapped to database tables                      ││
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              External Services                            │   │
│  │  ┌──────────────┐                                        │   │
│  │  │  Cloudinary   │  (Image upload & storage)              │   │
│  │  └──────────────┘                                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     PostgreSQL Database                          │
│  (Flyway migrations for schema management)                       │
└─────────────────────────────────────────────────────────────────┘
```

## Module Overview

### Core Modules

| Module | Package | Responsibility |
|--------|---------|----------------|
| **Auth** | `auth/` | User authentication, registration, JWT token management |
| **User** | `user/` | User profile management, address management |
| **Product** | `product/` | Product catalog browsing, category listing |
| **Cart** | `cart/` | Shopping cart operations (add, update, remove, clear) |
| **Order** | `order/` | Order creation, history, cancellation |
| **Review** | `review/` | Product reviews with image upload |
| **GiftBox** | `giftbox/` | Custom gift box creation and management |
| **Payment** | `payment/` | Payment entity and repository |

### Admin Modules

| Module | Package | Responsibility |
|--------|---------|----------------|
| **Admin Products** | `admin/` | Full CRUD for products with filtering |
| **Admin Categories** | `admin/` | Full CRUD for categories |
| **Admin Orders** | `admin/` | Order management, status updates |
| **Admin Users** | `admin/` | User management, role assignment |
| **Admin Reviews** | `admin/` | Review moderation (approve/reject/delete) |
| **Admin Dashboard** | `admin/` | Dashboard statistics and metrics |

### Infrastructure Modules

| Module | Package | Responsibility |
|--------|---------|----------------|
| **Config** | `config/` | Security, OpenAPI, JPA, Cloudinary, Web MVC config |
| **Cloudinary** | `cloudinary/` | Image upload and management service |
| **Common** | `common/` | Shared DTOs (ApiResponse), enums, utilities |

## Data Flow

### 1. Authentication Flow

```
Client → POST /api/auth/login → AuthController → AuthenticationService
                                              → BCryptPasswordEncoder (verify)
                                              → JwtUtil (generate tokens)
                                              ← AuthResponse { accessToken, refreshToken }
```

### 2. Protected Request Flow

```
Client → Request with Bearer token → JwtAuthenticationFilter
                                    → Validate JWT token
                                    → Load user details (CustomUserDetailsService)
                                    → Spring Security Authorization
                                    → Controller → Service → Repository → Database
                                    ← Response wrapped in ApiResponse
```

### 3. Order Creation Flow

```
Client → POST /api/orders → OrderController → OrderService
                                              → Validate cart items
                                              → Check stock availability
                                              → Create Order entity
                                              → Create OrderItem entities
                                              → Create Payment entity
                                              → Clear cart
                                              ← OrderResponse
```

### 4. Image Upload Flow

```
Client → Multipart request → Controller → Service → CloudinaryService
                                              → Upload to Cloudinary API
                                              ← { imageUrl, secureUrl, publicId }
                                              → Save to database
```

## Security Architecture

### Authentication

- **JWT (JSON Web Tokens)** for stateless authentication
- Access tokens expire in 1 hour
- Refresh tokens expire in 7 days
- Tokens signed with HMAC-SHA256

### Authorization

- **Role-Based Access Control (RBAC)** with two roles:
  - `ROLE_USER` — Standard customer access
  - `ROLE_ADMIN` — Full admin panel access
- Method-level security with `@PreAuthorize("hasRole('ADMIN')")`
- URL-level security in `SecurityConfig`

### Security Filter Chain

```
Request → CSRF disabled → CORS enabled → Stateless session
        → JWT Filter → Authorization rules → Controller
```

### Access Rules

| Path | Access |
|------|--------|
| `/api/auth/**` | Public (no auth required) |
| `/api/products/**` | Public |
| `/api/categories/**` | Public |
| `/api/admin/**` | ADMIN role required |
| All other `/api/**` | Authenticated user required |

## Configuration

### Application Properties

Key configuration in `application.properties`:

| Property | Purpose |
|----------|---------|
| `jwt.secret` | JWT signing key |
| `jwt.access-token.expiration` | Access token TTL (ms) |
| `jwt.refresh-token.expiration` | Refresh token TTL (ms) |
| `cloudinary.cloud-name` | Cloudinary cloud name |
| `cloudinary.api-key` | Cloudinary API key |
| `cloudinary.api-secret` | Cloudinary API secret |
| `admin.default.*` | Default admin account settings |
| `springdoc.*` | OpenAPI/Swagger configuration |

### Database Migrations

Flyway manages schema versioning:

| Migration | Description |
|-----------|-------------|
| `V1__init_schema.sql` | Initial schema (14 tables, indexes, default roles) |
| `V2__enable_pg_trgm.sql` | PostgreSQL trigram extension for fuzzy search |
| `V3__add_review_constraint.sql` | Unique constraint on review.order_item_id |

## Design Patterns

### Layered Architecture

Each module follows the standard layered pattern:

```
Controller (REST endpoints)
    ↓
Service (business logic)
    ↓
Repository (data access)
    ↓
Entity (JPA mapping)
```

### DTO Pattern

- **Request DTOs** — Record types for incoming requests with validation annotations
- **Response DTOs** — Record types for outgoing responses
- **ApiResponse\<T\>** — Generic wrapper for consistent response format
- **MapStruct** — Compiler-time DTO-to-entity mapping

### Repository Pattern

- Spring Data JPA repositories extend `JpaRepository`
- Custom query methods with method naming conventions
- `@Query` annotations for complex queries

### Filter Pattern

- `JwtAuthenticationFilter` — Custom `OncePerRequestFilter` for JWT validation
- Integrated into Spring Security filter chain before `UsernamePasswordAuthenticationFilter`

## Technology Decisions

| Decision | Rationale |
|----------|-----------|
| **Stateless JWT** | No server-side session storage; horizontal scaling |
| **Flyway** | Version-controlled schema migrations |
| **Cloudinary** | Managed image storage with CDN, no local storage needed |
| **MapStruct** | Type-safe, compile-time DTO mapping (no reflection overhead) |
| **Lombok** | Reduce boilerplate (getters, setters, constructors) |
| **PostgreSQL** | ACID compliance, JSON support, full-text search |
| **pg_trgm** | Typo-tolerant fuzzy search for product names |
| **Cursor pagination** | Better performance for large datasets vs offset pagination |

## Testing Strategy

- **Unit Tests** — Service layer tested with mocked repositories
- **Integration Tests** — Controller layer tested with `@SpringBootTest`
- **Test Database** — H2 in-memory database for fast, isolated tests
- **Test Configuration** — `@TestConfiguration` with `TestDataFactory` for test data
