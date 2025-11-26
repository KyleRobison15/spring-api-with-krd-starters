package com.krd.store.users;

import com.krd.starter.user.BaseUserRepository;

/**
 * Repository for User entities.
 * <p>
 * Extends BaseUserRepository which provides:
 * - Standard CRUD operations
 * - Soft delete support (filters out deletedAt IS NOT NULL)
 * - Common queries: findByEmail, existsByEmail, existsByUsername, countByRolesContaining
 * <p>
 * Add domain-specific queries here.
 */
public interface UserRepository extends BaseUserRepository<User> {
    // All common user queries are inherited from BaseUserRepository
    // Add application-specific queries here if needed
}
