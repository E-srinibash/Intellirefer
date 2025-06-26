package com.yourcompany.intellirefer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.intellirefer.dto.GeminiResponseDto;
import com.yourcompany.intellirefer.dto.JdParsingResponse;
import com.yourcompany.intellirefer.dto.LLMResponse;
import com.yourcompany.intellirefer.dto.SkillExtractionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${llm.google.api.url_template}")
    private String apiUrlTemplate;

    @Value("${llm.google.api.key}")
    private String apiKey;

    @Autowired
    public LLMService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends a JD and Resume to the Gemini API to get a match score.
     * @param jdText The text of the Job Description.
     * @param resumeText The text of the Resume.
     * @return A Mono containing the structured match score and justification.
     */
    public Mono<LLMResponse> getMatchScore(String jdText, String resumeText) {
        String prompt = buildMatchScorePrompt(jdText, resumeText);
        Map<String, Object> requestBody = createGeminiRequestBody(prompt);
        String finalUrl = String.format(apiUrlTemplate, apiKey);

        logger.info("Sending request to Google Gemini API for match score.");
        return webClient.post()
                .uri(finalUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .map(this::parseJsonToLlmResponse);
    }

    /**
     * Sends resume text to the Gemini API to extract a list of skills.
     * @param resumeText The full text of the resume.
     * @return A Mono containing the structured list of skills.
     */
    public Mono<SkillExtractionResponse> extractSkillsFromResume(String resumeText) {
        String prompt = buildSkillExtractionPrompt(resumeText);
        Map<String, Object> requestBody = createGeminiRequestBody(prompt);
        String finalUrl = String.format(apiUrlTemplate, apiKey);

        logger.info("Sending request to Google Gemini API for skill extraction.");
        return webClient.post()
                .uri(finalUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .map(this::parseJsonToSkillExtractionResponse);
    }

    private Map<String, Object> createGeminiRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );
    }

    public Mono<JdParsingResponse> extractExperienceFromJd(String jdText) {
        String prompt = buildJdParsingPrompt(jdText);
        Map<String, Object> requestBody = createGeminiRequestBody(prompt);
        String finalUrl = String.format(apiUrlTemplate, apiKey);

        logger.info("Sending request to Google Gemini API for JD experience extraction.");
        return webClient.post()
                .uri(finalUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .map(this::parseJsonToJdParsingResponse);
    }

    private JdParsingResponse parseJsonToJdParsingResponse(GeminiResponseDto geminiResponse) {
        try {
            String jsonText = cleanJsonString(geminiResponse.candidates().get(0).content().parts().get(0).text());
            return objectMapper.readValue(jsonText, JdParsingResponse.class);
        } catch (Exception e) {
            logger.error("Failed to parse JSON from Gemini response for JdParsingResponse", e);
            throw new RuntimeException("Failed to parse LLM response for JD", e);
        }
    }

    private String buildJdParsingPrompt(String jdText) {
        return String.format(
                """
                You are an expert data extraction bot. Analyze the following Job Description text.
                Identify the minimum required years of experience.
                Return the result ONLY as a valid JSON object with a single key "required_experience" which contains the number as an integer.
                If no specific number of years is mentioned, return 0.
    
                Example format: {"required_experience": 5}
    
                Job Description Text:
                ---
                %s
                ---
                """, jdText
        );
    }


    private LLMResponse parseJsonToLlmResponse(GeminiResponseDto geminiResponse) {
        try {
            String rawText = geminiResponse.candidates().get(0).content().parts().get(0).text();
            String cleanJson = cleanJsonString(rawText); // Clean the string first
            return objectMapper.readValue(cleanJson, LLMResponse.class);
        } catch (Exception e) {
            logger.error("Failed to parse JSON from Gemini response for LLMResponse", e);
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

    private SkillExtractionResponse parseJsonToSkillExtractionResponse(GeminiResponseDto geminiResponse) {
        try {
            String rawText = geminiResponse.candidates().get(0).content().parts().get(0).text();
            String cleanJson = cleanJsonString(rawText); // Clean the string first
            return objectMapper.readValue(cleanJson, SkillExtractionResponse.class);
        } catch (Exception e) {
            logger.error("Failed to parse JSON from Gemini response for SkillExtractionResponse", e);
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

    private String cleanJsonString(String rawText) {
        String cleanedText = rawText.trim();
        if (cleanedText.startsWith("```json")) {
            cleanedText = cleanedText.substring(7); // Remove ```json
        }
        if (cleanedText.startsWith("```")) {
            cleanedText = cleanedText.substring(3); // Remove ```
        }
        if (cleanedText.endsWith("```")) {
            cleanedText = cleanedText.substring(0, cleanedText.length() - 3); // Remove ```
        }
        return cleanedText.trim();
    }

    private String buildMatchScorePrompt(String jdText, String resumeText) {
        return String.format(
                """
                You are an expert HR recruitment assistant. Analyze the following Job Description and Resume.
                1. Provide a matching score from 0 to 100.
                2. Provide a 2-sentence summary explaining your score.
                3. Identify the top 5 to 6 key skills from the resume that directly match the job description's requirements.
        
                Return the result ONLY in a valid JSON format like this:
                {"score": 92, "justification": "This is a summary.", "matching_skills": ["Java", "Spring Boot", "Microservices", "REST APIs", "SQL"]}
                
                Do not include ```json and ``` at the start or end.
        
                **Job Description:**
                %s
        
                **Resume:**
                %s
                """, jdText, resumeText
        );
    }

    private String buildSkillExtractionPrompt(String resumeText) {
        return String.format(
                """
                You are an expert technical recruiter. Analyze the following resume text and extract all relevant technical skills.
                Return the result ONLY as a valid JSON object with a single key "skills" which contains an array of strings. Do not include any explanation or introductory text.
                Do not include ```json and ``` at the start or end.
    
                Example format:
                {"skills": ["Java", "Spring Boot", "React", "PostgreSQL", "AWS", "Agile", "Team Leadership"]}
    
                Resume Text:
                ---
                %s
                ---
                """, resumeText
        );
    }
}