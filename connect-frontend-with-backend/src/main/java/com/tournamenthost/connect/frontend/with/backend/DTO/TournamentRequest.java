package com.tournamenthost.connect.frontend.with.backend.DTO;

public class TournamentRequest {
    private String name;

    public TournamentRequest() {}

    public TournamentRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}