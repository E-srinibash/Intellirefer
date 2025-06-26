package com.yourcompany.intellirefer.config;

import com.yourcompany.intellirefer.entity.User;
import com.yourcompany.intellirefer.model.enums.Role;
import com.yourcompany.intellirefer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This component runs on application startup and ensures that a default
 * manager/admin user exists in the database.
 * It is idempotent, meaning it will only create the user if one with the
 * specified email does not already exist.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // These values are injected from application.properties
    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    /**
     * This method will be executed automatically by Spring Boot on startup.
     */
    @Override
    @Transactional // Use a transaction to ensure data consistency
    public void run(String... args) throws Exception {
        // Check if the default manager user already exists in the database
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            logger.info("Default manager user not found. Creating one with email: {}", adminEmail);

            User managerUser = new User();
            managerUser.setEmail(adminEmail);

            // IMPORTANT: Always hash the password before saving
            managerUser.setPasswordHash(passwordEncoder.encode(adminPassword));

            // Set the role to MANAGER
            managerUser.setRole(Role.MANAGER);

            // Save the new manager user to the database
            userRepository.save(managerUser);

            logger.info("Default manager user created successfully.");

        } else {
            logger.info("Default manager user with email '{}' already exists. Skipping creation.", adminEmail);
        }
    }
}