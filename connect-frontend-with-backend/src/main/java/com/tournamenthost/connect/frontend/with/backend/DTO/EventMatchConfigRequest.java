package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMatchConfigRequest {
    private String matchType; // "SINGLES" or "DOUBLES"
    private int gamesPerMatch; // 1, 3, 5, etc.
}
