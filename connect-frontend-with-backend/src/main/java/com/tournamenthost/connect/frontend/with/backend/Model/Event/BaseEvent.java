package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type")
public abstract class BaseEvent implements Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    public Long getId(){
        return id;
    }

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private String name;

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @ManyToMany
    @JoinTable(
        name = "user_events",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> players;

    public void addPlayer(User player) {
        players.add(player);
    }

    public void addPlayer(List<User> players) {
        for(User player: players) {
            this.addPlayer(player);
        }
    }

    public List<User> getPlayers(){
        return players;
    }

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    public void setTournament(Tournament tournament){
        this.tournament = tournament;
    }

    public Tournament getTournament(){
        return tournament;
    }

    private boolean initialized;

    public boolean isEventInitialized(){
        return initialized;
    }

    public void initializeEvent(){
        this.initialized = true;
    }

    public void unInitiateEvent(){
        this.initialized =false;
    }

    public BaseEvent() {
        // Initialize collections to avoid NullPointerException
        this.players = new ArrayList<>();
        this.initialized = false;
    }

    // Optionally, add a constructor with arguments if needed
    public BaseEvent(String name, List<User> players, Tournament tournament, int index) {
        this.initialized =false;
        this.name = name;
        this.players = (players != null) ? players : new ArrayList<>();
        this.tournament = tournament;
        this.index = index;
    }


    
}
