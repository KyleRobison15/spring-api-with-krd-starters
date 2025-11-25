# Spring API Reusability Roadmap

## 🎯 Executive Summary

This document outlines a comprehensive plan to extract reusable components from this Spring Boot e-commerce API into libraries/starters that can be used across future projects. The goal is to eliminate repetitive setup work (especially JWT authentication) and accelerate development of new APIs.

**Key Pain Point:** JWT authentication setup is cumbersome but needed in nearly every API.

**Solution:** Extract into Spring Boot Starter libraries with auto-configuration.

---

## 📊 Three Implementation Strategies

### Option 1: Spring Boot Starter Library ⭐ (Recommended)
Create custom Spring Boot starters that auto-configure functionality when added as dependencies.

**Benefits:**
- Zero configuration in new projects
- Just add dependency, everything works
- Spring Boot's auto-configuration magic
- Industry-standard approach

**Usage Example:**
```gradle
// In a new project, just add:
dependencies {
    implementation 'com.yourcompany:jwt-auth-starter:1.0.0'
}

// Add environment variables, done!
```

### Option 2: Project Template/Archetype
Create a GitHub template repository with everything pre-configured.

**Benefits:**
- Fastest way to start new projects
- Full project structure included
- Easy to customize per project
- No external dependencies

**How it works:**
- Click "Use this template" on GitHub
- Clone and rename
- Start coding business logic

### Option 3: Shared Library Module
Create a common library you import and wire up manually.

**Benefits:**
- Most flexible
- Easy to understand
- No magic
- Versioned and testable

---

## 🎯 Components to Extract (Priority Ranked)

### Tier 1: Highest ROI - Extract First

#### 1. JWT Authentication System (95% Reusable) 🔥

**Files to Extract:**
- `src/main/java/com/krd/store/auth/JwtService.java`
- `src/main/java/com/krd/store/auth/JwtConfig.java`
- `src/main/java/com/krd/store/auth/JwtAuthenticationFilter.java`
- `src/main/java/com/krd/store/auth/Jwt.java`
- `src/main/java/com/krd/store/auth/SecurityConfig.java` (base)

**Why This is Priority #1:**
- Used in every API project
- Currently 500+ lines of code to set up
- Takes hours to configure properly
- Highest pain point identified

**Current Dependencies:**
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
implementation 'io.jsonwebtoken:jjwt-impl:0.12.6'
implementation 'io.jsonwebtoken:jjwt-jackson:0.12.5'
implementation 'org.springframework.boot:spring-boot-starter-security'
```

**What Makes It Reusable:**
- Generic User interface requirement (only needs: id, email, name, role)
- Configurable token expiration
- Environment-based secret management
- Dual-token system (access + refresh)
- HttpOnly cookie handling

**Future Usage:**
```gradle
dependencies {
    implementation 'com.yourcompany:jwt-auth-starter:1.0.0'
}
```

```yaml
# application.yaml
spring:
  jwt:
    secret: ${JWT_SECRET}
    accessTokenExpiration: 900
    refreshTokenExpiration: 604800
