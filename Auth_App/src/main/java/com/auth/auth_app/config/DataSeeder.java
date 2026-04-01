package com.auth.auth_app.config;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.Role;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    // CommandLineRunner.run() executes once automatically after Spring Boot starts up.
    // This is the standard way to seed data without writing raw SQL inserts manually.
    @Override
    public void run(String... args) {

        // The roles we need in the system
        List<String> requiredRoles = List.of("ROLE_USER", "ROLE_SELLER", "ROLE_ADMIN");

        for (String roleName : requiredRoles) {
            // Only insert if it doesn't already exist — safe to restart the server
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .id(UUID.randomUUID())
                        .name(roleName)
                        .build();
                roleRepository.save(role);
                log.info("Seeded role: {}", roleName);
            }
        }

        // Inside DataSeeder.run() — after seeding roles:

// Create a default admin user if none exists
        if (authUserRepository.findByEmail("admin@yourapp.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            AuthUser adminUser = AuthUser.builder()
                    .email("admin@yourapp.com")
                    .password(passwordEncoder.encode("ChangeMe123!")) // change on first login
                    .name("System Admin")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            authUserRepository.save(adminUser);
            log.info("Default admin created: admin@yourapp.com");
        }
    }
}