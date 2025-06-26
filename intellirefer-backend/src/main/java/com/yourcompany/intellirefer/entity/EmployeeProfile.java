package com.yourcompany.intellirefer.entity;

import com.yourcompany.intellirefer.model.enums.AvailabilityStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "employee_profiles")
public class EmployeeProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * This establishes a one-to-one relationship where the primary key of this table
     * is also a foreign key to the 'users' table.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This annotation tells JPA that the 'userId' field is the primary key and is mapped from the User entity.
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

//    @Column(name = "resume_s3_key", unique = true)
//    private String resumeS3Key;

    @Column(name = "resume_file_path", unique = true) // <-- RENAMED from resume_s3_key
    private String resumeFilePath; // <-- RENAMED from resumeS3Key

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availability = AvailabilityStatus.AVAILABLE;

    @Column(name = "job_level", length = 50)
    private String jobLevel;

    @Column(name = "current_role", length = 100)
    private String currentRole;

    @Column(name = "expected_availability_date")
    private LocalDate expectedAvailabilityDate;

    /**
     * This sets up the many-to-many relationship with Skill.
     * JPA will automatically manage the 'employee_skills' join table for us.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "employee_skills",
            joinColumns = @JoinColumn(name = "employee_user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
