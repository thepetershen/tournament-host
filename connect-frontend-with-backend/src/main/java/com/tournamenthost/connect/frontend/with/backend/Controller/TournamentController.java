package com.tournamenthost.connect.frontend.with.backend.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.PlayerRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentRequest;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Service.TournamentService;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "http://localhost:5173")
public class TournamentController {
    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    // URL: POST /api/tournaments
    @PostMapping
    public ResponseEntity<?> createTournament(@RequestBody TournamentRequest request) {
        try {
            Tournament tournament = tournamentService.addTournament(request.getName());
            TournamentDTO dto = new TournamentDTO();
            dto.setName(tournament.getName());
            dto.setId(tournament.getId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            // Duplicate tournament name
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change tournament name
    @PutMapping("/{id}/name")
    public ResponseEntity<?> setTournamentName(@PathVariable Long id, @RequestBody String newName) {
        try{
            Tournament tournament = tournamentService.changeTournamentName(id, newName.replace("\"", "")); // Remove quotes if sent as raw string
            TournamentDTO dto = new TournamentDTO();
            dto.setName(tournament.getName());
            dto.setId(tournament.getId());
            return ResponseEntity.ok(dto);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<?> getTournamentName(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.getTournament(id);
            TournamentDTO dto = new TournamentDTO();
            dto.setName(tournament.getName());
            dto.setId(tournament.getId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        
    }

    @GetMapping
    public List<TournamentDTO> getAllTournaments() {
        List<Tournament> tournaments = tournamentService.getAllTournaments();
        List<TournamentDTO> answer = new ArrayList<>();

        for(Tournament cur: tournaments) {
            TournamentDTO dto = new TournamentDTO();
            dto.setName(cur.getName());
            dto.setId(cur.getId());
            answer.add(dto);
        }
        return answer;
    }

    // Add players (accepts a list, even if only one)
    @PostMapping("/{id}/players")
    public ResponseEntity<?> addPlayers(@PathVariable Long id, @RequestBody List<PlayerRequest> playerRequests) {
        try {
            List<String> names = playerRequests.stream()
                .map(PlayerRequest::getName)
                .toList();
            tournamentService.addPlayer(id, names);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all players
    @GetMapping("/{id}/players")
    public ResponseEntity<?> getPlayers(@PathVariable Long id) {
        try {
            List<String> players = tournamentService.getPlayers(id);
            return ResponseEntity.ok(players);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Initialize tournament (stub)
    @PostMapping("/{id}/initialize")
    public ResponseEntity<?> initializeTournament(@PathVariable Long id) {
        try {
            tournamentService.initializeTournament(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Deinitialize tournament (removes rounds and matches, keeps players)
    @PostMapping("/{id}/deinitialize")
    public ResponseEntity<?> deinitializeTournament(@PathVariable Long id) {
        try {
            tournamentService.deinitializeTournament(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/draw")
    public ResponseEntity<?> getTournamentDraw(@PathVariable Long id) {
        List<List<Match>> draw;
        try {
            draw = tournamentService.getTournamentDraw(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        List<List<MatchDTO>> dtoDraw = new java.util.ArrayList<>();
        for (List<Match> round : draw) {
            List<MatchDTO> roundDTOs = new java.util.ArrayList<>();
            for (Match match : round) {
                MatchDTO dto = new MatchDTO();
                dto.setId(match.getId());
                dto.setPlayerA(match.getPlayerA());
                dto.setPlayerB(match.getPlayerB());
                // Add more fields if needed
                roundDTOs.add(dto);
            }
            dtoDraw.add(roundDTOs);
        }
        return ResponseEntity.ok(dtoDraw);
    }
}