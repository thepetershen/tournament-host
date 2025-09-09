package com.tournamenthost.connect.frontend.with.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.EventRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.Round;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.util.TournamentUtil;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MatchRepository matchRepo;

    public Tournament createTournament(String name) {
        if (tournamentRepo.existsByNameIgnoreCaseAndSpaces(name)) {
            throw new IllegalArgumentException("Tournament with name '" + name + "' already exists");
        }
        Tournament tournament = new Tournament(name);
        return tournamentRepo.save(tournament);
    }

    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        tournamentRepo.findAll().forEach(tournaments::add);
        return tournaments;
    }

    public Tournament getTournament(Long id) {
        return tournamentRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }

    public Set<User> getAllPlayers(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));


        Set<User> answer = new HashSet<>();
        for(BaseEvent cur: tournament.getEvents()){
            for(User curUser: cur.getPlayers()) {
                answer.add(curUser);
            }
        }

        return answer;
    }

    public List<Match> getAllMatches(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        List<Match> answer = new ArrayList<>();
        for (BaseEvent cur : tournament.getEvents()) {
            answer.addAll(getMatchesForEvent(tournamentId, cur.getIndex()));
        }
        return answer;
    }
    // Event-related methods merged from EventService
    public BaseEvent addEvent(String name, EventType eventType, Long tournamentId) {
        BaseEvent event;
        switch (eventType) {
            case SINGLE_ELIM -> event = new SingleElimEvent();
            default -> throw new IllegalArgumentException("Unsupported event type");
        }
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        event.setName(name);
        event.setTournament(tournament);
        event.setIndex(tournament.getEvents().size());
        tournament.addEvent(event);
        eventRepo.save(event);
        tournamentRepo.save(tournament);
        return event;
    }

    public void addPlayer(Long tournamentId, int eventIndex, Long playerId) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        User user = userRepo.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + playerId + " not found"));
        if (event.isEventInitialized()) {
            throw new IllegalArgumentException("Event has already been initiated");
        }
        event.addPlayer(user);
        eventRepo.save(event);
    }

    public List<User> getPlayers(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        return event.getPlayers();
    }

    public List<Match> getMatchesForEvent(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        if (!event.isEventInitialized()) {
            throw new IllegalArgumentException("This event hasn't been initialized, please initialize it first");
        }
        if (event instanceof SingleElimEvent singleElim) {
            List<Round> rounds = singleElim.getRounds();
            List<Match> answer = new ArrayList<>();
            for (Round r : rounds) {
                answer.addAll(r.getMatches());
            }
            return answer;
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
    }

    public void initializeEvent(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        List<User> players = event.getPlayers();
        if (players.size() <= 2) {
            throw new IllegalArgumentException("There are too few players, the beginning of this event demands at least 3 players");
        }
        if (event.isEventInitialized()) {
            throw new IllegalArgumentException("This event has already been initialized, please deinitialize it first");
        } else {
            event.initializeEvent();
        }
        if (event instanceof SingleElimEvent singleElim) {
            int matchAmount = TournamentUtil.nextPowerOfTwo(players.size()) / 2;
            int matchAmountForCurRound = matchAmount;
            List<List<Match>> eventInNestedArr = new ArrayList<>();
            List<Match> allMatch = new ArrayList<>();
            while (matchAmountForCurRound >= 1) {
                List<Match> curRoundMatches = new ArrayList<>();
                for (int i = 0; i < matchAmountForCurRound; i++) {
                    Match curMatch = new Match();
                    curMatch.setEvent(singleElim);
                    curRoundMatches.add(curMatch);
                    allMatch.add(curMatch);
                }
                eventInNestedArr.add(curRoundMatches);
                matchAmountForCurRound /= 2;
            }
            ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, matchAmount);
            List<Match> bottomRoundMatches = eventInNestedArr.get(0);
            for (int i = 0; i < draw.size(); i++) {
                if (i % 2 == 0) {
                    bottomRoundMatches.get(i / 2).setPlayerA(draw.get(i));
                } else {
                    bottomRoundMatches.get(i / 2).setPlayerB(draw.get(i));
                }
            }
            List<Round> rounds = new ArrayList<>();
            for (int i = 0; i < eventInNestedArr.size(); i++) {
                Round round = new Round();
                round.setMatches(eventInNestedArr.get(i));
                round.setRoundNumber(i + 1);
                rounds.add(round);
                round.setEvent(singleElim);
            }
            singleElim.addRound(rounds);
            matchRepo.saveAll(allMatch);
            eventRepo.save(singleElim);
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
    }

    public void deinitializeEvent(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        if (!event.isEventInitialized()) {
            throw new IllegalArgumentException("This event was never initialized, nothing to deinitialize");
        } else {
            event.unInitiateEvent();
        }
        if (event instanceof SingleElimEvent singleElimEvent) {
            singleElimEvent.getRounds().clear();
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
        eventRepo.save(event);
    }

    public List<List<Match>> getEventDraw(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        if (event instanceof SingleElimEvent singleElimEvent) {
            List<List<Match>> draw = new ArrayList<>();
            if (singleElimEvent.getRounds() != null) {
                for (Round round : singleElimEvent.getRounds()) {
                    draw.add(round.getMatches());
                }
            }
            return draw;
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
    }


    public List<BaseEvent> getEventsForTournament(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        List<BaseEvent> events = tournament.getEvents();
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("No events found for tournament with id " + tournamentId);
        }
        return events;
    }
}