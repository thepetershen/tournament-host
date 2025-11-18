package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tournamenthost.connect.frontend.with.backend.Model.MatchType;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type")
public abstract class BaseEvent implements Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    public Long getId(){
        return id;
    }

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private String name;

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @ManyToMany
    @JoinTable(
        name = "user_events",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> players;

    public void addPlayer(User player) {
        players.add(player);
    }

    public void addPlayer(List<User> players) {
        for(User player: players) {
            this.addPlayer(player);
        }
    }

    public List<User> getPlayers(){
        return players;
    }

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    public void setTournament(Tournament tournament){
        this.tournament = tournament;
    }

    public Tournament getTournament(){
        return tournament;
    }

    private boolean initialized;

    public boolean isEventInitialized(){
        return initialized;
    }

    public void initializeEvent(){
        this.initialized = true;
    }

    public void unInitiateEvent(){
        this.initialized =false;
    }

    // Match configuration: Singles vs Doubles
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchType matchType = MatchType.SINGLES;

    // Number of games per match (e.g., 1 for single game, 3 for best-of-3, 5 for best-of-5)
    @Column(nullable = false)
    private int gamesPerMatch = 1;

    // Number of games required to win the match (e.g., 1 for single game, 2 for best-of-3, 3 for best-of-5)
    @Column(nullable = false)
    private int gamesRequiredToWin = 1;

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot change match type after event has been initialized");
        }
        this.matchType = matchType;
    }

    public int getGamesPerMatch() {
        return gamesPerMatch;
    }

    public void setGamesPerMatch(int gamesPerMatch) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot change games per match after event has been initialized");
        }
        this.gamesPerMatch = gamesPerMatch;
        // Auto-calculate gamesRequiredToWin based on gamesPerMatch
        this.gamesRequiredToWin = (gamesPerMatch / 2) + 1;
    }

    public int getGamesRequiredToWin() {
        return gamesRequiredToWin;
    }

    public void setGamesRequiredToWin(int gamesRequiredToWin) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot change games required to win after event has been initialized");
        }
        this.gamesRequiredToWin = gamesRequiredToWin;
    }

    // Seeding system: Maps User ID to their seed number (1 = first seed, 2 = second seed, etc.)
    @ElementCollection
    @CollectionTable(name = "event_seeds", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "seed_number")
    private Map<Long, Integer> playerSeeds;

    // Team seeding system: Maps Team ID to their seed number (for doubles events)
    @ElementCollection
    @CollectionTable(name = "event_team_seeds", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "team_id")
    @Column(name = "seed_number")
    private Map<Long, Integer> teamSeeds;

    public Map<Long, Integer> getPlayerSeeds() {
        return playerSeeds;
    }

    public void setPlayerSeeds(Map<Long, Integer> playerSeeds) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set seeds after event has been initialized");
        }
        this.playerSeeds = playerSeeds;
    }

    public void setPlayerSeed(Long userId, Integer seed) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set seeds after event has been initialized");
        }
        this.playerSeeds.put(userId, seed);
    }

    public Integer getPlayerSeed(Long userId) {
        return playerSeeds.get(userId);
    }

    public Map<Long, Integer> getTeamSeeds() {
        return teamSeeds;
    }

    public void setTeamSeeds(Map<Long, Integer> teamSeeds) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set seeds after event has been initialized");
        }
        this.teamSeeds = teamSeeds;
    }

    public void setTeamSeed(Long teamId, Integer seed) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set seeds after event has been initialized");
        }
        this.teamSeeds.put(teamId, seed);
    }

    public Integer getTeamSeed(Long teamId) {
        return teamSeeds.get(teamId);
    }

    public boolean isPlayerSeeded(Long userId) {
        return playerSeeds.containsKey(userId);
    }

    public void clearSeeds() {
        if (this.initialized) {
            throw new IllegalStateException("Cannot clear seeds after event has been initialized");
        }
        this.playerSeeds.clear();
    }

    // Registration system: Tracks pending signups for this event
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations;

    public List<EventRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<EventRegistration> registrations) {
        this.registrations = registrations;
    }

    public void addRegistration(EventRegistration registration) {
        this.registrations.add(registration);
        registration.setEvent(this);
    }

    public BaseEvent() {
        // Initialize collections to avoid NullPointerException
        this.players = new ArrayList<>();
        this.playerSeeds = new HashMap<>();
        this.registrations = new ArrayList<>();
        this.initialized = false;
    }

    // Optionally, add a constructor with arguments if needed
    public BaseEvent(String name, List<User> players, Tournament tournament, int index) {
        this.initialized =false;
        this.name = name;
        this.players = (players != null) ? players : new ArrayList<>();
        this.playerSeeds = new HashMap<>();
        this.teamSeeds = new HashMap<>();
        this.registrations = new ArrayList<>();
        this.tournament = tournament;
        this.index = index;
    }


    
}
