package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;

public interface TournamentRepository extends CrudRepository<Tournament, Long>{
    default boolean existsByNameIgnoreCaseAndSpaces(String name) {
        for (Tournament t : findAll()) {
            if (t.getName() != null &&
                t.getName().replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                return true;
            }
        }
        return false;
    }
}
