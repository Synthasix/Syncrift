package com.hexplatoon.syncrift_backend.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when JWT token validation fails.
 */
public class InvalidJwtAuthenticationException extends AuthenticationException {

    /**
     * Constructs a new InvalidJwtAuthenticationException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidJwtAuthenticationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidJwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

