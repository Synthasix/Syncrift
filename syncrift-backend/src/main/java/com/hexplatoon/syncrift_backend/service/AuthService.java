package com.hexplatoon.syncrift_backend.service;

import com.hexplatoon.syncrift_backend.dto.auth.AuthRequest;
import com.hexplatoon.syncrift_backend.dto.auth.AuthResponse;
import com.hexplatoon.syncrift_backend.dto.auth.RegisterRequest;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.exception.InvalidJwtAuthenticationException;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import com.hexplatoon.syncrift_backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public AuthResponse register(@Valid RegisterRequest request) {
        validateRegistrationData(request);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of("ROLE_USER"))
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.createToken(
                savedUser.getUsername(),
                savedUser.getAuthorities()
        );

        Date expirationDate = jwtTokenProvider.extractExpiration(token);
        LocalDateTime tokenExpiresAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        List<String> roles = savedUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(roles)
                .createdAt(savedUser.getCreatedAt())
                .tokenExpiresAt(tokenExpiresAt)
                .build();
    }

    private void validateRegistrationData(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(@Valid AuthRequest request) {
        try {
            User user = findUserByLoginIdentifier(request.getLoginIdentifier());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.createToken(
                    user.getUsername(),
                    user.getAuthorities()
            );

            Date expirationDate = jwtTokenProvider.extractExpiration(token);
            LocalDateTime tokenExpiresAt = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .createdAt(user.getCreatedAt())
                    .tokenExpiresAt(tokenExpiresAt)
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    private User findUserByLoginIdentifier(String loginIdentifier) {
        Optional<User> userByUsername = userRepository.findByUsername(loginIdentifier);
        if (userByUsername.isPresent()) {
            return userByUsername.get();
        }

        Optional<User> userByEmail = userRepository.findByEmail(loginIdentifier);
        if (userByEmail.isPresent()) {
            return userByEmail.get();
        }

        throw new IllegalArgumentException("User not found with provided username or email");
    }

    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (InvalidJwtAuthenticationException e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}