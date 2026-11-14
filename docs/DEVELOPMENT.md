# Development Guide

## Setup Instructions

### Prerequisites

- **JDK 21** — Install via [Adoptium](https://adoptium.net/) or your package manager
- **PostgreSQL 15+** — Install locally or use Docker:
  ```bash
  docker run --name giftly-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=giftly -p 5432:5432 -d postgres:15
  ```
- **Gradle 8.x** — Or use the included wrapper `./gradlew`

### Initial Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd giftly
   ```

2. **Configure the database**

   Create a PostgreSQL database named `giftly` and update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/giftly
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

   Flyway will automatically apply all pending migrations on startup. The admin account will be created by `AdminInitializer`.

4. **Verify the setup**
   - Open http://localhost:8080/swagger-ui.html to see the interactive API docs
   - Test login with the default admin credentials

### IDE Configuration

#### IntelliJ IDEA (Recommended)

1. Open the project root directory
2. Enable **Annotation Processors**:
   - Settings → Build → Compilation → Annotation Processors → ✅ Enable annotation processing
3. Install the **Lombok** plugin if not already installed
4. Import Gradle project: File → New → Project from Existing Sources → Select `build.gradle.kts`

#### VS Code

1. Install the **Extension Pack for Java** by Microsoft
2. Install the **Lombok** annotation processor extension
3. Open the folder and let Gradle sync complete

## Project Conventions

### Package Structure

Each module follows a consistent layered structure:

```
elin.le.giftly.<module>/
├── controller/     # REST controllers (@RestController)
├── dto/            # Request/Response DTOs (Java records)
├── entity/         # JPA entities (@Entity)
├── repository/     # Spring Data JPA repositories
└── service/        # Business logic services
```

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Controllers | `<Domain>Controller` | `OrderController`, `AdminProductController` |
| Services | `<Domain>Service` | `OrderService`, `AdminProductService` |
| Repositories | `<Entity>Repository` | `OrderRepository`, `ProductRepository` |
| Request DTOs | `<Action><Domain>Request` | `CreateOrderRequest`, `UpdateProductRequest` |
| Response DTOs | `<Domain>Response` | `OrderResponse`, `ProductResponse` |
| Entities | `<Domain>` | `Order`, `Product`, `User` |

### DTO Pattern

- **Request DTOs** use Java `record` types with validation annotations:
  ```java
  public record CreateOrderRequest(
      @NotNull UUID addressId,
      @NotNull @Positive BigDecimal shippingFee
  ) {}
  ```

- **Response DTOs** use Java `record` types or classes with constructors:
  ```java
  public record OrderResponse(
      UUID id,
      String orderNumber,
      BigDecimal totalAmount,
      OrderStatus status
  ) {}
  ```

- **ApiResponse\<T\>** wraps all responses for consistency:
  ```java
  {
    "success": true,
    "message": "Order created successfully",
    "data": { ... }
  }
  ```

### Validation

Use Jakarta Bean Validation annotations on request DTOs:

| Annotation | Usage |
|------------|-------|
| `@NotNull` | Field must not be null |
| `@NotBlank` | String must not be blank |
| `@Email` | Valid email format |
| `@Size(min, max)` | String/collection size bounds |
| `@Min`, `@Max` | Numeric bounds |
| `@Positive` | Positive number |
| `@Pattern` | Regex pattern match |

Add `@Valid` to controller method parameters to trigger validation.

## Testing

### Test Structure

Tests mirror the source structure:

```
src/test/java/elin/le/giftly/
├── auth/jwt/JwtUtilTest.java
├── cart/service/CartServiceTest.java
├── order/service/OrderServiceTest.java
├── admin/service/AdminOrderServiceTest.java
└── test/
    ├── TestConfig.java
    └── TestDataFactory.java
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "elin.le.giftly.order.service.OrderServiceTest"

# Run with debug output
./gradlew test --info
```

### Test Configuration

- Tests use **H2 in-memory database** instead of PostgreSQL
- `TestConfig` provides test-specific bean configuration
- `TestDataFactory` creates sample entities for tests
- Use `@MockBean` for mocking external services (e.g., Cloudinary)

### Writing Tests

```java
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void shouldCreateOrder() {
        // Arrange
        var user = testDataFactory.createUser();
        var product = testDataFactory.createProduct();
        
        // Act
        var order = orderService.createOrder(request);
        
        // Assert
        assertNotNull(order.getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}
```

## Database Migrations

### Creating a New Migration

1. Create a new file in `src/main/resources/db/migration/`:
   ```
   V<N>__<description>.sql
   ```
   Where `<N>` is the next version number (e.g., `V4__add_user_avatar.sql`)

2. Write your SQL:
   ```sql
   -- V4: Add avatar column to user table
   ALTER TABLE "user" ADD COLUMN avatar_url VARCHAR(500);
   ```

3. Flyway will automatically apply it on next startup

### Migration Best Practices

- ✅ Always test migrations on a copy of production data
- ✅ Never modify an already-applied migration
- ✅ Use descriptive names (e.g., `V4__add_user_avatar.sql`)
- ❌ Don't drop columns that might still be in use
- ❌ Don't rename tables without a migration plan

## API Development

### Adding a New Endpoint

1. **Create the request/response DTOs** in the module's `dto/` package
2. **Add the endpoint** to the appropriate controller with `@Operation` annotation
3. **Implement the service method** with business logic
4. **Add repository methods** if needed
5. **Write tests** for the new functionality
6. **Verify in Swagger UI** at http://localhost:8080/swagger-ui.html

### Example: Adding a New Endpoint

```java
// 1. DTO
public record UpdateQuantityRequest(
    @NotNull @Positive Integer quantity
) {}

// 2. Controller
@PutMapping("/{id}/quantity")
@Operation(summary = "Update quantity", description = "Update item quantity")
public ResponseEntity<ApiResponse<Product>> updateQuantity(
    @PathVariable UUID id,
    @Valid @RequestBody UpdateQuantityRequest request
) {
    Product product = productService.updateQuantity(id, request.quantity());
    return ResponseEntity.ok(ApiResponse.success("Quantity updated", product));
}

// 3. Service
public Product updateQuantity(UUID id, Integer quantity) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    product.setStockQuantity(quantity);
    return productRepository.save(product);
}
```

### OpenAPI Annotations

Use SpringDoc annotations for better API documentation:

| Annotation | Purpose |
|------------|---------|
| `@Tag` | Group endpoints in Swagger UI |
| `@Operation` | Describe an endpoint |
| `@Parameter` | Describe a request parameter |
| `@ApiResponse` | Document a response |
| `@SecurityRequirement` | Mark auth requirement |

## Security

### JWT Configuration

JWT settings in `application.properties`:

```properties
jwt.secret=YourSecretKeyHere  # Change in production!
jwt.access-token.expiration=3600000    # 1 hour
jwt.refresh-token.expiration=604800000 # 7 days
```

> ⚠️ **Production:** Use a strong, randomly generated secret key (at least 256 bits). Store it in environment variables or a secrets manager, never in source code.

### Adding New Security Rules

Edit `SecurityConfig.java`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/products/**").permitAll()
    .requestMatchers("/api/categories/**").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "MANAGER")  // New rule
    .anyRequest().authenticated()
)
```

## Cloudinary Integration

### Configuration

```properties
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret
```

### Usage

```java
@Autowired
private CloudinaryService cloudinaryService;

// Upload image
var result = cloudinaryService.uploadImage(file);
// Returns: { url, secureUrl, publicId }

// Delete image
cloudinaryService.deleteImage(publicId);
```

## Deployment

### Build for Production

```bash
./gradlew bootJar
```

The JAR is output to `build/libs/giftly-0.0.1-SNAPSHOT.jar`.

### Run the JAR

```bash
java -jar build/libs/giftly-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://prod-host:5432/giftly \
  --spring.datasource.username=prod_user \
  --spring.datasource.password=prod_password \
  --jwt.secret=production-secret-key
```

### Environment Variables

Override any `application.properties` value with environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-host:5432/giftly
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
export JWT_SECRET=production-secret-key
```

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY build/libs/giftly-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:

```bash
docker build -t giftly:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/giftly \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  giftly:latest
```

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| `Connection refused` on PostgreSQL | Ensure PostgreSQL is running and accessible on the configured port |
| Flyway migration fails | Check `flyway_schema_history` table; reset with `./gradlew flywayClean flywayMigrate` |
| JWT token invalid | Verify `jwt.secret` is consistent across restarts |
| Cloudinary upload fails | Check API credentials and network connectivity |
| Lombok annotations not processed | Enable annotation processing in your IDE |
| MapStruct mapper is null | Ensure `mapstruct-processor` is in annotationProcessor scope |

### Useful Gradle Commands

```bash
./gradlew build              # Build + test
./gradlew bootRun            # Run development server
./gradlew test               # Run tests only
./gradlew clean              # Clean build output
./gradlew flywayMigrate      # Run pending migrations
./gradlew flywayClean        # Reset database (DANGER: drops all tables)
```

## Git Workflow

### Branch Naming

```
feature/add-user-avatar
fix/order-cancellation-bug
refactor/cart-service
docs/api-documentation
```

### Commit Messages

Follow conventional commits:

```
feat: add gift box image upload
fix: handle null cart on order creation
docs: update API reference for reviews
refactor: extract price calculation from order service
test: add integration test for cart clearing
```
