package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {
    private Long id;
    private UserDTO player1;
    private UserDTO player2; // null for singles
    private String teamType; // "SINGLES" or "DOUBLES"
    private String teamName; // Formatted name for display
}
