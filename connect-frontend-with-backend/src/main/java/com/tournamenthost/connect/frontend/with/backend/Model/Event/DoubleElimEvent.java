package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import java.util.ArrayList;
import java.util.List;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

/**
 * Double Elimination Event with Full Feed-in
 *
 * Structure:
 * - Winners Bracket: Players who haven't lost yet. Winner of this bracket wins.
 * - Losers Bracket: Players who lost once (get a second chance). Winner of this bracket places second.
 *
 * Feed-in Rules:
 * - Early rounds: When you lose in Winners Bracket, you drop into Losers Bracket
 * - Late rounds (semifinals and beyond): No dropping - if you lose, you're eliminated
 *
 * No Grand Finals - both brackets play out independently to determine 1st and 2nd place.
 */
@Entity
@DiscriminatorValue("DOUBLE_ELIM")
public class DoubleElimEvent extends BaseEvent {

    /**
     * Winners Bracket rounds (upper bracket)
     * Round 0: First round (e.g., 8 matches for 16 players)
     * Round 1: Quarterfinals
     * Round 2: Semifinals
     * Round 3: Finals
     *
     * Winner of this bracket = 1st place
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoubleElimRound> winnersBracket;

    /**
     * Losers Bracket rounds (lower bracket)
     * Receives players who lose in Winners Bracket (until cutoff point)
     * Has approximately 2x the rounds of winners bracket
     *
     * Winner of this bracket = 2nd place
     *
     * Example for 8 players:
     * - Winners Bracket: 3 rounds (QF, SF, F)
     * - Losers Bracket: 5 rounds (LR1, LR2, LR3, LR4, LF)
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoubleElimRound> losersBracket;

    /**
     * The round number in Winners Bracket after which players no longer drop to Losers Bracket
     * For example, if feedInCutoffRound = 2:
     * - Rounds 0, 1 losers drop to Losers Bracket
     * - Round 2+ (semifinals, finals) losers are eliminated
     *
     * Typically set to (totalWinnersRounds - 2) so semifinals and finals don't feed in
     */
    private int feedInCutoffRound;

    // Constructors
    public DoubleElimEvent() {
        super();
        this.winnersBracket = new ArrayList<>();
        this.losersBracket = new ArrayList<>();
        this.feedInCutoffRound = -1; // -1 means not set yet, will be calculated on initialization
    }

    public DoubleElimEvent(String name, List<User> players, Tournament tournament, int index) {
        super(name, players, tournament, index);
        this.winnersBracket = new ArrayList<>();
        this.losersBracket = new ArrayList<>();
        this.feedInCutoffRound = -1;
    }

    // Winners Bracket methods
    public void addWinnersRound(DoubleElimRound round) {
        round.setBracketType(BracketType.WINNERS);
        winnersBracket.add(round);
    }

    public void addWinnersRounds(List<DoubleElimRound> rounds) {
        for (DoubleElimRound round : rounds) {
            addWinnersRound(round);
        }
    }

    public List<DoubleElimRound> getWinnersBracket() {
        return winnersBracket;
    }

    /**
     * Replace winners bracket rounds (safe for orphanRemoval)
     * Clears existing and adds new rounds to maintain JPA collection tracking
     */
    public void setWinnersBracket(List<DoubleElimRound> winnersBracket) {
        if (this.winnersBracket == null) {
            this.winnersBracket = new ArrayList<>();
        }
        this.winnersBracket.clear();
        if (winnersBracket != null) {
            for (DoubleElimRound round : winnersBracket) {
                addWinnersRound(round);
            }
        }
    }

    // Losers Bracket methods
    public void addLosersRound(DoubleElimRound round) {
        round.setBracketType(BracketType.LOSERS);
        losersBracket.add(round);
    }

    public void addLosersRounds(List<DoubleElimRound> rounds) {
        for (DoubleElimRound round : rounds) {
            addLosersRound(round);
        }
    }

    public List<DoubleElimRound> getLosersBracket() {
        return losersBracket;
    }

    /**
     * Replace losers bracket rounds (safe for orphanRemoval)
     * Clears existing and adds new rounds to maintain JPA collection tracking
     */
    public void setLosersBracket(List<DoubleElimRound> losersBracket) {
        if (this.losersBracket == null) {
            this.losersBracket = new ArrayList<>();
        }
        this.losersBracket.clear();
        if (losersBracket != null) {
            for (DoubleElimRound round : losersBracket) {
                addLosersRound(round);
            }
        }
    }

    // Feed-in cutoff methods
    public int getFeedInCutoffRound() {
        return feedInCutoffRound;
    }

    public void setFeedInCutoffRound(int feedInCutoffRound) {
        this.feedInCutoffRound = feedInCutoffRound;
    }

    /**
     * Calculate and set the feed-in cutoff round
     * Typically set to 2 rounds before the end (so semifinals and finals don't feed in)
     */
    public void calculateFeedInCutoff() {
        if (winnersBracket.isEmpty()) {
            this.feedInCutoffRound = 0;
        } else {
            // Don't allow feed-in for last 2 rounds (semifinals and finals)
            this.feedInCutoffRound = Math.max(0, winnersBracket.size() - 2);
        }
    }

    /**
     * Check if a winners bracket round should feed into losers bracket
     * @param winnersRoundNumber The round number in winners bracket (0-indexed)
     * @return true if losers from this round drop to losers bracket
     */
    public boolean shouldFeedIntoLosersBracket(int winnersRoundNumber) {
        if (feedInCutoffRound == -1) {
            calculateFeedInCutoff();
        }
        return winnersRoundNumber < feedInCutoffRound;
    }

    /**
     * Get all rounds organized by bracket
     */
    public List<List<DoubleElimRound>> getAllBrackets() {
        List<List<DoubleElimRound>> brackets = new ArrayList<>();
        brackets.add(winnersBracket);
        brackets.add(losersBracket);
        return brackets;
    }

    /**
     * Get total number of rounds across both brackets
     */
    public int getTotalRounds() {
        return winnersBracket.size() + losersBracket.size();
    }

    /**
     * Get a human-readable description of the bracket structure
     */
    public String getBracketStructureDescription() {
        return String.format(
            "Double Elimination: %d Winners Rounds, %d Losers Rounds (Feed-in cutoff: Round %d) - Total: %d rounds for %d players",
            winnersBracket.size(),
            losersBracket.size(),
            feedInCutoffRound,
            getTotalRounds(),
            getPlayers() != null ? getPlayers().size() : 0
        );
    }
}