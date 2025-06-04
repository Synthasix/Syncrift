package com.hexplatoon.syncrift_backend.security;

import com.hexplatoon.syncrift_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService implements the Spring Security UserDetailsService interface
 * to load user-specific data from the database during authentication.
 * <p>
 * This service connects Spring Security with our custom user repository and maps
 * our User entity to Spring Security's UserDetails interface.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username during authentication.
     * <p>
     * This method locates the user based on the username and returns a UserDetails object
     * that Spring Security can use for authentication and validation.
     * </p>
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated user record (never null)
     * @throws UsernameNotFoundException if the user could not be found or the user has no authorities
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user in the database by username

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }
}

