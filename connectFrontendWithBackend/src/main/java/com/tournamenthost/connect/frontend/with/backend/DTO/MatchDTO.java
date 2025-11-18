package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;

    // NEW: Team-based fields
    private TeamDTO teamA;
    private TeamDTO teamB;
    private TeamDTO winnerTeam;
    private List<GameDTO> games;
    private String matchType; // "SINGLES" or "DOUBLES"
    private int gamesRequiredToWin;

    // OLD: Player-based fields (kept for backward compatibility)
    private UserDTO playerA;
    private UserDTO playerB;
    private UserDTO winner;
    private List<Integer> score;

    private boolean completed;
}