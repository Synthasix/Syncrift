package com.hexplatoon.syncrift_backend.config;

import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        System.out.println("Initializing Data");
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of("ROLE_ADMIN"))
                    .email("admin@email.com")
                    .build();
            userRepository.save(admin);
        }
    }
}
