package com.tournamenthost.connect.frontend.with.backend.Controller;

import com.tournamenthost.connect.frontend.with.backend.DTO.TournamentRequest;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Service.TournamentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentControllerTest {

    @Test
    void testCreateTournamentSuccess() {
        TournamentService service = mock(TournamentService.class);
        TournamentController controller = new TournamentController(service);

        TournamentRequest request = new TournamentRequest();
        request.setName("My Tournament");

        Tournament tournament = new Tournament();
        tournament.setName("My Tournament");

        when(service.addTournament("My Tournament")).thenReturn(tournament);

        ResponseEntity<?> response = controller.createTournament(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(tournament, response.getBody());
    }

    @Test
    void testCreateTournamentDuplicateName() {
        TournamentService service = mock(TournamentService.class);
        TournamentController controller = new TournamentController(service);

        TournamentRequest request = new TournamentRequest();
        request.setName("Duplicate Tournament");

        when(service.addTournament("Duplicate Tournament"))
            .thenThrow(new IllegalArgumentException("Tournament with name 'Duplicate Tournament' already exists"));

        ResponseEntity<?> response = controller.createTournament(request);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Tournament with name 'Duplicate Tournament' already exists", response.getBody());
    }
}
