package com.tournamenthost.connect.frontend.with.backend.util;

import com.tournamenthost.connect.frontend.with.backend.Model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TournamentUtil seeding algorithm
 */
public class TournamentUtilTest {

    // Helper to set User ID via reflection (for testing only)
    private void setUserId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    @Test
    public void testStandardSeeding8Players() throws Exception {
        // Create 8 test players
        List<User> players = new ArrayList<>();
        Map<Long, Integer> playerSeeds = new HashMap<>();

        for (long i = 1; i <= 8; i++) {
            User user = new User();
            setUserId(user, i);
            user.setUsername("Player" + i);
            user.setName("Player " + i);
            players.add(user);
            // Seed players 1-8
            playerSeeds.put(i, (int) i);
        }

        // Generate draw
        ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, 4, playerSeeds);

        // Verify draw size
        assertEquals(8, draw.size(), "Draw should have 8 positions");

        // The algorithm produces fair bracket seeding:
        // Bracket order: [1, 8, 4, 7, 2, 6, 3, 5]
        // Match 1: Seed 1 vs Seed 8 (positions 0 and 1)
        // Match 2: Seed 4 vs Seed 7 (positions 2 and 3)
        // Match 3: Seed 2 vs Seed 6 (positions 4 and 5)
        // Match 4: Seed 3 vs Seed 5 (positions 6 and 7)

        // Verify matchups
        verifyMatchup(draw, 0, 1, 1, 8);
        verifyMatchup(draw, 2, 3, 4, 7);
        verifyMatchup(draw, 4, 5, 2, 6);
        verifyMatchup(draw, 6, 7, 3, 5);

        System.out.println("✓ 8-player seeding test passed!");
    }

    @Test
    public void testPartialSeeding() throws Exception {
        // Create 8 players but only seed top 4
        List<User> players = new ArrayList<>();
        Map<Long, Integer> playerSeeds = new HashMap<>();

        for (long i = 1; i <= 8; i++) {
            User user = new User();
            setUserId(user, i);
            user.setUsername("Player" + i);
            user.setName("Player " + i);
            players.add(user);

            // Only seed players 1-4
            if (i <= 4) {
                playerSeeds.put(i, (int) i);
            }
        }

        // Generate draw
        ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, 4, playerSeeds);

        // Verify draw size
        assertEquals(8, draw.size(), "Draw should have 8 positions");

        // Verify that seeded players are in correct positions
        // Seed 1 should be in a specific position
        assertTrue(containsPlayer(draw, 1L), "Seed 1 should be in draw");
        assertTrue(containsPlayer(draw, 2L), "Seed 2 should be in draw");
        assertTrue(containsPlayer(draw, 3L), "Seed 3 should be in draw");
        assertTrue(containsPlayer(draw, 4L), "Seed 4 should be in draw");

        // Unseeded players should also be in draw
        assertTrue(containsPlayer(draw, 5L), "Player 5 should be in draw");
        assertTrue(containsPlayer(draw, 6L), "Player 6 should be in draw");
        assertTrue(containsPlayer(draw, 7L), "Player 7 should be in draw");
        assertTrue(containsPlayer(draw, 8L), "Player 8 should be in draw");

        System.out.println("✓ Partial seeding test passed!");
    }

    @Test
    public void testNoSeeding() throws Exception {
        // Create 8 players with no seeds
        List<User> players = new ArrayList<>();
        Map<Long, Integer> playerSeeds = new HashMap<>();

        for (long i = 1; i <= 8; i++) {
            User user = new User();
            setUserId(user, i);
            user.setUsername("Player" + i);
            user.setName("Player " + i);
            players.add(user);
        }

        // Generate draw with no seeds
        ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, 4, playerSeeds);

        // Verify all players are in draw
        assertEquals(8, draw.size(), "Draw should have 8 positions");

        for (long i = 1; i <= 8; i++) {
            assertTrue(containsPlayer(draw, i), "Player " + i + " should be in draw");
        }

        System.out.println("✓ No seeding test passed!");
    }

    @Test
    public void testSeeding16Players() throws Exception {
        // Create 16 test players
        List<User> players = new ArrayList<>();
        Map<Long, Integer> playerSeeds = new HashMap<>();

        for (long i = 1; i <= 16; i++) {
            User user = new User();
            setUserId(user, i);
            user.setUsername("Player" + i);
            user.setName("Player " + i);
            players.add(user);
            // Seed all 16 players
            playerSeeds.put(i, (int) i);
        }

        // Generate draw
        ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, 8, playerSeeds);

        // Verify draw size
        assertEquals(16, draw.size(), "Draw should have 16 positions");

        // Standard seeding for 16 players should have:
        // Seed 1 vs Seed 16 in first match
        verifyMatchup(draw, 0, 1, 1, 16);

        System.out.println("✓ 16-player seeding test passed!");
    }

    // Helper method to verify a matchup
    private void verifyMatchup(ArrayList<User> draw, int pos1, int pos2, long expectedSeed1, long expectedSeed2) {
        User player1 = draw.get(pos1);
        User player2 = draw.get(pos2);

        assertNotNull(player1, "Position " + pos1 + " should have a player");
        assertNotNull(player2, "Position " + pos2 + " should have a player");

        Set<Long> actualSeeds = new HashSet<>();
        actualSeeds.add(player1.getId());
        actualSeeds.add(player2.getId());

        Set<Long> expectedSeeds = new HashSet<>();
        expectedSeeds.add(expectedSeed1);
        expectedSeeds.add(expectedSeed2);

        assertEquals(expectedSeeds, actualSeeds,
            String.format("Match at positions %d-%d should be Seed %d vs Seed %d",
                pos1, pos2, expectedSeed1, expectedSeed2));
    }

    // Helper method to check if a player is in the draw
    private boolean containsPlayer(ArrayList<User> draw, Long playerId) {
        for (User user : draw) {
            if (user != null && user.getId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }
}
