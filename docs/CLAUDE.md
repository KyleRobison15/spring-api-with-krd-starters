# Spring Store API - Project Documentation

## Project Overview

This is a full-featured e-commerce REST API built with Spring Boot. It provides complete backend functionality for an online store including user authentication, product catalog, shopping cart, order management, and payment processing via Stripe.

**Technology Stack:**
- Spring Boot 3.4.5
- Java 21
- Maven (migrating to Gradle - see `gradle-migration` branch)
- MySQL database
- Flyway for database migrations
- JWT authentication (dual-token system)
- Stripe payment integration
- MapStruct for DTO mapping
- Swagger/OpenAPI for API documentation

## Project Architecture

### Directory Structure

```
src/main/java/com/codewithmosh/store/
├── auth/                # JWT authentication & security
│   ├── JwtService.java             # Token generation & validation
│   ├── JwtAuthenticationFilter.java # Request authentication filter
│   ├── SecurityConfig.java         # Spring Security configuration
│   └── AuthController.java         # Login, refresh, /me endpoints
├── users/               # User management
├── products/            # Product catalog
│   ├── Product.java
│   ├── Category.java
│   └── ProductController.java
├── carts/               # Shopping cart functionality
│   ├── Cart.java
│   ├── CartItem.java
│   └── CartController.java
├── orders/              # Order management
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderController.java
├── payments/            # Stripe payment processing
│   ├── CheckoutController.java     # Checkout & webhook endpoints
│   ├── CheckoutService.java
│   ├── StripePaymentGateway.java   # Stripe API integration
│   └── PaymentSecurityRules.java
├── common/              # Shared utilities
│   └── SecurityRules.java          # Security rules interface
└── admin/               # Admin functionality

src/main/resources/
├── application.yaml              # Main configuration
├── application-dev.yaml          # Dev environment config
├── application-prod.yaml         # Production config
└── db/migration/                 # Flyway migrations
    ├── V1__initial_migration.sql
    ├── V2__create_carts_table.sql
    ├── V3__add_role_to_users.sql
    ├── V4__create_orders_tables.sql
    └── V5__populate_database.sql
```

### Key Design Patterns

- **Layered Architecture**: Controller → Service → Repository separation
- **DTO Pattern**: MapStruct for entity-to-DTO conversions
- **Modular Security**: Each domain module defines its own security rules via `SecurityRules` interface
- **Dependency Injection**: Constructor-based injection with Lombok's `@AllArgsConstructor`
- **Filter Chain**: JWT authentication via `OncePerRequestFilter`

## Database (MySQL)

### Connection Configuration
- **Database**: `store_api` (auto-created if not exists)
- **Host**: `localhost:3306`
- **Config location**: `src/main/resources/application-dev.yaml:3-7`

**SECURITY WARNING**: Database password is currently hardcoded in `application-dev.yaml` - should be moved to environment variables.

### Flyway Migrations

Migrations run automatically on startup. Current schema version: V5

1. **V1**: Initial schema (users, products, categories)
2. **V2**: Shopping cart tables
3. **V3**: User roles column
4. **V4**: Order and order_items tables
5. **V5**: Database seed data

### Creating New Migrations

Create a new file: `src/main/resources/db/migration/V{N}__description.sql` where N is the next version number.

## JWT Authentication (Dual-Token System)

### Authentication Flow

**1. Login** (`POST /auth/login`)
- User sends email/password
- Server returns:
  - **Access token** in response body (15 min expiration)
  - **Refresh token** in HttpOnly cookie (7 day expiration)

**2. API Requests**
- Client includes `Authorization: Bearer <access-token>` header
- `JwtAuthenticationFilter` intercepts every request
- Token is validated and user context is set in Spring Security

**3. Token Refresh** (`POST /auth/refresh`)
- Refresh token automatically sent via cookie
- Returns new access token
- Refresh token remains valid

### JWT Token Structure

**Access Token Claims** (JwtService.java:29-36):
```json
{
  "sub": "userId",
  "email": "user@example.com",
  "name": "User Name",
  "role": "CUSTOMER|ADMIN",
  "iat": 1234567890,
  "exp": 1234568790
}
```

### Security Features

- **HttpOnly Cookies**: Refresh tokens stored in HttpOnly cookies (cannot be accessed by JavaScript - XSS protection)
- **Path Restriction**: Refresh token cookie only sent to `/auth/refresh`
- **Secure Flag**: Cookies only transmitted over HTTPS
- **BCrypt Password Hashing**: Passwords never stored in plaintext
- **Stateless Sessions**: No server-side session storage
- **Short-lived Access Tokens**: 15 minutes (limits damage from token theft)
- **Long-lived Refresh Tokens**: 7 days (better UX)

