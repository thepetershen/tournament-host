package com.tournamenthost.connect.frontend.with.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tournamenthost.connect.frontend.with.backend.Model.Team;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.TeamSchedule;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.RoundRobinEvent;

@Repository
public interface TeamScheduleRepository extends JpaRepository<TeamSchedule, Long> {

    /**
     * Find all schedules for a specific event
     */
    List<TeamSchedule> findByEvent(RoundRobinEvent event);

    /**
     * Find schedule for a specific team in an event
     */
    Optional<TeamSchedule> findByTeamAndEvent(Team team, RoundRobinEvent event);

    /**
     * Delete all schedules for a specific event
     */
    void deleteByEvent(RoundRobinEvent event);
}
