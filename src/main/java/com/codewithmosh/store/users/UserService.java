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
            throw new DuplicateUserException();
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash the user's password before we store it in the database!
        user.setRoles(Set.of("USER")); // Assign default USER role
        userRepository.save(user);

        return userMapper.toDto(user);
    }


    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userMapper.update(request, user);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public void deleteUser(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Password does not match");
        }

        user.setPassword(request.getNewPassword());
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
