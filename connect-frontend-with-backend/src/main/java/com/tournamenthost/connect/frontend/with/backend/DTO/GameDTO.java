package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    private Long id;
    private int gameNumber;
    private int teamAScore;
    private int teamBScore;
    private Long winnerTeamId; // ID of winning team, null if tie or incomplete
}
