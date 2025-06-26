package com.yourcompany.intellirefer.repository;

import com.yourcompany.intellirefer.entity.Referral;
import com.yourcompany.intellirefer.model.enums.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    /**
     * Finds all referrals for a specific job description, ordered by the match score in descending order.
     * This is the primary query for the manager's recommendation view.
     * @param jobDescriptionId The ID of the job description.
     * @return A sorted list of Referrals.
     */
    List<Referral> findByJobDescriptionIdOrderByMatchScoreDesc(Long jobDescriptionId);

    /**
     * Finds a specific referral for a given job and employee combination.
     * Useful for checking if a match has already been processed.
     * @param jobDescriptionId The ID of the job description.
     * @param employeeUserId The ID of the employee.
     * @return An Optional containing the Referral if it exists.
     */
    Optional<Referral> findByJobDescriptionIdAndEmployeeUserId(Long jobDescriptionId, Long employeeUserId);
    Optional<Referral> findFirstByEmployeeUserIdAndStatusIn(Long employeeUserId, List<ReferralStatus> statuses);
}