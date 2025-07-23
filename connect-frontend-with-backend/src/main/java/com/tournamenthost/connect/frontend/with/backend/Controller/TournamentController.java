package com.tournamenthost.connect.frontend.with.backend.Controller;

import com.tournamenthost.connect.frontend.with.backend.DTO.EventDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Service.EventService;
import com.tournamenthost.connect.frontend.with.backend.Service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "http://localhost:5173")
public class TournamentController {

    private final TournamentService tournamentService;

    private final EventService eventService;

    public TournamentController(TournamentService tournamentService, EventService eventService) {
        this.tournamentService = tournamentService;
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<TournamentDTO> createTournament(@RequestBody TournamentRequest tournamentRequest) {
        try {
            Tournament tournament = tournamentService.createTournament(tournamentRequest.getName());
            TournamentDTO dto = new TournamentDTO();
            dto.setName(tournament.getName());
            dto.setId(tournament.getId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAllTournaments() {
        List<Tournament> tournaments = tournamentService.getAllTournaments();
        List<TournamentDTO> answer = new ArrayList<>();

        for(Tournament t: tournaments){
            TournamentDTO dto = new TournamentDTO();
            dto.setId(t.getId());
            dto.setName(t.getName());
            answer.add(dto);
        }
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournament(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.getTournament(id);
            TournamentDTO dto = new TournamentDTO();
            dto.setId(tournament.getId());
            dto.setName(tournament.getName());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDTO>> getAllUser(@PathVariable Long id){
        try {
            List<UserDTO> answer = new ArrayList<>();
            for(User user: tournamentService.getUsers(id)){
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setUsername(user.getUsername());
                answer.add(dto);
            }
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{tournamentId}/events/{eventId}")
    public ResponseEntity<TournamentDTO> addEventToTournament(
            @PathVariable Long tournamentId,
            @PathVariable Long eventId) {
        try {
            // Find the event by ID using the eventService
            BaseEvent event = eventService.getEvent(eventId);
            Tournament tournament = tournamentService.addEventToTournament(tournamentId, event);

            TournamentDTO dto = new TournamentDTO();
            dto.setId(tournament.getId());
            dto.setName(tournament.getName());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getEventsForTournament(@PathVariable Long id) {
        try {
            List<BaseEvent> events = tournamentService.getEventsForTournament(id);
            List<EventDTO> eventDTOs = new ArrayList<>();
            for (BaseEvent event : events) {
                EventDTO dto = new EventDTO();
                dto.setId(event.getId());
                dto.setName(event.getName());
                eventDTOs.add(dto);
            }
            return ResponseEntity.ok(eventDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<?> getMatches(@PathVariable Long id) {
        try {
            List<Match> matches = tournamentService.getAllMatches(id);
            List<MatchDTO> matchDTOs = new ArrayList<>();
            for (Match match : matches) {
                MatchDTO dto = new MatchDTO();
                dto.setId(match.getId());
                User playerA = match.getPlayerA();
                User playerB = match.getPlayerB();
                UserDTO playerADTO = new UserDTO();
                UserDTO playerBDTO = new UserDTO();
                if (playerA != null) {
                    playerADTO.setUsername(playerA.getUsername());
                    playerADTO.setId(playerA.getId());
                }
                if (playerB != null) {
                    playerBDTO.setUsername(playerB.getUsername());
                    playerBDTO.setId(playerB.getId());
                }
                dto.setPlayerA(playerADTO);
                dto.setPlayerB(playerBDTO);
                // Add more fields if needed
                matchDTOs.add(dto);
            }
            return ResponseEntity.ok(matchDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}