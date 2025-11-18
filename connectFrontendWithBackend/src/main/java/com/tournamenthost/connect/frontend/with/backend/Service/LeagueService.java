package com.tournamenthost.connect.frontend.with.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.tournamenthost.connect.frontend.with.backend.Model.League;
import com.tournamenthost.connect.frontend.with.backend.Model.LeaguePlayerRanking;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Repository.LeagueRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TournamentService tournamentService;

    // ==================== LEAGUE CRUD OPERATIONS ====================

    /**
     * Create a new league
     */
    public League createLeague(String name, User owner) {
        League league = new League(name, owner);
        return leagueRepo.save(league);
    }

    /**
     * Get all leagues
     */
    public List<League> getAllLeagues() {
        List<League> leagues = new ArrayList<>();
        leagueRepo.findAll().forEach(leagues::add);
        return leagues;
    }

    /**
     * Get league by ID
     */
    public League getLeague(Long id) {
        return leagueRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("League not found"));
    }

    /**
     * Update league name
     */
    public League updateLeagueName(Long leagueId, String newName, User user) {
        League league = getLeague(leagueId);
        verifyEditPermission(leagueId, user);
        league.setName(newName);
        return leagueRepo.save(league);
    }

    /**
     * Delete a league
     */
    public void deleteLeague(Long leagueId, User user) {
        League league = getLeague(leagueId);
        verifyEditPermission(leagueId, user);
        leagueRepo.delete(league);
    }

    // ==================== TOURNAMENT MANAGEMENT ====================

    /**
     * Add tournament to league
     * Requires edit permission for both league and tournament
     */
    public void addTournamentToLeague(Long leagueId, Long tournamentId, User user) {
        League league = getLeague(leagueId);
        Tournament tournament = tournamentService.getTournament(tournamentId);

        // Check if user can edit both the league and the tournament
        if (!league.canUserEdit(user)) {
            throw new IllegalArgumentException("You do not have permission to edit this league");
        }
        if (!tournament.canUserEdit(user)) {
            throw new IllegalArgumentException("You do not have permission to add this tournament");
        }

        // Check if tournament is already in the league
        if (league.getTournaments().contains(tournament)) {
            throw new IllegalArgumentException("Tournament is already in this league");
        }

        league.addTournament(tournament);
        leagueRepo.save(league);
    }

    /**
     * Remove tournament from league
     */
    public void removeTournamentFromLeague(Long leagueId, Long tournamentId, User user) {
        League league = getLeague(leagueId);
        Tournament tournament = tournamentService.getTournament(tournamentId);

        verifyEditPermission(leagueId, user);

        if (!league.getTournaments().contains(tournament)) {
            throw new IllegalArgumentException("Tournament is not in this league");
        }

        league.removeTournament(tournament);
        leagueRepo.save(league);

        // Recalculate rankings after removing tournament
        recalculateLeagueRankings(leagueId);
    }

    /**
     * Get all tournaments in a league
     */
    public Set<Tournament> getLeagueTournaments(Long leagueId) {
        League league = getLeague(leagueId);
        return league.getTournaments();
    }

    // ==================== PLAYER RANKINGS ====================

    /**
     * Calculate and update league rankings
     * Rankings are based on cumulative points from all tournaments in the league
     */
    public List<LeaguePlayerRanking> recalculateLeagueRankings(Long leagueId) {
        League league = getLeague(leagueId);

        // Collect all players from all tournaments in the league
        Set<User> allPlayers = new HashSet<>();
        for (Tournament tournament : league.getTournaments()) {
            allPlayers.addAll(tournamentService.getAllPlayers(tournament.getId()));
        }

        // Calculate total points for each player across all tournaments
        Map<User, Integer> playerTotalPoints = new HashMap<>();
        Map<User, Integer> playerMatchesWon = new HashMap<>();
        Map<User, Integer> playerMatchesLost = new HashMap<>();
        Map<User, Integer> playerMatchesPlayed = new HashMap<>();

        for (User player : allPlayers) {
            int totalPoints = 0;
            int totalMatchesWon = 0;
            int totalMatchesLost = 0;
            int totalMatchesPlayed = 0;

            // Sum points from each tournament in the league
            for (Tournament tournament : league.getTournaments()) {
                try {
                    // Use the new cumulative points calculation method
                    // This automatically sums points across all events with points distributions
                    Map<User, Integer> tournamentPoints = tournamentService.calculateCumulativeTournamentPoints(tournament.getId());
                    if (tournamentPoints.containsKey(player)) {
                        totalPoints += tournamentPoints.get(player);
                    }
                } catch (IllegalArgumentException e) {
                    // Tournament has no completed events or no points distributions, skip
                }

                // TODO: Calculate matches won/lost/played from tournament data
                // This would require iterating through all matches in the tournament
                // For now, we'll leave these as 0
            }

            playerTotalPoints.put(player, totalPoints);
            playerMatchesWon.put(player, totalMatchesWon);
            playerMatchesLost.put(player, totalMatchesLost);
            playerMatchesPlayed.put(player, totalMatchesPlayed);
        }

        // Sort players by total points (descending)
        List<User> sortedPlayers = new ArrayList<>(allPlayers);
        sortedPlayers.sort((p1, p2) -> {
            int pointsCompare = Integer.compare(
                playerTotalPoints.getOrDefault(p2, 0),
                playerTotalPoints.getOrDefault(p1, 0)
            );
            if (pointsCompare != 0) return pointsCompare;

            // Tiebreaker: matches won
            return Integer.compare(
                playerMatchesWon.getOrDefault(p2, 0),
                playerMatchesWon.getOrDefault(p1, 0)
            );
        });

        // Clear existing rankings
        league.getPlayerRankings().clear();

        // Create new rankings
        List<LeaguePlayerRanking> newRankings = new ArrayList<>();
        for (int i = 0; i < sortedPlayers.size(); i++) {
            User player = sortedPlayers.get(i);
            int rank = i + 1;
            int points = playerTotalPoints.getOrDefault(player, 0);

            LeaguePlayerRanking ranking = new LeaguePlayerRanking(league, player, rank, points);
            ranking.setMatchesPlayed(playerMatchesPlayed.getOrDefault(player, 0));
            ranking.setMatchesWon(playerMatchesWon.getOrDefault(player, 0));
            ranking.setMatchesLost(playerMatchesLost.getOrDefault(player, 0));

            league.addPlayerRanking(ranking);
            newRankings.add(ranking);
        }

        leagueRepo.save(league);
        return newRankings;
    }

    /**
     * Get league rankings (ordered by rank)
     */
    public List<LeaguePlayerRanking> getLeagueRankings(Long leagueId) {
        League league = getLeague(leagueId);
        return league.getPlayerRankings();
    }

    /**
     * Get ranking for a specific player in a league
     */
    public LeaguePlayerRanking getPlayerRanking(Long leagueId, Long playerId) {
        League league = getLeague(leagueId);
        User player = userRepo.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return league.getPlayerRankings().stream()
            .filter(ranking -> ranking.getPlayer().equals(player))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not found in league rankings"));
    }

    // ==================== AUTHORIZATION METHODS ====================

    /**
     * Check if a user can edit a league (owner or authorized editor)
     */
    public boolean canUserEditLeague(Long leagueId, User user) {
        League league = getLeague(leagueId);
        return league.canUserEdit(user);
    }

    /**
     * Add an authorized editor to a league (only owner can do this)
     */
    public void addAuthorizedEditor(Long leagueId, Long editorId, User currentUser) {
        League league = getLeague(leagueId);

        // Only owner can add editors
        if (!league.getOwner().equals(currentUser)) {
            throw new IllegalArgumentException("Only the league owner can add editors");
        }

        User editor = userRepo.findById(editorId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + editorId + " not found"));

        league.addAuthorizedEditor(editor);
        leagueRepo.save(league);
    }

    /**
     * Remove an authorized editor from a league (only owner can do this)
     */
    public void removeAuthorizedEditor(Long leagueId, Long editorId, User currentUser) {
        League league = getLeague(leagueId);

        // Only owner can remove editors
        if (!league.getOwner().equals(currentUser)) {
            throw new IllegalArgumentException("Only the league owner can remove editors");
        }

        User editor = userRepo.findById(editorId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + editorId + " not found"));

        league.removeAuthorizedEditor(editor);
        leagueRepo.save(league);
    }

    /**
     * Verify user has edit permission, throw exception if not
     */
    public void verifyEditPermission(Long leagueId, User user) {
        if (!canUserEditLeague(leagueId, user)) {
            throw new IllegalArgumentException("You do not have permission to edit this league");
        }
    }

    // ==================== LEAGUE STATISTICS ====================

    /**
     * Get all players in a league (from all tournaments)
     */
    public Set<User> getAllLeaguePlayers(Long leagueId) {
        League league = getLeague(leagueId);
        Set<User> allPlayers = new HashSet<>();

        for (Tournament tournament : league.getTournaments()) {
            allPlayers.addAll(tournamentService.getAllPlayers(tournament.getId()));
        }

        return allPlayers;
    }

    /**
     * Get league statistics
     */
    public Map<String, Object> getLeagueStatistics(Long leagueId) {
        League league = getLeague(leagueId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTournaments", league.getTournaments().size());
        stats.put("totalPlayers", getAllLeaguePlayers(leagueId).size());
        stats.put("leagueName", league.getName());
        stats.put("owner", league.getOwner().getName());

        return stats;
    }
}
