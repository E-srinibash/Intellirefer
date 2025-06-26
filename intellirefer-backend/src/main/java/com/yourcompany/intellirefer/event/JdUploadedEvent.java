package com.yourcompany.intellirefer.event;

import org.springframework.context.ApplicationEvent;

/**
 * A custom event that is published when a new Job Description is successfully created and committed.
 */
public class JdUploadedEvent extends ApplicationEvent {

    private final Long jobDescriptionId;

    public JdUploadedEvent(Object source, Long jobDescriptionId) {
        super(source);
        this.jobDescriptionId = jobDescriptionId;
    }

    public Long getJobDescriptionId() {
        return jobDescriptionId;
    }
}