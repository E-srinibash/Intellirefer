package com.yourcompany.intellirefer.dto;

import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeProfileDto {
    private Long userId;
    private String email;
    private String fullName;
    private Integer yearsOfExperience;
    private AvailabilityStatus availability;
    private Set<String> skills; // We expose skill names, not the entire Skill object.
    private String jobLevel;
    private String currentRole;
    private LocalDate expectedAvailabilityDate;
}