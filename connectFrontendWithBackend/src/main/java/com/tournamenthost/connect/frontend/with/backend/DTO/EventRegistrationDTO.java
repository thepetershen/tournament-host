package com.tournamenthost.connect.frontend.with.backend.DTO;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventRegistration;

import java.util.Date;

/**
 * DTO for event registration information
 */
public class EventRegistrationDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private UserDTO user;
    private String status;
    private Date registeredAt;
    private Date reviewedAt;
    private UserDTO reviewedBy;
    private String desiredPartner;

    public EventRegistrationDTO() {
    }

    public EventRegistrationDTO(EventRegistration registration) {
        this.id = registration.getId();
        this.eventId = registration.getEvent().getId();
        this.eventName = registration.getEvent().getName();
        this.status = registration.getStatus().name();
        this.registeredAt = registration.getRegisteredAt();
        this.reviewedAt = registration.getReviewedAt();
        this.desiredPartner = registration.getDesiredPartner();

        // Map user - create simple DTO without tournaments to avoid circular references
        if (registration.getUser() != null) {
            this.user = new UserDTO();
            this.user.setId(registration.getUser().getId());
            this.user.setUsername(registration.getUser().getUsername());
            this.user.setName(registration.getUser().getName());
            this.user.setTournaments(null); // Explicitly set to null to avoid serialization issues
        }

        // Map reviewer - create simple DTO without tournaments to avoid circular references
        if (registration.getReviewedBy() != null) {
            this.reviewedBy = new UserDTO();
            this.reviewedBy.setId(registration.getReviewedBy().getId());
            this.reviewedBy.setUsername(registration.getReviewedBy().getUsername());
            this.reviewedBy.setName(registration.getReviewedBy().getName());
            this.reviewedBy.setTournaments(null); // Explicitly set to null to avoid serialization issues
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Date registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Date getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Date reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public UserDTO getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UserDTO reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getDesiredPartner() {
        return desiredPartner;
    }

    public void setDesiredPartner(String desiredPartner) {
        this.desiredPartner = desiredPartner;
    }
}
