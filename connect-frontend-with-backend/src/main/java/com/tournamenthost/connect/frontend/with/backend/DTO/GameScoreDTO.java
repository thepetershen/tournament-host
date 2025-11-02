package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameScoreDTO {
    private List<Integer> scores; // Flat array: [teamA_game1, teamB_game1, teamA_game2, teamB_game2, ...]
}
