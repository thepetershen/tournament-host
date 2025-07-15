package com.tournamenthost.connect.frontend.with.backend.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
/*
 * Thought this is called Tournament, it implements a single elim tournament, this will be changed later
 */
@Entity
@Getter
@Table(name = "tournaments")
public class Tournament {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;

    public void setName(String name){
        this.name = name;
    }

    @ElementCollection
    private List<String> players;

    public void addPlayer(String player) {
        players.add(player);
    }

    public void addPlayer(List<String> players) {
        for(String player: players) {
            this.addPlayer(player);
        }
    }

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds;

    
    public void addRound(Round round) {
        rounds.add(round);
    }

    public void addRound(List<Round> rounds) {
        for (Round round : rounds) {
            this.addRound(round);
        }
    }


   public Tournament() {
        players = new ArrayList<>();
        rounds = new ArrayList<>();

   }

   public Tournament(final Long id, final String name, final List<String> players, final List<Match> allMatches, final Match rootMatch, final List<Round> rounds) {
      this.id = id;
      this.name = name;
      this.players = players;
      this.rounds = rounds;
   }
}