package com.krd.store.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, Long> {
    List<RoleChangeLog> findByUserIdOrderByChangedAtDesc(Long userId);
    List<RoleChangeLog> findByChangedByUserIdOrderByChangedAtDesc(Long changedByUserId);
}
