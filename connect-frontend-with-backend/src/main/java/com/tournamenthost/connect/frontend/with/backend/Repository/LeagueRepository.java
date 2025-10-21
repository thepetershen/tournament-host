package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.League;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;

import java.util.List;

public interface LeagueRepository extends CrudRepository<League, Long> {
    List<League> findByTournamentsContaining(Tournament tournament);
}
