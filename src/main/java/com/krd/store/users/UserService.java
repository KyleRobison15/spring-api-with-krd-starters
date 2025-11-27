package com.krd.store.users;

import com.krd.starter.user.BaseUserService;
import com.krd.starter.user.RoleChangeLogRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User service extending the base user service from spring-api-starter.
 * <p>
 * All CRUD and role management operations are inherited from BaseUserService.
 * Add custom business logic methods here if needed.
 */
@Service
public class UserService extends BaseUserService<User, UserDto> {

    public UserService(UserRepository repository,
                       UserMapper mapper,
                       PasswordEncoder passwordEncoder,
                       RoleChangeLogRepository roleChangeLogRepository) {
        super(repository, mapper, passwordEncoder, roleChangeLogRepository);
    }

    // All methods are inherited from BaseUserService:
    // - getAllUsers(String sort)
    // - getUser(Long id)
    // - registerUser(RegisterUserRequest request)
    // - updateUser(Long userId, UpdateUserRequest request)
    // - deleteUser(Long userId)
    // - changePassword(Long userId, ChangePasswordRequest request)
    // - addRole(Long userId, AddRoleRequest request)
    // - removeRole(Long userId, RemoveRoleRequest request)

    // Add custom methods here if needed for this specific application
}
