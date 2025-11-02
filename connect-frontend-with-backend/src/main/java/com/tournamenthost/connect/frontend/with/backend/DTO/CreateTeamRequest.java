package com.tournamenthost.connect.frontend.with.backend.DTO;

/**
 * Request DTO for creating a team from registrations
 */
public class CreateTeamRequest {
    private Long player1Id;
    private Long player2Id;

    public CreateTeamRequest() {
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
    }
}
