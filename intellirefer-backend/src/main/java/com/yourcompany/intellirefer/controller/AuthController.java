package com.yourcompany.intellirefer.controller;

import com.yourcompany.intellirefer.dto.ApiResponse;
import com.yourcompany.intellirefer.dto.AuthResponse;
import com.yourcompany.intellirefer.dto.LoginRequest;
import com.yourcompany.intellirefer.dto.RegisterRequest;
import com.yourcompany.intellirefer.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // This endpoint now correctly returns the AuthResponse DTO from the service.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerEmployee(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerEmployee(registerRequest);
        return new ResponseEntity<>(new ApiResponse(true, "Employee registered successfully! Please log in."), HttpStatus.CREATED);
    }
}