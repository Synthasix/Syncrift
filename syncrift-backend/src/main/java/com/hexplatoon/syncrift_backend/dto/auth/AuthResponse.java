package com.hexplatoon.syncrift_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for authentication responses.
 * Contains JWT token and user details returned after successful authentication.
 * Uses the Builder pattern for easy instantiation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * The JWT authentication token
     */
    private String token;
    
    /**
     * The user's unique identifier
     */
    private Long userId;
    
    /**
     * The user's username
     */
    private String username;
    
    /**
     * The user's email address
     */
    private String email;
    
    /**
     * List of roles assigned to the user
     */
    private List<String> roles;

    // Not required for now
//    /**
//     * Account status (ACTIVE, SUSPENDED, etc.)
//     */
//    private String accountStatus;
    
    /**
     * Timestamp when the account was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the token will expire
     */
    private LocalDateTime tokenExpiresAt;

    // Not required for now
//    /**
//     * Indicates whether this is the user's first login
//     */
//    private boolean firstLogin;
}

