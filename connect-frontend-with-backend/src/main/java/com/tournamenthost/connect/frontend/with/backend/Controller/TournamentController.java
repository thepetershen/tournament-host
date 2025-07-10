package com.tournamenthost.connect.frontend.with.backend.Controller;

import java.util.List;

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
    public Tournament createTournament(@RequestBody TournamentRequest request) {
        return tournamentService.addTournament(request.getName());
    }

    // Change tournament name
    @PutMapping("/{id}/name")
    public Tournament changeTournamentName(@PathVariable Long id, @RequestBody String newName) {
        return tournamentService.changeTournamentName(id, newName.replace("\"", "")); // Remove quotes if sent as raw string
    }

    // Add a single player
    @PostMapping("/{id}/players")
    public void addPlayer(@PathVariable Long id, @RequestBody PlayerRequest playerRequest) {
        tournamentService.addPlayer(id, playerRequest.getName());
    }

    // Add multiple players
    @PostMapping("/{id}/players/batch")
    public void addPlayers(@PathVariable Long id, @RequestBody List<PlayerRequest> playerRequests) {
        List<String> names = playerRequests.stream()
            .map(PlayerRequest::getName)
            .toList();
        tournamentService.addPlayer(id, names);
    }

    // Get all players
    @GetMapping("/{id}/players")
    public List<String> getPlayers(@PathVariable Long id) {
        return tournamentService.getPlayers(id);
    }

    // Initialize tournament (stub)
    @PostMapping("/{id}/initialize")
    public void initializeTournament(@PathVariable Long id) {
        tournamentService.initializeTournament(id);
    }

    @GetMapping("/{id}/draw")
    public List<List<MatchDTO>> getTournamentDraw(@PathVariable Long id) {
        List<List<Match>> draw = tournamentService.getTournamentDraw(id);
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

        return dtoDraw;
    }
}