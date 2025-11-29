# Spring API Starter - Implementation Roadmap

## 📋 Project Overview

**Goal:** Create a monolithic Spring Boot starter that provides complete authentication and user management out of the box.

**Approach:** Single "batteries included" starter (not separate starters) for rapid internal development.

**What's Included:**
- ✅ JWT authentication (dual token: access + refresh)
- ✅ User management (BaseUser with soft delete)
- ✅ Password validation (configurable via YAML)
- ✅ Role-based access control
- ✅ Audit logging (role changes)
- ✅ All DTOs, exceptions, and utilities
- ✅ Database migrations (Flyway)
- ✅ Auto-configuration

**Result:** Add one dependency → extend BaseUser → authentication and user management work immediately.

---

## 📊 Progress Summary

| Phase | Description | Time | Status |
|-------|-------------|------|--------|
| **spring-api-starter** | | | |
| 1 | Project setup | 30 min | ✅ Complete |
| 2 | Extract JWT authentication | 1-2 hours | ✅ Complete |
| 3 | Extract user management | 2-3 hours | ✅ Complete |
| 4 | Extract password validation | 30 min | ✅ Complete |
| 5 | Create database migrations | 30 min | ✅ Complete |
| 6 | Create auto-configuration | 1 hour | ✅ Complete |
| 7 | Extract auth endpoints | 1 hour | ✅ Complete |
| 8 | Add hard delete feature | 1 hour | ✅ Complete |
| 9 | Testing & integration | 1 hour | ✅ Complete |
| 10 | Refactor consumer project | 1 hour | ✅ Complete |
| 11 | Create template project | 2-3 hours | ⏸️ Pending |
| **Subtotal** | **Core API Starter** | **12-16 hours** | **91% Complete** |
| | | | |
| **payment-gateway-starter** | | | |
| 12 | Extract payment gateway | 2-3 hours | ✅ Complete |
| 13 | Add version catalog | 30 min | ✅ Complete |
| **Subtotal** | **Payment Gateway** | **2.5-3.5 hours** | **100% Complete** |
| | | | |
| **Grand Total** | **Complete System** | **14.5-19.5 hours** | **92% Complete** |

---

## ✅ What's Been Completed

### Phase 1-10: Core spring-api-starter ✅

**Published to Maven Local:** `com.krd:spring-api-starter:1.0.0`

**What's Included:**

#### JWT Authentication (`com.krd.starter.jwt`)
- `Jwt.java` - JWT claims wrapper with convenience methods
- `JwtConfig.java` - @ConfigurationProperties for token settings
- `JwtService.java` - Token generation and parsing
- `JwtAuthenticationFilter.java` - Spring Security filter
- `JwtUser.java` - Interface for user entities
- `BaseAuthService<T>` - Generic auth service (login, refresh, getCurrentUser)
- `BaseAuthController<T, D>` - Generic auth controller (/login, /refresh, /me)
- DTOs: `LoginRequest`, `LoginResponse`, `JwtResponse`

#### User Management (`com.krd.starter.user`)
- `BaseUser.java` - @MappedSuperclass with id, email, password, roles, enabled, deletedAt
- `BaseUserRepository<T>` - Generic repository with soft delete queries
- `BaseUserService<T, D>` - Generic user service with all CRUD operations
- `BaseUserController<T, D>` - Generic user controller with all REST endpoints
- `UserHardDeleteScheduler<T>` - Scheduled hard delete of soft-deleted users
- `UserManagementConfig` - Configuration for hard delete feature
- `RoleChangeLog.java` - Audit logging entity
- `RoleChangeLogRepository.java` - Audit repository
- DTOs: `BaseUserDto`, `RegisterUserRequest`, `UpdateUserRequest`, `ChangePasswordRequest`, `AddRoleRequest`, `RemoveRoleRequest`
- Exceptions: `UserNotFoundException`, `DuplicateUserException`

#### Validation (`com.krd.starter.validation`)
- `PasswordPolicy.java` - Configurable password requirements
- `@ValidPassword` - Custom annotation for password validation
- `PasswordValidator.java` - Validator with specific error messages
- `@Lowercase` - Annotation for lowercase string validation

#### Database Migrations
- `V1__create_users_and_roles_tables.sql` - Complete schema
  - users table with indexes
  - user_roles table with CASCADE delete
  - role_change_logs table with SET NULL delete (preserves audit trail)

#### Auto-Configuration
- `SpringApiStarterAutoConfiguration.java` - Auto-configures:
  - PasswordEncoder (BCrypt)
  - JwtService
  - JwtAuthenticationFilter
  - AuthenticationManager
  - Spring Security with stateless JWT
  - Method-level security (@PreAuthorize)
  - @EnableScheduling for scheduled tasks
  - UserManagementConfig for hard delete configuration
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Features:**
- ✅ JWT dual-token authentication
- ✅ User CRUD with soft delete
- ✅ Auto-reactivation on re-registration
- ✅ Scheduled hard delete (configurable retention period)
- ✅ Role-based access control
- ✅ Password validation
- ✅ Audit logging
- ✅ Auth endpoints (/login, /refresh, /me)
- ✅ User endpoints (CRUD, roles, password change)

