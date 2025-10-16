package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.League;

public interface LeagueRepository extends CrudRepository<League, Long> {
    
}
