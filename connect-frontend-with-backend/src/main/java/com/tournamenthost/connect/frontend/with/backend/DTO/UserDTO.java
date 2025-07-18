package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.List;

public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private List<TournamentDTO> tournaments;

    public UserDTO() {}

    public UserDTO(Long id, String email, String username, List<TournamentDTO> tournaments) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.tournaments = tournaments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<TournamentDTO> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<TournamentDTO> tournaments) {
        this.tournaments = tournaments;
    }
}