### JWT Configuration

**Environment Variables Required**:
- `JWT_SECRET`: Secret key for signing tokens (application.yaml:6)

**Token Expiration** (application.yaml:8-14):
- Access Token: 900 seconds (15 minutes)
- Refresh Token: 604800 seconds (7 days)

## Security Configuration

### CORS Settings (SecurityConfig.java:57-78)

- **Allowed Origin**: `http://localhost:5173` (React/Vite dev server)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All (including Authorization)
- **Credentials**: Enabled (required for cookies)
- **Max Age**: 3600s (1 hour preflight cache)

### Modular Security Rules

Each domain module can define its own access rules by implementing `SecurityRules` interface:

```java
@Component
public class CartSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry c) {
        // Define cart-specific security rules
    }
}
```

All rules are automatically collected and applied in `SecurityConfig.java:97`.

### Role-Based Authorization

- Roles stored in JWT claims
- Spring Security uses `ROLE_` prefix automatically
- User roles: `CUSTOMER`, `ADMIN` (defined in User entity)

## Stripe Payment Integration

### Checkout Flow

1. **Create Checkout Session** (`POST /checkout`)
   - Client sends cart/order details
   - Server creates Stripe checkout session
   - Returns checkout URL for Stripe hosted page
   - Order ID metadata is attached to payment

2. **User Completes Payment** (Stripe hosted page)

3. **Webhook Notification** (`POST /checkout/webhook`)
   - Stripe sends payment result
   - Server verifies webhook signature
   - Updates order status in database

### Configuration

**Environment Variables Required** (application.yaml:19-21):
- `STRIPE_SECRET_KEY`: Stripe API key
- `STRIPE_WEBHOOK_SECRET_KEY`: Webhook signing secret

### Error Handling

Checkout process handles these exceptions (README.md:11-15):
- Invalid API Key
- Network Issues
- Bad Requests (e.g., negative amounts)
- Stripe service outages

On error: Order is deleted from database to prevent bad data, exception passed to controller.

### Testing with Stripe CLI

```bash
# 1. Install and login
stripe login

# 2. Forward webhooks to local server
stripe listen --forward-to http://localhost:8080/checkout/webhook

# 3. Trigger test events
stripe trigger payment_intent.succeeded
stripe trigger payment_intent.succeeded --add "payment_intent:metadata[order_id]=1"
```

**Important**: Keep Stripe SDK and API versions in sync to avoid deserialization errors (README.md:42-45).

### Recent Fixes

- Order ID metadata bug fixed in `StripePaymentGateway` (commit: 0482826)

## Development Setup

### Prerequisites

- Java 21
- Maven 3.x (or use `./mvnw`)
- MySQL 8.x running locally
- Stripe CLI (for webhook testing)

### Environment Variables

Create a `.env` file (see `.env.example`):
```bash
JWT_SECRET=your-secret-key-here
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET_KEY=whsec_...
```

### Build and Run

```bash
# Build the project
./mvnw clean install

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Or run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at `http://localhost:8080`

### Database Setup

1. Ensure MySQL is running on `localhost:3306`
2. Database `store_api` will be created automatically
3. Flyway runs all migrations on startup
4. Check logs for migration status

### Swagger Documentation

Access interactive API docs at: `http://localhost:8080/swagger-ui.html`

All controllers are tagged for organization:
- Auth
- Products
- Carts
- Orders
- Checkout

## Coding Standards

### Naming Conventions

- **Classes**: PascalCase (e.g., `OrderController`, `PaymentService`)
- **Methods**: camelCase (e.g., `processPayment`, `getUserCart`)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: `com.codewithmosh.store.{domain}`

### Code Style

- Use Lombok annotations to reduce boilerplate (`@AllArgsConstructor`, `@Data`, etc.)
- Constructor injection preferred over field injection
- Add Swagger `@Tag` annotations to all controllers
- Keep controllers thin - business logic belongs in services
- Use MapStruct for entity/DTO conversions
- Validate request DTOs with Jakarta Validation (`@Valid`)

### Exception Handling

- Domain-specific exceptions (e.g., `CartNotFoundException`, `PaymentException`)
- `@ExceptionHandler` methods in controllers for clean error responses
- Return appropriate HTTP status codes (400 for bad requests, 401 for auth failures, etc.)

