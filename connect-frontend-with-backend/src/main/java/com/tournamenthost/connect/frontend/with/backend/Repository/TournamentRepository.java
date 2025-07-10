package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;

public interface TournamentRepository extends CrudRepository<Tournament, Long>{

    boolean existsByAllMatches_Id(Long matchId);
    
}
