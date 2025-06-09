package com.hexplatoon.syncrift_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response for operations
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {
    
    private boolean success;
    private String message;
    
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .build();
    }
    
    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}

