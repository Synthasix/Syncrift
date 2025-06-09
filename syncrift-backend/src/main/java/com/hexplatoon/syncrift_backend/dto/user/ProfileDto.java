package com.hexplatoon.syncrift_backend.dto.user;

import com.hexplatoon.syncrift_backend.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for representing user profile information.
 * This DTO contains only the information that can be safely exposed via APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

    @NotNull(message = "User ID must not be null")
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Size(max = 30, message = "First name must not exceed 30 characters")
    private String firstName;

    @Size(max = 30, message = "Last name must not exceed 30 characters")
    private String lastName;

    @Size(max = 200, message = "Bio must not exceed 200 characters")
    private String bio;

    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Profile picture must be a valid URL"
    )
    private String profilePicture;

    @Min(value = 0, message = "Level must be non-negative")
    private Integer level;

    @Min(value = 0, message = "Experience must be non-negative")
    private Integer experience;

    @Min(value = 0, message = "Typing rating must be non-negative")
    private Integer typingRating;

    @Min(value = 0, message = "CSS design rating must be non-negative")
    private Integer cssDesignRating;

    @Min(value = 0, message = "Codeforces rating must be non-negative")
    private Integer codeforcesRating;

    @NotNull(message = "Status must not be null")
    private User.UserStatus status;
}
