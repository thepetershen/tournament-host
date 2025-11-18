package com.tournamenthost.connect.frontend.with.backend.Repository;

import com.tournamenthost.connect.frontend.with.backend.Model.PointsDistribution;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PointsDistributionRepository extends CrudRepository<PointsDistribution, Long> {
    Optional<PointsDistribution> findByTournament(Tournament tournament);
    Optional<PointsDistribution> findByEvent(BaseEvent event);
    List<PointsDistribution> findAllByTournament(Tournament tournament);
}
