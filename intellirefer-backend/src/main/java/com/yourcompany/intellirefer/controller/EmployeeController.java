package com.yourcompany.intellirefer.controller;

import com.yourcompany.intellirefer.dto.ApiResponse;
import com.yourcompany.intellirefer.dto.EmployeeProfileDto;
import com.yourcompany.intellirefer.dto.EmployeeProfileUpdateRequest;
import com.yourcompany.intellirefer.entity.User;
import com.yourcompany.intellirefer.repository.UserRepository;
import com.yourcompany.intellirefer.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    @Autowired private EmployeeService employeeService;
    @Autowired private UserRepository userRepository; // To fetch user ID from email

    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileDto> getCurrentEmployeeProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        EmployeeProfileDto profileDto = employeeService.getEmployeeProfile(user.getId());
        return ResponseEntity.ok(profileDto);
    }

    @PutMapping("/me")
    public ResponseEntity<EmployeeProfileDto> updateCurrentEmployeeProfile(Authentication authentication, @Valid @RequestBody EmployeeProfileUpdateRequest updateRequest) {
        User user = getCurrentUser(authentication);
        EmployeeProfileDto updatedProfile = employeeService.updateEmployeeProfile(user.getId(), updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/me/resume")
    public ResponseEntity<ApiResponse> uploadResume(Authentication authentication, @RequestParam("file") MultipartFile file) {
        User user = getCurrentUser(authentication);
        String message = employeeService.uploadResume(user.getId(), file);
        return ResponseEntity.ok(new ApiResponse(true, message));
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}