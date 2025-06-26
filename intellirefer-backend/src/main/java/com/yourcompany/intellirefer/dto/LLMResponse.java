package com.yourcompany.intellirefer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A Data Transfer Object representing the structured JSON response
 * expected from the Language Model (LLM) for a resume-JD match analysis.
 *
 * The expected JSON format is:
 * {
 *   "score": 92,
 *   "justification": "This is a summary.",
 *   "matching_skills": ["Java", "Spring Boot", "REST APIs"]
 * }
 */
public record LLMResponse(

        @JsonProperty("score")
        int score,

        @JsonProperty("justification")
        String justification,

        @JsonProperty("matching_skills")
        List<String> matchingSkills

) {}