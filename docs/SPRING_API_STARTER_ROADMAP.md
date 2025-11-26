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

**Result:** Add one dependency → extend BaseUser → authentication and user management work immediately.

---

## 🎯 Why Monolithic Instead of Separate Starters?

**Decision:** Single `spring-api-starter` instead of separate `jwt-auth-starter` + `user-management-starter`

**Reasoning:**
1. **Always used together** - Will never need JWT without user management
2. **Faster to implement** - One project, one version, one publish
3. **Simpler to use** - One dependency instead of two
4. **Internal use case** - Optimized for personal rapid development, not public distribution
5. **Still internally organized** - Clean package structure (jwt/, user/, validation/)
6. **Easy to split later** - Can extract if needed (but probably won't need to)

---

## 🗂️ Project Structure

```
krd-spring-starters/
├── jwt-auth-starter/          (existing - may deprecate later)
├── security-rules-starter/     (existing - still useful)
└── spring-api-starter/         (NEW - monolithic)
    ├── src/
    │   ├── main/
    │   │   ├── java/com/krd/starter/
    │   │   │   ├── jwt/                          ← JWT Authentication
    │   │   │   │   ├── Jwt.java
    │   │   │   │   ├── JwtConfig.java
    │   │   │   │   ├── JwtService.java
    │   │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   │   ├── JwtUser.java (interface)
    │   │   │   │   ├── AuthService.java
    │   │   │   │   ├── AuthController.java
    │   │   │   │   └── dto/
    │   │   │   │       ├── LoginRequest.java
    │   │   │   │       ├── LoginResponse.java
    │   │   │   │       └── JwtResponse.java
    │   │   │   │
    │   │   │   ├── user/                         ← User Management
    │   │   │   │   ├── entity/
    │   │   │   │   │   ├── BaseUser.java (@MappedSuperclass)
    │   │   │   │   │   └── RoleChangeLog.java
    │   │   │   │   ├── repository/
    │   │   │   │   │   ├── BaseUserRepository.java (with soft delete)
    │   │   │   │   │   └── RoleChangeLogRepository.java
    │   │   │   │   ├── service/
    │   │   │   │   │   └── BaseUserService.java (abstract, generic)
    │   │   │   │   ├── controller/
    │   │   │   │   │   └── BaseUserController.java (optional)
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── BaseUserDto.java
    │   │   │   │   │   ├── RegisterUserRequest.java
    │   │   │   │   │   ├── UpdateUserRequest.java
    │   │   │   │   │   ├── ChangePasswordRequest.java
    │   │   │   │   │   ├── AddRoleRequest.java
    │   │   │   │   │   └── RemoveRoleRequest.java
    │   │   │   │   ├── exception/
    │   │   │   │   │   ├── UserNotFoundException.java
    │   │   │   │   │   └── DuplicateUserException.java
    │   │   │   │   └── security/
    │   │   │   │       ├── BaseUserDetailsService.java
    │   │   │   │       └── SecurityConfig.java (base template)
    │   │   │   │
    │   │   │   ├── validation/                   ← Password Validation
    │   │   │   │   ├── ValidPassword.java
    │   │   │   │   ├── PasswordValidator.java
    │   │   │   │   ├── PasswordPolicy.java
    │   │   │   │   ├── Lowercase.java
    │   │   │   │   └── LowercaseValidator.java
    │   │   │   │
    │   │   │   └── SpringApiAutoConfiguration.java
    │   │   │
    │   │   └── resources/
    │   │       ├── META-INF/spring/
    │   │       │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │   │       └── db/migration/
    │   │           ├── V1__create_users_table.sql
    │   │           ├── V2__create_user_roles_table.sql
    │   │           └── V3__create_role_change_logs_table.sql
    │   │
    │   └── test/
    │       └── java/com/krd/starter/
    │           └── ... (unit tests)
    │
    ├── build.gradle
    └── README.md
```

---

## 📦 Phase-by-Phase Implementation

### **Phase 1: Project Setup** ⏱️ 30 minutes

**Goal:** Create project skeleton and configure build.

**Tasks:**
1. ✅ Create `spring-api-starter` directory in `krd-spring-starters`
2. ✅ Create `build.gradle` with all dependencies
3. ✅ Create package structure (jwt/, user/, validation/)
4. ✅ Update `settings.gradle` to include new module
5. ✅ Create `README.md` stub

**build.gradle:**
```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

group = 'com.krd'
version = '1.0.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Spring Boot Core
    api 'org.springframework.boot:spring-boot-starter-web'
    api 'org.springframework.boot:spring-boot-starter-security'
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    api 'org.springframework.boot:spring-boot-starter-validation'

    // JWT
    api 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.6'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.5'

    // Database migrations
    api 'org.flywaydb:flyway-core'
    api 'org.flywaydb:flyway-mysql'

    // Utilities
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            versionMapping {
                usage('java-api') {
                    fromResolutionOf('runtimeClasspath')
                }
                usage('java-runtime') {
                    fromResolutionResult()
                }
            }
            pom {
                name = 'Spring API Starter'
                description = 'Opinionated Spring Boot starter with JWT auth and user management'
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
```

**settings.gradle update:**
```gradle
rootProject.name = 'krd-spring-starters'

include 'jwt-auth-starter'
include 'security-rules-starter'
include 'spring-api-starter'  // ADD THIS
```

**Validation:**
```bash
cd ~/MyDev/Spring_Starters/krd-spring-starters
./gradlew spring-api-starter:build
# Should compile successfully (no code yet)
```

---

### **Phase 2: Extract JWT Authentication** ⏱️ 1-2 hours

**Goal:** Move JWT authentication from spring-api-with-krd-starters to starter.

**Source Location:** `spring-api-with-krd-starters/src/main/java/com/krd/store/auth/`

**Destination:** `spring-api-starter/src/main/java/com/krd/starter/jwt/`

**Files to Extract:**

| Source File | Destination | Changes Needed |
|-------------|-------------|----------------|
| `Jwt.java` | `jwt/Jwt.java` | Update package |
| `JwtConfig.java` | `jwt/JwtConfig.java` | Update package |
| `JwtService.java` | `jwt/JwtService.java` | Update package + imports |
| `JwtAuthenticationFilter.java` | `jwt/JwtAuthenticationFilter.java` | Update package + imports |
| `JwtUser.java` | `jwt/JwtUser.java` | Keep generic (interface) |
| `AuthService.java` | `jwt/AuthService.java` | Update package + imports |
| `AuthController.java` | `jwt/AuthController.java` | Update package + imports |
| `LoginRequest.java` | `jwt/dto/LoginRequest.java` | Update package |
| `LoginResponse.java` | `jwt/dto/LoginResponse.java` | Update package |
| `JwtResponse.java` | `jwt/dto/JwtResponse.java` | Update package |

**Key Interface - JwtUser.java:**
```java
package com.krd.starter.jwt;

import java.util.Set;

/**
 * Interface that user entities must implement to work with JWT authentication.
 * Provides the minimal contract needed for token generation.
 */
public interface JwtUser {
    Long getId();
    String getEmail();
    String getFirstName();
    String getLastName();
    Set<String> getRoles();
}
```

**Changes:**
- Update all package declarations: `com.krd.store.auth` → `com.krd.starter.jwt`
- Update all imports
- Remove any store-specific business logic
- Keep everything generic and reusable

**Validation:**
```bash
./gradlew spring-api-starter:compileJava
# Should compile successfully
```

---

### **Phase 3: Extract User Management** ⏱️ 2-3 hours

**Goal:** Create reusable base user entity, repository, and service.

#### **3a. Create BaseUser Entity**

**Source:** `User.java` in spring-api-with-krd-starters

**Destination:** `user/entity/BaseUser.java`

**Key Changes:**
- Change from `@Entity` to `@MappedSuperclass`
- Remove domain-specific fields (addresses, favoriteProducts)
- Implement `JwtUser` interface
- Keep core fields: id, firstName, lastName, username, email, password, roles, enabled, deletedAt

```java
package com.krd.starter.user.entity;

import com.krd.starter.jwt.JwtUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass  // Not @Entity - this is a base class
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseUser implements JwtUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // JwtUser interface implementation
    @Override
    public Long getId() { return id; }

    @Override
    public String getEmail() { return email; }

    @Override
    public String getFirstName() { return firstName; }

    @Override
    public String getLastName() { return lastName; }

    @Override
    public Set<String> getRoles() { return roles; }

    // Utility methods
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "username = " + username + ", " +
                "email = " + email + ", " +
                "enabled = " + enabled + ")";
    }
}
```

#### **3b. Create BaseUserRepository**

**Source:** `UserRepository.java`

**Destination:** `user/repository/BaseUserRepository.java`

**Key Changes:**
- Add `@NoRepositoryBean` annotation
- Make generic with type parameter `<T extends BaseUser>`
- Use `#{#entityName}` in queries (works with any entity)

```java
package com.krd.starter.user.repository;

import com.krd.starter.user.entity.BaseUser;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Base repository for user entities with soft delete support.
 * All queries automatically filter out soft-deleted users (deletedAt IS NOT NULL).
 *
 * Extend this in your application:
 * public interface UserRepository extends BaseUserRepository<User> { }
 */
@NoRepositoryBean  // Prevents Spring from creating a bean for this interface
public interface BaseUserRepository<T extends BaseUser> extends JpaRepository<T, Long> {

    // Override default methods to exclude soft-deleted users

    @Query("SELECT u FROM #{#entityName} u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<T> findById(@Param("id") Long id);

    @Query("SELECT u FROM #{#entityName} u WHERE u.deletedAt IS NULL")
    List<T> findAll();

    @Query("SELECT u FROM #{#entityName} u WHERE u.deletedAt IS NULL")
    List<T> findAll(Sort sort);

    @Query("SELECT u FROM #{#entityName} u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<T> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM #{#entityName} u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM #{#entityName} u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) FROM #{#entityName} u JOIN u.roles r WHERE r = :role AND u.deletedAt IS NULL")
    long countByRolesContaining(@Param("role") String role);
}
```

#### **3c. Create BaseUserService**

**Source:** `UserService.java`

**Destination:** `user/service/BaseUserService.java`

**Key Changes:**
- Make abstract with generic types
- Add template methods for customization
- Keep all business logic (registration, update, delete, password change, role management)

```java
package com.krd.starter.user.service;

import com.krd.starter.user.entity.BaseUser;
import com.krd.starter.user.entity.RoleChangeLog;
import com.krd.starter.user.repository.BaseUserRepository;
import com.krd.starter.user.repository.RoleChangeLogRepository;
import com.krd.starter.user.dto.*;
import com.krd.starter.user.exception.*;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Base service for user management operations.
 * Provides all CRUD operations, password management, and role management.
 *
 * Extend this in your application and implement the template methods:
 *
 * @Service
 * public class UserService extends BaseUserService<User, UserRepository, UserDto> {
 *     @Override
 *     protected User createNewUser() { return new User(); }
 *
 *     @Override
 *     protected UserDto toDto(User user) { return mapper.toDto(user); }
 * }
 */
public abstract class BaseUserService<
    T extends BaseUser,
    R extends BaseUserRepository<T>,
    D  // DTO type (flexible - could be BaseUserDto or custom)
> {
    protected final R repository;
    protected final PasswordEncoder passwordEncoder;
    protected final RoleChangeLogRepository roleChangeLogRepository;

    public BaseUserService(R repository,
                          PasswordEncoder passwordEncoder,
                          RoleChangeLogRepository roleChangeLogRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleChangeLogRepository = roleChangeLogRepository;
    }

    // Template methods - must be implemented by subclass
    protected abstract T createNewUser();
    protected abstract D toDto(T user);
    protected abstract void updateEntityFromRequest(UpdateUserRequest request, T user);

    // Concrete methods - inherited by all subclasses

    public Iterable<D> getAllUsers(String sort) {
        if(!Set.of("firstName", "lastName", "username", "email").contains(sort)) {
            sort = "email";
        }

        return repository.findAll(Sort.by(sort))
                .stream()
                .map(this::toDto)
                .toList();
    }

    public D getUser(Long id) {
        T user = repository.findById(id).orElseThrow(UserNotFoundException::new);
        return toDto(user);
    }

    public D registerUser(RegisterUserRequest request) {
        if (repository.existsByEmail(request.getEmail())){
            throw new DuplicateUserException();
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()
            && repository.existsByUsername(request.getUsername())){
            throw new DuplicateUserException("A user with this username already exists");
        }

        T user = createNewUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of("USER")); // Default role
        user.setEnabled(true);

        repository.save(user);
        return toDto(user);
    }

    public D updateUser(Long userId, UpdateUserRequest request) {
        var currentUserId = getCurrentUserId();
        var currentUser = repository.findById(currentUserId).orElseThrow();
        var targetUser = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Authorization: Users can update themselves, admins can update anyone
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        if (!userId.equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(targetUser.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new DuplicateUserException("Email already in use");
            }
        }

        // Validate username uniqueness if changed
        if (request.getUsername() != null && !request.getUsername().equals(targetUser.getUsername())) {
            if (repository.existsByUsername(request.getUsername())) {
                throw new DuplicateUserException("Username already in use");
            }
        }

        updateEntityFromRequest(request, targetUser);
        repository.save(targetUser);

        return toDto(targetUser);
    }

    public void deleteUser(Long userId) {
        var currentUserId = getCurrentUserId();
        var userToDelete = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Prevent self-deletion by admin
        if (userId.equals(currentUserId)) {
            throw new AccessDeniedException("Admins cannot delete their own account");
        }

        // Prevent deleting the last admin
        if (userToDelete.getRoles().contains("ADMIN")) {
            long adminCount = repository.countByRolesContaining("ADMIN");
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user. There must always be at least one admin.");
            }
        }

        // Soft delete: Mark as deleted and disable account
        userToDelete.setDeletedAt(LocalDateTime.now());
        userToDelete.setEnabled(false);
        repository.save(userToDelete);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        var currentUserId = getCurrentUserId();

        // Authorization: Users can only change their own password
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only change your own password");
        }

        var user = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Verify current password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Hash the new password before storing (CRITICAL)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    public D addRole(Long userId, AddRoleRequest request) {
        var user = repository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Add the role to the user's existing roles
        boolean wasAdded = user.getRoles().add(request.getRole());

        if (wasAdded) {
            repository.save(user);
            logRoleChange(user, request.getRole(), "ADDED");
        }

        return toDto(user);
    }

    public D removeRole(Long userId, RemoveRoleRequest request) {
        var user = repository.findById(userId).orElseThrow(UserNotFoundException::new);
        var currentUserId = getCurrentUserId();

        // Prevent self-demotion from ADMIN role
        if (userId.equals(currentUserId) && "ADMIN".equals(request.getRole())) {
            throw new AccessDeniedException("Cannot remove ADMIN role from yourself");
        }

        // Ensure user has at least one role
        if (user.getRoles().size() <= 1) {
            throw new IllegalStateException("User must have at least one role");
        }

        // Remove the role
        boolean wasRemoved = user.getRoles().remove(request.getRole());

        if (wasRemoved) {
            repository.save(user);
            logRoleChange(user, request.getRole(), "REMOVED");
        }

        return toDto(user);
    }

    // Helper methods

    protected Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }

    private void logRoleChange(T user, String role, String action) {
        var currentUserId = getCurrentUserId();
        var currentUser = repository.findById(currentUserId).orElse(null);

        var log = RoleChangeLog.builder()
                .userId(user.getId())
                .changedByUserId(currentUserId)
                .role(role)
                .action(action)
                .changedAt(LocalDateTime.now())
                .userEmail(user.getEmail())
                .changedByEmail(currentUser != null ? currentUser.getEmail() : "unknown")
                .build();

        roleChangeLogRepository.save(log);
    }
}
```

#### **3d. Extract All DTOs**

Copy from `spring-api-with-krd-starters` to `user/dto/`:
- ✅ `RegisterUserRequest.java`
- ✅ `UpdateUserRequest.java`
- ✅ `ChangePasswordRequest.java`
- ✅ `AddRoleRequest.java`
- ✅ `RemoveRoleRequest.java`

Create new:
- ✅ `BaseUserDto.java` (simple DTO with core fields)

#### **3e. Extract Exceptions**

Copy to `user/exception/`:
- ✅ `UserNotFoundException.java`
- ✅ `DuplicateUserException.java`

#### **3f. Extract UserDetailsService**

Create `user/security/BaseUserDetailsService.java`:

```java
package com.krd.starter.user.security;

import com.krd.starter.user.entity.BaseUser;
import com.krd.starter.user.repository.BaseUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

/**
 * UserDetailsService implementation for BaseUser.
 * Loads users by email for authentication.
 */
public class BaseUserDetailsService<T extends BaseUser, R extends BaseUserRepository<T>>
    implements UserDetailsService {

    private final R userRepository;

    public BaseUserDetailsService(R userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}
```

#### **3g. Extract Audit Logging**

Copy to `user/entity/`:
- ✅ `RoleChangeLog.java`

Copy to `user/repository/`:
- ✅ `RoleChangeLogRepository.java`

**Validation:**
```bash
./gradlew spring-api-starter:compileJava
```

---

### **Phase 4: Extract Password Validation** ⏱️ 30 minutes

**Goal:** Bundle password validation into the starter.

**Source:** `spring-api-with-krd-starters/src/main/java/com/krd/store/common/validation/` and `common/config/`

**Destination:** `spring-api-starter/src/main/java/com/krd/starter/validation/`

**Files to Extract:**
- ✅ `PasswordPolicy.java` (@ConfigurationProperties)
- ✅ `ValidPassword.java` (annotation)
- ✅ `PasswordValidator.java` (validator)
- ✅ `Lowercase.java` (annotation - used for email)
- ✅ `LowercaseValidator.java`

**Changes:**
- Update package: `com.krd.store.common.validation` → `com.krd.starter.validation`
- Update package: `com.krd.store.common.config` → `com.krd.starter.validation`
- Update imports in validators

**Validation:**
```bash
./gradlew spring-api-starter:compileJava
```

---

### **Phase 5: Create Database Migrations** ⏱️ 30 minutes

**Goal:** Provide Flyway migrations that run automatically.

**Location:** `spring-api-starter/src/main/resources/db/migration/`

**V1__create_users_table.sql:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    deleted_at DATETIME,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_deleted_at (deleted_at)
);
```

**V2__create_user_roles_table.sql:**
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**V3__create_role_change_logs_table.sql:**
```sql
CREATE TABLE role_change_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    changed_at DATETIME NOT NULL,
    user_email VARCHAR(255),
    changed_by_email VARCHAR(255),
    INDEX idx_user_id (user_id),
    INDEX idx_changed_at (changed_at)
);
```

**Note:** These migrations will run automatically when a consumer project starts with Flyway enabled.

---

### **Phase 6: Create Auto-Configuration** ⏱️ 1 hour

**Goal:** Auto-configure beans so everything works automatically.

**File:** `SpringApiAutoConfiguration.java`

```java
package com.krd.starter;

