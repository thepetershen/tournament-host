package com.tournamenthost.connect.frontend.with.backend.DTO;

/**
 * Request DTO for guest signup to events
 * Allows unauthenticated users to sign up by providing only their full name
 */
public class GuestSignupRequest {
    private String fullName;
    private String desiredPartner; // Optional, for doubles events

    public GuestSignupRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDesiredPartner() {
        return desiredPartner;
    }

    public void setDesiredPartner(String desiredPartner) {
        this.desiredPartner = desiredPartner;
    }
}
