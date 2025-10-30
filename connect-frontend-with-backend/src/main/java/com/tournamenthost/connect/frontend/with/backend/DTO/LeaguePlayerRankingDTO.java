package com.tournamenthost.connect.frontend.with.backend.DTO;

public class LeaguePlayerRankingDTO {
    private Long id;
    private UserDTO player;
    private Integer rank;
    private Integer points;
    private Integer matchesPlayed;
    private Integer matchesWon;
    private Integer matchesLost;

    public LeaguePlayerRankingDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getPlayer() {
        return player;
    }

    public void setPlayer(UserDTO player) {
        this.player = player;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(Integer matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public Integer getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(Integer matchesWon) {
        this.matchesWon = matchesWon;
    }

    public Integer getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(Integer matchesLost) {
        this.matchesLost = matchesLost;
    }
}
