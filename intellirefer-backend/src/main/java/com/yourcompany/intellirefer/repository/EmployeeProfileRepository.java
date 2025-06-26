package com.yourcompany.intellirefer.repository;

import com.yourcompany.intellirefer.entity.EmployeeProfile;
import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    /**
     * Finds all employee profiles with a specific availability status.
     * This is crucial for the matching engine to only consider available employees.
     * @param availability The availability status to filter by.
     * @return A list of matching EmployeeProfiles.
     */
    List<EmployeeProfile> findByAvailability(AvailabilityStatus availability);

    List<EmployeeProfile> findByAvailabilityIn(List<AvailabilityStatus> busyStatuses);
    @Query("SELECT e FROM EmployeeProfile e WHERE e.availability = 'AVAILABLE' OR " +
            "(e.availability = 'ON_PROJECT' AND e.expectedAvailabilityDate <= :thresholdDate)")
    List<EmployeeProfile> findAvailableOrSoonToBeAvailable(@Param("thresholdDate") LocalDate thresholdDate);
}