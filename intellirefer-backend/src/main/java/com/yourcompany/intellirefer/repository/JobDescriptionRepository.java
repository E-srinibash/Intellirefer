package com.yourcompany.intellirefer.repository;

import com.yourcompany.intellirefer.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {

    /**
     * Finds all job descriptions uploaded by a specific manager.
     * This will be used for the manager's dashboard.
     * @param managerId The ID of the manager who uploaded the JDs.
     * @return A list of JobDescriptions.
     */
    List<JobDescription> findByUploadedByManagerId(Long managerId);
}