package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;

@Entity
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Many users can participate in many tournaments
    @ManyToMany
    @JoinTable(
        name = "tournament_users",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    // One tournament can have many events
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BaseEvent> events;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUsers(User user) {
        this.users.add(user);
    }
    
    public List<BaseEvent> getEvents() {
        return events;
    }

    public void addEvent(BaseEvent event) {
        this.events.add(event);
    }

    // Constructors
    public Tournament() {
        this.users = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public Tournament(String name) {
        this.name = name;
        this.users = new ArrayList<>();
        this.events = new ArrayList<>();
    }
}