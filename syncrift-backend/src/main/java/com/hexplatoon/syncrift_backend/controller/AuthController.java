package com.hexplatoon.syncrift_backend.controller;

import com.hexplatoon.syncrift_backend.dto.auth.AuthRequest;
import com.hexplatoon.syncrift_backend.dto.auth.AuthResponse;
import com.hexplatoon.syncrift_backend.dto.auth.RegisterRequest;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.service.AuthService;
import com.hexplatoon.syncrift_backend.service.user.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller handling authentication related API endpoints including registration, login,
 * token validation, and user information retrieval.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    /**
     * Endpoint to register a new user.
     *
     * @param registerRequest The registration details of the user
     * @return ResponseEntity with the authentication token and user details
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
            AuthResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint to authenticate a user and generate a JWT token.
     *
     * @param authRequest The authentication request containing credentials
     * @return ResponseEntity with the authentication token and user details
     */
    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
            AuthResponse response = authService.login(authRequest);
            return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to validate a JWT token.
     *
     * @param token The JWT token to validate
     * @return ResponseEntity with the validation result
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        Map<String, Boolean> response = new HashMap<>();
        boolean isValid = authService.validateToken(token);
        response.put("valid", isValid);
        
        if (isValid) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Endpoint to get the currently authenticated user's information.
     * PreAuthorize is used for additional security to allow only authenticated
     * users to access this endpoint.
     *
     * @return ResponseEntity with the user information
     */

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser() {
        User user = currentUserService.getCurrentUserDetails();
        // Don't expose the password hash in the response
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * Endpoint to check if a username is available for registration.
     *
     * @param username The username to check
     * @return ResponseEntity with the availability result
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        boolean isAvailable = !authService.existsByUsername(username);
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to check if an email is available for registration.
     *
     * @param email The email to check
     * @return ResponseEntity with the availability result
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        boolean isAvailable = !authService.existsByEmail(email);
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }
}

