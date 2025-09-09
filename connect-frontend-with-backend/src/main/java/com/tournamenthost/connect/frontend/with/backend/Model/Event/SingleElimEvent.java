package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import java.util.ArrayList;
import java.util.List;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
@DiscriminatorValue("SINGLE_ELIM")
public class SingleElimEvent extends BaseEvent {
    private int index; // Unique within tournament

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

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

    public SingleElimEvent(final String name, final List<User> players, final List<Round> rounds, Tournament tournament, int index) {
        super(name, players, tournament);
        this.rounds = rounds;
        this.index = index;
    }
}