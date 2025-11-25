package com.tournamenthost.connect.frontend.with.backend.Controller;

import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public SearchController(UserRepository userRepository, TournamentRepository tournamentRepository) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> unifiedSearch(@RequestParam("query") String query) {
        // Limit search results to 20 for performance
        Pageable limit = PageRequest.of(0, 20);

        // Search players by username or name
        List<User> users = userRepository.findByUsernameOrNameContainingIgnoreCaseAndSpaces(query, limit);
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setName(user.getName());
            userDTOs.add(dto);
        }
        // Search tournaments by name
        List<Tournament> tournaments = tournamentRepository.findByNameContainingIgnoreCaseAndSpaces(query, limit);
        List<TournamentDTO> tournamentDTOs = new ArrayList<>();
        for (Tournament t : tournaments) {
            TournamentDTO dto = new TournamentDTO();
            dto.setId(t.getId());
            dto.setName(t.getName());
            tournamentDTOs.add(dto);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("players", userDTOs);
        result.put("tournaments", tournamentDTOs);
        return ResponseEntity.ok(result);
    }
}
