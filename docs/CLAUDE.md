# Claude Development Guide for Spring API with KRD Starters

This document provides comprehensive information about the Spring API with KRD Starters project to help Claude (and developers) understand the codebase, development practices, and how to make effective contributions.

## Project Overview

This is a Spring Boot REST API application demonstrating enterprise-grade authentication, authorization, and user management. The project showcases:

- Custom Spring Boot Starters for reusable security components
- JWT-based authentication with dual-token system (access + refresh)
- Role-based access control (RBAC) with USER and ADMIN roles
- Comprehensive user management with security best practices
- Password security with configuration-based validation
- Soft delete pattern for data preservation
- Audit logging for security-sensitive operations
- Stripe payment integration
- Flyway database migrations

## Technology Stack

**Backend Framework:**
- Spring Boot 3.4.5
- Java 21
- Gradle (multi-module build)

**Security:**
- Spring Security 6.x
- JWT (JSON Web Tokens) for stateless authentication
- BCrypt password hashing
- Custom security starters (jwt-auth-starter, security-rules-starter)

**Database:**
- MySQL (production)
- Flyway for version-controlled migrations
- Spring Data JPA with Hibernate

**Validation & Mapping:**
- Jakarta Bean Validation (formerly javax.validation)
- MapStruct for DTO-Entity mapping
- Custom validation annotations

**Documentation:**
- Swagger/OpenAPI 3 (Springdoc)

**Payment Processing:**
- Stripe API integration

**Testing:**
- JUnit 5
- Spring Boot Test

## Repository Structure

This project is part of a multi-repository setup:

1. **krd-spring-starters** - Custom Spring Boot starters
   - `jwt-auth-starter` (v1.0.0) - JWT authentication infrastructure
   - `security-rules-starter` (v1.0.0) - Modular security configuration

2. **spring-api-with-krd-starters** - Main application (this repository)
   - Uses the custom starters
   - Implements business logic and REST APIs

## Development Workflow

### Branching Strategy

This project follows a feature branch workflow:

1. **Main Branch (`main`)**
   - Production-ready code
   - All features must be tested before merging
   - Protected branch (typically)

2. **Feature Branches**
   - Created for major changes, new features, or significant refactoring
   - Naming convention: `feature/<descriptive-name>`
     - Example: `feature/security-rules-starter`
     - Example: `feature/password-security-enhancements`
     - Example: `feature/soft-delete-users`

3. **Branch Lifecycle**
   - Create feature branch from `main`
   - Make changes with descriptive commits
   - Test thoroughly
   - Merge to `main` when complete
   - Push to GitHub

### Commit Guidelines

Make descriptive commits at logical stopping points:

**Good commit practices:**
- Commits should describe WHAT changed and WHY
- Group related changes together
- Commit after completing a coherent unit of work
- Use present tense ("Add feature" not "Added feature")

**Examples from this project:**
```
Add robust security and validation to user management endpoints
- Add authorization checks to PUT /users/{id} (self or admin)
- Add authorization checks to DELETE /users/{id} (admin only)
- Add email/username uniqueness validation on update
- Prevent self-deletion by admins
- Prevent deletion of last admin user
- Add countByRolesContaining to UserRepository
```

```
Implement soft delete for user management
- Add deletedAt timestamp field to User entity
- Create Flyway migration V9 for deleted_at column with index
- Override JPA repository methods to filter soft-deleted users
- Update deleteUser to set deletedAt and disable account
- Preserve data for audit and compliance requirements
```

```
Fix critical security vulnerabilities in change password endpoint
- Add authorization check (users can only change own password)
- Add password confirmation validation
- Fix CRITICAL bug: Hash password before storing (was storing plaintext!)
- Improve error messages for better UX
- Add @Valid annotation to controller endpoint
```

## Key Components

### 1. Authentication System

**Location:** `com.krd.store.auth`

**How it works:**
- Users authenticate with email/password
- Server issues two JWT tokens:
  - **Access Token**: Short-lived (15 minutes), used for API requests
  - **Refresh Token**: Long-lived (7 days), used to obtain new access tokens
- Tokens contain user ID as principal
- Spring Security validates tokens on each request

