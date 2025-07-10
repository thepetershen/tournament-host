package com.tournamenthost.connect.frontend.with.backend.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * Thought this is called Tournament, it implements a single elim tournament, this will be changed later
 */
@Entity
@Data
@Table(name = "tournaments")
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    private List<String> players;

    @OneToMany
    private List<Match> allMatches;

    @OneToOne(mappedBy = "tournament")
    private Match rootMatch;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    private List<Round> rounds;

    public void addPlayer(String player) {
        players.add(player);
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }
}