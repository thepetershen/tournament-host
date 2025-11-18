package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.Map;

/**
 * DTO for manually setting player seeds
 * Maps user ID to seed number (1 = first seed, 2 = second seed, etc.)
 */
public class ManualSeedingRequest {
    private Map<Long, Integer> playerSeeds;

    public ManualSeedingRequest() {
    }

    public ManualSeedingRequest(Map<Long, Integer> playerSeeds) {
        this.playerSeeds = playerSeeds;
    }

    public Map<Long, Integer> getPlayerSeeds() {
        return playerSeeds;
    }

    public void setPlayerSeeds(Map<Long, Integer> playerSeeds) {
        this.playerSeeds = playerSeeds;
    }
}
