package com.hexplatoon.syncrift_backend.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration requests.
 * Contains all necessary fields and validation for creating a new user account.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    /**
     * User's first name.
     * Cannot be blank and must be between 2 and 50 characters.
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "First name can only contain letters, spaces, and hyphens")
    private String firstName;

    /**
     * User's last name.
     * Cannot be blank and must be between 2 and 50 characters.
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Last name can only contain letters, spaces, and hyphens")
    private String lastName;

    /**
     * User's chosen username.
     * Must be between 3 and 50 characters and cannot be blank.
     * Can only contain letters, numbers, dots, underscores, and hyphens.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;
    /**
     * User's email address.
     * Must be a valid email format and cannot be blank.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Email must be in a valid format")
    private String email;
    /**
     * User's password.
     * Must be at least 8 characters long and include at least one uppercase letter,
     * one lowercase letter, one digit, and one special character.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character"
    )
    private String password;

    /**
     * Confirmation of the user's password.
     * Must match the password field.
     */
    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirmation;

    /**
     * Validates that the password and password confirmation match.
     * This validation is automatically checked during bean validation.
     *
     * @return true if passwords match, false otherwise
     */
    @AssertTrue(message = "Password confirmation must match password")
    public boolean isPasswordConfirmationValid() {
        return password != null && password.equals(passwordConfirmation);
    }
}

