package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.List;

/**
 * DTO for approving multiple registrations at once
 */
public class ApproveRegistrationsRequest {
    private List<Long> registrationIds;

    public ApproveRegistrationsRequest() {
    }

    public ApproveRegistrationsRequest(List<Long> registrationIds) {
        this.registrationIds = registrationIds;
    }

    public List<Long> getRegistrationIds() {
        return registrationIds;
    }

    public void setRegistrationIds(List<Long> registrationIds) {
        this.registrationIds = registrationIds;
    }
}
