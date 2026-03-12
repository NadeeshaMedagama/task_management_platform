package com.taskmanager.infrastructure;

import com.taskmanager.domain.model.Role;
import com.taskmanager.domain.model.User;
import com.taskmanager.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a default admin account on first startup if no admin user exists.
 *
 * <p>Credentials are configured via application properties or environment variables:
 * <ul>
 *   <li>{@code app.admin.username} / {@code ADMIN_USERNAME} — default: {@code admin}</li>
 *   <li>{@code app.admin.email}    / {@code ADMIN_EMAIL}    — default: {@code admin@taskmanager.com}</li>
 *   <li>{@code app.admin.password} / {@code ADMIN_PASSWORD} — default: {@code admin123}</li>
 * </ul>
 *
 * <p><strong>Important:</strong> Change the default password immediately
 * after first login in any non-local environment.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:${ADMIN_USERNAME:admin}}")
    private String adminUsername;

    @Value("${app.admin.email:${ADMIN_EMAIL:admin@taskmanager.com}}")
    private String adminEmail;

    @Value("${app.admin.password:${ADMIN_PASSWORD:admin123}}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            var existingUser = userRepository.findByUsername(adminUsername);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (user.getRole() != Role.ADMIN) {
                    user.setRole(Role.ADMIN);
                    userRepository.save(user);
                    log.info("Existing user '{}' promoted to ADMIN role.", adminUsername);
                } else {
                    log.info("Admin user '{}' already exists — skipping seed.", adminUsername);
                }
                return;
            }

            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user '{}' created successfully.", adminUsername);
        } catch (Exception e) {
            log.error("Failed to seed admin user: {}", e.getMessage(), e);
        }
    }
}

