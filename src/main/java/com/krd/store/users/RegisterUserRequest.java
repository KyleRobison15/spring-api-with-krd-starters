package com.krd.store.users;

import com.krd.store.common.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @Size(max = 255, message = "First name must be less than 255 characters")
    private String firstName;

    @Size(max = 255, message = "Last name must be less than 255 characters")
    private String lastName;

    @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Lowercase(message = "Email must be lowercase")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;
}
