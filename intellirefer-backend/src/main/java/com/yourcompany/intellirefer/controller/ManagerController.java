package com.yourcompany.intellirefer.controller;

import com.yourcompany.intellirefer.dto.JobDescriptionDto;
import com.yourcompany.intellirefer.dto.ReferralDto;
import com.yourcompany.intellirefer.dto.SelectedEmployeeDto;
import com.yourcompany.intellirefer.entity.User;
import com.yourcompany.intellirefer.model.enums.JdStatus;
import com.yourcompany.intellirefer.model.enums.ReferralStatus;
import com.yourcompany.intellirefer.repository.UserRepository;
import com.yourcompany.intellirefer.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    @Autowired private ManagerService managerService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/jds")
    public ResponseEntity<JobDescriptionDto> uploadJd(Authentication authentication,
                                                      @RequestParam("title") String title,
                                                      @RequestParam("clientName") String clientName,
                                                      @RequestParam("file") MultipartFile file) {
        User manager = getCurrentUser(authentication);
        JobDescriptionDto createdJd = managerService.uploadJd(manager.getId(), title, clientName, file);
        return new ResponseEntity<>(createdJd, HttpStatus.CREATED);
    }

    @GetMapping("/selected-employees")
    public ResponseEntity<List<SelectedEmployeeDto>> getSelectedEmployees() {
        List<SelectedEmployeeDto> selectedEmployees = managerService.getSelectedAndReservedEmployees();
        return ResponseEntity.ok(selectedEmployees);
    }

    @GetMapping("/jds")
    public ResponseEntity<List<JobDescriptionDto>> getMyJds(Authentication authentication) {
        User manager = getCurrentUser(authentication);
        List<JobDescriptionDto> jds = managerService.getJdsForManager(manager.getId());
        return ResponseEntity.ok(jds);
    }

    @GetMapping("/jds/{jdId}/recommendations")
    public ResponseEntity<List<ReferralDto>> getRecommendations(@PathVariable Long jdId) {
        List<ReferralDto> recommendations = managerService.getRecommendationsForJd(jdId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/referrals/{referralId}/select")
    public ResponseEntity<ReferralDto> selectEmployee(@PathVariable Long referralId) {
        ReferralDto updatedReferral = managerService.updateReferralStatus(referralId, ReferralStatus.SELECTED);
        return ResponseEntity.ok(updatedReferral);
    }

    @PostMapping("/referrals/{referralId}/reserve")
    public ResponseEntity<ReferralDto> reserveEmployee(@PathVariable Long referralId) {
        ReferralDto updatedReferral = managerService.updateReferralStatus(referralId, ReferralStatus.RESERVED);
        return ResponseEntity.ok(updatedReferral);
    }

    @PostMapping("/referrals/{referralId}/reject")
    public ResponseEntity<ReferralDto> rejectEmployee(@PathVariable Long referralId) {
        ReferralDto updatedReferral = managerService.updateReferralStatus(referralId, ReferralStatus.REJECTED);
        return ResponseEntity.ok(updatedReferral);
    }

    @PostMapping("/jds/{jdId}/close")
    public ResponseEntity<JobDescriptionDto> closeJobDescription(@PathVariable Long jdId) {
        JobDescriptionDto updatedJd = managerService.updateJdStatus(jdId, JdStatus.CLOSED);
        return ResponseEntity.ok(updatedJd);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}