package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.List;

/**
 * Extended UserDTO that includes seed information for tournament draws
 */
public class SeededUserDTO extends UserDTO {
    private Integer seed;

    public SeededUserDTO() {
        super();
    }

    public SeededUserDTO(Long id, String username, String name, List<TournamentDTO> tournaments, Integer seed) {
        super(id, username, name, tournaments);
        this.seed = seed;
    }

    public SeededUserDTO(UserDTO userDTO, Integer seed) {
        super(userDTO.getId(), userDTO.getUsername(), userDTO.getName(), userDTO.getTournaments());
        this.seed = seed;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public boolean isSeeded() {
        return seed != null && seed > 0;
    }
}
