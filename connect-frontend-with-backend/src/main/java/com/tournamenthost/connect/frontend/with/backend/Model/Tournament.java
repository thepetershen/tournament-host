package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;

@Entity
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Owner of the tournament (creator)
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // Users authorized to edit this tournament
    @ManyToMany
    @JoinTable(
        name = "tournament_editors",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> authorizedEditors = new HashSet<>();

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getAuthorizedEditors() {
        return authorizedEditors;
    }

    public void addAuthorizedEditor(User user) {
        this.authorizedEditors.add(user);
    }

    public void removeAuthorizedEditor(User user) {
        this.authorizedEditors.remove(user);
    }

    public boolean canUserEdit(User user) {
        if (user == null) return false;
        System.out.println(user.toString());
        return user.equals(owner) || authorizedEditors.contains(user);
    }

    // Constructors
    public Tournament() {
        this.events = new ArrayList<>();
        this.authorizedEditors = new HashSet<>();
    }

    public Tournament(String name) {
        this.name = name;
        this.events = new ArrayList<>();
        this.authorizedEditors = new HashSet<>();
    }

    public Tournament(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.events = new ArrayList<>();
        this.authorizedEditors = new HashSet<>();
    }
}