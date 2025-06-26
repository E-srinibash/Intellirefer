package com.yourcompany.intellirefer.service;

import com.yourcompany.intellirefer.dto.EmployeeProfileDto;
import com.yourcompany.intellirefer.dto.EmployeeProfileUpdateRequest;
import com.yourcompany.intellirefer.entity.EmployeeProfile;
import com.yourcompany.intellirefer.entity.Skill;
import com.yourcompany.intellirefer.exception.ResourceNotFoundException;
import com.yourcompany.intellirefer.mapper.DtoMapper;
import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import com.yourcompany.intellirefer.repository.EmployeeProfileRepository;
import com.yourcompany.intellirefer.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for handling all business logic related to Employees.
 * This includes profile management, resume uploads, and automated skill extraction.
 */
@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    // Repositories for DB access
    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;
    @Autowired
    private SkillRepository skillRepository;

    // Dependent services
    @Autowired
    private FileSystemStorageService fileSystemStorageService;
    @Autowired
    private DocumentParsingService parsingService;
    @Autowired
    private LLMService llmService;

    // DTO Mapper
    @Autowired
    private DtoMapper dtoMapper;

    /**
     * Retrieves the profile for a given employee.
     *
     * @param userId The ID of the employee.
     * @return An EmployeeProfileDto containing the employee's public data.
     */
    public EmployeeProfileDto getEmployeeProfile(Long userId) {
        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeProfile", "userId", userId));
        return dtoMapper.toEmployeeProfileDto(profile);
    }

    /**
     * Updates an employee's profile information, including their job level,
     * role, availability, expected availability date, and skills.
     *
     * @param userId The ID of the employee to update.
     * @param updateRequest DTO with the new profile data.
     * @return The updated EmployeeProfileDto.
     */
    @Transactional
    public EmployeeProfileDto updateEmployeeProfile(Long userId, EmployeeProfileUpdateRequest updateRequest) {
        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeProfile", "userId", userId));

        // Update standard profile fields
        profile.setFullName(updateRequest.getFullName());
        profile.setYearsOfExperience(updateRequest.getYearsOfExperience());
        profile.setAvailability(updateRequest.getAvailability());

        // Update new professional detail fields
        profile.setJobLevel(updateRequest.getJobLevel());
        profile.setCurrentRole(updateRequest.getCurrentRole());

        // Business Logic: The expected availability date is only relevant if the user
        // is currently on a project. Otherwise, it should be null.
        if (updateRequest.getAvailability() == AvailabilityStatus.ON_PROJECT) {
            profile.setExpectedAvailabilityDate(updateRequest.getExpectedAvailabilityDate());
        } else {
            profile.setExpectedAvailabilityDate(null);
        }

        // Handle skills with a "find or create" approach
        Set<Skill> skillsToSet = updateRequest.getSkills().stream()
                .map(skillName -> skillRepository.findByNameIgnoreCase(skillName.trim())
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(StringUtils.capitalize(skillName.trim()));
                            return skillRepository.save(newSkill);
                        }))
                .collect(Collectors.toSet());
        profile.setSkills(skillsToSet);

        EmployeeProfile updatedProfile = employeeProfileRepository.save(profile);
        logger.info("Successfully updated profile for user ID: {}", userId);
        return dtoMapper.toEmployeeProfileDto(updatedProfile);
    }

    /**
     * Handles the upload of a new resume for an employee.
     * This method saves the file and then triggers the asynchronous skill extraction process.
     *
     * @param userId The ID of the employee uploading the resume.
     * @param file The resume file.
     * @return A status message for the user.
     */
    @Transactional
    public String uploadResume(Long userId, MultipartFile file) {
        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeProfile", "userId", userId));

        // Delete the old resume file from storage if it exists to save space.
        if (profile.getResumeFilePath() != null) {
            fileSystemStorageService.delete(profile.getResumeFilePath());
        }

        // Store the new file in the "resumes" subfolder and get its relative path.
        String filePath = fileSystemStorageService.store(file, "resumes");
        profile.setResumeFilePath(filePath);
        employeeProfileRepository.save(profile);

        // Trigger the background job to extract skills from the new resume.
        this.extractAndSaveSkills(profile.getUserId(), filePath);

        return "Resume uploaded successfully. Skills are being extracted in the background.";
    }

    /**

     * Asynchronous method to extract skills from a resume using an LLM
     * and persist them to the database.
     *
     * @param userId The ID of the user whose skills are being updated.
     * @param resumePath The path to the resume file in storage.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void extractAndSaveSkills(Long userId, String resumePath) {
        logger.info("Starting background skill extraction for user ID: {} from path: {}", userId, resumePath);

        String resumeText;
        try (InputStream resumeStream = fileSystemStorageService.loadAsResource(resumePath).getInputStream()) {
            resumeText = parsingService.parse(resumeStream, getFileExtension(resumePath));
            if (StringUtils.isEmpty(resumeText)) {
                logger.warn("Parsed resume text is empty for user ID: {}. Aborting skill extraction.", userId);
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to parse resume for skill extraction for user ID: {}", userId, e);
            return;
        }

        llmService.extractSkillsFromResume(resumeText).subscribe(
                skillResponse -> {
                    if (skillResponse != null && skillResponse.skills() != null && !skillResponse.skills().isEmpty()) {
                        logger.info("LLM returned {} skills for user ID: {}", skillResponse.skills().size(), userId);

                        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("EmployeeProfile", "userId", userId));

                        Set<Skill> skillsToSet = skillResponse.skills().stream()
                                .map(skillName -> skillRepository.findByNameIgnoreCase(skillName.trim())
                                        .orElseGet(() -> {
                                            logger.info("New skill found: '{}'. Saving to master skills list.", skillName.trim());
                                            Skill newSkill = new Skill();
                                            newSkill.setName(StringUtils.capitalize(skillName.trim()));
                                            return skillRepository.save(newSkill);
                                        }))
                                .collect(Collectors.toSet());

                        profile.setSkills(skillsToSet);
                        employeeProfileRepository.save(profile);
                        logger.info("Successfully updated skills for user ID: {}", userId);
                    } else {
                        logger.warn("LLM returned no skills for user ID: {}", userId);
                    }
                },
                error -> logger.error("Error in LLM reactive stream for user ID: {}", userId, error)
        );
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}