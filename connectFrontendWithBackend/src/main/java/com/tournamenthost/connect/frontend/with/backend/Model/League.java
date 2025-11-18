package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Owner of the league (creator)
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // Users authorized to edit this league
    @ManyToMany
    @JoinTable(
        name = "league_editors",
        joinColumns = @JoinColumn(name = "league_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> authorizedEditors = new HashSet<>();

    // Many-to-many: Tournaments can belong to multiple leagues
    @ManyToMany
    @JoinTable(
        name = "league_tournaments",
        joinColumns = @JoinColumn(name = "league_id"),
        inverseJoinColumns = @JoinColumn(name = "tournament_id")
    )
    private Set<Tournament> tournaments = new HashSet<>();

    // Player rankings within this league
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    private List<LeaguePlayerRanking> playerRankings = new ArrayList<>();

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getAuthorizedEditors() {
        return authorizedEditors;
    }

    public void addAuthorizedEditor(User user) {
        this.authorizedEditors.add(user);
    }

    public void removeAuthorizedEditor(User user) {
        this.authorizedEditors.remove(user);
    }

    public boolean canUserEdit(User user) {
        if (user == null) return false;
        return user.equals(owner) || authorizedEditors.contains(user);
    }

    public Set<Tournament> getTournaments() {
        return tournaments;
    }

    public void addTournament(Tournament tournament) {
        this.tournaments.add(tournament);
    }

    public void removeTournament(Tournament tournament) {
        this.tournaments.remove(tournament);
    }

    public List<LeaguePlayerRanking> getPlayerRankings() {
        return playerRankings;
    }

    public void addPlayerRanking(LeaguePlayerRanking ranking) {
        this.playerRankings.add(ranking);
        ranking.setLeague(this);
    }

    public void removePlayerRanking(LeaguePlayerRanking ranking) {
        this.playerRankings.remove(ranking);
        ranking.setLeague(null);
    }

    // Constructors
    public League() {
        this.tournaments = new HashSet<>();
        this.authorizedEditors = new HashSet<>();
        this.playerRankings = new ArrayList<>();
    }

    public League(String name) {
        this.name = name;
        this.tournaments = new HashSet<>();
        this.authorizedEditors = new HashSet<>();
        this.playerRankings = new ArrayList<>();
    }

    public League(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.tournaments = new HashSet<>();
        this.authorizedEditors = new HashSet<>();
        this.playerRankings = new ArrayList<>();
    }
}
