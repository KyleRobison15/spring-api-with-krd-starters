package com.codewithmosh.store.users;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Override default methods to exclude soft-deleted users
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAll();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAll(Sort sort);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role AND u.deletedAt IS NULL")
    long countByRolesContaining(@Param("role") String role);
}
