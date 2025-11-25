package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleChangeLogRepository roleChangeLogRepository;

    public Iterable<UserDto> getAllUsers(String sort) {
        if(!Set.of("firstName", "lastName", "username", "email").contains(sort)) {
            sort = "email";
        }

        return userRepository.findAll(Sort.by(sort))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.toDto(user);
    }

    public UserDto registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateUserException();
        }

        // Only check for duplicate username if one is provided
        if (request.getUsername() != null && !request.getUsername().isBlank()
            && userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateUserException("A user with this username already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash the user's password before we store it in the database!
        user.setRoles(Set.of("USER")); // Assign default USER role
        userRepository.save(user);

        return userMapper.toDto(user);
    }


    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        var currentUserId = getCurrentUserId();
        var currentUser = userRepository.findById(currentUserId).orElseThrow();
        var targetUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Authorization: Users can update themselves, admins can update anyone
        boolean isAdmin = currentUser.getRoles().contains("ADMIN");
        if (!userId.equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(targetUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateUserException("Email already in use");
            }
        }

        // Validate username uniqueness if changed
        if (request.getUsername() != null && !request.getUsername().equals(targetUser.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateUserException("Username already in use");
            }
        }

        userMapper.update(request, targetUser);
        userRepository.save(targetUser);

        return userMapper.toDto(targetUser);
    }

    public void deleteUser(Long userId) {
        var currentUserId = getCurrentUserId();
        var userToDelete = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Prevent self-deletion by admin
        if (userId.equals(currentUserId)) {
            throw new AccessDeniedException("Admins cannot delete their own account");
        }

        // Prevent deleting the last admin
        if (userToDelete.getRoles().contains("ADMIN")) {
            long adminCount = userRepository.countByRolesContaining("ADMIN");
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user. There must always be at least one admin.");
            }
        }

        // Soft delete: Mark as deleted and disable account
        userToDelete.setDeletedAt(LocalDateTime.now());
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        var currentUserId = getCurrentUserId();

        // Authorization: Users can only change their own password
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only change your own password");
        }

        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

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
        userRepository.save(user);
    }

    public UserDto addRole(Long userId, AddRoleRequest request) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        // Add the role to the user's existing roles
        boolean wasAdded = user.getRoles().add(request.getRole());

        if (wasAdded) {
            userRepository.save(user);
            logRoleChange(user, request.getRole(), "ADDED");
        }

        return userMapper.toDto(user);
    }

    public UserDto removeRole(Long userId, RemoveRoleRequest request) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
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
            userRepository.save(user);
            logRoleChange(user, request.getRole(), "REMOVED");
        }

        return userMapper.toDto(user);
    }

    private void logRoleChange(User user, String role, String action) {
        var currentUserId = getCurrentUserId();
        var currentUser = userRepository.findById(currentUserId).orElse(null);

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

    private Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }

}