import com.krd.starter.jwt.*;
import com.krd.starter.validation.PasswordPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Auto-configuration for Spring API Starter.
 * Provides JWT authentication and password validation infrastructure.
 *
 * User-specific beans (UserRepository, UserService) must be provided by the application.
 */
@Configuration
@EnableConfigurationProperties({JwtConfig.class, PasswordPolicy.class})
public class SpringApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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

    @Bean
    @ConditionalOnMissingBean
    public AuthService authService(
        JwtService jwtService,
        PasswordEncoder passwordEncoder,
        UserDetailsService userDetailsService
    ) {
        return new AuthService(jwtService, passwordEncoder, userDetailsService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthController authController(AuthService authService) {
        return new AuthController(authService);
    }
}
```

**File:** `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
com.krd.starter.SpringApiAutoConfiguration
```

**What's Auto-Configured:**
- ✅ PasswordEncoder (BCrypt)
- ✅ JwtService (token generation/validation)
- ✅ JwtAuthenticationFilter (Spring Security filter)
- ✅ AuthService (login/refresh logic)
- ✅ AuthController (POST /auth/login, /auth/refresh)
- ✅ JwtConfig and PasswordPolicy configuration properties

**What Consumer Must Provide:**
- ❌ User entity (extends BaseUser)
- ❌ UserRepository (extends BaseUserRepository)
- ❌ UserService (extends BaseUserService)
- ❌ UserDetailsService (use BaseUserDetailsService)

---

### **Phase 7: Testing** ⏱️ 1 hour

**Goal:** Publish to Maven local and test in spring-api-with-krd-starters.

**Step 1: Build and Publish**

```bash
cd ~/MyDev/Spring_Starters/krd-spring-starters
./gradlew spring-api-starter:clean build
./gradlew spring-api-starter:publishToMavenLocal
```

**Step 2: Update Consumer Project**

**build.gradle:**
```gradle
dependencies {
    // Replace jwt-auth-starter with spring-api-starter
    implementation 'com.krd:spring-api-starter:1.0.0'

    // Other dependencies
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'mysql:mysql-connector-java'
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    // ...
}
```

**User.java (simplified):**
```java
@Entity
@Table(name = "users")
public class User extends BaseUser {
    // BaseUser provides: id, firstName, lastName, username, email, password, roles, enabled, deletedAt

    // Add only domain-specific fields
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Address> addresses = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "wishlist")
    private Set<Product> favoriteProducts = new HashSet<>();

    // Domain-specific methods
    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }
}
```

**UserRepository.java (simplified):**
```java
public interface UserRepository extends BaseUserRepository<User> {
    // Inherits all base queries with soft delete support
    // Add custom queries if needed
}
```

**UserService.java (simplified):**
```java
@Service
public class UserService extends BaseUserService<User, UserRepository, UserDto> {
    private final UserMapper mapper;

