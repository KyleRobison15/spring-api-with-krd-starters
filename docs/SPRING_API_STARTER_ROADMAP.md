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
| 7 | Testing & integration | 1 hour | ✅ Complete |
| 8 | Refactor consumer project | 1 hour | ✅ Complete |
| 9 | Create template project | 2-3 hours | ⏸️ Pending |
| **Subtotal** | **Core API Starter** | **10-14 hours** | **80% Complete** |
| | | | |
| **payment-gateway-starter (Optional)** | | | |
| 10 | Extract payment gateway | 2-3 hours | ⏸️ Pending |
| | | | |
| **Grand Total** | **Complete System** | **12-17 hours** | **66% Complete** |

---

## ✅ What's Been Completed

### Phase 1-8: Core spring-api-starter ✅

**Published to Maven Local:** `com.krd:spring-api-starter:1.0.0`

**What's Included:**

#### JWT Authentication (`com.krd.starter.jwt`)
- `Jwt.java` - JWT claims wrapper with convenience methods
- `JwtConfig.java` - @ConfigurationProperties for token settings
- `JwtService.java` - Token generation and parsing
- `JwtAuthenticationFilter.java` - Spring Security filter
- `JwtUser.java` - Interface for user entities
- DTOs: `LoginRequest`, `LoginResponse`, `JwtResponse`

#### User Management (`com.krd.starter.user`)
- `BaseUser.java` - @MappedSuperclass with id, email, password, roles, enabled, deletedAt
- `BaseUserRepository<T>` - Generic repository with soft delete queries
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
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Testing Status:** ✅ Tested in spring-api-with-krd-starters
- Application starts successfully
- JWT authentication works
- User registration with validation works
- Soft delete works
- Compilation successful (73 lines of code eliminated)

---

## 🚧 What Remains

### **Phase 9: Create Template Project** ⏸️ Pending (2-3 hours)

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

### **Phase 10: Extract Payment Gateway** ⏸️ Optional (2-3 hours)

**Goal:** Create a separate payment-gateway-starter for optional payment functionality.

**Why Separate?**
Unlike authentication and user management (needed in every API), payment processing is **optional**:
- ✅ E-commerce APIs need it
- ❌ Internal tools don't
- ❌ Read-only APIs don't

**What to Extract:**

1. **Provider-Agnostic Interface**
   ```java
   public interface PaymentGateway {
       CheckoutSession createCheckoutSession(CheckoutRequest request);
       Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);
   }
   ```

2. **Stripe Implementation**
   - StripePaymentGateway.java
   - StripeConfig.java (@ConfigurationProperties)
   - Auto-configuration with @ConditionalOnProperty

3. **Generic DTOs**
   - CheckoutRequest (works with any domain model)
   - CheckoutSession
   - CheckoutResponse
   - WebhookRequest
   - PaymentResult
   - PaymentStatus

4. **Source Files** (from spring-api-with-krd-starters)
   - `payments/PaymentGateway.java`
   - `payments/StripePaymentGateway.java`
   - `payments/StripeConfig.java`
   - All payment DTOs

**Design Benefits:**
- 🎯 Optional dependency (only add when needed)
- 🔌 Provider-agnostic (easy to switch Stripe → Square)
- 🌍 Domain-agnostic (works with orders, bookings, subscriptions)
- 💰 No unnecessary Stripe SDK in non-payment APIs

**Future Extensions:**
- SquarePaymentGateway
- PayPalPaymentGateway
- Configuration-based provider selection

---

## ✅ Success Criteria

### For spring-api-starter: ✅ ALL COMPLETE
- [x] Builds successfully: `./gradlew spring-api-starter:build`
- [x] Publishes to Maven local: `./gradlew spring-api-starter:publishToMavenLocal`
- [x] Consumer can add single dependency
- [x] Extend BaseUser and it works immediately
- [x] POST /auth/login returns JWT tokens
- [x] POST /users registers users with validation
- [x] Password policy configurable via YAML
- [x] Soft delete works automatically
- [x] Role management works
- [x] Integration tested in spring-api-with-krd-starters

### For spring-api-template: ⏸️ PENDING
- [ ] "Use this template" creates working repository
- [ ] Clone → Run works in < 5 minutes
- [ ] All endpoints documented in Swagger
- [ ] README has clear instructions
- [ ] Example implementations included
- [ ] Can deploy to Railway/Docker
- [ ] Documentation complete

### For payment-gateway-starter: ⏸️ PENDING (Optional)
- [ ] Builds successfully
- [ ] Publishes to Maven local
- [ ] PaymentGateway interface is provider-agnostic
- [ ] StripePaymentGateway works with generic CheckoutRequest
- [ ] Auto-configures when stripe.secretKey is present
- [ ] Webhook parsing and verification works
- [ ] Easy to add new providers

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

### 2. Consumer Project ✅ Refactored
- **73 lines of code eliminated**
- User extends BaseUser
- UserRepository extends BaseUserRepository<User>
- UserDto extends BaseUserDto
- All auth and validation code removed
- Still fully functional

---

## 🚀 Next Steps

**Option A: Create Template Project** (Recommended)
- Build spring-api-template repository
- Make it public on GitHub
- Enable template repository
- Document usage
- **Time:** 2-3 hours
- **Benefit:** Fastest way to start new APIs in the future

**Option B: Extract Payment Gateway** (Optional)
- Only needed if building more e-commerce APIs
- Can be done anytime
- **Time:** 2-3 hours
- **Benefit:** Reusable payment infrastructure

**Option C: Call it Done** ✅
- You have a working spring-api-starter
- It's tested and integrated
- Ready to use in new projects
- Template and payment gateway can wait

---

## 📝 Notes

**Document Created:** 2025-11-26
**Last Updated:** 2025-11-26
**Status:** 80% Complete (core starter done, template pending)
**Next Action:** Create spring-api-template repository (Phase 9)
