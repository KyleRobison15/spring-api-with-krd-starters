package com.krd.store.users;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role_change_logs")
public class RoleChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "action", nullable = false)
    private String action; // "ADDED" or "REMOVED"

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "changed_by_email")
    private String changedByEmail;
}