    public UserService(
        UserRepository repository,
        PasswordEncoder passwordEncoder,
        RoleChangeLogRepository roleChangeLogRepository,
        UserMapper mapper
    ) {
        super(repository, passwordEncoder, roleChangeLogRepository);
        this.mapper = mapper;
    }

    // Implement template methods
    @Override
    protected User createNewUser() {
        return new User();
    }

    @Override
    protected UserDto toDto(User user) {
        return mapper.toDto(user);
    }

    @Override
    protected void updateEntityFromRequest(UpdateUserRequest request, User user) {
        mapper.update(request, user);
    }

    // Add custom business logic if needed
}
```

**UserDetailsServiceImpl.java (simplified):**
```java
@Service
public class UserDetailsServiceImpl extends BaseUserDetailsService<User, UserRepository> {
    public UserDetailsServiceImpl(UserRepository repository) {
        super(repository);
    }
}
```

**application.yaml (no changes):**
```yaml
spring:
  jwt:
    secret: ${JWT_SECRET}
    accessTokenExpiration: 900
    refreshTokenExpiration: 604800

app:
  security:
    password:
      min-length: 8
      require-uppercase: true
```

**Step 3: Run Tests**

```bash
cd ~/MyDev/IntelliJ_Projects/spring-api-with-krd-starters
./gradlew clean build
./gradlew bootRun
```

**Step 4: Manual Testing**

```bash
# Register user
POST http://localhost:8080/users
{
  "email": "test@example.com",
  "password": "SecurePass123!",
  "firstName": "Test",
  "lastName": "User"
}

