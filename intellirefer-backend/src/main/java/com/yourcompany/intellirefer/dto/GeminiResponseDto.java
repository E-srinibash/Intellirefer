package com.yourcompany.intellirefer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponseDto(
        @JsonProperty("candidates") List<Candidate> candidates
) {}