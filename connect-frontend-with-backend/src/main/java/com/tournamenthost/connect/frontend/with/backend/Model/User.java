package com.tournamenthost.connect.frontend.with.backend.Model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ManyToAny;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String username;

    private String password;

    @ManyToMany
    private List<Tournament> tournaments;

    public Long getId(){
        return id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Tournament> getTournaments() {
        return tournaments;
    }

    public void addTournament(Tournament tournament) {
        if (this.tournaments != null) {
            this.tournaments.add(tournament);
        }
    }

    public User () {

    }

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.tournaments = new ArrayList<>();
    }

}
