package com.tournamenthost.connect.frontend.with.backend.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tournamenthost.connect.frontend.with.backend.DTO.EventDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.MatchDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TournamentRepository tournamentRepo;

    @Autowired
    private TournamentService tournamentService;

    public User setUser(User user, String username, String name, String password) {
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepo.existsByUsername(username)) {
                throw new IllegalArgumentException("User with username " + username + " already exists");
            }
            user.setUsername(username);
        }

        if (name != null) {
            user.setName(name);
        }

        if (password != null && !password.isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(password));
        }

        return userRepo.save(user);
    }

    /**
     * Get complete user information including all tournaments and matches they participated in
     * Tournaments are ordered by most recent first
     */
    public UserDTO getUserWithTournamentsAndMatches(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + userId + " not found"));

        // Get all tournaments where this player participated
        List<Tournament> tournaments = tournamentRepo.findAllTournamentsWithPlayer(user);

        // Sort tournaments by most recent first (using Tournament's compareTo)
        Collections.sort(tournaments);

        // Build tournament DTOs with event-match mappings
        List<TournamentDTO> tournamentDTOs = new ArrayList<>();

        for (Tournament tournament : tournaments) {
            TournamentDTO tournamentDTO = new TournamentDTO();
            tournamentDTO.setId(tournament.getId());
            tournamentDTO.setName(tournament.getName());
            tournamentDTO.setMessage(tournament.getMessage());
            tournamentDTO.setBegin(tournament.getBegin());
            tournamentDTO.setEnd(tournament.getEnd());
            tournamentDTO.setLocation(tournament.getLocation());

            // Get all matches for this player in this tournament
            Map<BaseEvent, List<Match>> eventMatchesMap = tournamentService.getPlayerMatchesForTournament(tournament.getId(), userId);

            // Convert to DTO map
            Map<EventDTO, List<MatchDTO>> eventMatchDTOMap = new LinkedHashMap<>();
            for (Map.Entry<BaseEvent, List<Match>> entry : eventMatchesMap.entrySet()) {
                BaseEvent event = entry.getKey();
                List<Match> matches = entry.getValue();

                // Create EventDTO
                EventDTO eventDTO = new EventDTO(event.getName(), event.getIndex());

                // Create MatchDTOs
                List<MatchDTO> matchDTOs = matches.stream()
                    .map(match -> convertToMatchDTO(match))
                    .collect(Collectors.toList());

                eventMatchDTOMap.put(eventDTO, matchDTOs);
            }

            tournamentDTO.setEventMatches(eventMatchDTOMap);
            tournamentDTOs.add(tournamentDTO);
        }

        // Create UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setName(user.getName());
        userDTO.setTournaments(tournamentDTOs);

        return userDTO;
    }

    /**
     * Convert Match entity to MatchDTO
     */
    private MatchDTO convertToMatchDTO(Match match) {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(match.getId());
        matchDTO.setCompleted(match.isCompleted());
        matchDTO.setScore(match.getScore());

        // Convert players to simple UserDTOs (without tournaments to avoid circular reference)
        if (match.getPlayerA() != null) {
            UserDTO playerADTO = new UserDTO();
            playerADTO.setId(match.getPlayerA().getId());
            playerADTO.setUsername(match.getPlayerA().getUsername());
            playerADTO.setName(match.getPlayerA().getName());
            matchDTO.setPlayerA(playerADTO);
        }

        if (match.getPlayerB() != null) {
            UserDTO playerBDTO = new UserDTO();
            playerBDTO.setId(match.getPlayerB().getId());
            playerBDTO.setUsername(match.getPlayerB().getUsername());
            playerBDTO.setName(match.getPlayerB().getName());
            matchDTO.setPlayerB(playerBDTO);
        }

        if (match.getWinner() != null) {
            UserDTO winnerDTO = new UserDTO();
            winnerDTO.setId(match.getWinner().getId());
            winnerDTO.setUsername(match.getWinner().getUsername());
            winnerDTO.setName(match.getWinner().getName());
            matchDTO.setWinner(winnerDTO);
        }

        return matchDTO;
    }

}   
