package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SINGLE_ELIM")
public class SingleElimEvent extends BaseEvent{
    

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds;

    
    public void addRound(Round round) {
        rounds.add(round);
    }

    public void addRound(List<Round> rounds) {
        for (Round round : rounds) {
            this.addRound(round);
        }
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public SingleElimEvent() {
        super();
        rounds = new ArrayList<>();

    }

    public SingleElimEvent(final String name, final List<User> players, final List<Round> rounds, Tournament tournament) {
        super(name, players, tournament);
        this.rounds = rounds;
    }
}