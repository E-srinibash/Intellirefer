package com.yourcompany.intellirefer.entity;

import com.yourcompany.intellirefer.model.enums.JdStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "client_name")
    private String clientName;

//    @Column(name = "jd_s3_key", nullable = false, unique = true)
//    private String jdS3Key;


    @Column(name = "jd_file_path", nullable = false, unique = true) // <-- RENAMED from jd_s3_key
    private String jdFilePath; // <-- RENAMED from jdS3Key


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JdStatus status = JdStatus.OPEN;

    @Column(name = "required_experience")
    private Integer requiredExperience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_manager_id", nullable = false)
    private User uploadedByManager;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}