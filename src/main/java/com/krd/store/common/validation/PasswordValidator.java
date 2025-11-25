package com.krd.store.common.validation;

import com.krd.store.common.config.PasswordPolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;

/**
 * Validator for {@link ValidPassword} annotation.
 *
 * <p>Validates passwords against the configured policy defined in {@link PasswordPolicy}.
 * Uses Spring dependency injection to access the password policy configuration.
 *
 * <p>Provides specific, user-friendly error messages for each validation failure.
 */
@AllArgsConstructor
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private final PasswordPolicy passwordPolicy;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        // Disable default message to provide specific feedback
        context.disableDefaultConstraintViolation();

        // Check minimum length
        if (password.length() < passwordPolicy.getMinLength()) {
            context.buildConstraintViolationWithTemplate(
                "Password must be at least " + passwordPolicy.getMinLength() + " characters long"
            ).addConstraintViolation();
            return false;
        }

        // Check maximum length (prevent DoS attacks from bcrypt)
        if (password.length() > passwordPolicy.getMaxLength()) {
            context.buildConstraintViolationWithTemplate(
                "Password must not exceed " + passwordPolicy.getMaxLength() + " characters"
            ).addConstraintViolation();
            return false;
        }

        // Check for at least one lowercase letter (if required)
        if (passwordPolicy.isRequireLowercase() && !password.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for at least one uppercase letter (if required)
        if (passwordPolicy.isRequireUppercase() && !password.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for at least one digit (if required)
        if (passwordPolicy.isRequireDigit() && !password.matches(".*\\d.*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one number"
            ).addConstraintViolation();
            return false;
        }

        // Check for at least one special character (if required)
        if (passwordPolicy.isRequireSpecialChar() && !password.matches(".*[@$!%*?&#^()\\-_=+\\[\\]{}|;:,.<>].*")) {
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one special character (@$!%*?&#^()-_=+[]{}|;:,.<>)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
