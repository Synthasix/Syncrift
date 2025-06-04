package com.hexplatoon.syncrift_backend.dto;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for standardized error responses.
 * This class provides a structured format for returning error information to clients.
 */
@Getter
public class ErrorResponse {
    
    /**
     * The timestamp when the error occurred.
     * -- GETTER --
     *  Gets the timestamp when the error occurred.
     *

     */
    private final LocalDateTime timestamp;
    
    /**
     * The HTTP status code associated with the error.
     * -- GETTER --
     *  Gets the HTTP status code associated with the error.
     *

     */
    private final Integer status;
    
    /**
     * The error type or name.
     * -- GETTER --
     *  Gets the error type or name.
     *

     */
    private final String error;
    
    /**
     * A detailed message describing the error.
     * -- GETTER --
     *  Gets the detailed message describing the error.
     *

     */
    private final String message;
    
    /**
     * The request path that resulted in the error.
     * -- GETTER --
     *  Gets the request path that resulted in the error.
     *

     */
    private final String path;
    
    /**
     * Private constructor used by the Builder.
     * 
     * @param builder The Builder instance containing the values for this ErrorResponse
     */
    private ErrorResponse(Builder builder) {
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.path = builder.path;
    }
    
    /**
     * Creates a new Builder instance to build an ErrorResponse.
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating instances of ErrorResponse.
     * This implements the Builder pattern to allow for clear and flexible object creation.
     */
    public static class Builder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private Integer status;
        private String error;
        private String message;
        private String path;
        
        /**
         * Private constructor used by the static builder() method.
         */
        private Builder() {
        }
        
        /**
         * Sets the timestamp for the error response.
         * 
         * @param timestamp The timestamp when the error occurred
         * @return This Builder instance for method chaining
         */
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Sets the HTTP status code for the error response.
         * 
         * @param status The HTTP status code
         * @return This Builder instance for method chaining
         */
        public Builder status(Integer status) {
            this.status = status;
            return this;
        }
        
        /**
         * Sets the error type or name for the error response.
         * 
         * @param error The error type
         * @return This Builder instance for method chaining
         */
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        /**
         * Sets the detailed message for the error response.
         * 
         * @param message The detailed error message
         * @return This Builder instance for method chaining
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        /**
         * Sets the request path for the error response.
         * 
         * @param path The request path that resulted in the error
         * @return This Builder instance for method chaining
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        /**
         * Builds a new ErrorResponse with the values set in this Builder.
         * 
         * @return A new ErrorResponse instance
         */
        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}

