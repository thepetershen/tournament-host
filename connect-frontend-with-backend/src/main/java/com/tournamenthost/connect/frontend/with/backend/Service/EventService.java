package com.tournamenthost.connect.frontend.with.backend.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonSerializable.Base;
import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.Round;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.EventRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;
import com.tournamenthost.connect.frontend.with.backend.util.TournamentUtil;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepo;

    @Autowired 
    private MatchRepository matchRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired 
    private TournamentRepository tournamentRepo;

    public BaseEvent addEvent(String name, EventType eventType, Long tournamentId) {
        BaseEvent event;
        switch (eventType) {
            case SINGLE_ELIM -> event = new SingleElimEvent();
            default -> throw new IllegalArgumentException("Unsupported event type");
        }

        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + tournamentId + " not found"));
        event.setName(name);
        event.setTournament(tournament);
        return eventRepo.save(event);
    }

    public BaseEvent changeEventName(Long eventId, String newName) {
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));

        event.setName(newName);
        return eventRepo.save(event);
    }

    public BaseEvent getEvent(Long eventId){
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));
        return event;
    }

    public List<BaseEvent> getAllEvents() {
        List<BaseEvent> events = new ArrayList<>();
        eventRepo.findAll().forEach(events::add);
        return events;
    }

    public void addPlayer(Long eventId, Long playerId) {
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));
        
        User user = userRepo.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + playerId + " not found"));

        if(event.isEventInitialized()) {
            throw new IllegalArgumentException("Event with id " + eventId + " has already been initiated");
        }
        event.addPlayer(user);
        eventRepo.save(event);
    }

    public void addPlayer(Long eventId, List<Long> namesId) {
        for(Long name: namesId) {
            this.addPlayer(eventId, name);
        }
    }

    public List<User> getPlayers(Long eventId) {
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));
        return event.getPlayers();
    }

    public void initializeEvent(Long eventId) {
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));
        List<User> players = event.getPlayers();

        if (players.size() <= 2) {
            throw new IllegalArgumentException("There are too few players, the beginning of this event demands at least 3 players");
        }

        if  (event.isEventInitialized()){
            throw new IllegalArgumentException("This event has already been initialized, please deinitialize it first");
        } else {
            event.initializeEvent();
        }
        //first create the necessary matches in a nested arraylist
        if (event instanceof SingleElimEvent singleElim) {
            int matchAmount = TournamentUtil.nextPowerOfTwo(players.size())/2;
            int matchAmountForCurRound = matchAmount;
            List<List<Match>> eventInNestedArr = new ArrayList<>();
            List<Match> allMatch = new ArrayList<>();
            
            while(matchAmountForCurRound >= 1) {
                List<Match> curRoundMatches = new ArrayList<>();
                for(int i = 0; i < matchAmountForCurRound; i++) {
                    Match curMatch = new Match();
                    curMatch.setEvent(singleElim);
                    curRoundMatches.add(curMatch);
                    allMatch.add(curMatch);
                }
                eventInNestedArr.add(curRoundMatches);
                matchAmountForCurRound /= 2;
            }

            //assign proper players to the draw, using another method, and then assign them to the first round matches

            ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, matchAmount);
            List<Match> bottomRoundMatches = eventInNestedArr.get(0);
            for(int i = 0; i < draw.size(); i++) {
                if(i%2 == 0) {
                    bottomRoundMatches.get(i/2).setPlayerA(draw.get(i));
                } else {
                    bottomRoundMatches.get(i/2).setPlayerB(draw.get(i));
                }
            }

            // Create and save rounds
            List<Round> rounds = new ArrayList<>();
            for (int i = 0; i < eventInNestedArr.size(); i++) {
                Round round = new Round();
                round.setMatches(eventInNestedArr.get(i));
                round.setRoundNumber(i + 1);
                rounds.add(round);
                round.setEvent(singleElim);
            }
            // Save rounds (matches will be cascaded)
            // If you have a RoundRepository, you can saveAll(rounds) here, otherwise rely on cascade

            singleElim.addRound(rounds);

            // Save all matches to the database
            matchRepo.saveAll(allMatch);

            // Save (update) the event in the database (rounds will be cascaded)
            eventRepo.save(singleElim);
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
        
    }

    public void deinitializeEvent(Long eventId) {
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));

        if (!event.isEventInitialized()) {
            throw new IllegalArgumentException("This event was never initialized, nothing to deinitialize");
        }

        // Clear rounds
        if(event instanceof SingleElimEvent singleElimEvent) {
            singleElimEvent.getRounds().clear();
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }

        // Save event
        eventRepo.save(event);
    }

    /**
     * Returns the event draw as a nested list of matches (one list per round).
     */
    public List<List<Match>> getEventDraw(Long eventId) {
        BaseEvent event = eventRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found"));

        if(event instanceof SingleElimEvent singleElimEvent) {
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

    

}
