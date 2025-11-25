package com.krd.store.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for password validation policy.
 *
 * <p>These properties can be configured in application.yaml under the prefix
 * "app.security.password" to customize password requirements across the application.
 *
 * <p><b>Example configuration:</b>
 * <pre>
 * app:
 *   security:
 *     password:
 *       min-length: 8
 *       max-length: 128
 *       require-uppercase: true
 *       require-lowercase: true
 *       require-digit: true
 *       require-special-char: true
 * </pre>
 *
 * <p>Based on OWASP and NIST SP 800-63B password guidelines.
 */
@Component
@ConfigurationProperties(prefix = "app.security.password")
@Data
public class PasswordPolicy {

    /**
     * Minimum password length (NIST recommends at least 8)
     */
    private int minLength = 8;

    /**
     * Maximum password length (prevents DoS attacks, allows passphrases)
     */
    private int maxLength = 128;

    /**
     * Whether to require at least one uppercase letter
     */
    private boolean requireUppercase = true;

    /**
     * Whether to require at least one lowercase letter
     */
    private boolean requireLowercase = true;

    /**
     * Whether to require at least one digit
     */
    private boolean requireDigit = true;

    /**
     * Whether to require at least one special character
     */
    private boolean requireSpecialChar = true;
}
