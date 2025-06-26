package com.yourcompany.intellirefer.dto;

import com.yourcompany.intellirefer.model.enums.JdStatus;
import lombok.Data;
import java.time.Instant;

@Data
public class JobDescriptionDto {
    private Long id;
    private String title;
    private String clientName;
    private JdStatus status;
    private Instant createdAt;
}