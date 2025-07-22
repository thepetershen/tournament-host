package com.tournamenthost.connect.frontend.with.backend.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserGetRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventRequest;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Service.EventService;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidRequest(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Invalid request body: " + ex.getMostSpecificCause().getMessage());
    }

    // URL: POST /api/events
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request) {
        try {
            EventType type = EventType.valueOf(request.getEventType().toUpperCase());
            BaseEvent event = eventService.addEvent(request.getName(), type, request.getTournamentId());
            EventDTO dto = new EventDTO();
            dto.setName(event.getName());
            dto.setId(event.getId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            // Duplicate event name
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change event name
    @PutMapping("/{id}/name")
    public ResponseEntity<?> setEventName(@PathVariable Long id, @RequestBody String newName) {
        try{
            BaseEvent event = eventService.changeEventName(id, newName.replace("\"", "")); // Remove quotes if sent as raw string
            EventDTO dto = new EventDTO();
            dto.setName(event.getName());
            dto.setId(event.getId());
            return ResponseEntity.ok(dto);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<?> getEventName(@PathVariable Long id) {
        try {
            BaseEvent event = eventService.getEvent(id);
            EventDTO dto = new EventDTO();
            dto.setName(event.getName());
            dto.setId(event.getId());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<EventDTO> getAllEvents() {
        List<BaseEvent> events = eventService.getAllEvents();
        List<EventDTO> answer = new ArrayList<>();

        for(BaseEvent cur: events) {
            EventDTO dto = new EventDTO();
            dto.setName(cur.getName());
            dto.setId(cur.getId());
            answer.add(dto);
        }
        return answer;
    }

    // Add players (accepts a list, even if only one)
    @PostMapping("/{id}/players")
    public ResponseEntity<?> addPlayers(@PathVariable Long id, @RequestBody List<UserGetRequest> playerRequests) {
        try {
            for(UserGetRequest request: playerRequests){
                Long userId = request.getId();
                eventService.addPlayer(id, userId);
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<?> getPlayers(@PathVariable Long id) {
        try {
            List<User> players = eventService.getPlayers(id);
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
                UserDTO dto = new UserDTO(user.getId(), user.getEmail(), user.getUsername(), tournamentDTOs);
                playerDTOs.add(dto);
            }
            return ResponseEntity.ok(playerDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Initialize event (stub)
    @PostMapping("/{id}/initialize")
    public ResponseEntity<?> initializeEvent(@PathVariable Long id) {
        try {
            eventService.initializeEvent(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Deinitialize event (removes rounds and matches, keeps players)
    @PostMapping("/{id}/deinitialize")
    public ResponseEntity<?> deinitializeEvent(@PathVariable Long id) {
        try {
            eventService.deinitializeEvent(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/draw")
    public ResponseEntity<?> getEventDraw(@PathVariable Long id) {
        List<List<Match>> draw;
        try {
            draw = eventService.getEventDraw(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        List<List<MatchDTO>> dtoDraw = new java.util.ArrayList<>();
        for (List<Match> round : draw) {
            List<MatchDTO> roundDTOs = new java.util.ArrayList<>();
            for (Match match : round) {
                MatchDTO dto = new MatchDTO();
                dto.setId(match.getId());
                User playerA = match.getPlayerA();
                User playerB = match.getPlayerB();
                UserDTO playerADTO  = new UserDTO();
                UserDTO playerBDTO  = new UserDTO();
                if(playerA != null) playerADTO.setUsername(playerA.getUsername());
                if(playerA != null) playerADTO.setId(playerA.getId());
                if(playerB != null) playerBDTO.setUsername(playerB.getUsername());
                if(playerB != null) playerBDTO.setId(playerB.getId());
                dto.setPlayerA(playerADTO);
                dto.setPlayerB(playerBDTO);
                // Add more fields if needed
                roundDTOs.add(dto);
            }
            dtoDraw.add(roundDTOs);
        }
        return ResponseEntity.ok(dtoDraw);
    }
}