package com.yourcompany.intellirefer.mapper;

import com.yourcompany.intellirefer.dto.EmployeeProfileDto;
import com.yourcompany.intellirefer.dto.JobDescriptionDto;
import com.yourcompany.intellirefer.dto.ReferralDto;
import com.yourcompany.intellirefer.entity.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public EmployeeProfileDto toEmployeeProfileDto(EmployeeProfile profile) {
        EmployeeProfileDto dto = new EmployeeProfileDto();
        dto.setUserId(profile.getUserId());
        dto.setEmail(profile.getUser().getEmail());
        dto.setFullName(profile.getFullName());
        dto.setYearsOfExperience(profile.getYearsOfExperience());
        dto.setAvailability(profile.getAvailability());
        dto.setJobLevel(profile.getJobLevel());
        dto.setCurrentRole(profile.getCurrentRole());
        dto.setExpectedAvailabilityDate(profile.getExpectedAvailabilityDate());
        dto.setSkills(profile.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet()));
        return dto;
    }

    public JobDescriptionDto toJobDescriptionDto(JobDescription jd) {
        JobDescriptionDto dto = new JobDescriptionDto();
        dto.setId(jd.getId());
        dto.setTitle(jd.getTitle());
        dto.setClientName(jd.getClientName());
        dto.setStatus(jd.getStatus());
        dto.setCreatedAt(jd.getCreatedAt());
        return dto;
    }

    public ReferralDto toReferralDto(Referral referral) {
        ReferralDto dto = new ReferralDto();
        EmployeeProfile employee = referral.getEmployee();
        dto.setReferralId(referral.getId());
        dto.setEmployeeUserId(employee.getUserId());
        dto.setEmployeeFullName(employee.getFullName());
        dto.setYearsOfExperience(employee.getYearsOfExperience());
        dto.setCurrentRole(employee.getCurrentRole());
        dto.setJobLevel(employee.getJobLevel());
        dto.setAvailability(employee.getAvailability());
        dto.setExpectedAvailabilityDate(employee.getExpectedAvailabilityDate());
        dto.setMatchScore(referral.getMatchScore());
        dto.setJustification(referral.getJustification());
        dto.setStatus(referral.getStatus());
        dto.setSkills(employee.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet()));
        if (referral.getMatchingSkills() != null && !referral.getMatchingSkills().isEmpty()) {
            dto.setMatchingSkills(Arrays.asList(referral.getMatchingSkills().split(",")));
        } else {
            dto.setMatchingSkills(Collections.emptyList()); // Ensure it's never null
        }
        return dto;
    }
}