package com.tournamenthost.connect.frontend.with.backend.Controller;

import com.tournamenthost.connect.frontend.with.backend.DTO.DrawResponseDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.MatchResultRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentUpdateRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TeamDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserGetRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.PointsDistributionRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.PointsDistributionDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.ManualSeedingRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.AutoSeedingRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.SeededUserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventRegistrationDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.ApproveRegistrationsRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventMatchConfigRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.EventSignupRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.CreateTeamRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.GameDTO;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Team;
import com.tournamenthost.connect.frontend.with.backend.Model.PointsDistribution;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventRegistration;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.RoundRobinEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.DoubleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<?> createTournament(@RequestBody TournamentRequest tournamentRequest) {
        try {
            User currentUser = getCurrentUser();
            Tournament tournament = tournamentService.createTournament(
                tournamentRequest.getName(),
                currentUser,
                tournamentRequest.getMessage(),
                tournamentRequest.getBegin(),
                tournamentRequest.getEnd(),
                tournamentRequest.getLocation()
            );
            TournamentDTO dto = new TournamentDTO();
            dto.setName(tournament.getName());
            dto.setId(tournament.getId());
            dto.setMessage(tournament.getMessage());
            dto.setBegin(tournament.getBegin());
            dto.setEnd(tournament.getEnd());
            dto.setLocation(tournament.getLocation());

            // Add owner information
            UserDTO ownerDTO = new UserDTO();
            ownerDTO.setId(currentUser.getId());
            ownerDTO.setUsername(currentUser.getUsername());
            ownerDTO.setName(currentUser.getName());
            dto.setOwner(ownerDTO);

            // Initialize empty editors list
            dto.setAuthorizedEditors(new ArrayList<>());

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        // URL: POST /api/events
    @PostMapping("/{tournamentId}/event")
    public ResponseEntity<?> createEvent(@PathVariable Long tournamentId, @RequestBody EventRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

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
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

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
    public ResponseEntity<List<TournamentDTO>> getAllTournaments(
            @RequestParam(defaultValue = "20") int limit) {
        List<Tournament> tournaments = tournamentService.getActiveTournaments(limit);
        List<TournamentDTO> answer = new ArrayList<>();
        Date now = new Date();

        for(Tournament t: tournaments){
            TournamentDTO dto = new TournamentDTO();
            dto.setId(t.getId());
            dto.setName(t.getName());
            dto.setBegin(t.getBegin());
            dto.setEnd(t.getEnd());
            dto.setLocation(t.getLocation());

            // Set owner info
            if (t.getOwner() != null) {
                UserDTO ownerDTO = new UserDTO();
                ownerDTO.setId(t.getOwner().getId());
                ownerDTO.setUsername(t.getOwner().getUsername());
                ownerDTO.setName(t.getOwner().getName());
                dto.setOwner(ownerDTO);
            }

            // Compute and set additional fields
            dto.setStatus(tournamentService.calculateStatus(t, now));
            dto.setEventCount(t.getEvents() != null ? t.getEvents().size() : 0);
            dto.setParticipantCount(tournamentService.countUniqueParticipants(t));

            // Explicitly exclude message field (too long for listing)
            // dto.setMessage() is not called

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
            dto.setMessage(tournament.getMessage());
            dto.setBegin(tournament.getBegin());
            dto.setEnd(tournament.getEnd());
            dto.setLocation(tournament.getLocation());

            // Include owner information
            if (tournament.getOwner() != null) {
                UserDTO ownerDTO = new UserDTO();
                ownerDTO.setId(tournament.getOwner().getId());
                ownerDTO.setUsername(tournament.getOwner().getUsername());
                ownerDTO.setName(tournament.getOwner().getName());
                dto.setOwner(ownerDTO);
            }

            // Include authorized editors
            List<UserDTO> editorDTOs = new ArrayList<>();
            for (User editor : tournament.getAuthorizedEditors()) {
                UserDTO editorDTO = new UserDTO();
                editorDTO.setId(editor.getId());
                editorDTO.setUsername(editor.getUsername());
                editorDTO.setName(editor.getName());
                editorDTOs.add(editorDTO);
            }
            dto.setAuthorizedEditors(editorDTOs);

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/details")
    public ResponseEntity<?> updateTournamentDetails(
            @PathVariable Long id,
            @RequestBody TournamentUpdateRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(id, currentUser);

            Tournament tournament = tournamentService.updateTournamentDetails(
                id,
                request.getMessage(),
                request.getBegin(),
                request.getEnd(),
                request.getLocation()
            );

            TournamentDTO dto = new TournamentDTO();
            dto.setId(tournament.getId());
            dto.setName(tournament.getName());
            dto.setMessage(tournament.getMessage());
            dto.setBegin(tournament.getBegin());
            dto.setEnd(tournament.getEnd());
            dto.setLocation(tournament.getLocation());

            // Include owner information
            if (tournament.getOwner() != null) {
                UserDTO ownerDTO = new UserDTO();
                ownerDTO.setId(tournament.getOwner().getId());
                ownerDTO.setUsername(tournament.getOwner().getUsername());
                ownerDTO.setName(tournament.getOwner().getName());
                dto.setOwner(ownerDTO);
            }

            // Include authorized editors
            List<UserDTO> editorDTOs = new ArrayList<>();
            for (User editor : tournament.getAuthorizedEditors()) {
                UserDTO editorDTO = new UserDTO();
                editorDTO.setId(editor.getId());
                editorDTO.setUsername(editor.getUsername());
                editorDTO.setName(editor.getName());
                editorDTOs.add(editorDTO);
            }
            dto.setAuthorizedEditors(editorDTOs);

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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

                // Set event type
                if (event instanceof SingleElimEvent) {
                    dto.setEventType("SINGLE_ELIM");
                } else if (event instanceof DoubleElimEvent) {
                    dto.setEventType("DOUBLE_ELIM");
                } else if (event instanceof RoundRobinEvent) {
                    dto.setEventType("ROUND_ROBIN");
                }

                // Set match configuration
                dto.setMatchType(event.getMatchType().name());
                dto.setGamesPerMatch(event.getGamesPerMatch());
                dto.setGamesRequiredToWin(event.getGamesRequiredToWin());

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
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

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
                matchDTOs.add(createMatchDTO(match));
            }
            return ResponseEntity.ok(matchDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{tournamentId}/event/{eventIndex}/draw")
    public ResponseEntity<?> getEventDraw(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            String eventType;
            List<List<Object>> dtoDraw = new ArrayList<>();

            if (event instanceof SingleElimEvent) {
                eventType = "SINGLE_ELIM";
                Object drawObject = tournamentService.getEventDraw(tournamentId, eventIndex);
                List<List<Match>> draw = (List<List<Match>>) drawObject;
                for (List<Match> round : draw) {
                    List<Object> roundDTOs = new ArrayList<>();
                    for (Match match : round) {
                        MatchDTO dto = createMatchDTO(match, event);
                        roundDTOs.add(dto);
                    }
                    dtoDraw.add(roundDTOs);
                }
            } else if (event instanceof RoundRobinEvent) {
                eventType = "ROUND_ROBIN";
                Object drawObject = tournamentService.getEventDraw(tournamentId, eventIndex);
                Map<Team, List<Match>> draw = (Map<Team, List<Match>>) drawObject;
                for (Map.Entry<Team, List<Match>> entry : draw.entrySet()) {
                    Team team = entry.getKey();
                    TeamDTO teamDTO = convertTeamToDTO(team);

                    List<Object> teamRow = new ArrayList<>();
                    teamRow.add(teamDTO);

                    for (Match match : entry.getValue()) {
                        MatchDTO dto = createMatchDTO(match, event);
                        teamRow.add(dto);
                    }
                    dtoDraw.add(teamRow);
                }
            } else if (event instanceof DoubleElimEvent doubleElimEvent) {
                eventType = "DOUBLE_ELIM";
                Object drawObject = tournamentService.getEventDraw(tournamentId, eventIndex);
                Map<String, List<List<Match>>> draw = (Map<String, List<List<Match>>>) drawObject;

                // Create a map to return with winners, losers brackets, and bronze match
                Map<String, Object> doubleElimDraw = new TreeMap<>();

                // Process winners bracket
                List<List<MatchDTO>> winnersDTOs = new ArrayList<>();
                for (List<Match> round : draw.get("winners")) {
                    List<MatchDTO> roundDTOs = new ArrayList<>();
                    for (Match match : round) {
                        roundDTOs.add(createMatchDTO(match, event));
                    }
                    winnersDTOs.add(roundDTOs);
                }
                doubleElimDraw.put("winners", winnersDTOs);

                // Process losers bracket
                List<List<MatchDTO>> losersDTOs = new ArrayList<>();
                for (List<Match> round : draw.get("losers")) {
                    List<MatchDTO> roundDTOs = new ArrayList<>();
                    for (Match match : round) {
                        roundDTOs.add(createMatchDTO(match, event));
                    }
                    losersDTOs.add(roundDTOs);
                }
                doubleElimDraw.put("losers", losersDTOs);

                // Add bronze match (3rd/4th place)
                Match bronzeMatch = doubleElimEvent.getBronzeMatch();
                if (bronzeMatch != null) {
                    doubleElimDraw.put("bronze", createMatchDTO(bronzeMatch, event));
                }

                DrawResponseDTO response = new DrawResponseDTO();
                response.setEventType(eventType);
                response.setDraw(doubleElimDraw);
                return ResponseEntity.ok(response);
            } else {
                throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getSimpleName());
            }

            DrawResponseDTO response = new DrawResponseDTO();
            response.setEventType(eventType);
            response.setDraw(dtoDraw);
            return ResponseEntity.ok(response);
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
                matchDTOs.add(createMatchDTO(match));
            }
            return ResponseEntity.ok(matchDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Configure match type and games per match for an event (must be done before initialization)
    @PostMapping("/{tournamentId}/event/{eventIndex}/match-config")
    public ResponseEntity<?> configureEventMatches(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody EventMatchConfigRequest config) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.configureEventMatches(tournamentId, eventIndex, config);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        // Initialize event (stub)
    // Initialize event (requires both tournamentId and eventIndex)
    @PostMapping("/{tournamentId}/event/{eventIndex}/initialize")
    public ResponseEntity<?> initializeEvent(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

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
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.deinitializeEvent(tournamentId, eventIndex);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Record match result with tournament context validation
    @PostMapping("/{tournamentId}/matches/{matchId}/result")
    public ResponseEntity<?> recordMatchResult(
            @PathVariable Long tournamentId,
            @PathVariable Long matchId,
            @RequestBody MatchResultRequest request) {
        try {
            User currentUser = getCurrentUser();
            // Verify user has permission to edit this tournament
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            // Verify the match belongs to this tournament and record the result
            tournamentService.recordMatchResult(tournamentId, matchId, request.getWinnerId(), request.getScore());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper method to create MatchDTO with all fields (no seed info)
    private MatchDTO createMatchDTO(Match match) {
        return createMatchDTO(match, null);
    }

    // Helper method to create MatchDTO with seed information
    private MatchDTO createMatchDTO(Match match, BaseEvent event) {
        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setCompleted(match.isCompleted());
        dto.setScore(match.getScore());
        dto.setMatchType(match.getMatchType() != null ? match.getMatchType().name() : "SINGLES");
        dto.setGamesRequiredToWin(match.getGamesRequiredToWin());

        // Convert games
        if (match.getGames() != null) {
            List<GameDTO> gameDTOs = match.getGames().stream()
                .map(game -> {
                    GameDTO gameDTO = new GameDTO();
                    gameDTO.setId(game.getId());
                    gameDTO.setTeamAScore(game.getTeamAScore());
                    gameDTO.setTeamBScore(game.getTeamBScore());
                    return gameDTO;
                })
                .collect(java.util.stream.Collectors.toList());
            dto.setGames(gameDTOs);
        }

        // Handle team-based matches (doubles)
        Team teamA = match.getTeamA();
        Team teamB = match.getTeamB();
        Team winnerTeam = match.getWinnerTeam();

        if (teamA != null || teamB != null) {
            // This is a team-based match
            dto.setTeamA(convertTeamToDTO(teamA));
            dto.setTeamB(convertTeamToDTO(teamB));
            dto.setWinnerTeam(winnerTeam != null ? convertTeamToDTO(winnerTeam) : null);
        }

        // Handle player-based matches (singles) - for backward compatibility
        User playerA = match.getPlayerA();
        User playerB = match.getPlayerB();
        User winner = match.getWinner();

        if (playerA != null || playerB != null) {
            // Use SeededUserDTO if event is provided, otherwise regular UserDTO
            UserDTO playerADTO = createUserDTO(playerA, event);
            UserDTO playerBDTO = createUserDTO(playerB, event);
            UserDTO winnerDTO = createUserDTO(winner, event);

            dto.setPlayerA(playerADTO);
            dto.setPlayerB(playerBDTO);
            dto.setWinner(winner != null ? winnerDTO : null);
        }

        return dto;
    }

    // Helper method to convert Team to TeamDTO
    private TeamDTO convertTeamToDTO(Team team) {
        if (team == null) return null;

        UserDTO player1DTO = null;
        if (team.getPlayer1() != null) {
            player1DTO = new UserDTO();
            player1DTO.setId(team.getPlayer1().getId());
            player1DTO.setUsername(team.getPlayer1().getUsername());
            player1DTO.setName(team.getPlayer1().getName());
            player1DTO.setTournaments(null);
        }

        UserDTO player2DTO = null;
        if (team.getPlayer2() != null) {
            player2DTO = new UserDTO();
            player2DTO.setId(team.getPlayer2().getId());
            player2DTO.setUsername(team.getPlayer2().getUsername());
            player2DTO.setName(team.getPlayer2().getName());
            player2DTO.setTournaments(null);
        }

        return new TeamDTO(
            team.getId(),
            player1DTO,
            player2DTO,
            team.getTeamType().name(),
            team.getTeamName()
        );
    }

    // Helper to create UserDTO with optional seed information
    private UserDTO createUserDTO(User user, BaseEvent event) {
        if (user == null) {
            UserDTO emptyDto = new UserDTO();
            emptyDto.setTournaments(null);
            return emptyDto;
        }

        // Check if we should include seed information
        if (event != null && event.getPlayerSeeds() != null) {
            Integer seed = event.getPlayerSeed(user.getId());
            SeededUserDTO dto = new SeededUserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setName(user.getName());
            dto.setSeed(seed);
            dto.setTournaments(null); // Prevent circular reference
            return dto;
        } else {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setName(user.getName());
            dto.setTournaments(null); // Prevent circular reference
            return dto;
        }
    }

    // ==================== EDITOR MANAGEMENT ENDPOINTS ====================

    @GetMapping("/{id}/owner")
    public ResponseEntity<?> getTournamentOwner(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.getTournament(id);
            User owner = tournament.getOwner();
            if (owner == null) {
                return ResponseEntity.ok().body("No owner assigned");
            }
            UserDTO ownerDTO = new UserDTO();
            ownerDTO.setId(owner.getId());
            ownerDTO.setUsername(owner.getUsername());
            ownerDTO.setName(owner.getName());
            return ResponseEntity.ok(ownerDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/editors")
    public ResponseEntity<?> getTournamentEditors(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.getTournament(id);
            List<UserDTO> editorDTOs = new ArrayList<>();
            for (User editor : tournament.getAuthorizedEditors()) {
                UserDTO dto = new UserDTO();
                dto.setId(editor.getId());
                dto.setUsername(editor.getUsername());
                dto.setName(editor.getName());
                editorDTOs.add(dto);
            }
            return ResponseEntity.ok(editorDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/editors")
    public ResponseEntity<?> addTournamentEditor(@PathVariable Long id, @RequestBody UserGetRequest editorRequest) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.addAuthorizedEditor(id, editorRequest.getId(), currentUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/editors/{editorId}")
    public ResponseEntity<?> removeTournamentEditor(@PathVariable Long id, @PathVariable Long editorId) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.removeAuthorizedEditor(id, editorId, currentUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== POINTS DISTRIBUTION ENDPOINTS ====================

    /**
     * Set or update points distribution for a specific event
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/points-distribution
     * Body: { "pointsMap": { "1": 10000, "2": 8000, "3": 6000, "5": 4000, "9": 2000 } }
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/points-distribution")
    public ResponseEntity<?> setPointsDistribution(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody PointsDistributionRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            PointsDistribution pointsDistribution = tournamentService.setPointsDistribution(
                    tournamentId, eventIndex, request.getPointsMap());

            PointsDistributionDTO dto = new PointsDistributionDTO();
            dto.setId(pointsDistribution.getId());
            dto.setTournamentId(tournamentId);
            dto.setEventIndex(eventIndex);
            dto.setPointsMap(pointsDistribution.getPointsMap());

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get points distribution for a specific event
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/points-distribution
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/points-distribution")
    public ResponseEntity<?> getPointsDistribution(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex) {
        try {
            PointsDistribution pointsDistribution = tournamentService.getPointsDistribution(tournamentId, eventIndex);

            if (pointsDistribution == null) {
                return ResponseEntity.ok().body(null);
            }

            PointsDistributionDTO dto = new PointsDistributionDTO();
            dto.setId(pointsDistribution.getId());
            dto.setTournamentId(tournamentId);
            dto.setEventIndex(eventIndex);
            dto.setPointsMap(pointsDistribution.getPointsMap());

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all points distributions for a tournament
     * GET /api/tournaments/{tournamentId}/points-distributions
     */
    @GetMapping("/{tournamentId}/points-distributions")
    public ResponseEntity<?> getAllPointsDistributions(@PathVariable Long tournamentId) {
        try {
            List<PointsDistribution> pointsDistributions = tournamentService.getAllPointsDistributions(tournamentId);
            List<PointsDistributionDTO> dtos = new ArrayList<>();

            for (PointsDistribution pd : pointsDistributions) {
                PointsDistributionDTO dto = new PointsDistributionDTO();
                dto.setId(pd.getId());
                dto.setTournamentId(tournamentId);
                dto.setEventIndex(pd.getEvent().getIndex());
                dto.setPointsMap(pd.getPointsMap());
                dtos.add(dto);
            }

            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Calculate and get points for a specific event
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/points
     * Returns: { "userId": points, ... }
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/points")
    public ResponseEntity<?> calculateEventPoints(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            Map<User, Integer> userPoints = tournamentService.calculateEventPoints(tournamentId, eventIndex);

            // Convert to proper JSON format: List of {user: UserDTO, points: Integer}
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<User, Integer> entry : userPoints.entrySet()) {
                User user = entry.getKey();
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUsername(user.getUsername());
                userDTO.setName(user.getName());

                Map<String, Object> playerPoints = new TreeMap<>();
                playerPoints.put("user", userDTO);
                playerPoints.put("points", entry.getValue());
                result.add(playerPoints);
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Calculate and get cumulative tournament points across all events
     * GET /api/tournaments/{tournamentId}/cumulative-points
     * Returns: Map of UserDTO -> total points across all events
     */
    @GetMapping("/{tournamentId}/cumulative-points")
    public ResponseEntity<?> calculateCumulativeTournamentPoints(@PathVariable Long tournamentId) {
        try {
            Map<User, Integer> cumulativePoints = tournamentService.calculateCumulativeTournamentPoints(tournamentId);

            // Convert to proper JSON format: List of {user: UserDTO, points: Integer}
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<User, Integer> entry : cumulativePoints.entrySet()) {
                User user = entry.getKey();
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUsername(user.getUsername());
                userDTO.setName(user.getName());

                Map<String, Object> playerPoints = new TreeMap<>();
                playerPoints.put("user", userDTO);
                playerPoints.put("points", entry.getValue());
                result.add(playerPoints);
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== SEEDING ENDPOINTS ====================

    /**
     * Manually set seeds for an event
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/seeds/manual
     * Body: { "playerSeeds": { "userId": seedNumber, ... } }
     * Example: { "playerSeeds": { "1": 1, "2": 2, "3": 3, "4": 4 } }
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/seeds/manual")
    public ResponseEntity<?> setManualSeeds(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody ManualSeedingRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.setManualSeeds(tournamentId, eventIndex, request.getPlayerSeeds());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Automatically set seeds from league rankings
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/seeds/auto
     * Body: { "numberOfSeeds": 8 }
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/seeds/auto")
    public ResponseEntity<?> setAutoSeeds(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody AutoSeedingRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.setAutoSeeds(tournamentId, eventIndex, request.getNumberOfSeeds());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get current seeds for an event
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/seeds
     * Returns: { "userId": seedNumber, ... }
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/seeds")
    public ResponseEntity<?> getSeeds(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            return ResponseEntity.ok(event.getPlayerSeeds());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Clear all seeds for an event
     * DELETE /api/tournaments/{tournamentId}/event/{eventIndex}/seeds
     */
    @DeleteMapping("/{tournamentId}/event/{eventIndex}/seeds")
    public ResponseEntity<?> clearSeeds(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            event.clearSeeds();
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Manually set team seeds for a doubles event
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/seeds/teams/manual
     * Body: { "teamSeeds": { "teamId": seedNumber, ... } }
     * Example: { "teamSeeds": { "1": 1, "2": 2, "3": 3, "4": 4 } }
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/seeds/teams/manual")
    public ResponseEntity<?> setManualTeamSeeds(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            @SuppressWarnings("unchecked")
            Map<String, Integer> teamSeedsStr = (Map<String, Integer>) request.get("teamSeeds");
            Map<Long, Integer> teamSeeds = new TreeMap<>();
            for (Map.Entry<String, Integer> entry : teamSeedsStr.entrySet()) {
                teamSeeds.put(Long.parseLong(entry.getKey()), entry.getValue());
            }

            tournamentService.setManualTeamSeeds(tournamentId, eventIndex, teamSeeds);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Automatically set team seeds from league rankings (averaged from both players)
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/seeds/teams/auto
     * Body: { "numberOfSeeds": 8 }
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/seeds/teams/auto")
    public ResponseEntity<?> setAutoTeamSeeds(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody AutoSeedingRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.setAutoTeamSeeds(tournamentId, eventIndex, request.getNumberOfSeeds());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get current team seeds for a doubles event
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/seeds/teams
     * Returns: { "teamId": seedNumber, ... }
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/seeds/teams")
    public ResponseEntity<?> getTeamSeeds(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            return ResponseEntity.ok(event.getTeamSeeds());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== EVENT REGISTRATION ENDPOINTS ====================

    /**
     * Sign up for an event (any authenticated user)
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/signup
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/signup")
    public ResponseEntity<?> signUpForEvent(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody(required = false) EventSignupRequest request) {
        try {
            User currentUser = getCurrentUser();
            String desiredPartner = (request != null) ? request.getDesiredPartner() : null;
            EventRegistration registration = tournamentService.signUpForEvent(tournamentId, eventIndex, currentUser, desiredPartner);

            EventRegistrationDTO dto = new EventRegistrationDTO(registration);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cancel your own registration
     * DELETE /api/tournaments/{tournamentId}/event/{eventIndex}/signup
     */
    @DeleteMapping("/{tournamentId}/event/{eventIndex}/signup")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.cancelRegistration(tournamentId, eventIndex, currentUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all registrations for an event (moderators only)
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/registrations
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/registrations")
    public ResponseEntity<?> getEventRegistrations(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            List<EventRegistration> registrations = tournamentService.getEventRegistrations(tournamentId, eventIndex);
            List<EventRegistrationDTO> dtos = registrations.stream()
                .map(EventRegistrationDTO::new)
                .toList();

            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get pending registrations for an event (moderators only)
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/pending
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/registrations/pending")
    public ResponseEntity<?> getPendingRegistrations(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            List<EventRegistration> registrations = tournamentService.getPendingRegistrations(tournamentId, eventIndex);
            List<EventRegistrationDTO> dtos = registrations.stream()
                .map(EventRegistrationDTO::new)
                .toList();

            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all registrations (pending and approved) for an event (moderators only)
     * This includes desired partner information even after approval
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/all
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/registrations/all")
    public ResponseEntity<?> getAllRegistrations(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            List<EventRegistration> registrations = tournamentService.getAllRegistrations(tournamentId, eventIndex);
            List<EventRegistrationDTO> dtos = registrations.stream()
                .map(EventRegistrationDTO::new)
                .toList();

            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Approve multiple registrations (moderators only)
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/approve
     * Body: { "registrationIds": [1, 2, 3] }
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/registrations/approve")
    public ResponseEntity<?> approveRegistrations(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody ApproveRegistrationsRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.approveRegistrations(tournamentId, eventIndex, request.getRegistrationIds(), currentUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Reject a registration (moderators only)
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/{registrationId}/reject
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/registrations/{registrationId}/reject")
    public ResponseEntity<?> rejectRegistration(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @PathVariable Long registrationId) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.rejectRegistration(tournamentId, eventIndex, registrationId, currentUser);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Create a team from two players for a doubles event (moderators only)
     * POST /api/tournaments/{tournamentId}/event/{eventIndex}/create-team
     */
    @PostMapping("/{tournamentId}/event/{eventIndex}/create-team")
    public ResponseEntity<?> createTeam(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @RequestBody CreateTeamRequest request) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            Team team = tournamentService.createTeam(tournamentId, eventIndex, request.getPlayer1Id(), request.getPlayer2Id());
            return ResponseEntity.ok(team);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all teams for an event
     * GET /api/tournaments/{tournamentId}/event/{eventIndex}/teams
     */
    @GetMapping("/{tournamentId}/event/{eventIndex}/teams")
    public ResponseEntity<?> getTeams(@PathVariable Long tournamentId, @PathVariable int eventIndex) {
        try {
            BaseEvent event = tournamentService.getEventsForTournament(tournamentId).get(eventIndex);
            List<Team> teams = tournamentService.getTeamsForEvent(event);

            // Convert to DTOs to avoid circular reference issues
            List<TeamDTO> teamDTOs = teams.stream()
                .map(team -> {
                    UserDTO player1DTO = null;
                    if (team.getPlayer1() != null) {
                        player1DTO = new UserDTO();
                        player1DTO.setId(team.getPlayer1().getId());
                        player1DTO.setUsername(team.getPlayer1().getUsername());
                        player1DTO.setName(team.getPlayer1().getName());
                        player1DTO.setTournaments(null);
                    }

                    UserDTO player2DTO = null;
                    if (team.getPlayer2() != null) {
                        player2DTO = new UserDTO();
                        player2DTO.setId(team.getPlayer2().getId());
                        player2DTO.setUsername(team.getPlayer2().getUsername());
                        player2DTO.setName(team.getPlayer2().getName());
                        player2DTO.setTournaments(null);
                    }

                    return new TeamDTO(
                        team.getId(),
                        player1DTO,
                        player2DTO,
                        team.getTeamType().name(),
                        team.getTeamName()
                    );
                })
                .toList();

            return ResponseEntity.ok(teamDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a team from an event (before event is initialized)
     * DELETE /api/tournaments/{tournamentId}/event/{eventIndex}/teams/{teamId}
     */
    @DeleteMapping("/{tournamentId}/event/{eventIndex}/teams/{teamId}")
    public ResponseEntity<?> deleteTeam(
            @PathVariable Long tournamentId,
            @PathVariable int eventIndex,
            @PathVariable Long teamId) {
        try {
            User currentUser = getCurrentUser();
            tournamentService.verifyEditPermission(tournamentId, currentUser);

            tournamentService.deleteTeam(tournamentId, eventIndex, teamId);
            return ResponseEntity.ok("Team deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete team: " + e.getMessage());
        }
    }
}