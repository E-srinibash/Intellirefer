package com.yourcompany.intellirefer.service;

import com.yourcompany.intellirefer.dto.AuthResponse;
import com.yourcompany.intellirefer.dto.LoginRequest;
import com.yourcompany.intellirefer.dto.RegisterRequest;
import com.yourcompany.intellirefer.entity.EmployeeProfile;
import com.yourcompany.intellirefer.entity.User;
import com.yourcompany.intellirefer.model.enums.Role;
import com.yourcompany.intellirefer.repository.EmployeeProfileRepository;
import com.yourcompany.intellirefer.repository.UserRepository;
import com.yourcompany.intellirefer.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Authenticates a user and returns an AuthResponse containing the JWT
     * and a clean role name (e.g., "MANAGER", "EMPLOYEE").
     *
     * @param loginRequest DTO with user credentials.
     * @return AuthResponse with token and role.
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            logger.info("Authentication successful for user: {}", loginRequest.getEmail());

            // Generate the token.
            String token = tokenProvider.generateToken(authentication);

            // === THIS IS THE KEY CHANGE ===
            // Extract the user's authority, find the first one, and clean it up
            // by removing the "ROLE_" prefix before sending it to the frontend.
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .orElse("UNKNOWN");

            // Return the new AuthResponse object with the clean role name.
            return new AuthResponse(token, role);

        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}. Reason: {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Registers a new user with the EMPLOYEE role.
     */
    @Transactional
    public User registerEmployee(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email Address already in use!");
        }
        logger.info("Registering new employee with email: {}", registerRequest.getEmail());

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.EMPLOYEE);
        User savedUser = userRepository.save(user);

        EmployeeProfile profile = new EmployeeProfile();
        profile.setUser(savedUser);
        profile.setFullName(registerRequest.getFullName());
        profile.setYearsOfExperience(registerRequest.getYearsOfExperience());
        employeeProfileRepository.save(profile);

        logger.info("Successfully registered and created profile for employee: {}", savedUser.getEmail());

        return savedUser;
    }
}