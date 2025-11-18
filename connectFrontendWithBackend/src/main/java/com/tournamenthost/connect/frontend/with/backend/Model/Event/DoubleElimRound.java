package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import jakarta.persistence.*;
import java.util.List;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;

/**
 * Represents a round in a Double Elimination tournament
 * Can belong to either Winners Bracket or Losers Bracket
 */
@Entity
public class DoubleElimRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderColumn(name = "match_position")
    private List<Match> matches;

    /**
     * Round number within its bracket
     * Winners Bracket: 0 (first round), 1 (quarters), 2 (semis), 3 (finals)
     * Losers Bracket: 0, 1, 2, 3, 4, 5... (more rounds than winners)
     */
    private int roundNumber;

    /**
     * Which bracket this round belongs to
     */
    @Enumerated(EnumType.STRING)
    private BracketType bracketType;

    /**
     * Reference to the parent event
     */
    @ManyToOne
    @JoinColumn(name = "event_id")
    private DoubleElimEvent event;

    /**
     * For feed-in tracking: Which winners round feeds into this losers round
     * Only relevant for losers bracket rounds that receive feed-ins
     * -1 means no feed-in for this round
     */
    private int feedsFromWinnersRound;

    // Constructors
    public DoubleElimRound() {
        this.feedsFromWinnersRound = -1;
    }

    public DoubleElimRound(int roundNumber, BracketType bracketType) {
        this.roundNumber = roundNumber;
        this.bracketType = bracketType;
        this.feedsFromWinnersRound = -1;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public BracketType getBracketType() {
        return bracketType;
    }

    public void setBracketType(BracketType bracketType) {
        this.bracketType = bracketType;
    }

    public DoubleElimEvent getEvent() {
        return event;
    }

    public void setEvent(DoubleElimEvent event) {
        this.event = event;
    }

    public int getFeedsFromWinnersRound() {
        return feedsFromWinnersRound;
    }

    public void setFeedsFromWinnersRound(int feedsFromWinnersRound) {
        this.feedsFromWinnersRound = feedsFromWinnersRound;
    }

    /**
     * Check if this is a feed-in round (losers bracket receiving winners bracket losers)
     */
    public boolean isFeedInRound() {
        return bracketType == BracketType.LOSERS && feedsFromWinnersRound >= 0;
    }

    /**
     * Get a human-readable name for this round
     */
    public String getRoundName() {
        if (bracketType == BracketType.WINNERS) {
            return getWinnersRoundName(roundNumber);
        } else {
            return "Losers Round " + (roundNumber + 1);
        }
    }

    /**
     * Get standard names for winners bracket rounds
     */
    private String getWinnersRoundName(int round) {
        int matchCount = matches != null ? matches.size() : 0;

        // Work backwards from finals
        if (matchCount == 1) return "Finals";
        if (matchCount == 2) return "Semifinals";
        if (matchCount == 4) return "Quarterfinals";
        if (matchCount == 8) return "Round of 16";
        if (matchCount == 16) return "Round of 32";

        // Generic naming
        return "Winners Round " + (round + 1);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%d matches)",
            bracketType,
            getRoundName(),
            matches != null ? matches.size() : 0);
    }
}