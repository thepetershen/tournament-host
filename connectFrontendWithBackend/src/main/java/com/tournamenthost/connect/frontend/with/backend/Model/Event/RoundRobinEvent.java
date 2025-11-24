package com.tournamenthost.connect.frontend.with.backend.Model.Event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore  // Prevent circular reference when serializing event
    private List<TeamSchedule> teamSchedules;

    public List<TeamSchedule> getTeamSchedules() {
        return teamSchedules;
    }

    public void addTeamSchedule(TeamSchedule teamSchedule) {
        teamSchedules.add(teamSchedule);
    }

    public void addTeamSchedule(List<TeamSchedule> teamSchedules) {
        for (TeamSchedule schedule : teamSchedules) {
            this.addTeamSchedule(schedule);
        }
    }

    public RoundRobinEvent() {
        super();
        this.teamSchedules = new ArrayList<>();
    }

    public RoundRobinEvent(String name, List<User> players, Tournament tournament, int index) {
        super(name, players, tournament, index);
        this.teamSchedules = new ArrayList<>();
    }
}