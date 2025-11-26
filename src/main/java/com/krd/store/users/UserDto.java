package com.krd.store.users;

import com.krd.starter.user.dto.BaseUserDto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DTO for User entity.
 * <p>
 * Extends BaseUserDto which provides: id, firstName, lastName, username, email, roles, enabled
 * <p>
 * Add domain-specific fields here if needed.
 */
@Getter
@SuperBuilder
public class UserDto extends BaseUserDto {
    // Base fields (id, firstName, lastName, username, email, roles, enabled) are inherited
    // Add domain-specific fields here if needed (e.g., orderCount, addressCount)
}