```

That's it! JWT authentication fully configured.

---

#### 2. Modular Security Rules Pattern (100% Reusable)

**Files to Extract:**
- `src/main/java/com/krd/store/common/SecurityRules.java`
- All `*SecurityRules.java` implementations as examples

**Current Implementation:**
```java
public interface SecurityRules {
    void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>
        .AuthorizationManagerRequestMatcherRegistry registry);
}
```

**Why This is Brilliant:**
- Each module defines its own security independently
- No central configuration file to maintain
- Spring automatically collects all implementations
- Perfect separation of concerns
- Zero coupling between modules

**Example Usage:**
```java
@Component
public class ProductSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>
        .AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN");
    }
}
```

**How SecurityConfig Uses It:**
```java
@Configuration
public class SecurityConfig {
    private final List<SecurityRules> securityRules; // Auto-injected by Spring

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(c -> {
            securityRules.forEach(rule -> rule.configure(c)); // Apply all rules
            c.anyRequest().authenticated();
        });
    }
}
```

**Benefits:**
- Add new feature? Just create a new SecurityRules class
- Remove feature? Delete its SecurityRules class
- No need to touch central configuration
- Each team member can work independently

---

#### 3. Payment Gateway Pattern (95% Reusable)

**Files to Extract:**
- `src/main/java/com/krd/store/payments/PaymentGateway.java`
- `src/main/java/com/krd/store/payments/StripePaymentGateway.java`
- `src/main/java/com/krd/store/payments/CheckoutSession.java`
- `src/main/java/com/krd/store/payments/CheckoutResponse.java`
- `src/main/java/com/krd/store/payments/CheckoutRequest.java`
- `src/main/java/com/krd/store/payments/WebhookRequest.java`
- `src/main/java/com/krd/store/payments/PaymentResult.java`
- `src/main/java/com/krd/store/payments/PaymentStauts.java` (note: has typo)
- `src/main/java/com/krd/store/payments/StripeConfig.java`

**Interface Pattern (Strategy Pattern):**
```java
public interface PaymentGateway {
    CheckoutSession createCheckoutSession(Order order);
    Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);
}
```

**Why This Design is Excellent:**
- Interface-based (can swap implementations)
- Provider-agnostic DTOs
- Webhook signature verification built-in
- Transactional order management
- Metadata tracking

**Future Usage:**
```gradle
dependencies {
    implementation 'com.yourcompany:payment-gateway-starter:1.0.0'
    implementation 'com.yourcompany:stripe-payment-gateway:1.0.0'
    // or implementation 'com.yourcompany:square-payment-gateway:1.0.0'
}
```

```yaml
payment:
  provider: stripe # Switch providers with config

stripe:
  secretKey: ${STRIPE_SECRET_KEY}
  webhookSecretKey: ${STRIPE_WEBHOOK_SECRET_KEY}
```

```java
@Service
public class CheckoutService {
    private final PaymentGateway paymentGateway; // Auto-wired based on config

    public CheckoutResponse checkout(Order order) {
        CheckoutSession session = paymentGateway.createCheckoutSession(order);
        return new CheckoutResponse(session.getUrl());
    }
}
```

---

### Tier 2: High Value - Extract Soon

#### 4. Exception Handling Architecture (70% Reusable)

**Current Files:**
- `src/main/java/com/krd/store/common/GlobalExceptionHandler.java`
- Various domain exceptions

**Recommended Enhancement:**
```java
// Base exception class
public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public ApplicationException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}

// Consistent error response
@Data
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String requestId;
    private Map<String, String> fieldErrors;
}
```

#### 5. CORS Configuration

**Current Issue:** Hardcoded to `localhost:5173` in SecurityConfig

**Solution:**
```yaml
spring:
  cors:
    allowedOrigins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

#### 6. MapStruct Patterns

**Create Reusable Base Mapper:**
```java
public interface BaseMapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
    void update(D dto, @MappingTarget E entity);
    List<D> toDtoList(List<E> entities);
}
```

---

### Tier 3: Nice to Have - Extract Later

7. **Base REST Controller** - CRUD patterns
8. **Flyway Configuration Templates** - Database migration setup
9. **Logging Filter** (needs refactoring to SLF4J first)

---

## 📦 Implementation Plan

### Phase 1: Create JWT Auth Starter (Priority 1) - 1-2 Days

**Goal:** Extract the most painful component - JWT authentication.

**Project Structure:**
```
jwt-auth-starter/
├── src/
│   ├── main/
│   │   ├── java/com/yourcompany/auth/
│   │   │   ├── JwtService.java
│   │   │   ├── JwtConfig.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── Jwt.java
│   │   │   └── JwtAutoConfiguration.java  // NEW
│   │   └── resources/
│   │       └── META-INF/spring/
│   │           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
└── build.gradle
```

**Key File - JwtAutoConfiguration.java:**
```java
@Configuration
@ConditionalOnProperty(
    name = "spring.jwt.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(JwtConfig.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtConfig config) {
        return new JwtService(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }
}
```

**META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports:**
```
com.yourcompany.auth.JwtAutoConfiguration
```

**build.gradle:**
```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

group = 'com.yourcompany'
version = '1.0.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api 'org.springframework.boot:spring-boot-starter-security'
    api 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.5'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
        }
    }
    repositories {
        mavenLocal()
    }
}
```

---

### Phase 2: Extract Security Rules + Payment Gateway - 1 Day Each

Follow same pattern with auto-configuration.

---

### Phase 3: Create Project Template - 1 Day

