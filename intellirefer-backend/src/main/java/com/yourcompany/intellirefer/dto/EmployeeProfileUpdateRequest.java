package com.yourcompany.intellirefer.dto;

import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeProfileUpdateRequest {

    @NotBlank(message = "Full name cannot be blank.")
    private String fullName;

    @NotNull(message = "Years of experience cannot be null.")
    private Integer yearsOfExperience;

    @NotNull(message = "Availability status cannot be null.")
    private AvailabilityStatus availability;

    @NotNull(message = "Skills set cannot be null.")
    private Set<String> skills;

    private String jobLevel;
    private String currentRole;
    private LocalDate expectedAvailabilityDate;
}