# Login
POST http://localhost:8080/auth/login
{
  "email": "test@example.com",
  "password": "SecurePass123!"
}

# Get all users (with JWT token)
GET http://localhost:8080/users
Authorization: Bearer <access_token>
```

**Success Criteria:**
- ✅ Application starts without errors
- ✅ Flyway migrations run automatically
- ✅ POST /auth/login returns JWT tokens
- ✅ POST /users registers users with validation
- ✅ Password policy enforced
- ✅ Soft delete works
- ✅ Role management works

---

### **Phase 8: Refactor Consumer Project** ⏱️ 1 hour

**Goal:** Clean up spring-api-with-krd-starters by removing extracted code.

**Files to Delete:**
- ❌ Most of `auth/` package (except SecurityConfig if customized)
- ❌ `common/validation/` package (now in starter)
- ❌ `common/config/PasswordPolicy.java` (now in starter)
- ❌ Old User entity fields (now in BaseUser)
- ❌ Old UserRepository queries (now in BaseUserRepository)
- ❌ Old UserService methods (now in BaseUserService)

**Files to Keep/Update:**
- ✅ `User.java` (extends BaseUser, keep custom fields)
- ✅ `UserRepository.java` (extends BaseUserRepository)
- ✅ `UserService.java` (extends BaseUserService)
- ✅ `UserController.java` (may need import updates)
- ✅ `SecurityConfig.java` (may reference starter classes)
- ✅ Domain-specific code (Address, Product, Cart, Order, Payment, etc.)

**Create Commit:**
```bash
git add .
git commit -m "Refactor to use spring-api-starter for authentication and user management

