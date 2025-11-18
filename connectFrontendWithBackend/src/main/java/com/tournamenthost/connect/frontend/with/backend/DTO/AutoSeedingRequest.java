package com.tournamenthost.connect.frontend.with.backend.DTO;

/**
 * DTO for automatically seeding players from league rankings
 */
public class AutoSeedingRequest {
    private int numberOfSeeds;

    public AutoSeedingRequest() {
    }

    public AutoSeedingRequest(int numberOfSeeds) {
        this.numberOfSeeds = numberOfSeeds;
    }

    public int getNumberOfSeeds() {
        return numberOfSeeds;
    }

    public void setNumberOfSeeds(int numberOfSeeds) {
        this.numberOfSeeds = numberOfSeeds;
    }
}
