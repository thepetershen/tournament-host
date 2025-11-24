package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tournamenthost.connect.frontend.with.backend.Model.Team;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import jakarta.persistence.*;

@Entity
@Table(name = "team_schedules")
public class TeamSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore  // Prevent circular reference: TeamSchedule -> Event -> TeamSchedule
    private RoundRobinEvent event;

    @ManyToMany
    @JoinTable(
        name = "team_schedule_matches",
        joinColumns = @JoinColumn(name = "schedule_id"),
        inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    @JsonIgnore  // Prevent circular reference: TeamSchedule -> Match -> potentially back to schedule
    private Set<Match> matches;

    public Long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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

    public TeamSchedule() {
        this.matches = new HashSet<>();
    }

    public TeamSchedule(Team team, RoundRobinEvent event) {
        this.team = team;
        this.event = event;
        this.matches = new HashSet<>();
    }
}
