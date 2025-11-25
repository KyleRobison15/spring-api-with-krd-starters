package com.codewithmosh.store.users;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String username;

    @Email(message = "Email must be valid")
    private String email;
}