**Testing Status:** ✅ Tested in spring-api-with-krd-starters
- Application starts successfully
- JWT authentication works
- User registration with validation works
- Soft delete and auto-reactivation work
- Hard delete scheduler configured
- Auth endpoints work
- User endpoints work
- 140+ lines of code eliminated from consumer

---

### Phase 12-13: payment-gateway-starter ✅

**Published to Maven Local:** `com.krd:payment-gateway-starter:1.0.0`

**What's Included:**

#### Payment Gateway (`com.krd.starter.payment.gateway`)
- `PaymentGateway` - Interface for provider-agnostic payment processing
- `StripePaymentGateway` - Complete Stripe integration
- `PaymentException` - Payment-specific exceptions

#### Models (`com.krd.starter.payment.models`)
- `OrderInfo` - Interface for order entities (decouples from domain)
- `PaymentStatus` - Enum (PENDING, PAID, FAILED, CANCELLED)
- `CheckoutSession` - Payment URL wrapper
- `WebhookRequest` - Webhook payload model
- `PaymentResult` - Payment event result

#### DTOs (`com.krd.starter.payment.dto`)
- `CheckoutRequest` - Checkout initiation DTO
- `CheckoutResponse` - Checkout response with order ID and payment URL

#### Configuration (`com.krd.starter.payment.config`)
- `StripeConfig` - Stripe API initialization
- `PaymentGatewayAutoConfiguration` - Auto-configuration

#### Security (`com.krd.starter.payment.security`)
- `PaymentSecurityRules` - Auto-configures webhook endpoint as public

**Features:**
- ✅ PaymentGateway interface for multiple providers
- ✅ Complete Stripe integration (checkout, webhooks)
- ✅ Webhook signature verification
- ✅ OrderInfo interface to decouple from domain
- ✅ Auto-configured security rules
- ✅ Type-safe with comprehensive logging

**Testing Status:** ✅ Tested in spring-api-with-krd-starters
- Application starts successfully
- Stripe gateway auto-configured
- 226 lines of code eliminated from consumer (264 deleted, 38 added)
- Fixed typo: PaymentStauts → PaymentStatus

---

### Phase 13: Gradle Version Catalog ✅

**What's Included:**

#### Version Catalog (`gradle/libs.versions.toml`)
- Centralized version management for all custom dependencies
- Type-safe accessors (libs.stripe, libs.jjwt.api, libs.mapstruct)
- Single source of truth for non-Spring dependencies

**Benefits:**
- ✅ Update versions in ONE place, applies to all starters
- ✅ IDE autocomplete for dependencies
- ✅ No version duplication across modules
- ✅ Modern Gradle best practice (7.0+)

---

## 🚧 What Remains

### **Phase 11: Create Template Project** ⏸️ Pending (2-3 hours)

**Goal:** Create a GitHub template repository for instant project setup.

**Purpose:**
The spring-api-starter provides reusable code as a library dependency. The template project provides a complete **starting point** for new APIs with:
- Pre-configured project structure
- Example implementations
- Ready-to-run application
- Documentation and guides

**Template vs Starter:**

| Aspect | spring-api-starter (Library) | spring-api-template (Template) |
|--------|----------------------------|------------------------------|
| What it is | Gradle dependency | GitHub repository template |
| How you use it | `implementation 'com.krd:spring-api-starter:1.0.0'` | Click "Use this template" |
| What you get | Reusable classes/interfaces | Complete project structure |
| Customization | Extend classes, override methods | Edit files directly |
| Updates | Update version in build.gradle | Manual (copy changes) |
| Best for | Shared functionality | New project starting point |

**What to Include:**

1. **Complete Project Structure**
   ```
   spring-api-template/
   ├── src/main/java/com/example/api/
   │   ├── SpringApiTemplateApplication.java
   │   ├── users/ (example User extending BaseUser)
   │   ├── security/ (SecurityConfig, UserDetailsService)
   │   └── config/ (SwaggerConfig)
   ├── src/main/resources/
   │   ├── application.yaml
   │   ├── application-dev.yaml
   │   └── db/migration/V10__add_custom_tables.sql
   ├── docs/
   │   ├── CLAUDE.md
   │   ├── GETTING_STARTED.md
   │   └── DEPLOYMENT.md
   ├── .env.example
   ├── build.gradle (with spring-api-starter dependency)
   └── README.md
   ```