- Replace jwt-auth-starter with spring-api-starter
- Extend BaseUser instead of defining all fields
- Extend BaseUserRepository for soft delete queries
- Extend BaseUserService for CRUD operations
- Remove duplicated auth and validation code
- Simplify User entity to domain-specific fields only
- All authentication and user management now provided by starter"
```

---

### **Phase 9: Create Template Project** ⏱️ 2-3 hours

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

**Project Structure:**

```
spring-api-template/
├── .github/
│   └── workflows/
│       └── build.yml (CI/CD)
├── src/
│   ├── main/
│   │   ├── java/com/example/api/
│   │   │   ├── SpringApiTemplateApplication.java
│   │   │   │
│   │   │   ├── users/
│   │   │   │   ├── User.java (extends BaseUser)
│   │   │   │   ├── UserRepository.java (extends BaseUserRepository)
│   │   │   │   ├── UserService.java (extends BaseUserService)
│   │   │   │   ├── UserController.java
│   │   │   │   ├── UserMapper.java
│   │   │   │   └── UserDto.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java (configured for JWT)
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │
│   │   │   └── config/
│   │   │       └── SwaggerConfig.java
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       ├── application-prod.yaml
│   │       └── db/migration/
│   │           └── V10__add_custom_tables.sql (example)
│   │
│   └── test/
│       └── java/com/example/api/
│           ├── SpringApiTemplateApplicationTests.java
│           └── users/
│               └── UserServiceTests.java
│
├── docs/
│   ├── CLAUDE.md (Claude Code guide)
│   ├── GETTING_STARTED.md
│   └── DEPLOYMENT.md
│
├── .env.example
├── .gitignore
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

**Key Files:**

**build.gradle:**
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.5'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()  // For local krd-spring-starters
    mavenCentral()
}

