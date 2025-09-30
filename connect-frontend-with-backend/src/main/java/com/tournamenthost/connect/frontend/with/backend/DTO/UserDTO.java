package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.List;

public class UserDTO implements Comparable<UserDTO>{
    private Long id;
    private String username;
    private String name;
    private List<TournamentDTO> tournaments;

    public UserDTO() {}

    public UserDTO(Long id, String username, String name, List<TournamentDTO> tournaments) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.tournaments = tournaments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TournamentDTO> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<TournamentDTO> tournaments) {
        this.tournaments = tournaments;
    }

    @Override
    public int compareTo(UserDTO other) {
        if (this.name == null && other.name == null) return 0;
        if (this.name == null) return -1;
        if (other.name == null) return 1;
        return this.name.compareToIgnoreCase(other.name);
    }
}