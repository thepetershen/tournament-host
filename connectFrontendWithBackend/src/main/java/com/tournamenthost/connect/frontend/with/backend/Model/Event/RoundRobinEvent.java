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
@DiscriminatorValue("ROUND_ROBIN")
public class RoundRobinEvent extends BaseEvent {
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerSchedule> playerSchedules;

    public List<PlayerSchedule> getPlayerSchedules() {
        return playerSchedules;
    }

    public void addPlayerSchedule(PlayerSchedule playerSchedule) {
        playerSchedules.add(playerSchedule);
    }

    public void addPlayerSchedule(List<PlayerSchedule> playerSchedules) {
        for (PlayerSchedule schedule : playerSchedules) {
            this.addPlayerSchedule(schedule);
        }
    }

    public RoundRobinEvent() {
        super();
        this.playerSchedules = new ArrayList<>();
    }

    public RoundRobinEvent(String name, List<User> players, Tournament tournament, int index) {
        super(name, players, tournament, index);
        this.playerSchedules = new ArrayList<>();
    }
}