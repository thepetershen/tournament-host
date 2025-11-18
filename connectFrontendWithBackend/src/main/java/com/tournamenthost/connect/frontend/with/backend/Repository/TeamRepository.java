package com.tournamenthost.connect.frontend.with.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tournamenthost.connect.frontend.with.backend.Model.Team;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Find all teams for a specific event
     */
    List<Team> findByEvent(BaseEvent event);

    /**
     * Find all teams that include a specific player
     */
    @Query("SELECT t FROM Team t WHERE t.player1 = :player OR t.player2 = :player")
    List<Team> findByPlayer(@Param("player") User player);

    /**
     * Find all teams for a specific event that include a specific player
     */
    @Query("SELECT t FROM Team t WHERE t.event = :event AND (t.player1 = :player OR t.player2 = :player)")
    List<Team> findByEventAndPlayer(@Param("event") BaseEvent event, @Param("player") User player);

    /**
     * Check if a team with specific players exists in an event
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t " +
           "WHERE t.event = :event AND t.player1 = :player1 AND " +
           "(t.player2 = :player2 OR (:player2 IS NULL AND t.player2 IS NULL))")
    boolean existsByEventAndPlayers(@Param("event") BaseEvent event,
                                     @Param("player1") User player1,
                                     @Param("player2") User player2);

    /**
     * Delete all teams for a specific event
     */
    void deleteByEvent(BaseEvent event);
}
