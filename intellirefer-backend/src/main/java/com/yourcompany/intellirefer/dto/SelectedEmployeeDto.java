package com.yourcompany.intellirefer.dto;

import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SelectedEmployeeDto {
    private Long employeeUserId;
    private String employeeFullName;
    private String employeeEmail;
    private AvailabilityStatus availability; // e.g., ON_PROJECT, RESERVED

    // Details of the job they were selected for
    private Long jobId;
    private String jobTitle;
    private String clientName;
}