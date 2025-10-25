package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;

@Entity
public class Tournament implements Comparable<Tournament> {

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

    private String message;

    @Column(name = "\"begin\"")
    private Date begin;

    @Column(name = "\"end\"")
    private Date end;

    private String location;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    @Override
    public int compareTo(Tournament other) {
        // Sort by begin date in descending order (most recent first)
        if (this.begin == null && other.begin == null) return 0;
        if (this.begin == null) return 1;
        if (other.begin == null) return -1;
        return other.begin.compareTo(this.begin);
    }
}