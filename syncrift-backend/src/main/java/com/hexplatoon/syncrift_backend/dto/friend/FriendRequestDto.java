package com.hexplatoon.syncrift_backend.dto.friend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending friend requests
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDto {
    
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
}

