package com.tournamenthost.connect.frontend.with.backend.DTO;

public class UserGetRequest {
    private Long id;

    public UserGetRequest() {}

    public UserGetRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}