## Common Tasks

### Add a New REST Endpoint

1. Create request/response DTOs in appropriate package
2. Add controller method with proper annotations:
```java
@RestController
@RequestMapping("/api/items")
@Tag(name = "Items")
public class ItemController {
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long id) {
        // implementation
    }
}
```
3. Implement service layer logic
4. Add repository methods if needed
5. Define security rules if needed
6. Add unit tests
7. Test via Swagger UI

### Add Security Rules for a New Module

1. Create a class implementing `SecurityRules`:
```java
@Component
public class MyModuleSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry c) {
        c.requestMatchers(HttpMethod.GET, "/api/mymodule/**").permitAll();
        c.requestMatchers(HttpMethod.POST, "/api/mymodule/**").authenticated();
    }
}
```
2. Spring automatically picks it up and applies rules in `SecurityConfig`

### Create a Database Migration

1. Create file: `src/main/resources/db/migration/V{N}__description.sql`
2. Write SQL DDL/DML
3. Restart application - Flyway runs migration automatically
4. Check logs to confirm success

### Process a Payment

```java
// Service layer
StripePaymentGateway gateway = new StripePaymentGateway(stripeApiKey);
PaymentResult result = gateway.processPayment(amount, orderId);
// Order ID metadata is automatically included
```

## Important Notes & Gotchas

### Security Concerns

1. **Database Password Exposed**: `application-dev.yaml` contains plaintext password - move to environment variables
2. **CORS Origin**: Currently hardcoded to `localhost:5173` - should be configurable per environment
3. **Cookie Secure Flag**: Enabled in production but may cause issues in local development without HTTPS

### Known Issues

- Stripe SDK version must match API version to avoid deserialization errors
- JPA shows SQL in dev mode (`show-sql: true`) - disable in production for performance
- Recent fix for order ID metadata in Stripe payments (commit 0482826)

### Git Workflow

- **Current branch**: `gradle-migration` (migrating from Maven to Gradle)
- **Main development work**: Most recent commits on this branch
- Always run tests before committing
- Check `.gitignore` before committing - ensure `.env` is excluded

### Recent Changes

1. **Fix order ID metadata bug** in StripePaymentGateway (commit: 0482826)
2. **Modularize Security Rules** and configure CORS (commit: 1abc85f)
3. **Add controller tags** for Swagger documentation (commit: 55f34e4)
4. **Update local DB password** and security rules (commit: 96e8479)
5. **Add Flyway migration** for database population (commit: 824bfb5)

## Testing

### Manual Testing

1. Start application
2. Navigate to Swagger UI: `http://localhost:8080/swagger-ui.html`
3. Test authentication:
   - POST `/auth/login` with test credentials
   - Copy access token from response
   - Click "Authorize" button, enter `Bearer <token>`
4. Test protected endpoints

### Stripe Testing

1. Start Stripe CLI listener:
```bash
stripe listen --forward-to http://localhost:8080/checkout/webhook
```
2. Use Stripe test card: `4242 4242 4242 4242`
3. Trigger test events:
```bash
stripe trigger payment_intent.succeeded
```

## Frontend Integration

**Frontend URL**: `http://localhost:5173` (configured in CORS and `application-dev.yaml:10`)

### Authentication Flow (Client Side)

1. **Login**: POST to `/auth/login` with credentials
   - Store access token in memory (not localStorage - security risk)
   - Refresh token automatically stored in cookie

2. **API Calls**: Include header `Authorization: Bearer <access-token>`

3. **Token Refresh**: POST to `/auth/refresh`
   - Cookie sent automatically
   - Update in-memory access token

4. **Get Current User**: GET `/auth/me` to fetch logged-in user details

## Reference Documentation

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Flyway](https://flywaydb.org/documentation/)
- [Stripe API](https://stripe.com/docs/api)
- [Stripe CLI](https://docs.stripe.com/stripe-cli)
- [JJWT (JWT Library)](https://github.com/jwtk/jjwt)
- [MapStruct](https://mapstruct.org/)

## Useful Commands

```bash
# Build
./mvnw clean install

# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Package as JAR
./mvnw package

# Stripe CLI
stripe listen --forward-to http://localhost:8080/checkout/webhook
stripe trigger payment_intent.succeeded

# MySQL
mysql -u root -p
USE store_api;
SHOW TABLES;
```
