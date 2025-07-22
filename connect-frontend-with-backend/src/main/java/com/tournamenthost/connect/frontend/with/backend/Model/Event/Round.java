package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import jakarta.persistence.*;
import java.util.List;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;

@Entity
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Match> matches;

    // Optionally, you can add a round number or name
    private int roundNumber;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private SingleElimEvent event;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public SingleElimEvent getEvent() {
        return event;
    }

    public void setEvent(SingleElimEvent event) {
        this.event = event;
    }
}
