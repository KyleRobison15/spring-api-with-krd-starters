package com.krd.store.users;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Endpoint is only accessible by users with "ADMIN" role
    public Iterable<UserDto> getAllUsers(@RequestParam(required = false, defaultValue = "", name = "sort") String sort) {
        return userService.getAllUsers(sort);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder) {

        UserDto userDto = userService.registerUser(request);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();

        return ResponseEntity.created(uri).body(userDto);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable(name = "id") Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a role to a user. Only accessible by users with ADMIN role.
     *
     * This endpoint allows administrators to promote users by adding roles
     * (e.g., adding ADMIN role to make a user an administrator).
     *
     * All role changes are logged in the audit log with timestamp and who made the change.
     *
     * @param id The user ID to add the role to
     * @param request The role to add (USER or ADMIN)
     * @return The updated user with the new role
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto addRole(@PathVariable Long id, @Valid @RequestBody AddRoleRequest request) {
        return userService.addRole(id, request);
    }

    /**
     * Remove a role from a user. Only accessible by users with ADMIN role.
     *
     * This endpoint allows administrators to demote users by removing roles.
     *
     * Security restrictions:
     * - Admins cannot remove their own ADMIN role (prevents accidental lockout)
     * - Users must have at least one role (prevents roleless users)
     *
     * All role changes are logged in the audit log.
     *
     * @param id The user ID to remove the role from
     * @param request The role to remove (USER or ADMIN)
     * @return The updated user without the removed role
     */
    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto removeRole(@PathVariable Long id, @Valid @RequestBody RemoveRoleRequest request) {
        return userService.removeRole(id, request);
    }

    /**
     * Get all roles for a specific user.
     * Only accessible by Admins
     *
     * @param id The user ID
     * @return Set of role strings (e.g., ["USER", "ADMIN"])
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long id) {
        var user = userService.getUser(id);
        return ResponseEntity.ok(user.getRoles());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<?> handleDuplicateUserException(DuplicateUserException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

}
