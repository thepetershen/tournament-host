package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawResponseDTO {
    private String eventType;
    private Object draw;
    // Single Elim: List<List<MatchDTO>>
    // Round Robin: List<List<Object>> where each inner list is [UserDTO, MatchDTO, MatchDTO, ...]
    // Double Elim: Map<String, List<List<MatchDTO>>> with keys "winners" and "losers"
}