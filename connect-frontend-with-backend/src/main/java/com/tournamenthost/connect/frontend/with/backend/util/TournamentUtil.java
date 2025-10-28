package com.tournamenthost.connect.frontend.with.backend.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tournamenthost.connect.frontend.with.backend.Model.User;

public class TournamentUtil {
    public static int nextPowerOfTwo(int n) {
        if (n < 1) return 1;
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }

    /**
     * Generate a tournament draw with proper seeding
     * @param players List of all players
     * @param matchAmount Number of matches in the first round (bracket size / 2)
     * @param playerSeeds Map of user ID to seed number (1 = first seed, 2 = second, etc.)
     * @return Ordered list of players for bracket placement (playerA, playerB, playerA, playerB, ...)
     */
    public static ArrayList<User> generateDrawUsingSeeding(List<User> players, int matchAmount, Map<Long, Integer> playerSeeds) {
        int bracketSize = matchAmount * 2;

        // Create position array - index represents position in bracket (0 to bracketSize-1)
        User[] positions = new User[bracketSize];

        // Get standard seeding positions for power-of-2 bracket
        int[] seedPositions = generateStandardSeedPositions(bracketSize);

        // Separate seeded and unseeded players
        List<User> seededPlayers = new ArrayList<>();
        List<User> unseededPlayers = new ArrayList<>();

        // Create inverse map: seed number -> user
        Map<Integer, User> seedToUser = new HashMap<>();
        for (User player : players) {
            Integer seed = playerSeeds.get(player.getId());
            if (seed != null && seed > 0) {
                seededPlayers.add(player);
                seedToUser.put(seed, player);
            } else {
                unseededPlayers.add(player);
            }
        }

        // Shuffle unseeded players for randomness
        Collections.shuffle(unseededPlayers);

        // If no players are seeded, treat all players as if they were seeded 1-N
        // and byes as seeds (N+1) to bracketSize
        if (seededPlayers.isEmpty()) {
            // Assign unseeded players to seed positions 1 through totalPlayers
            for (int i = 0; i < unseededPlayers.size(); i++) {
                int seed = i + 1;
                int position = seedPositions[seed - 1];
                positions[position] = unseededPlayers.get(i);
            }

            // Byes occupy the remaining seed positions (totalPlayers+1 to bracketSize)
            // These are left as null
        } else {
            // Place seeded players first
            for (int seed = 1; seed <= seededPlayers.size(); seed++) {
                User player = seedToUser.get(seed);
                if (player != null && seed - 1 < seedPositions.length) {
                    positions[seedPositions[seed - 1]] = player;
                }
            }

            // Fill remaining positions with unseeded players, then byes
            int unseededIndex = 0;

            // First, find all empty positions and sort them by seed priority
            List<Integer> emptyPositions = new ArrayList<>();
            for (int seed = 1; seed <= bracketSize; seed++) {
                int position = seedPositions[seed - 1];
                if (positions[position] == null) {
                    emptyPositions.add(position);
                }
            }

            // Fill empty positions with unseeded players first, then leave rest as byes
            for (int position : emptyPositions) {
                if (unseededIndex < unseededPlayers.size()) {
                    positions[position] = unseededPlayers.get(unseededIndex++);
                }
                // else leave as null (bye)
            }
        }

        // Convert to answer format (playerA, playerB, playerA, playerB, ...)
        ArrayList<User> answer = new ArrayList<>();
        for (int i = 0; i < bracketSize; i++) {
            answer.add(positions[i]);
        }

        return answer;
    }

    /**
     * Generate standard seeding positions for a bracket
     * Returns array where index is (seed-1) and value is bracket position
     *
     * Standard pattern ensures:
     * - Seed 1 vs Seed 8 (positions 0-1)
     * - Seed 4 vs Seed 5 (positions 2-3)
     * - Seed 2 vs Seed 7 (positions 4-5)
     * - Seed 3 vs Seed 6 (positions 6-7)
     */
    private static int[] generateStandardSeedPositions(int bracketSize) {
        int[] positions = new int[bracketSize];

        // Standard tournament seeding using fair pairing
        // Start with seed 1, then interleave remaining seeds
        List<Integer> seeds = new ArrayList<>();
        seeds.add(1);
        seeds.add(2);

        // Recursively insert seeds to maintain proper spacing
        while (seeds.size() < bracketSize) {
            List<Integer> newSeeds = new ArrayList<>();
            int nextSeed = seeds.size() + 1;

            for (int i = 0; i < seeds.size(); i++) {
                newSeeds.add(seeds.get(i));
                newSeeds.add(nextSeed + seeds.size() - 1 - i);
            }
            seeds = newSeeds;
        }

        // Map each seed to its bracket position
        for (int i = 0; i < seeds.size(); i++) {
            int seed = seeds.get(i);
            positions[seed - 1] = i;
        }

        return positions;
    }
}
