package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import java.util.*;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import jakarta.persistence.*;

@Entity
@Table(name = "player_schedules")
public class PlayerSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private User player;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private RoundRobinEvent event;

    @ManyToMany
    @JoinTable(
        name = "schedule_matches",
        joinColumns = @JoinColumn(name = "schedule_id"),
        inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private Set<Match> matches;

    public Long getId() {
        return id;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public RoundRobinEvent getEvent() {
        return event;
    }

    public void setEvent(RoundRobinEvent event) {
        this.event = event;
    }

    public Set<Match> getMatches() {
        return matches;
    }

    public void setMatches(Set<Match> matches) {
        this.matches = matches;
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public PlayerSchedule() {
        this.matches = new HashSet<>();
    }

    public PlayerSchedule(User player, RoundRobinEvent event) {
        this.player = player;
        this.event = event;
        this.matches = new HashSet<>();
    }
}