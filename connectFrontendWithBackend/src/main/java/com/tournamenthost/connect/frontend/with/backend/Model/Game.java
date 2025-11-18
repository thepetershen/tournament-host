package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(nullable = false)
    private int gameNumber;

    @Column(nullable = false)
    private int teamAScore;

    @Column(nullable = false)
    private int teamBScore;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    // Constructors
    public Game() {
    }

    public Game(Match match, int gameNumber) {
        this.match = match;
        this.gameNumber = gameNumber;
        this.teamAScore = 0;
        this.teamBScore = 0;
    }

    public Game(Match match, int gameNumber, int teamAScore, int teamBScore) {
        this.match = match;
        this.gameNumber = gameNumber;
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        determineWinner();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    public int getTeamAScore() {
        return teamAScore;
    }

    public void setTeamAScore(int teamAScore) {
        this.teamAScore = teamAScore;
        determineWinner();
    }

    public int getTeamBScore() {
        return teamBScore;
    }

    public void setTeamBScore(int teamBScore) {
        this.teamBScore = teamBScore;
        determineWinner();
    }

    public Team getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(Team winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    /**
     * Set scores for both teams and determine winner
     */
    public void setScores(int teamAScore, int teamBScore) {
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        determineWinner();
    }

    /**
     * Automatically determine the winner based on scores
     */
    private void determineWinner() {
        if (match == null) return;

        if (teamAScore > teamBScore) {
            this.winnerTeam = match.getTeamA();
        } else if (teamBScore > teamAScore) {
            this.winnerTeam = match.getTeamB();
        } else {
            this.winnerTeam = null; // Tie or no score yet
        }
    }
}
