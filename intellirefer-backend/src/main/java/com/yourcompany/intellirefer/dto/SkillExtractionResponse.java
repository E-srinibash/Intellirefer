package com.yourcompany.intellirefer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO to capture the list of skills extracted by the LLM.
 * Expected JSON format: {"skills": ["Java", "Spring Boot", "PostgreSQL", "Teamwork"]}
 */
public record SkillExtractionResponse(
        @JsonProperty("skills") List<String> skills
) {}