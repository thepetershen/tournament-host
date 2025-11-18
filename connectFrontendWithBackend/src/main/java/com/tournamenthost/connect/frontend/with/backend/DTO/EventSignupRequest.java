package com.tournamenthost.connect.frontend.with.backend.DTO;

/**
 * Request DTO for event signup
 * Allows users to specify a desired partner for doubles events
 */
public class EventSignupRequest {
    private String desiredPartner;

    public EventSignupRequest() {
    }

    public String getDesiredPartner() {
        return desiredPartner;
    }

    public void setDesiredPartner(String desiredPartner) {
        this.desiredPartner = desiredPartner;
    }
}
