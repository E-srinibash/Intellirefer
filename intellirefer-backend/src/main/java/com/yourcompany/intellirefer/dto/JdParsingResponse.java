package com.yourcompany.intellirefer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO to capture the structured data extracted by the LLM from a Job Description.
 * Expected JSON: {"required_experience": 5}
 */
public record JdParsingResponse(
        @JsonProperty("required_experience") Integer requiredExperience
) {}