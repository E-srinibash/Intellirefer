package com.yourcompany.intellirefer.dto;

import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import com.yourcompany.intellirefer.model.enums.ReferralStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class ReferralDto {
    private Long referralId;
    private Long employeeUserId;
    private String employeeFullName;
    private Integer yearsOfExperience;
    private Integer matchScore;
    private String justification;
    private ReferralStatus status;
    private Set<String> skills; // It's helpful for the manager to see the employee's key skills
    private List<String> matchingSkills;
    private String currentRole;
    private String jobLevel;
    private AvailabilityStatus availability;
    private LocalDate expectedAvailabilityDate;
}