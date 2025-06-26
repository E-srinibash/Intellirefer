package com.yourcompany.intellirefer.entity;

import com.yourcompany.intellirefer.model.enums.ReferralStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "referrals",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_description_id", "employee_user_id"})
        }
)
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_user_id", nullable = false)
    private EmployeeProfile employee;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "matching_skills")
    private String matchingSkills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status = ReferralStatus.PENDING_REVIEW;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}