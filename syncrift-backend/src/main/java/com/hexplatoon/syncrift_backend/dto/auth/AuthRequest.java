package com.hexplatoon.syncrift_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication requests.
 * Contains validation rules to ensure data integrity before processing login attempts.
 * Uses the Builder pattern for easy instantiation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    
    /**
     * The unique identifier for the user, either username or email
     * Must not be blank and have a length between 3 and 50 characters
     */
    @NotBlank(message = "Login identifier cannot be empty")
    @Size(min = 3, max = 50, message = "Login identifier must be between 3 and 50 characters")
    private String loginIdentifier;
    
    /**
     * The user's password
     * Must not be blank, have a length between 8 and 100 characters,
     * and match the pattern for secure passwords (at least one uppercase letter,
     * one lowercase letter, one digit, and one special character)
     */
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
}