**Configuration:**
```yaml
spring:
  jwt:
    secret: ${JWT_SECRET}
    accessTokenExpiration: 900    # 15 minutes
    refreshTokenExpiration: 604800 # 7 days
```

### 2. User Management

**Location:** `com.krd.store.users`

**Key Features:**
- User registration with validation
- Email and username uniqueness enforcement
- Soft delete (preserves data, sets deletedAt timestamp)
- Role management (add/remove roles)
- Audit logging for role changes
- Password change with confirmation

**Security Rules:**
- **GET /users**: Admin only
- **GET /users/{id}**: Authenticated users (can view any user)
- **POST /users** (register): Public
- **PUT /users/{id}**: Self or admin
- **DELETE /users/{id}**: Admin only (cannot delete self or last admin)
- **POST /users/{id}/change-password**: Self only
- **POST /users/{id}/roles**: Admin only
- **DELETE /users/{id}/roles**: Admin only (cannot remove own admin role)
- **GET /users/{id}/roles**: Admin only

### 3. Password Security

**Location:** `com.krd.store.common.validation`, `com.krd.store.common.config`

**Configuration-Based Validation:**
- Custom `@ValidPassword` annotation
- `PasswordValidator` constraint validator with Spring DI
- `PasswordPolicy` configuration class with `@ConfigurationProperties`
- Centralized rules in `application.yaml`

**Current Policy:**
```yaml
app:
  security:
    password:
      min-length: 8
      max-length: 128
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
```

**Usage:**
```java
@NotBlank(message = "Password is required")
@ValidPassword
private String password;
```

### 4. Role-Based Access Control

**Implementation:**
- Roles stored as `Set<String>` in User entity
- Two primary roles: `USER`, `ADMIN`
- Default role: `USER` (assigned on registration)
- Method-level security with `@PreAuthorize("hasRole('ADMIN')")`
- Modular security rules with `SecurityRules` interface

**Authorization Patterns:**

*Option 1: SecurityRules interface (modular, reusable)*
```java
@Component
public class AdminSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers("/admin/**").hasRole("ADMIN");
    }
}
```

*Option 2: @PreAuthorize annotation (method-level, explicit)*
```java
@PreAuthorize("hasRole('ADMIN')")
public UserDto addRole(@PathVariable Long id, @RequestBody AddRoleRequest request) {
    return userService.addRole(id, request);
}
```

**When to use which:**
- Use `SecurityRules` for URL patterns, public endpoints, authentication config
- Use `@PreAuthorize` for fine-grained method-level authorization
- `@PreAuthorize` returns 403 Forbidden (correct for authorization failures)
- Missing authentication returns 401 Unauthorized

### 5. Soft Delete Pattern

**Implementation:**
- `deletedAt` timestamp field in User entity (nullable)
- Non-deleted users: `deletedAt = NULL`
- Deleted users: `deletedAt = <timestamp>` AND `enabled = false`
- JPA repository methods overridden with `@Query` to filter deleted users

**Example:**
```java
@Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
Optional<User> findById(@Param("id") Long id);

@Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
List<User> findAll(Sort sort);
```

**Benefits:**
- Preserves data for audit/compliance
- Maintains referential integrity
- Enables "undelete" functionality (future feature)
- Supports analytics on historical data

### 6. Database Migrations

**Location:** `src/main/resources/db/migration`

**Current Migrations:**
- V1: Initial schema (users, roles)
- V2-V8: Various enhancements
- V9: Add `deleted_at` column with index

**Flyway Configuration:**
- Runs automatically on application startup
- Version-controlled schema changes
- Never modify existing migrations (create new ones)

## Recent Enhancements

### Password Security & Validation (Completed)

**What was done:**
1. Created configuration-based password validation system
2. Fixed CRITICAL security bug in change password endpoint (passwords were stored in plaintext!)
3. Added authorization checks (users can only change own password)
4. Added password confirmation validation
5. Centralized password rules in `application.yaml`

**Files changed:**
- Created: `PasswordPolicy.java`, `ValidPassword.java`, `PasswordValidator.java`
- Modified: `application.yaml`, `RegisterUserRequest.java`, `ChangePasswordRequest.java`
- Modified: `UserService.changePassword()`, `UserController.changePassword()`

### User Management Security (Completed)

