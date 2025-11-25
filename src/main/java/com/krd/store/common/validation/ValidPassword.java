package com.krd.store.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a password meets the configured security requirements.
 *
 * <p>Password requirements are defined in {@link com.krd.store.common.config.PasswordPolicy}
 * and can be customized via application.yaml configuration.
 *
 * <p><b>Usage:</b>
 * <pre>
 * &#64;ValidPassword
 * private String password;
 * </pre>
 *
 * <p>Validation checks include:
 * <ul>
 *   <li>Minimum and maximum length</li>
 *   <li>Uppercase letter requirement (if enabled)</li>
 *   <li>Lowercase letter requirement (if enabled)</li>
 *   <li>Digit requirement (if enabled)</li>
 *   <li>Special character requirement (if enabled)</li>
 * </ul>
 *
 * @see PasswordValidator
 * @see com.krd.store.common.config.PasswordPolicy
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password does not meet security requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
