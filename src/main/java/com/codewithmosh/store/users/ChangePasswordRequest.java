package com.codewithmosh.store.users;

import com.codewithmosh.store.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
