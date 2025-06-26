package com.yourcompany.intellirefer.service;

import com.yourcompany.intellirefer.dto.LLMResponse;
import com.yourcompany.intellirefer.entity.*;
import com.yourcompany.intellirefer.event.JdUploadedEvent;
import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import com.yourcompany.intellirefer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
     * This method listens for a JdUploadedEvent.
     * It is triggered automatically AFTER the transaction that published the event has
     * successfully committed, ensuring the JobDescription is visible in the database.
     * It runs asynchronously on a background thread.
     *
     * @param event The event containing the ID of the newly created Job Description.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJdUploadedEvent(JdUploadedEvent event) {
        Long jdId = event.getJobDescriptionId();

        logger.info("============== [MATCHING START from Event] for JD ID: {} ==============", jdId);

        // This findById call is now safe and will find the JD.
        JobDescription jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new RuntimeException("JobDescription not found in event listener even after commit: " + jdId));

        try (InputStream jdStream = fileSystemStorageService.loadAsResource(jd.getJdFilePath()).getInputStream()) {
            String jdText = parsingService.parse(jdStream, getFileExtension(jd.getJdFilePath()));

            // Step 1: Extract required experience from the JD and save it.
            llmService.extractExperienceFromJd(jdText).subscribe(
                    jdResponse -> {
                        Integer requiredExp = jdResponse.requiredExperience() != null ? jdResponse.requiredExperience() : 0;
                        logger.info("LLM extracted required experience for JD ID {}: {} years.", jd.getId(), requiredExp);

                        // Since we're in a new transaction, we need to save the updated JD.
                        // To do this safely within a reactive chain, we can fetch it again.
                        jdRepository.findById(jd.getId()).ifPresent(jobDescToUpdate -> {
                            jobDescToUpdate.setRequiredExperience(requiredExp);
                            jdRepository.save(jobDescToUpdate);
                            // Step 2: Now proceed with matching employees using the updated JD.
                            matchEmployeesAgainstJd(jobDescToUpdate, jdText);
                        });
                    },
                    error -> {
                        logger.error("Failed to extract experience from JD ID {}. Matching process will continue without this filter. Error: {}", jd.getId(), error.getMessage());
                        matchEmployeesAgainstJd(jd, jdText);
                    }
            );
        } catch (Exception e) {
            logger.error("CRITICAL error during JD parsing for JD ID: {}. Process halted. Error: {}", jdId, e.getMessage());
        }
    }

    /**
     * Private helper method to contain the core matching logic.
     * It pre-filters candidates by experience and then calls the LLM for detailed analysis.
     */
    private void matchEmployeesAgainstJd(JobDescription jd, String jdText) {
        LocalDate availabilityThreshold = LocalDate.now().plusDays(90);
        List<EmployeeProfile> potentialCandidates = employeeRepository.findAvailableOrSoonToBeAvailable(availabilityThreshold);

        logger.info("Found {} potential candidates to match against.", potentialCandidates.size());
        if (potentialCandidates.isEmpty()) {
            logger.warn("No potential candidates found. Ending matching process for JD ID: {}", jd.getId());
            return;
        }

        Integer requiredExperience = jd.getRequiredExperience() != null ? jd.getRequiredExperience() : 0;

        for (EmployeeProfile employee : potentialCandidates) {
            logger.info("Processing employee ID: {} - {}", employee.getUserId(), employee.getFullName());

            Integer employeeExperience = employee.getYearsOfExperience() != null ? employee.getYearsOfExperience() : 0;
            if (employeeExperience < requiredExperience) {
                logger.warn("Skipping employee ID: {} due to insufficient experience (Has: {}, Requires: {}).", employee.getUserId(), employeeExperience, requiredExperience);
                continue;
            }

            if (employee.getResumeFilePath() == null) {
                logger.warn("Skipping employee ID: {} because they have no resume file path.", employee.getUserId());
                continue;
            }

            try (InputStream resumeStream = fileSystemStorageService.loadAsResource(employee.getResumeFilePath()).getInputStream()) {
                String resumeText = parsingService.parse(resumeStream, getFileExtension(employee.getResumeFilePath()));

                llmService.getMatchScore(jdText, resumeText).subscribe(
                        llmResponse -> {
                            logger.info("LLM successful for Employee ID: {}. Score: {}. Saving referral...", employee.getUserId(), llmResponse.score());
                            saveReferral(jd, employee, llmResponse);
                        },
                        error -> logger.error("LLM API call FAILED for Employee ID: {}. Reason: {}", employee.getUserId(), error.getMessage())
                );
            } catch (Exception e) {
                logger.error("Failed to process resume for Employee ID: {}. Skipping. Error: {}", employee.getUserId(), e.getMessage());
            }
        }
    }

    /**
     * Saves a new Referral record. Runs in its own new transaction to ensure
     * it's independent of the main loop.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveReferral(JobDescription jd, EmployeeProfile employee, LLMResponse llmResponse) {
        Referral referral = new Referral();
        referral.setJobDescription(jd);
        referral.setEmployee(employee);
        referral.setMatchScore(llmResponse.score());
        referral.setJustification(llmResponse.justification());

        if (llmResponse.matchingSkills() != null && !llmResponse.matchingSkills().isEmpty()) {
            referral.setMatchingSkills(String.join(",", llmResponse.matchingSkills()));
        }

        referralRepository.save(referral);
        logger.info("SUCCESS: Referral saved for Employee ID {} and JD ID {}.", employee.getUserId(), jd.getId());
    }

    /**
     * Helper utility to safely get a file's extension.
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}