package com.tournamenthost.connect.frontend.with.backend.Repository;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;

public interface EventRepository extends CrudRepository<BaseEvent, Long>{
    
}
