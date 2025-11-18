package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;

public interface MatchRepository extends CrudRepository<Match, Long>{
    
}