dependencies {
    // KRD Starters (main dependency)
    implementation 'com.krd:spring-api-starter:1.0.0'
    implementation 'com.krd:security-rules-starter:1.0.0'

    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'

    // MapStruct
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**User.java (example implementation):**
```java
package com.example.api.users;

import com.krd.starter.user.entity.BaseUser;
import jakarta.persistence.*;
import lombok.*;

/**
 * User entity extending BaseUser from spring-api-starter.
 *
 * BaseUser provides:
 * - id, firstName, lastName, username, email, password
 * - roles (Set<String>), enabled, deletedAt
 * - JwtUser interface implementation
 * - Soft delete support
 *
 * Add domain-specific fields here.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUser {

    // Add custom fields for your domain
    // Example: phone number, profile picture, etc.

    @Column(name = "phone_number")
    private String phoneNumber;

    // Add relationships if needed
    // Example: @OneToMany, @ManyToMany, etc.
}
```

**UserService.java (example implementation):**
```java
package com.example.api.users;

import com.krd.starter.user.entity.RoleChangeLog;
import com.krd.starter.user.repository.RoleChangeLogRepository;
import com.krd.starter.user.service.BaseUserService;
import com.krd.starter.user.dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User service extending BaseUserService from spring-api-starter.
 *
 * Inherits all standard operations:
 * - getAllUsers, getUser, registerUser, updateUser, deleteUser
 * - changePassword, addRole, removeRole
 *
 * Add custom business logic here.
 */
@Service
public class UserService extends BaseUserService<User, UserRepository, UserDto> {

    private final UserMapper userMapper;

    public UserService(
        UserRepository repository,
        PasswordEncoder passwordEncoder,
        RoleChangeLogRepository roleChangeLogRepository,
        UserMapper userMapper
    ) {
        super(repository, passwordEncoder, roleChangeLogRepository);
        this.userMapper = userMapper;
    }

    // Template method implementations

    @Override
    protected User createNewUser() {
        return new User();
    }

    @Override
    protected UserDto toDto(User user) {
        return userMapper.toDto(user);
    }

    @Override
    protected void updateEntityFromRequest(UpdateUserRequest request, User user) {
        userMapper.update(request, user);
    }

    // Add custom business logic methods here
}
```

**README.md:**
```markdown
# Spring API Template

A ready-to-use Spring Boot API template with authentication and user management built-in.

## 🚀 Quick Start

### Prerequisites
- Java 21
- MySQL database
- Maven/Gradle

### 1. Use This Template
Click "Use this template" button on GitHub to create your own repository.

### 2. Clone and Configure
\`\`\`bash
git clone https://github.com/yourusername/your-api-name.git
cd your-api-name

# Copy environment variables
cp .env.example .env

# Edit .env with your values
\`\`\`

### 3. Update Package Names
Replace `com.example.api` with your package name:
\`\`\`bash
# macOS/Linux
find src -type f -name "*.java" -exec sed -i '' 's/com.example.api/com.yourcompany.yourapp/g' {} +

# Update directory structure
mv src/main/java/com/example/api src/main/java/com/yourcompany/yourapp
\`\`\`

### 4. Run the Application
\`\`\`bash
./gradlew bootRun
\`\`\`

### 5. Test the API
Open Swagger UI: http://localhost:8080/swagger-ui.html

## 📦 What's Included

### Out of the Box
- ✅ JWT authentication (dual token: access + refresh)
- ✅ User registration and management
- ✅ Password validation (configurable)
- ✅ Role-based access control
- ✅ Soft delete support
- ✅ Audit logging
- ✅ Swagger documentation
- ✅ Database migrations (Flyway)

### Endpoints Provided
- `POST /auth/login` - Authenticate user
- `POST /auth/refresh` - Refresh access token
- `POST /users` - Register new user
- `GET /users` - List all users (admin only)
- `GET /users/{id}` - Get user by ID
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Soft delete user (admin only)
- `POST /users/{id}/change-password` - Change password
- `POST /users/{id}/roles` - Add role (admin only)
- `DELETE /users/{id}/roles` - Remove role (admin only)

## 🔧 Configuration

Edit `application.yaml`:

\`\`\`yaml
spring:
  jwt:
    secret: ${JWT_SECRET}
    accessTokenExpiration: 900    # 15 minutes
    refreshTokenExpiration: 604800 # 7 days

app:
  security:
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special-char: true
\`\`\`

## 📚 Documentation

- [Getting Started Guide](docs/GETTING_STARTED.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Claude Code Guide](docs/CLAUDE.md)

## 🛠️ Adding Features

### Add a New Entity
1. Create entity class in appropriate package
2. Create repository interface
3. Create service class
4. Create controller
5. Add Flyway migration

### Customize User Entity
Edit `User.java` to add custom fields:
\`\`\`java
@Entity
public class User extends BaseUser {
    private String phoneNumber;
    private LocalDate birthDate;
}
\`\`\`

### Add Custom Security Rules
Implement `SecurityRules` interface:
\`\`\`java
@Component
public class ProductSecurityRules implements SecurityRules {
    @Override
    public void configure(AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers("/products/**").permitAll();
    }
}
\`\`\`

## 🧪 Testing
\`\`\`bash
./gradlew test
\`\`\`

## 📦 Building
\`\`\`bash
./gradlew clean build
\`\`\`

## 🚀 Deployment

See [DEPLOYMENT.md](docs/DEPLOYMENT.md) for platform-specific guides:
- Railway
- AWS
- Docker

## 📄 License
MIT
\`\`\`

**.env.example:**
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/your_database
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production

# Application
SPRING_PROFILES_ACTIVE=dev
```

**docs/GETTING_STARTED.md:**
- Step-by-step setup guide
- First API call examples
- Common customization scenarios

**docs/DEPLOYMENT.md:**
- Railway deployment guide
- Docker setup
- Environment configuration

**docs/CLAUDE.md:**
- How to use Claude Code with this template
- Common prompts and workflows
- Architecture overview

**GitHub Template Setup:**
1. Create repository: `spring-api-template`
2. Push code
3. Go to Settings → Template repository → ✅ Enable
4. Add description: "Spring Boot API template with JWT auth and user management"
5. Add topics: `spring-boot`, `template`, `jwt`, `rest-api`, `starter-template`

**Usage:**
1. User clicks "Use this template" on GitHub
2. Names their new repository
3. Clones and runs `./gradlew bootRun`
4. Has working API in minutes

**Benefits:**
- ⚡ Zero setup time
- 📦 Complete project structure
- 📝 Documentation included
- 🔧 Examples of how to extend
- 🎯 Just add business logic

---

### **Phase 10: Extract Payment Gateway** ⏱️ 2-3 hours

**Goal:** Create a separate payment-gateway-starter for optional payment functionality.

**Why Separate?**
Unlike authentication and user management (needed in every API), payment processing is **optional**:
- ✅ E-commerce APIs need it
- ❌ Internal tools don't
- ❌ Read-only APIs don't
- ❌ Non-commercial APIs don't

**Benefits of Separate Starter:**
- 🎯 Keep spring-api-starter focused on core functionality
- 📦 Optional dependency (only add when needed)
- 🔌 Provider-agnostic interface (Stripe, Square, PayPal)
- 🔄 Version independently
- 💰 No unnecessary Stripe SDK dependency in non-payment APIs

---

#### **Project Structure**

```
krd-spring-starters/
├── spring-api-starter/         (core auth + user management)
├── security-rules-starter/      (security configuration)
└── payment-gateway-starter/     (NEW - optional payments)
    ├── src/
    │   ├── main/
    │   │   ├── java/com/krd/payment/
    │   │   │   ├── gateway/
    │   │   │   │   ├── PaymentGateway.java (interface)
    │   │   │   │   ├── PaymentGatewayFactory.java
    │   │   │   │   └── PaymentConfig.java
    │   │   │   ├── stripe/
    │   │   │   │   ├── StripePaymentGateway.java
    │   │   │   │   ├── StripeConfig.java
    │   │   │   │   └── StripeAutoConfiguration.java
    │   │   │   ├── dto/
    │   │   │   │   ├── CheckoutSession.java
    │   │   │   │   ├── CheckoutRequest.java
    │   │   │   │   ├── CheckoutResponse.java
    │   │   │   │   ├── WebhookRequest.java
    │   │   │   │   ├── PaymentResult.java
    │   │   │   │   └── PaymentStatus.java
    │   │   │   ├── exception/
    │   │   │   │   └── PaymentException.java
    │   │   │   └── PaymentGatewayAutoConfiguration.java
    │   │   └── resources/
    │   │       └── META-INF/spring/
    │   │           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │   └── test/
    └── build.gradle
```

---

#### **Files to Extract**

**Source:** `spring-api-with-krd-starters/src/main/java/com/krd/store/payments/`

| File | Destination | Changes |
|------|-------------|---------|
| `PaymentGateway.java` | `gateway/PaymentGateway.java` | Make generic (not tied to Order entity) |
| `StripePaymentGateway.java` | `stripe/StripePaymentGateway.java` | Update package, make configurable |
| `StripeConfig.java` | `stripe/StripeConfig.java` | Update package |
| `CheckoutSession.java` | `dto/CheckoutSession.java` | Update package |
| `CheckoutRequest.java` | `dto/CheckoutRequest.java` | Update package |
| `CheckoutResponse.java` | `dto/CheckoutResponse.java` | Update package |
| `WebhookRequest.java` | `dto/WebhookRequest.java` | Update package |
| `PaymentResult.java` | `dto/PaymentResult.java` | Update package |
| `PaymentStauts.java` | `dto/PaymentStatus.java` | Fix typo, update package |
| `PaymentException.java` | `exception/PaymentException.java` | Update package |

---

#### **Key Design: Provider-Agnostic Interface**

**PaymentGateway.java (generic interface):**
```java
package com.krd.payment.gateway;

import java.util.Optional;

/**
 * Generic payment gateway interface.
 * Implement this for different payment providers (Stripe, Square, PayPal, etc.).
 */
public interface PaymentGateway {

    /**
     * Create a checkout session for the given payment request.
     * @param request Payment details (items, amounts, metadata)
     * @return Checkout session with redirect URL
     */
    CheckoutSession createCheckoutSession(CheckoutRequest request);

    /**
     * Parse and verify a webhook request from the payment provider.
     * @param request Webhook payload and headers
     * @return Payment result if event is relevant, empty otherwise
     */
    Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);
}
```

**CheckoutRequest.java (provider-agnostic):**
```java
package com.krd.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CheckoutRequest {
    private List<LineItem> items;
    private String currency;
    private String successUrl;
    private String cancelUrl;
    private Map<String, String> metadata; // For storing order ID, user ID, etc.

    @Data
    @Builder
    public static class LineItem {
        private String name;
        private String description;
        private BigDecimal unitPrice;
        private Long quantity;
    }
}
```

This design allows the gateway to work with ANY domain model (Order, Booking, Subscription, etc.), not just e-commerce Orders.

---

#### **build.gradle**

```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

group = 'com.krd'
version = '1.0.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Spring Boot
    api 'org.springframework.boot:spring-boot-starter-web'

    // Stripe SDK (for Stripe implementation)
    api 'com.stripe:stripe-java:26.13.0'

    // Utilities
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            versionMapping {
                usage('java-api') {
                    fromResolutionOf('runtimeClasspath')
                }
                usage('java-runtime') {
                    fromResolutionResult()
                }
            }
            pom {
                name = 'Payment Gateway Starter'
                description = 'Provider-agnostic payment gateway with Stripe implementation'
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
```

---

#### **Auto-Configuration**

**PaymentGatewayAutoConfiguration.java:**
```java
package com.krd.payment;

import com.krd.payment.gateway.PaymentGateway;
import com.krd.payment.stripe.StripeConfig;
import com.krd.payment.stripe.StripePaymentGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeConfig.class)
public class PaymentGatewayAutoConfiguration {

    /**
     * Auto-configure Stripe payment gateway if stripe.secretKey is present.
     */
    @Bean
    @ConditionalOnProperty(name = "stripe.secretKey")
    @ConditionalOnMissingBean
    public PaymentGateway stripePaymentGateway(StripeConfig stripeConfig) {
        return new StripePaymentGateway(stripeConfig);
    }

    // Future: Add other providers
    // @Bean
    // @ConditionalOnProperty(name = "square.accessToken")
    // public PaymentGateway squarePaymentGateway(SquareConfig config) { ... }
}
```

---

#### **Usage in Consumer Projects**

**1. Add Dependency (only in APIs that need payments):**
```gradle
dependencies {
    implementation 'com.krd:spring-api-starter:1.0.0'          // Always
    implementation 'com.krd:payment-gateway-starter:1.0.0'    // Optional
}
```

**2. Configure:**
```yaml
# application.yaml
stripe:
  secretKey: ${STRIPE_SECRET_KEY}
  webhookSecretKey: ${STRIPE_WEBHOOK_SECRET_KEY}

website:
  url: ${WEBSITE_URL:http://localhost:3000}
```

**3. Use in Your Code:**
```java
@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final PaymentGateway paymentGateway;  // Auto-injected

    public CheckoutResponse createCheckout(Order order) {
        // Build provider-agnostic request
        var request = CheckoutRequest.builder()
            .items(order.getItems().stream()
                .map(item -> CheckoutRequest.LineItem.builder()
                    .name(item.getProduct().getName())
                    .unitPrice(item.getUnitPrice())
                    .quantity((long) item.getQuantity())
                    .build())
                .toList())
            .currency("usd")
            .successUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
            .cancelUrl(websiteUrl + "/checkout-cancel")
            .metadata(Map.of("order_id", order.getId().toString()))
            .build();

        // Create checkout session (Stripe or any provider)
        CheckoutSession session = paymentGateway.createCheckoutSession(request);

        return new CheckoutResponse(session.getUrl());
    }
}
```

**4. Handle Webhooks:**
```java
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final PaymentGateway paymentGateway;
    private final OrderService orderService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader Map<String, String> headers
    ) {
        var webhookRequest = new WebhookRequest(payload, headers);

        paymentGateway.parseWebhookRequest(webhookRequest)
            .ifPresent(result -> {
                Long orderId = Long.parseLong(result.getOrderId());
                orderService.updatePaymentStatus(orderId, result.getStatus());
            });

        return ResponseEntity.ok().build();
    }
}
```

---

#### **Future: Add More Providers**

The interface-based design makes it easy to add other providers:

**Square Payment Gateway:**
```java
@Service
@ConditionalOnProperty(name = "square.accessToken")
public class SquarePaymentGateway implements PaymentGateway {
    @Override
    public CheckoutSession createCheckoutSession(CheckoutRequest request) {
        // Square-specific implementation
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
        // Square webhook handling
    }
}
```

**PayPal Payment Gateway:**
```java
@Service
@ConditionalOnProperty(name = "paypal.clientId")
public class PayPalPaymentGateway implements PaymentGateway {
    // PayPal implementation
}
```

**Configuration-Based Provider Selection:**
```yaml
payment:
  provider: stripe  # or 'square', 'paypal'
```

---

#### **Implementation Steps**

**Step 1: Create Project (30 min)**
```bash
cd ~/MyDev/Spring_Starters/krd-spring-starters
mkdir -p payment-gateway-starter/src/main/java/com/krd/payment
mkdir -p payment-gateway-starter/src/main/resources/META-INF/spring
```

**Step 2: Extract Files (1 hour)**
- Copy payment files from spring-api-with-krd-starters
- Update packages: `com.krd.store.payments` → `com.krd.payment.*`
- Make PaymentGateway interface generic (not tied to Order)
- Update StripePaymentGateway to use CheckoutRequest

**Step 3: Create Auto-Configuration (30 min)**
- Create PaymentGatewayAutoConfiguration
- Add META-INF imports file
- Configure conditional bean creation

**Step 4: Test (1 hour)**
```bash
# Build and publish
./gradlew payment-gateway-starter:publishToMavenLocal

# Test in spring-api-with-krd-starters
# Add dependency, test checkout flow
```

---

#### **Benefits**

1. **Optional Dependency** - Only add to projects that need payments
2. **Provider Agnostic** - Easy to switch from Stripe to Square
3. **Domain Agnostic** - Works with any payment scenario (orders, bookings, subscriptions)
4. **Clean Separation** - Payment logic separate from core API functionality
5. **Future Proof** - Easy to add PayPal, Square, etc.
6. **Independent Versioning** - Update payment features without affecting core starter

---

#### **When to Implement**

**Option A: Parallel Track**
- Work on this alongside spring-api-starter (if comfortable with multiple projects)

**Option B: Sequential** (Recommended)
- Complete spring-api-starter first (Phases 1-9)
- Test it thoroughly in a consumer project
- Then extract payment gateway

**Recommendation:** Do it **after** spring-api-starter is working, so you can focus on one thing at a time.

---

## 📊 Complete Roadmap Summary

| Phase | Description | Time | Status |
|-------|-------------|------|--------|
| **spring-api-starter** | | | |
| 1 | Project setup | 30 min | ⏸️ Pending |
| 2 | Extract JWT authentication | 1-2 hours | ⏸️ Pending |
| 3 | Extract user management | 2-3 hours | ⏸️ Pending |
| 4 | Extract password validation | 30 min | ⏸️ Pending |
| 5 | Create database migrations | 30 min | ⏸️ Pending |
| 6 | Create auto-configuration | 1 hour | ⏸️ Pending |
| 7 | Testing & integration | 1 hour | ⏸️ Pending |
| 8 | Refactor consumer project | 1 hour | ⏸️ Pending |
| 9 | Create template project | 2-3 hours | ⏸️ Pending |
| **Subtotal** | **Core API Starter** | **10-14 hours** | |
| | | | |
| **payment-gateway-starter (Optional)** | | | |
| 10 | Extract payment gateway | 2-3 hours | ⏸️ Pending |
| | | | |
| **Grand Total** | **Complete System** | **12-17 hours** | |

---

## ✅ Success Criteria

### For spring-api-starter:
- [ ] Builds successfully: `./gradlew spring-api-starter:build`
- [ ] Publishes to Maven local: `./gradlew spring-api-starter:publishToMavenLocal`
- [ ] Consumer can add single dependency
- [ ] Extend BaseUser and it works immediately
- [ ] POST /auth/login returns JWT tokens
- [ ] POST /users registers users with validation
- [ ] Password policy configurable via YAML
- [ ] Soft delete works automatically
- [ ] Role management works
- [ ] All tests pass

### For spring-api-template:
- [ ] "Use this template" creates working repository
- [ ] Clone → Run works in < 5 minutes
- [ ] All endpoints documented in Swagger
- [ ] README has clear instructions
- [ ] Example implementations included
- [ ] Can deploy to Railway/Docker
- [ ] Documentation complete

### For payment-gateway-starter (Optional):
- [ ] Builds successfully: `./gradlew payment-gateway-starter:build`
- [ ] Publishes to Maven local: `./gradlew payment-gateway-starter:publishToMavenLocal`
- [ ] PaymentGateway interface is provider-agnostic
- [ ] StripePaymentGateway works with generic CheckoutRequest
- [ ] Auto-configures when stripe.secretKey is present
- [ ] Webhook parsing and verification works
- [ ] Can create checkout sessions
- [ ] Can handle payment success/failure events
- [ ] Easy to add new providers (Square, PayPal)

---

## 🎯 What You'll Have When Complete

### 1. spring-api-starter (Library)
- Gradle dependency: `com.krd:spring-api-starter:1.0.0`
- Provides authentication, user management, validation
- Required for all API projects
- Reusable across all projects

### 2. payment-gateway-starter (Optional Library)
- Gradle dependency: `com.krd:payment-gateway-starter:1.0.0`
- Provider-agnostic payment interface
- Stripe implementation included
- Easy to add Square, PayPal, etc.
- Only add to projects that need payments

### 3. spring-api-template (Template)
- GitHub template repository
- Click "Use this template" → working API
- Examples of how to use the starters
- Complete documentation

### 4. Simplified Consumer Projects
```gradle
dependencies {
    // Core starter (always)
    implementation 'com.krd:spring-api-starter:1.0.0'

    // Payment gateway (optional - only if needed)
    implementation 'com.krd:payment-gateway-starter:1.0.0'
}
```

```java
@Entity
public class User extends BaseUser {
    // Just add custom fields
}
```

**That's it.** Authentication, user management, validation all work.

---

## 🚀 Getting Started

Ready to begin? Start with **Phase 1: Project Setup**.

The recommended execution order:
1. **Phase 1** - Create spring-api-starter skeleton
2. **Phase 2** - Extract JWT code
3. **Phase 3** - Extract user management
4. **Phase 4-6** - Add validation, migrations, auto-config
5. **Phase 7-8** - Test and refactor
6. **Phase 9** - Create template (after starter is tested)

Each phase builds on the previous, so follow the order.

---

**Document Created:** 2025-11-26
**Status:** Ready to Execute
**Next Action:** Begin Phase 1 - Project Setup
