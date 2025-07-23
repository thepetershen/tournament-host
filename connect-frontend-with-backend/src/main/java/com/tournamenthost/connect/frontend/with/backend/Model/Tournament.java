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
    
    public List<BaseEvent> getEvents() {
        return events;
    }

    public void addEvent(BaseEvent event) {
        this.events.add(event);
    }

    // Constructors
    public Tournament() {
        this.events = new ArrayList<>();
    }

    public Tournament(String name) {
        this.name = name;
        this.events = new ArrayList<>();
    }
}