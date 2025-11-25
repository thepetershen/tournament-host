package com.tournamenthost.connect.frontend.with.backend.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;

public interface TournamentRepository extends CrudRepository<Tournament, Long>{
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM Tournament t WHERE LOWER(REPLACE(t.name, ' ', '')) = LOWER(REPLACE(:name, ' ', ''))")
    boolean existsByNameIgnoreCaseAndSpaces(@Param("name") String name);

    @Query("SELECT t FROM Tournament t WHERE LOWER(REPLACE(t.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))")
    List<Tournament> findByNameContainingIgnoreCaseAndSpaces(@Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Tournament t JOIN t.events e JOIN e.players p WHERE p = :player")
    List<Tournament> findAllTournamentsWithPlayer(@Param("player") User player);
}
