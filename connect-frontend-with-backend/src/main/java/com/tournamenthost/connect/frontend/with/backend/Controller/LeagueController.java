package com.tournamenthost.connect.frontend.with.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tournamenthost.connect.frontend.with.backend.Model.League;
import com.tournamenthost.connect.frontend.with.backend.Model.LeaguePlayerRanking;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Service.LeagueService;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    // ==================== LEAGUE CRUD OPERATIONS ====================

    /**
     * Create a new league
     * POST /api/leagues
     */
    @PostMapping
    public ResponseEntity<?> createLeague(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("League name is required");
            }

            League league = leagueService.createLeague(name, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(league);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all leagues
     * GET /api/leagues
     */
    @GetMapping
    public ResponseEntity<?> getAllLeagues() {
        try {
            List<League> leagues = leagueService.getAllLeagues();
            return ResponseEntity.ok(leagues);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get league by ID
     * GET /api/leagues/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLeague(@PathVariable Long id) {
        try {
            League league = leagueService.getLeague(id);
            return ResponseEntity.ok(league);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Update league name
     * PUT /api/leagues/{id}/name
     */
    @PutMapping("/{id}/name")
    public ResponseEntity<?> updateLeagueName(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        try {
            String newName = request.get("name");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("New league name is required");
            }

            League league = leagueService.updateLeagueName(id, newName, user);
            return ResponseEntity.ok(league);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a league
     * DELETE /api/leagues/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeague(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        try {
            leagueService.deleteLeague(id, user);
            return ResponseEntity.ok("League deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== TOURNAMENT MANAGEMENT ====================

    /**
     * Add tournament to league
     * POST /api/leagues/{leagueId}/tournaments/{tournamentId}
     */
    @PostMapping("/{leagueId}/tournaments/{tournamentId}")
    public ResponseEntity<?> addTournamentToLeague(
            @PathVariable Long leagueId,
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal User user) {
        try {
            leagueService.addTournamentToLeague(leagueId, tournamentId, user);
            return ResponseEntity.ok("Tournament added to league successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Remove tournament from league
     * DELETE /api/leagues/{leagueId}/tournaments/{tournamentId}
     */
    @DeleteMapping("/{leagueId}/tournaments/{tournamentId}")
    public ResponseEntity<?> removeTournamentFromLeague(
            @PathVariable Long leagueId,
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal User user) {
        try {
            leagueService.removeTournamentFromLeague(leagueId, tournamentId, user);
            return ResponseEntity.ok("Tournament removed from league successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all tournaments in a league
     * GET /api/leagues/{leagueId}/tournaments
     */
    @GetMapping("/{leagueId}/tournaments")
    public ResponseEntity<?> getLeagueTournaments(@PathVariable Long leagueId) {
        try {
            Set<Tournament> tournaments = leagueService.getLeagueTournaments(leagueId);
            return ResponseEntity.ok(tournaments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ==================== PLAYER RANKINGS ====================

    /**
     * Recalculate league rankings
     * POST /api/leagues/{leagueId}/rankings/recalculate
     */
    @PostMapping("/{leagueId}/rankings/recalculate")
    public ResponseEntity<?> recalculateRankings(
            @PathVariable Long leagueId,
            @AuthenticationPrincipal User user) {
        try {
            leagueService.verifyEditPermission(leagueId, user);
            List<LeaguePlayerRanking> rankings = leagueService.recalculateLeagueRankings(leagueId);
            return ResponseEntity.ok(rankings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get league rankings
     * GET /api/leagues/{leagueId}/rankings
     */
    @GetMapping("/{leagueId}/rankings")
    public ResponseEntity<?> getLeagueRankings(@PathVariable Long leagueId) {
        try {
            List<LeaguePlayerRanking> rankings = leagueService.getLeagueRankings(leagueId);
            return ResponseEntity.ok(rankings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Get ranking for a specific player in a league
     * GET /api/leagues/{leagueId}/rankings/player/{playerId}
     */
    @GetMapping("/{leagueId}/rankings/player/{playerId}")
    public ResponseEntity<?> getPlayerRanking(
            @PathVariable Long leagueId,
            @PathVariable Long playerId) {
        try {
            LeaguePlayerRanking ranking = leagueService.getPlayerRanking(leagueId, playerId);
            return ResponseEntity.ok(ranking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ==================== AUTHORIZATION ====================

    /**
     * Add authorized editor to league
     * POST /api/leagues/{leagueId}/editors/{editorId}
     */
    @PostMapping("/{leagueId}/editors/{editorId}")
    public ResponseEntity<?> addAuthorizedEditor(
            @PathVariable Long leagueId,
            @PathVariable Long editorId,
            @AuthenticationPrincipal User user) {
        try {
            leagueService.addAuthorizedEditor(leagueId, editorId, user);
            return ResponseEntity.ok("Editor added successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Remove authorized editor from league
     * DELETE /api/leagues/{leagueId}/editors/{editorId}
     */
    @DeleteMapping("/{leagueId}/editors/{editorId}")
    public ResponseEntity<?> removeAuthorizedEditor(
            @PathVariable Long leagueId,
            @PathVariable Long editorId,
            @AuthenticationPrincipal User user) {
        try {
            leagueService.removeAuthorizedEditor(leagueId, editorId, user);
            return ResponseEntity.ok("Editor removed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== LEAGUE STATISTICS ====================

    /**
     * Get all players in a league
     * GET /api/leagues/{leagueId}/players
     */
    @GetMapping("/{leagueId}/players")
    public ResponseEntity<?> getAllLeaguePlayers(@PathVariable Long leagueId) {
        try {
            Set<User> players = leagueService.getAllLeaguePlayers(leagueId);
            return ResponseEntity.ok(players);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Get league statistics
     * GET /api/leagues/{leagueId}/statistics
     */
    @GetMapping("/{leagueId}/statistics")
    public ResponseEntity<?> getLeagueStatistics(@PathVariable Long leagueId) {
        try {
            Map<String, Object> stats = leagueService.getLeagueStatistics(leagueId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
