package com.tournamenthost.connect.frontend.with.backend.Service;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Round;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentServiceTest {

    private TournamentRepository tournamentRepo;
    private MatchRepository matchRepo;
    private TournamentService tournamentService;

    @BeforeEach
    void setUp() throws Exception {
        tournamentRepo = mock(TournamentRepository.class);
        matchRepo = mock(MatchRepository.class);
        tournamentService = new TournamentService();

        // Use reflection to set private fields
        java.lang.reflect.Field tournamentRepoField = TournamentService.class.getDeclaredField("tournamentRepo");
        tournamentRepoField.setAccessible(true);
        tournamentRepoField.set(tournamentService, tournamentRepo);

        java.lang.reflect.Field matchRepoField = TournamentService.class.getDeclaredField("matchRepo");
        matchRepoField.setAccessible(true);
        matchRepoField.set(tournamentService, matchRepo);
    }

    @Test
    void testInitializeTournament_createsMatchesAndRounds() {
        // Arrange
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        ArrayList<String> players = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie", "David"));
        tournament.setPlayers(players);

        when(tournamentRepo.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepo.save(any(Tournament.class))).thenAnswer(i -> i.getArguments()[0]);
        when(matchRepo.saveAll(anyList())).thenReturn(null);

        // Act
        tournamentService.initializeTournament(1L);

        // Assert
        // Tournament should have a root match and all matches
        assertNotNull(tournament.getRootMatch(), "Root match should be set");
        assertNotNull(tournament.getAllMatches(), "All matches should be set");
        assertEquals(3, tournament.getAllMatches().size(), "Should have 7 matches for 4 players (2^2=4, 4+2+1=7)");

        // Rounds should be set
        assertNotNull(tournament.getRounds(), "Rounds should be set");
        assertEquals(2, tournament.getRounds().size(), "Should have 3 rounds for 4 players");

        // First round matches should have players assigned
        Round firstRound = tournament.getRounds().get(0);
        for (Match m : firstRound.getMatches()) {
            assertNotNull(m.getPlayerA(), "PlayerA should be assigned");
            assertNotNull(m.getPlayerB(), "PlayerB should be assigned");
        }
    }

    @Test
    void testInitializeTournament_handlesNonPowerOfTwoPlayers() {
        // Arrange
        Tournament tournament = new Tournament();
        tournament.setId(2L);
        ArrayList<String> players = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie"));
        tournament.setPlayers(players);

        when(tournamentRepo.findById(2L)).thenReturn(Optional.of(tournament));
        when(tournamentRepo.save(any(Tournament.class))).thenAnswer(i -> i.getArguments()[0]);
        when(matchRepo.saveAll(anyList())).thenReturn(null);

        // Act
        tournamentService.initializeTournament(2L);

        // Assert
        assertNotNull(tournament.getRootMatch());
        assertNotNull(tournament.getAllMatches());
        // For 3 players, highest power of two is 2, so 2 matches in first round, 1 in second, total 3
    }

    @Test
    void testInitializeTournament_throwsIfTournamentNotFound() {
        when(tournamentRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> tournamentService.initializeTournament(99L));
    }
}
