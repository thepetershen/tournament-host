package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDTO {
    private String name;
    private Long id;
    private UserDTO owner;
    private List<UserDTO> authorizedEditors;

    public TournamentDTO(String name, Long id) {
        this.name = name;
        this.id = id;
    }
}