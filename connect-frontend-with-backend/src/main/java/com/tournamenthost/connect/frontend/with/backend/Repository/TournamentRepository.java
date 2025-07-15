package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;

public interface TournamentRepository extends CrudRepository<Tournament, Long>{
    
    // Custom method to check for tournament name existence (case and space insensitive)
    default boolean existsByNameIgnoreCaseAndSpaces(String name) {
        for (Tournament t : findAll()) {
            if (t.getName() != null &&
                t.getName().replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                return true;
            }
        }
        return false;
    }
    
    // Checks if a tournament is initialized (rootMatch exists or matches exist)
    default boolean isTournamentInitialized(Long tournamentId) {
        Tournament t = findById(tournamentId).orElse(null);
        if (t == null) return false;
        return t.getRounds() != null && !t.getRounds().isEmpty();
    }
}
