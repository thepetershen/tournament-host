package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawResponseDTO {
    private String eventType;
    private List<List<Object>> draw; // For Single Elim: List<List<MatchDTO>>, For Round Robin: List<List<Object>> where each inner list is [UserDTO, MatchDTO, MatchDTO, ...]
}