package com.tournamenthost.connect.frontend.with.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT t FROM Tournament t WHERE LOWER(REPLACE(t.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))")
    List<Tournament> findByNameContainingIgnoreCaseAndSpaces(@Param("name") String name);
}