**Template Structure:**
```
spring-api-template/
├── src/
│   ├── main/
│   │   ├── java/com/yourcompany/template/
│   │   │   └── TemplateApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       └── application-prod.yaml
├── build.gradle
├── CLAUDE.md
└── README.md
```

**build.gradle - Pre-configured:**
```gradle
dependencies {
    // Your extracted starters
    implementation 'com.yourcompany:jwt-auth-starter:1.0.0'
    implementation 'com.yourcompany:security-rules-starter:1.0.0'
    implementation 'com.yourcompany:payment-gateway-starter:1.0.0'

    // Common dependencies
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    // ... other common dependencies
}
```

---

## 🚀 Quick Start: Hybrid Approach (Recommended)

### Week 1: JWT Auth Starter (Biggest Pain Point)
1. Create jwt-auth-starter project
2. Copy JWT files
3. Add auto-configuration
4. Publish to Maven Local
5. Test in this project

### Week 2: Use Both in New Project
1. Create template from this project
2. Remove domain code
3. Add JWT starter as dependency
4. Test in real scenario
5. Gather feedback

### Week 3-4: Extract More Components
Based on real usage, extract:
- Security Rules pattern
- Payment Gateway pattern
- Exception handling
- Other utilities

---

## 💡 Example: Using Your Starters in a New Project

```gradle
// build.gradle - Clean and simple!
dependencies {
    // Your extracted reusable components
    implementation 'com.yourcompany:jwt-auth-starter:1.0.0'
    implementation 'com.yourcompany:security-rules-starter:1.0.0'
    implementation 'com.yourcompany:payment-gateway-starter:1.0.0'

    // Spring Boot basics
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
}
```

```yaml
# application.yaml - Just configuration!
spring:
  jwt:
    secret: ${JWT_SECRET}
  cors:
    allowedOrigins: ${CORS_ALLOWED_ORIGINS}

payment:
  provider: stripe

stripe:
  secretKey: ${STRIPE_SECRET_KEY}
```

```java
// Your code - Just business logic!
@RestController
@RequestMapping("/products")
public class ProductController {
    // JWT auth: ✅ Already working
    // Security rules: ✅ Just implement SecurityRules interface
    // Payments: ✅ Inject PaymentGateway

    // You write: Business logic only!
}
```

---

## 📊 ROI Calculation

### Time Investment:
- **Phase 1 (JWT Starter):** 1-2 days
- **Phase 2 (Security + Payment):** 2 days
- **Phase 3 (Template):** 1 day

**Total:** 4-5 days

### Return on Investment:
- **Current:** 2-3 days to set up each new API
- **With Starters:** 2-3 hours to set up each new API
- **Break-even:** After 3-4 new projects
- **Ongoing:** Save 2-3 days per project

**Example:**
- 10 projects per year
- Current: 20-30 days of setup work
- With starters: 2-3 days of setup work
- **Savings: 18-27 days per year**

---

## ✅ Success Criteria

### For JWT Auth Starter:
- [ ] Can add dependency and JWT works immediately
- [ ] Only requires environment variables
- [ ] No code changes needed in consuming project
- [ ] Works with any User entity that has id, email, name, role
- [ ] Access and refresh tokens working
- [ ] HttpOnly cookies configured

### For Project Template:
- [ ] "Use this template" → clone → runs in < 5 minutes
- [ ] All configuration externalized to .env
- [ ] README explains what's included
- [ ] Swagger UI works out of box
- [ ] Database migrations work
- [ ] Can deploy to Railway with zero config

---

## 💡 Pro Tips

1. **Start with Maven Local:** Publish starters to Maven Local for testing
2. **Version Carefully:** Use semantic versioning (1.0.0, 1.1.0, 2.0.0)
3. **Document Everything:** Each starter needs excellent README
4. **Add Tests:** Unit test your auto-configuration
5. **Use @ConditionalOn:** Make features optional
6. **Keep It Simple:** Don't over-engineer the first version
7. **Get Feedback:** Use in 2-3 real projects before polishing

---

## 📝 Next Steps

1. **Start with JWT Auth Starter** - Biggest pain point, highest ROI
2. **Test in real project** - Validate it works as expected
3. **Create template** - Quick win for immediate use
4. **Extract more components** - Based on actual needs

---

**Document Created:** 2025-11-23
**Status:** Ready to Execute
**Next Action:** Choose starting point and begin Phase 1
