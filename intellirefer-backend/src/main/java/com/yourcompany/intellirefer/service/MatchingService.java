package com.yourcompany.intellirefer.service;

import com.yourcompany.intellirefer.dto.LLMResponse;
import com.yourcompany.intellirefer.entity.EmployeeProfile;
import com.yourcompany.intellirefer.entity.JobDescription;
import com.yourcompany.intellirefer.entity.Referral;
import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import com.yourcompany.intellirefer.repository.EmployeeProfileRepository;
import com.yourcompany.intellirefer.repository.JobDescriptionRepository;
import com.yourcompany.intellirefer.repository.ReferralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class MatchingService {
    private static final Logger logger = LoggerFactory.getLogger(MatchingService.class);

    @Autowired private JobDescriptionRepository jdRepository;
    @Autowired private EmployeeProfileRepository employeeRepository;
    @Autowired private ReferralRepository referralRepository;
    @Autowired private FileSystemStorageService fileSystemStorageService;
    @Autowired private DocumentParsingService parsingService;
    @Autowired private LLMService llmService;

    /**
     * The main entry point for the asynchronous matching process.
     * It orchestrates the extraction of JD requirements and then calls the
     * employee matching logic.
     * @param jdId The ID of the JobDescription to process.
     */
    @Async
    @Transactional
    public void processJdMatching(Long jdId) {
        logger.info("============== [MATCHING START] for JD ID: {} ==============", jdId);
        JobDescription jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new RuntimeException("JobDescription not found in async task: " + jdId));

        try (InputStream jdStream = fileSystemStorageService.loadAsResource(jd.getJdFilePath()).getInputStream()) {
            String jdText = parsingService.parse(jdStream, getFileExtension(jd.getJdFilePath()));

            // Step 1: Extract required experience from the JD using the LLM and save it.
            llmService.extractExperienceFromJd(jdText).subscribe(
                    jdResponse -> {
                        Integer requiredExp = jdResponse.requiredExperience() != null ? jdResponse.requiredExperience() : 0;
                        logger.info("LLM extracted required experience for JD ID {}: {} years.", jd.getId(), requiredExp);
                        jd.setRequiredExperience(requiredExp);
                        jdRepository.save(jd);

                        // Step 2: Now that we have the required experience, match employees.
                        matchEmployeesAgainstJd(jd, jdText);
                    },
                    error -> {
                        logger.error("Failed to extract experience from JD ID {}. Matching will proceed without this filter. Error: {}", jd.getId(), error.getMessage());
                        // Fallback: Proceed with matching even if experience extraction fails.
                        matchEmployeesAgainstJd(jd, jdText);
                    }
            );
        } catch (Exception e) {
            logger.error("CRITICAL error during JD parsing for JD ID: {}. Process halted. Error: {}", jdId, e.getMessage());
        }
    }

    /**
     * Finds potential candidates and runs them through the matching filters.
     * @param jd The JobDescription entity, now with requiredExperience populated.
     * @param jdText The full text of the job description.
     */
    private void matchEmployeesAgainstJd(JobDescription jd, String jdText) {
        // Find employees who are either AVAILABLE or will be available within 90 days.
        LocalDate availabilityThreshold = LocalDate.now().plusDays(90);
        List<EmployeeProfile> potentialCandidates = employeeRepository.findAvailableOrSoonToBeAvailable(availabilityThreshold);

        logger.info("Found {} potential candidates (available or available soon) to match against.", potentialCandidates.size());

        if (potentialCandidates.isEmpty()) {
            logger.warn("No potential candidates found. Matching process for JD ID {} will stop.", jd.getId());
            return;
        }

        Integer requiredExperience = jd.getRequiredExperience() != null ? jd.getRequiredExperience() : 0;

        for (EmployeeProfile employee : potentialCandidates) {
            logger.info("Processing employee ID: {} - {}", employee.getUserId(), employee.getFullName());

            // Step 3: Pre-filter by years of experience.
            Integer employeeExperience = employee.getYearsOfExperience() != null ? employee.getYearsOfExperience() : 0;
            if (employeeExperience < requiredExperience) {
                logger.warn("Skipping employee ID: {} due to insufficient experience (Has: {}, Requires: {}).", employee.getUserId(), employeeExperience, requiredExperience);
                continue; // Skip to the next employee
            }

            if (employee.getResumeFilePath() == null || StringUtils.isEmpty(employee.getResumeFilePath())) {
                logger.warn("Skipping employee ID: {} because they have no resume file path.", employee.getUserId());
                continue;
            }

            // Step 4: For candidates who pass the filter, perform the full LLM resume scan.
            try (InputStream resumeStream = fileSystemStorageService.loadAsResource(employee.getResumeFilePath()).getInputStream()) {
                String resumeText = parsingService.parse(resumeStream, getFileExtension(employee.getResumeFilePath()));

                llmService.getMatchScore(jdText, resumeText).subscribe(
                        llmResponse -> {
                            logger.info("LLM match score received for Employee ID: {}. Score: {}. Saving referral...", employee.getUserId(), llmResponse.score());
                            saveReferral(jd, employee, llmResponse);
                        },
                        error -> logger.error("LLM API call for match score FAILED for Employee ID: {}. Reason: {}", employee.getUserId(), error.getMessage())
                );
            } catch (Exception e) {
                logger.error("Failed to process resume for Employee ID: {}. Skipping. Error: {}", employee.getUserId(), e.getMessage());
            }
        }
    }

    /**
     * Saves a new Referral record to the database. This runs in its own new
     * transaction to ensure it can commit even when called from a reactive chain.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveReferral(JobDescription jd, EmployeeProfile employee, LLMResponse llmResponse) {
        Referral referral = new Referral();
        referral.setJobDescription(jd);
        referral.setEmployee(employee);
        referral.setMatchScore(llmResponse.score());
        referral.setJustification(llmResponse.justification());

        if (llmResponse.matchingSkills() != null && !llmResponse.matchingSkills().isEmpty()) {
            String skillsString = String.join(",", llmResponse.matchingSkills());
            referral.setMatchingSkills(skillsString);
        }

        referralRepository.save(referral);
        logger.info("SUCCESS: Referral saved for Employee ID {} and JD ID {}.", employee.getUserId(), jd.getId());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}