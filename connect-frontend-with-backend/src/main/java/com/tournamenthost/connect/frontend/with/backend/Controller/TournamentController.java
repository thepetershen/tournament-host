package com.tournamenthost.connect.frontend.with.backend.Controller;

import com.tournamenthost.connect.frontend.with.backend.DTO.EventDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserGetRequest;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
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

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    public ResponseEntity<?> createTournament(@RequestBody TournamentRequest tournamentRequest) {
        try {
            Tournament tournament = tournamentService.createTournament(tournamentRequest.getName());
            TournamentDTO dto = new TournamentDTO();
            dto.setName(tournament.getName());
            dto.setId(tournament.getId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        // URL: POST /api/events
    @PostMapping("/{tournamentId}/event")
    public ResponseEntity<?> createEvent(@PathVariable Long tournamentId, @RequestBody EventRequest request) {
        try {
            EventType type = EventType.valueOf(request.getEventType().toUpperCase());
            BaseEvent event = tournamentService.addEvent(request.getName(), type, tournamentId);
            EventDTO dto = new EventDTO();
            dto.setName(event.getName());
            dto.setId(event.getIndex());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change event name
    @PutMapping("/{tournamentId}/event/{eventIndex}/name")
    public ResponseEntity<?> setEventName(@PathVariable Long tournamentId, @PathVariable int eventIndex, @RequestBody String newName) {
        try{
            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            event.setName(newName.replace("\"", ""));
            EventDTO dto = new EventDTO();
            dto.setName(event.getName());
            dto.setId(event.getIndex());
            return ResponseEntity.ok(dto);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
            for(User user: tournamentService.getAllPlayers(id)){
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setUsername(user.getUsername());
                dto.setName(user.getName()); // Add name
                answer.add(dto);
            }
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getEventsForTournament(@PathVariable Long id) {
        try {
            List<BaseEvent> events = tournamentService.getEventsForTournament(id);
            List<EventDTO> eventDTOs = new ArrayList<>();
            for (BaseEvent event : events) {
                EventDTO dto = new EventDTO();
                dto.setId(event.getIndex());
                dto.setName(event.getName());
                eventDTOs.add(dto);
            }
            return ResponseEntity.ok(eventDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Event-specific endpoints migrated from EventController
    @GetMapping("/{tournamentId}/event/{eventIndex}/name")
    public ResponseEntity<?> getEventName(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            EventDTO dto = new EventDTO();
            dto.setName(event.getName());
            dto.setId(event.getIndex());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{tournamentId}/event/{eventIndex}/players")
    public ResponseEntity<?> addPlayers(@PathVariable Long tournamentId, @PathVariable int eventIndex, @RequestBody List<UserGetRequest> playerRequests) {
        try {
            for(UserGetRequest request: playerRequests){
                Long userId = request.getId();
                tournamentService.addPlayer(tournamentId, eventIndex, userId);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{tournamentId}/event/{eventIndex}/players")
    public ResponseEntity<?> getPlayers(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            List<User> players = tournamentService.getPlayers(tournamentId, eventIndex);
            List<UserDTO> playerDTOs = new ArrayList<>();
            for (User user : players) {
                List<TournamentDTO> tournamentDTOs = new ArrayList<>();
                if (user.getTournaments() != null) {
                    for (Tournament e : user.getTournaments()) {
                        TournamentDTO tDto = new TournamentDTO();
                        tDto.setId(e.getId());
                        tDto.setName(e.getName());
                        tournamentDTOs.add(tDto);
                    }
                }
                UserDTO dto = new UserDTO(user.getId(), user.getUsername(), user.getName(), tournamentDTOs);
                playerDTOs.add(dto);
            }
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{tournamentId}/event/{eventIndex}/matches")
    public ResponseEntity<?> getMatchesForEvent(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            List<Match> matches = tournamentService.getMatchesForEvent(tournamentId, eventIndex);
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
                    playerADTO.setName(playerA.getName());
                }
                if (playerB != null) {
                    playerBDTO.setUsername(playerB.getUsername());
                    playerBDTO.setId(playerB.getId());
                    playerBDTO.setName(playerB.getName());
                }
                dto.setPlayerA(playerADTO);
                dto.setPlayerB(playerBDTO);
                matchDTOs.add(dto);
            }
            return ResponseEntity.ok(matchDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{tournamentId}/event/{eventIndex}/draw")
    public ResponseEntity<?> getEventDraw(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            List<List<Match>> draw = tournamentService.getEventDraw(tournamentId, eventIndex);
            List<List<MatchDTO>> dtoDraw = new ArrayList<>();
            for (List<Match> round : draw) {
                List<MatchDTO> roundDTOs = new ArrayList<>();
                for (Match match : round) {
                    MatchDTO dto = new MatchDTO();
                    dto.setId(match.getId());
                    User playerA = match.getPlayerA();
                    User playerB = match.getPlayerB();
                    UserDTO playerADTO  = new UserDTO();
                    UserDTO playerBDTO  = new UserDTO();
                    if(playerA != null) {
                        playerADTO.setUsername(playerA.getUsername());
                        playerADTO.setId(playerA.getId());
                        playerADTO.setName(playerA.getName());
                    }
                    if(playerB != null) {
                        playerBDTO.setUsername(playerB.getUsername());
                        playerBDTO.setId(playerB.getId());
                        playerBDTO.setName(playerB.getName());
                    }
                    dto.setPlayerA(playerADTO);
                    dto.setPlayerB(playerBDTO);
                    roundDTOs.add(dto);
                }
                dtoDraw.add(roundDTOs);
            }
            return ResponseEntity.ok(dtoDraw);
        } catch (Exception e) {
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
                    playerADTO.setName(playerA.getName()); // Add name
                }
                if (playerB != null) {
                    playerBDTO.setUsername(playerB.getUsername());
                    playerBDTO.setId(playerB.getId());
                    playerBDTO.setName(playerB.getName()); // Add name
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

        // Initialize event (stub)
    // Initialize event (requires both tournamentId and eventIndex)
    @PostMapping("/{tournamentId}/event/{eventIndex}/initialize")
    public ResponseEntity<?> initializeEvent(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            tournamentService.initializeEvent(tournamentId, eventIndex);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Deinitialize event (removes rounds and matches, keeps players)
    @PostMapping("/{tournamentId}/event/{eventIndex}/deinitialize")
    public ResponseEntity<?> deinitializeEvent(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            tournamentService.deinitializeEvent(tournamentId, eventIndex);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}