-- Create role change audit log table
CREATE TABLE role_change_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    changed_at DATETIME NOT NULL,
    user_email VARCHAR(255),
    changed_by_email VARCHAR(255),

    CONSTRAINT fk_role_change_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_change_by_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_user_id (user_id),
    INDEX idx_changed_by_user_id (changed_by_user_id),
    INDEX idx_changed_at (changed_at)
);
