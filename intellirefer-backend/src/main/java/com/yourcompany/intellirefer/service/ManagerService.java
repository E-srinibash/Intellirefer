package com.yourcompany.intellirefer.service;

import com.yourcompany.intellirefer.dto.JobDescriptionDto;
import com.yourcompany.intellirefer.dto.ReferralDto;
import com.yourcompany.intellirefer.dto.SelectedEmployeeDto;
import com.yourcompany.intellirefer.entity.*;
import com.yourcompany.intellirefer.exception.ResourceNotFoundException;
import com.yourcompany.intellirefer.mapper.DtoMapper;
import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import com.yourcompany.intellirefer.model.enums.JdStatus;
import com.yourcompany.intellirefer.model.enums.ReferralStatus;
import com.yourcompany.intellirefer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling all business logic related to Managers.
 * This includes managing Job Descriptions and making decisions on candidate referrals.
 */
@Service
public class ManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerService.class);

    // Repositories for database access
    @Autowired private JobDescriptionRepository jdRepository;
    @Autowired private ReferralRepository referralRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeProfileRepository employeeProfileRepository;

    // Dependent services
    @Autowired private FileSystemStorageService fileSystemStorageService;
    @Autowired private MatchingService matchingService;

    // DTO Mapper for converting entities to DTOs
    @Autowired private DtoMapper dtoMapper;

    /**
     * Handles the upload of a new Job Description. It saves the file, creates a
     * DB record, and triggers the asynchronous matching process.
     *
     * @param managerId The ID of the manager uploading the JD.
     * @param title The title of the job.
     * @param clientName The name of the client.
     * @param file The JD file (e.g., PDF, DOCX).
     * @return A DTO of the newly created JobDescription.
     */
    @Transactional
    public JobDescriptionDto uploadJd(Long managerId, String title, String clientName, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("JD file must not be empty");
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));

        // Store the file in a "jds" subfolder using the local file system service
        String filePath = fileSystemStorageService.store(file, "jds");
        logger.info("JD file stored at relative path: {}", filePath);

        JobDescription jd = new JobDescription();
        jd.setTitle(title);
        jd.setClientName(clientName);
        jd.setJdFilePath(filePath);
        jd.setUploadedByManager(manager);

        // Save the JobDescription entity to get its generated ID
        JobDescription savedJd = jdRepository.save(jd);
        logger.info("Saved new Job Description with ID: {}", savedJd.getId());

        // Trigger the asynchronous background process to match this JD against available employees.
        logger.info("Triggering asynchronous matching process for JD ID: {}", savedJd.getId());
        matchingService.processJdMatching(savedJd.getId());

        // Return the DTO for the newly created JD immediately.
        // The user does not have to wait for the matching to complete.
        return dtoMapper.toJobDescriptionDto(savedJd);
    }

    /**
     * Retrieves all Job Descriptions uploaded by a specific manager.
     *
     * @param managerId The ID of the manager.
     * @return A list of JobDescriptionDto objects.
     */
    public List<JobDescriptionDto> getJdsForManager(Long managerId) {
        return jdRepository.findByUploadedByManagerId(managerId).stream()
                .map(dtoMapper::toJobDescriptionDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all AI-generated recommendations for a specific Job Description,
     * sorted by the highest match score first.
     *
     * @param jdId The ID of the Job Description.
     * @return A sorted list of ReferralDto objects.
     */
    public List<ReferralDto> getRecommendationsForJd(Long jdId) {
        return referralRepository.findByJobDescriptionIdOrderByMatchScoreDesc(jdId).stream()
                .map(dtoMapper::toReferralDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a referral based on a manager's decision.
     * This method also handles the critical logic of updating the employee's
     * availability status.
     *
     * @param referralId The ID of the referral to update.
     * @param newStatus The new status (SELECTED, RESERVED, or REJECTED).
     * @return A DTO of the updated Referral.
     */
    @Transactional
    public ReferralDto updateReferralStatus(Long referralId, ReferralStatus newStatus) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", "id", referralId));

        EmployeeProfile employee = referral.getEmployee();

        // Update the employee's availability based on the manager's decision
        if (newStatus == ReferralStatus.SELECTED) {
            employee.setAvailability(AvailabilityStatus.ON_PROJECT);
            logger.info("Employee {} has been SELECTED. Availability set to ON_PROJECT.", employee.getUserId());
            // TODO: Trigger an email notification service call here
        } else if (newStatus == ReferralStatus.RESERVED) {
            employee.setAvailability(AvailabilityStatus.RESERVED);
            logger.info("Employee {} has been RESERVED.", employee.getUserId());
        } else if (referral.getStatus() == ReferralStatus.RESERVED && newStatus == ReferralStatus.REJECTED) {
            // Special case: if a reserved employee is now rejected, make them available again.
            employee.setAvailability(AvailabilityStatus.AVAILABLE);
            logger.info("Previously reserved employee {} has been REJECTED. Availability set back to AVAILABLE.", employee.getUserId());
        }

        // Save the updated employee profile
        employeeProfileRepository.save(employee);

        // Update and save the referral status
        referral.setStatus(newStatus);
        Referral updatedReferral = referralRepository.save(referral);

        // Return the updated data to the frontend
        return dtoMapper.toReferralDto(updatedReferral);
    }

    @Transactional
    public JobDescriptionDto updateJdStatus(Long jdId, JdStatus newStatus) {
        JobDescription jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new ResourceNotFoundException("JobDescription", "id", jdId));

        jd.setStatus(newStatus);
        JobDescription savedJd = jdRepository.save(jd);

        logger.info("Updated status of JD ID {} to {}", jdId, newStatus);
        return dtoMapper.toJobDescriptionDto(savedJd);
    }

    public List<SelectedEmployeeDto> getSelectedAndReservedEmployees() {
        // 1. Find all employees who are not currently available.
        List<AvailabilityStatus> busyStatuses = Arrays.asList(AvailabilityStatus.ON_PROJECT, AvailabilityStatus.RESERVED);
        List<EmployeeProfile> busyEmployees = employeeProfileRepository.findByAvailabilityIn(busyStatuses);

        List<SelectedEmployeeDto> result = new ArrayList<>();

        // 2. For each busy employee, find the job they were selected/reserved for.
        for (EmployeeProfile employee : busyEmployees) {
            SelectedEmployeeDto dto = new SelectedEmployeeDto();
            dto.setEmployeeUserId(employee.getUserId());
            dto.setEmployeeFullName(employee.getFullName());
            dto.setEmployeeEmail(employee.getUser().getEmail());
            dto.setAvailability(employee.getAvailability());

            // 3. Find the corresponding referral to get the job details.
            List<ReferralStatus> referralStatuses = Arrays.asList(ReferralStatus.SELECTED, ReferralStatus.RESERVED);
            referralRepository.findFirstByEmployeeUserIdAndStatusIn(employee.getUserId(), referralStatuses)
                    .ifPresent(referral -> {
                        JobDescription jd = referral.getJobDescription();
                        dto.setJobId(jd.getId());
                        dto.setJobTitle(jd.getTitle());
                        dto.setClientName(jd.getClientName());
                    });

            result.add(dto);
        }

        return result;
    }
}