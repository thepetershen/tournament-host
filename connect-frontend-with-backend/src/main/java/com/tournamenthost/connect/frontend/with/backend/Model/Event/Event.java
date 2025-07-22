package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;

import java.util.List;

public interface Event {
    Long getId();
    String getName();
    void setName(String name);
    List<User> getPlayers();
    void addPlayer(User player);
    void addPlayer(List<User> players);
    Tournament getTournament();
    void setTournament(Tournament tournament);
    boolean isEventInitialized();
    void initializeEvent();
    void unInitiateEvent();
}