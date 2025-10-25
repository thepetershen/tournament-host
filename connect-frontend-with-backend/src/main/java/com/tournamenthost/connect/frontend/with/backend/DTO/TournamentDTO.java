package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDTO {
    private String name;
    private Long id;
    private UserDTO owner;
    private List<UserDTO> authorizedEditors;
    private String message;
    private Date begin;
    private Date end;
    private String location;
    private Map<EventDTO, List<MatchDTO>> eventMatches;

    public TournamentDTO(String name, Long id) {
        this.name = name;
        this.id = id;
    }
}