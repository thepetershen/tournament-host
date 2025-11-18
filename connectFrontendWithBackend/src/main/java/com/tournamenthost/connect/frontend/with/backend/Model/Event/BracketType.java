package com.tournamenthost.connect.frontend.with.backend.Model.Event;

/**
 * Enum representing the type of bracket in a Double Elimination tournament
 */
public enum BracketType {
    /**
     * Winners Bracket (Upper Bracket)
     * Players who haven't lost yet
     * Winner of this bracket takes 1st place
     */
    WINNERS,

    /**
     * Losers Bracket (Lower Bracket)
     * Players who lost once in Winners Bracket (until cutoff round)
     * Winner of this bracket takes 5th
     */
    LOSERS
}