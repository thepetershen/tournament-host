package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import com.tournamenthost.connect.frontend.with.backend.Model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

/**
 * Represents a player's registration request for an event
 * Players can sign up, but must be approved by tournament owners/moderators
 */
@Entity
@Table(name = "event_registrations")
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private BaseEvent event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @CreationTimestamp
    @Column(updatable = false, name = "registered_at")
    private Date registeredAt;

    @Column(name = "reviewed_at")
    private Date reviewedAt;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "desired_partner")
    private String desiredPartner;

    public EventRegistration() {
        this.status = RegistrationStatus.PENDING;
    }

    public EventRegistration(BaseEvent event, User user) {
        this.event = event;
        this.user = user;
        this.status = RegistrationStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BaseEvent getEvent() {
        return event;
    }

    public void setEvent(BaseEvent event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
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

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getDesiredPartner() {
        return desiredPartner;
    }

    public void setDesiredPartner(String desiredPartner) {
        this.desiredPartner = desiredPartner;
    }

    public enum RegistrationStatus {
        PENDING,    // Waiting for approval
        APPROVED,   // Approved and added to event
        REJECTED    // Rejected by moderator
    }
}
