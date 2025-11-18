package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "league_player_rankings")
public class LeaguePlayerRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    // Ranking position (1 = first place, 2 = second place, etc.)
    @Column(name = "rank", nullable = false)
    private Integer rank;

    // Points accumulated in the league
    @Column(name = "points")
    private Integer points;

    // Number of matches played in this league
    @Column(name = "matches_played")
    private Integer matchesPlayed;

    // Number of matches won in this league
    @Column(name = "matches_won")
    private Integer matchesWon;

    // Number of matches lost in this league
    @Column(name = "matches_lost")
    private Integer matchesLost;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
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

    // Constructors
    public LeaguePlayerRanking() {
        this.points = 0;
        this.matchesPlayed = 0;
        this.matchesWon = 0;
        this.matchesLost = 0;
    }

    public LeaguePlayerRanking(League league, User player, Integer rank) {
        this.league = league;
        this.player = player;
        this.rank = rank;
        this.points = 0;
        this.matchesPlayed = 0;
        this.matchesWon = 0;
        this.matchesLost = 0;
    }

    public LeaguePlayerRanking(League league, User player, Integer rank, Integer points) {
        this.league = league;
        this.player = player;
        this.rank = rank;
        this.points = points;
        this.matchesPlayed = 0;
        this.matchesWon = 0;
        this.matchesLost = 0;
    }
}