**What was done:**
1. Added authorization to PUT /users/{id} (self or admin)
2. Added authorization to DELETE /users/{id} (admin only)
3. Added email/username uniqueness validation on updates
4. Prevented self-deletion by admins
5. Prevented deletion of last admin user
6. Added `countByRolesContaining` to UserRepository

**Files changed:**
- Modified: `UserService.updateUser()`, `UserService.deleteUser()`
- Modified: `UserController.updateUser()`, `UserController.deleteUser()`
- Modified: `UserRepository.java`
- Modified: `DuplicateUserException.java` (added custom message constructor)

### Soft Delete Implementation (Completed)

**What was done:**
1. Added `deletedAt` timestamp field to User entity
2. Created Flyway migration V9 with index
3. Overrode JPA repository methods to filter soft-deleted users
4. Changed delete logic to set timestamp and disable account

**Files changed:**
- Modified: `User.java` (added deletedAt field)
- Created: `V9__add_deleted_at_to_users.sql`
- Modified: `UserRepository.java` (added @Query overrides)
- Modified: `UserService.deleteUser()` (soft delete logic)

### Security Rules Starter (Completed)

**What was done:**
1. Created `security-rules-starter` module in krd-spring-starters
2. Implemented `SecurityRules` interface for modular security configuration
3. Created auto-configuration for Spring Boot
4. Published to Maven local (v1.0.0)
5. Integrated into spring-api-with-krd-starters

**Files created:**
- `SecurityRules.java` (core interface)
- `SecurityRulesAutoConfiguration.java`
- Auto-configuration imports file
- Module build.gradle

## Future Enhancements

### OAuth 2.0 Integration (Planned)

**See:** `docs/OAUTH_AND_AUTH0.md` for detailed analysis

**What it is:**
- Authorization framework for delegated access
- Allows "Sign in with Google/GitHub" functionality
- Industry standard for third-party authentication

**When to consider:**
- Need social login (Google, Facebook, GitHub, etc.)
- Building multi-tenant applications
- Want to delegate user management to identity provider

**Implementation approach:**
- Add spring-boot-starter-oauth2-client dependency
- Configure OAuth2 providers in application.yaml
- Update security configuration
- Potentially keep JWT for API-to-API communication

### Auth0 Migration (Planned)

**See:** `docs/OAUTH_AND_AUTH0.md` for detailed analysis

**What it is:**
- Authentication-as-a-Service (managed identity platform)
- Provides hosted login pages, user management, MFA
- Alternative to building custom authentication

**When to consider:**
- Want to outsource authentication infrastructure
- Need enterprise features (SSO, MFA, advanced security)
- Prefer managed service over DIY approach

**Trade-offs:**
- **Pros**: Less code to maintain, enterprise features, security updates handled
- **Cons**: Vendor lock-in, monthly costs, less control

**Competitors:**
- Okta (enterprise-focused)
- Firebase Authentication (Google, developer-friendly)
- AWS Cognito (AWS ecosystem)
- Azure AD B2C (Microsoft ecosystem)

## Common Development Patterns

### Adding a New Endpoint

1. Create/update DTO classes with validation annotations
2. Add method to Service class with business logic
3. Add endpoint to Controller with `@PreAuthorize` if needed
4. Update SecurityRules if URL-pattern security needed
5. Test manually and write unit/integration tests
6. Commit with descriptive message

### Adding a Database Column

1. Modify Entity class (add field with JPA annotations)
2. Create new Flyway migration (V{next}__description.sql)
3. Test migration with `./gradlew flywayMigrate` or app restart
4. Update DTOs and mappers if needed
5. Commit migration and code changes together

### Adding Password/User Validation

1. Add validation rules to PasswordPolicy (if password-related)
2. Update application.yaml with new configuration
3. Use existing `@ValidPassword` or create new custom annotation
4. Add validation logic to Service layer if complex business rules
5. Test with invalid/valid inputs
6. Commit with description of new validation rules

## Working with Claude

### Best Practices

1. **Read Before Modify**: Always ask Claude to read existing files before making changes
2. **Incremental Changes**: Make changes in logical steps, commit at stopping points
3. **Explain Context**: Reference this document when starting new sessions
4. **Review Before Commit**: Review all changes before creating commits
5. **Ask Questions**: If approach is unclear, ask Claude to explain options first

