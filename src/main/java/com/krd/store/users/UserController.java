package com.krd.store.users;

import com.krd.starter.user.BaseUserController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User controller extending the base user controller from spring-api-starter.
 * <p>
 * All standard user management endpoints are inherited from BaseUserController:
 * - GET    /users              - List all users (ADMIN only)
 * - GET    /users/{id}         - Get user by ID
 * - POST   /users              - Register new user
 * - PUT    /users/{id}         - Update user
 * - DELETE /users/{id}         - Delete user (ADMIN only)
 * - POST   /users/{id}/change-password - Change password
 * - POST   /users/{id}/roles   - Add role (ADMIN only)
 * - DELETE /users/{id}/roles   - Remove role (ADMIN only)
 * - GET    /users/{id}/roles   - Get user roles (ADMIN only)
 * <p>
 * Add custom endpoints here if needed for this specific application.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController extends BaseUserController<User, UserDto> {

    public UserController(UserService service) {
        super(service);
    }

    // All endpoints are inherited from BaseUserController
    // Add custom endpoints here if needed for this specific application
}
