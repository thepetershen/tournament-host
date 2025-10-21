package com.tournamenthost.connect.frontend.with.backend.Repository;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventRegistration;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends CrudRepository<EventRegistration, Long> {

    List<EventRegistration> findByEvent(BaseEvent event);

    List<EventRegistration> findByEventAndStatus(BaseEvent event, EventRegistration.RegistrationStatus status);

    Optional<EventRegistration> findByEventAndUser(BaseEvent event, User user);

    boolean existsByEventAndUser(BaseEvent event, User user);
}
