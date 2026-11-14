# Giftly — E-Commerce Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

**Giftly** is a full-featured e-commerce REST API built with Spring Boot 4.1.2 and Java 21. It provides a complete online gift shop experience with product browsing, shopping cart, order management, custom gift box creation, user reviews, and a comprehensive admin panel.

## ✨ Features

- **Authentication & Authorization** — JWT-based stateless authentication with role-based access control (USER / ADMIN)
- **Product Catalog** — Browse, search, and filter products by category, price range, and keyword (with typo-tolerant fuzzy search via PostgreSQL trigram)
- **Shopping Cart** — Add, update, and remove items; supports both regular products and custom gift boxes
- **Order Management** — Create orders from cart, view order history, cancel orders, and track order status
- **Custom Gift Boxes** — Create personalized gift boxes by combining products (box, wrapping, ribbon, card) with a greeting message
- **Product Reviews** — Submit reviews with ratings (1-5) and optional images; admin moderation (approve/reject)
- **Admin Panel** — Full CRUD for products, categories, users, orders, and reviews; dashboard with key metrics
- **Image Upload** — Cloudinary integration for product images and review photos
- **API Documentation** — Interactive Swagger UI at `/swagger-ui.html` and OpenAPI spec at `/api-docs`

## 🏗 Architecture

```
giftly/
├── src/main/java/elin/le/giftly/
│   ├── admin/            # Admin panel (products, categories, orders, users, reviews, dashboard)
│   ├── auth/             # Authentication (JWT, login, register, logout)
│   ├── cart/             # Shopping cart management
│   ├── cloudinary/       # Cloudinary image upload service
│   ├── common/           # Shared DTOs, enums, and utilities
│   ├── config/           # Application configuration (security, OpenAPI, JPA, Cloudinary)
│   ├── giftbox/          # Custom gift box creation and management
│   ├── order/            # Order processing and management
│   ├── payment/          # Payment entities and repository
│   ├── product/          # Product catalog and categories
│   ├── review/           # Product reviews
│   └── user/             # User profile management
├── src/main/resources/
│   └── db/migration/     # Flyway database migrations
└── src/test/java/        # Unit and integration tests
```

See [Architecture Overview](docs/ARCHITECTURE.md) for detailed module descriptions and data flow diagrams.

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK 21 or later)
- **PostgreSQL 15+**
- **Gradle 8.x** (or use the included wrapper `./gradlew`)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd giftly
   ```

2. **Configure the database**

   Create a PostgreSQL database and update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/giftly
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Build and run**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/api-docs
   - API endpoints: http://localhost:8080/api/...

### Default Admin Account

An admin account is auto-created on first startup:
- **Email:** `elin.le.dev@gmail.com`
- **Password:** `ElinLeDev`

> ⚠️ **Security Note:** Change these credentials in `application.properties` before deploying to production.

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [API Reference](docs/API.md) | Complete REST API endpoint reference with request/response examples |
| [Architecture](docs/ARCHITECTURE.md) | System architecture, module overview, and data flow |
| [Database Schema](docs/DATABASE.md) | Entity-relationship diagram and table reference |
| [Development Guide](docs/DEVELOPMENT.md) | Setup instructions, coding conventions, and testing |

## 🔐 Authentication

The API uses **JWT Bearer Token** authentication:

1. **Register** a new account via `POST /api/auth/register`
2. **Login** with `POST /api/auth/login` to receive a JWT access token
3. **Include** the token in subsequent requests:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

- **Access Token:** Expires in 1 hour (3600000 ms)
- **Refresh Token:** Expires in 7 days (604800000 ms)

### Public Endpoints (No Authentication Required)

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/login` | User login |
| `POST /api/auth/register` | User registration |
| `GET /api/products` | Browse products |
| `GET /api/categories` | List categories |

### Protected Endpoints

All other endpoints require a valid JWT token. Admin endpoints additionally require the `ADMIN` role.

## 🛠 Tech Stack

| Category | Technology |
|----------|-----------|
| **Framework** | Spring Boot 4.1.2 |
| **Language** | Java 21 |
| **Build Tool** | Gradle (Kotlin DSL) |
| **Database** | PostgreSQL 15+ |
| **Migrations** | Flyway |
| **ORM** | Spring Data JPA (Hibernate) |
| **Security** | Spring Security + JWT (jjwt 0.12.6) |
| **API Docs** | SpringDoc OpenAPI 2.6.0 |
| **Image Storage** | Cloudinary |
| **Code Generation** | MapStruct 1.5.5 + Lombok |
| **Validation** | Jakarta Bean Validation |
| **Testing** | JUnit 5 + Spring Boot Test + H2 (in-memory) |

## 📦 Project Structure

```
├── admin/
│   ├── config/           # Admin auto-initialization
│   ├── controller/       # Admin REST controllers
│   ├── dto/              # Admin request/response DTOs
│   └── service/          # Admin business logic
├── auth/
│   ├── controller/       # Auth endpoints (login, register, logout)
│   ├── dto/              # Auth request/response DTOs
│   ├── entity/           # User, Role, UserRole entities
│   ├── jwt/              # JWT utilities and authentication filter
│   ├── repository/       # User and Role repositories
│   └── service/          # Authentication and user details service
├── cart/
│   ├── controller/       # Cart endpoints
│   ├── dto/              # Cart request/response DTOs
│   ├── entity/           # Cart and CartItem entities
│   ├── repository/       # Cart repositories
│   └── service/          # Cart business logic
├── cloudinary/
│   └── service/          # Cloudinary image upload service
├── common/
│   ├── dto/              # Shared ApiResponse wrapper
│   └── enums/            # Shared enums (OrderStatus, PaymentStatus, etc.)
├── config/
│   ├── cloudinary/       # Cloudinary configuration
│   ├── jpa/              # JPA/Hibernate configuration
│   ├── openapi/          # OpenAPI/Swagger configuration
│   ├── security/         # Spring Security configuration
│   └── web/              # Web MVC configuration
├── giftbox/
│   ├── controller/       # Gift box endpoints
│   ├── dto/              # Gift box request/response DTOs
│   ├── entity/           # GiftBoxConfig and GiftBoxItem entities
│   ├── repository/       # Gift box repositories
│   └── service/          # Gift box business logic
├── order/
│   ├── controller/       # Order endpoints
│   ├── dto/              # Order request/response DTOs
│   ├── entity/           # Order, OrderItem, OrderItemComponent entities
│   ├── repository/       # Order repositories
│   └── service/          # Order business logic
├── payment/
│   ├── entity/           # Payment entity
│   └── repository/       # Payment repository
├── product/
│   ├── controller/       # Product and Category endpoints
│   ├── dto/              # Product request/response DTOs
│   ├── entity/           # Product, ProductImage, Category entities
│   ├── repository/       # Product repositories
│   └── service/          # Product business logic
├── review/
│   ├── controller/       # Review endpoints
│   ├── dto/              # Review request/response DTOs
│   ├── entity/           # Review entity
│   ├── repository/       # Review repository
│   └── service/          # Review business logic
└── user/
    ├── controller/       # User profile endpoints
    ├── dto/              # User request/response DTOs
    ├── entity/           # User and Address entities
    ├── repository/       # User repositories
    └── service/          # User business logic
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "elin.le.giftly.order.service.OrderServiceTest"
```

Tests use an in-memory H2 database and are configured with `@TestConfiguration` for isolated testing.

## 📝 License

This project is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).

## 👥 Contact

- **Support:** support@giftly.com
- **Project:** [Giftly](https://github.com/elin-le/giftly)