2. **Example Implementations**
   - User.java extending BaseUser
   - UserRepository extending BaseUserRepository<User>
   - UserService with template methods implemented
   - UserController (optional)
   - SecurityConfig overriding default to add public endpoints
   - UserDetailsServiceImpl

3. **Documentation**
   - README.md with quick start guide
   - GETTING_STARTED.md with detailed setup
   - DEPLOYMENT.md for Railway/Docker
   - CLAUDE.md for Claude Code users

4. **Configuration Examples**
   - application.yaml with all settings
   - .env.example for environment variables
   - Profile-specific configs (dev/prod)

5. **GitHub Template Setup**
   - Enable template repository in settings
   - Add topics: spring-boot, template, jwt, rest-api
   - Clear description and usage instructions

**Benefits:**
- ⚡ Zero setup time
- 📦 Complete project structure
- 📝 Documentation included
- 🔧 Examples of how to extend
- 🎯 Just add business logic

---

## ✅ Success Criteria

### For spring-api-starter: ✅ ALL COMPLETE
- [x] Builds successfully: `./gradlew spring-api-starter:build`
- [x] Publishes to Maven local: `./gradlew spring-api-starter:publishToMavenLocal`
- [x] Consumer can add single dependency
- [x] Extend BaseUser and it works immediately
- [x] POST /auth/login returns JWT tokens
- [x] POST /auth/refresh refreshes access token
- [x] GET /auth/me returns current user
- [x] POST /users registers users with validation
- [x] Password policy configurable via YAML
- [x] Soft delete works automatically
- [x] Auto-reactivation on re-registration works
- [x] Scheduled hard delete works (configurable)
- [x] Role management works
- [x] Integration tested in spring-api-with-krd-starters

### For payment-gateway-starter: ✅ ALL COMPLETE
- [x] Builds successfully
- [x] Publishes to Maven local
- [x] PaymentGateway interface is provider-agnostic
- [x] StripePaymentGateway works with OrderInfo interface
- [x] Auto-configures when stripe dependencies present
- [x] Webhook parsing and signature verification works
- [x] Easy to add new providers (PayPal, Square, etc.)
- [x] Integration tested in spring-api-with-krd-starters

### For version-catalog: ✅ ALL COMPLETE
- [x] gradle/libs.versions.toml created
- [x] All custom dependencies centralized
- [x] Type-safe accessors working (libs.stripe, etc.)
- [x] Documentation added to README
- [x] All starters using version catalog

### For spring-api-template: ⏸️ PENDING
- [ ] "Use this template" creates working repository
- [ ] Clone → Run works in < 5 minutes
- [ ] All endpoints documented in Swagger
- [ ] README has clear instructions
- [ ] Example implementations included
- [ ] Can deploy to Railway/Docker
- [ ] Documentation complete

---

## 🎯 What You Have Now

### 1. spring-api-starter ✅ Complete
```gradle
dependencies {
    implementation 'com.krd:spring-api-starter:1.0.0'
}
```

```java
@Entity
public class User extends BaseUser {
    // Just add custom fields - auth & user management work automatically
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}
```

### 2. payment-gateway-starter ✅ Complete
```gradle
dependencies {
    implementation 'com.krd:payment-gateway-starter:1.0.0'
}
```

```java
@Entity
public class Order implements OrderInfo {
    // Implement getOrderId() and getLineItems()
    // Payment gateway works automatically
}
```

### 3. Consumer Project ✅ Refactored
- **366+ lines of code eliminated** (140 from auth + 226 from payment)
- User extends BaseUser
- UserRepository extends BaseUserRepository<User>
- UserDto extends BaseUserDto
- AuthService extends BaseAuthService<User>
- AuthController extends BaseAuthController<User, UserDto>
- Order implements OrderInfo
- CheckoutService uses PaymentGateway
- All duplicated code removed
- Fully functional with cleaner architecture

---

## 🚀 Next Steps

**Option A: Create Template Project** (Recommended)
- Build spring-api-template repository
- Make it public on GitHub
- Enable template repository
- Document usage
- **Time:** 2-3 hours
- **Benefit:** Fastest way to start new APIs in the future

**Option B: Call it Done** ✅
- You have a working spring-api-starter
- It's tested and integrated
- Ready to use in new projects
- Template and payment gateway can wait

---

## 📝 Notes

**Document Created:** 2025-11-26
**Last Updated:** 2025-11-29
**Status:** 92% Complete (both starters done + version catalog, template pending)
**Starters Published:**
- `com.krd:spring-api-starter:1.0.0` ✅
- `com.krd:payment-gateway-starter:1.0.0` ✅
- `com.krd:security-rules-starter:1.0.0` ✅
- `com.krd:jwt-auth-starter:1.0.0` ✅

**Next Action:** Create spring-api-template repository (Phase 11) - Optional
