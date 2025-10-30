package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.ArrayList;
import java.util.List;

public class LeagueDTO {
    private Long id;
    private String name;
    private UserDTO owner;
    private List<UserDTO> authorizedEditors;
    private List<TournamentDTO> tournaments;
    private int tournamentCount;
    private int playerCount;

    public LeagueDTO() {
        this.authorizedEditors = new ArrayList<>();
        this.tournaments = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    public List<UserDTO> getAuthorizedEditors() {
        return authorizedEditors;
    }

    public void setAuthorizedEditors(List<UserDTO> authorizedEditors) {
        this.authorizedEditors = authorizedEditors;
    }

    public List<TournamentDTO> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<TournamentDTO> tournaments) {
        this.tournaments = tournaments;
    }

    public int getTournamentCount() {
        return tournamentCount;
    }

    public void setTournamentCount(int tournamentCount) {
        this.tournamentCount = tournamentCount;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
}
