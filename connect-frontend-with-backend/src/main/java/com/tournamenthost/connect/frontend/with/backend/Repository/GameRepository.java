package com.tournamenthost.connect.frontend.with.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tournamenthost.connect.frontend.with.backend.Model.Game;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Find all games for a specific match, ordered by game number
     */
    List<Game> findByMatchOrderByGameNumberAsc(Match match);

    /**
     * Delete all games for a specific match
     */
    void deleteByMatch(Match match);
}