### Useful Prompts

**Understanding Code:**
- "Explain how the JWT authentication flow works in this project"
- "What security checks are in place for the DELETE /users/{id} endpoint?"
- "How does the soft delete pattern work in the User repository?"

**Making Changes:**
- "Add email verification to the user registration flow"
- "Implement rate limiting for authentication endpoints"
- "Add pagination to the GET /users endpoint"

**Refactoring:**
- "Look at the UserService class and suggest improvements"
- "Is there duplicate code that could be extracted into a utility?"
- "Should this validation be in the controller or service layer?"

**Planning:**
- "What's the best approach to add password reset functionality?"
- "How should I structure a new payment module?"
- "What security considerations are needed for an admin dashboard?"

### Common Requests

**Create descriptive commits:**
"Let's create some commits with descriptions of what was changed."

**Start new feature:**
"Let's work on adding [feature]. First, let me understand the current implementation by reading [relevant files]."

**Improve existing endpoint:**
"Let's take a look at the [endpoint]. How can this be improved for security/validation/error handling?"

**Fix build errors:**
"The build is failing with [error]. Let's investigate and fix it."

## Quick Reference

### Build & Run

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Database Migrations

```bash
# Run migrations
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo

# Repair migration history (use carefully)
./gradlew flywayRepair
```

### Publishing Starters (krd-spring-starters)

```bash
# Publish to Maven local
./gradlew publishToMavenLocal

# Check dependencies
./gradlew dependencies
```

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/descriptive-name

# Make changes, then commit
git add .
git commit -m "Descriptive commit message"

# Merge to main
git checkout main
git merge feature/descriptive-name

# Push to GitHub
git push origin main

# Delete feature branch (optional)
git branch -d feature/descriptive-name
```

## Troubleshooting

### Build Failures

**Gradle dependency resolution errors:**
- Run `./gradlew clean build --refresh-dependencies`
- Check that custom starters are published: `./gradlew publishToMavenLocal` in krd-spring-starters

**Flyway migration errors:**
- Check that database is running and accessible
- Verify migration files are in correct location
- Use `./gradlew flywayInfo` to check migration status

### Runtime Issues

**JWT validation failures:**
- Check that JWT_SECRET environment variable is set
- Verify token hasn't expired (check expiration times in application.yaml)
- Confirm token format is correct (Bearer <token>)

**Authorization returning 401 instead of 403:**
- Ensure user is authenticated (valid JWT token)
- 401 = not authenticated, 403 = authenticated but not authorized
- Check that Spring Security is processing authentication before authorization

**Soft-deleted users still appearing:**
- Verify @Query annotations include `AND u.deletedAt IS NULL`
- Check that repository method is actually using the overridden version
- Test with `userRepository.findAll()` directly

## Important Notes

### Security Considerations

- **NEVER commit secrets** (.env files, JWT_SECRET, API keys)
- **Always hash passwords** with BCrypt before storing
- **Validate all user input** at controller and service layers
- **Use @Valid annotation** for DTO validation
- **Log security events** (role changes, failed auth attempts)
- **Follow principle of least privilege** (minimum necessary permissions)

### Code Quality

- **Avoid over-engineering**: Only add complexity when needed
- **Don't add features not requested**: Stay focused on requirements
- **Keep solutions simple**: Prefer clarity over cleverness
- **Don't add comments** unless logic is truly complex
- **Delete unused code**: Don't comment out or leave "removed" markers

### Testing

- Test security rules thoroughly (both positive and negative cases)
- Verify authorization at method and URL pattern levels
- Test edge cases (last admin, self-deletion, etc.)
- Validate error messages are user-friendly but not revealing sensitive info

## Related Documentation

- **OAuth & Auth0 Analysis**: `docs/OAUTH_AND_AUTH0.md`
- **JWT Auth Starter**: `~/MyDev/Spring_Starters/jwt-auth-starter/`
- **Security Rules Starter**: `~/MyDev/Spring_Starters/krd-spring-starters/security-rules-starter/`

---

**Last Updated**: 2025-01-25

This document should be updated whenever significant architectural changes are made or new patterns are established.
