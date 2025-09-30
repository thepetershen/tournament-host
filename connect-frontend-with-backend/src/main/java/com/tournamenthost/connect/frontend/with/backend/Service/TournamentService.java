package com.tournamenthost.connect.frontend.with.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.EventRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.Round;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.RoundRobinEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.PlayerSchedule;
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
            case ROUND_ROBIN -> event = new RoundRobinEvent();
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
        } else if (event instanceof RoundRobinEvent roundRobin) {
            List<Match> answer = new ArrayList<>();
            for (PlayerSchedule schedule : roundRobin.getPlayerSchedules()) {
                answer.addAll(schedule.getMatches());
            }
            return answer.stream().distinct().toList();
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
        } else if (event instanceof RoundRobinEvent roundRobin) {
            // Generate round robin matches: each player plays every other player
            List<Match> allMatches = new ArrayList<>();
            List<PlayerSchedule> playerSchedules = new ArrayList<>();

            // Create a PlayerSchedule for each player
            for (User player : players) {
                PlayerSchedule schedule = new PlayerSchedule(player, roundRobin);
                playerSchedules.add(schedule);
            }

            // Generate matches: each player vs every other player
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) {
                    User playerA = players.get(i);
                    User playerB = players.get(j);

                    // Create the match
                    Match match = new Match();
                    match.setPlayerA(playerA);
                    match.setPlayerB(playerB);
                    match.setEvent(roundRobin);
                    allMatches.add(match);

                    // Add this match to both players' schedules
                    playerSchedules.get(i).addMatch(match);
                    playerSchedules.get(j).addMatch(match);
                }
            }

            // Save matches first
            matchRepo.saveAll(allMatches);

            // Add schedules to the event and save
            roundRobin.addPlayerSchedule(playerSchedules);
            eventRepo.save(roundRobin);
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
        } else if (event instanceof RoundRobinEvent roundRobinEvent) {
            roundRobinEvent.getPlayerSchedules().clear();
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
        eventRepo.save(event);
    }

    public Object getEventDraw(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        if (event instanceof SingleElimEvent singleElimEvent) {
            List<List<Match>> draw = new ArrayList<>();
            if (singleElimEvent.getRounds() != null) {
                for (Round round : singleElimEvent.getRounds()) {
                    draw.add(round.getMatches());
                }
            }
            return draw;
        } else if (event instanceof RoundRobinEvent roundRobinEvent) {
            // For round robin, return a TreeMap mapping each user to their matches
            Map<User, List<Match>> draw = new TreeMap<>();
            for (PlayerSchedule schedule : roundRobinEvent.getPlayerSchedules()) {
                User player = schedule.getPlayer();
                List<Match> playerMatches = new ArrayList<>(schedule.getMatches());
                // Sort matches for consistent ordering
                playerMatches.sort((m1, m2) -> {
                    if (m1.getId() != null && m2.getId() != null) {
                        return m1.getId().compareTo(m2.getId());
                    }
                    return 0;
                });
                draw.put(player, playerMatches);
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

     public void recordMatchResult(Long matchId, Long winnerId, List<Integer> score) {
        // Find the match and winner
        Match match = matchRepo.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match with id " + matchId + " not found"));
        User winner = userRepo.findById(winnerId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + winnerId + " not found"));

        // Validate that the winner is actually in this match
        if (!winner.equals(match.getPlayerA()) && !winner.equals(match.getPlayerB())) {
            throw new IllegalArgumentException("Player with id " + winnerId + " is not participating in match " + matchId);
        }

        // Validate that the match hasn't already been completed
        if (match.isCompleted()) {
            throw new IllegalArgumentException("Match with id " + matchId + " has already been completed");
        }

        // Set match result
        match.setWinner(winner);
        match.setScore(score);
        match.setCompleted(true);

        // Save the match
        matchRepo.save(match);

        // Handle bracket advancement for single elimination events
        BaseEvent event = match.getEvent();
        if (event instanceof SingleElimEvent singleElim) {
            advanceWinnerInSingleElim(match, winner, singleElim);
        }
        // For round robin events, no advancement is needed
    }

    private void advanceWinnerInSingleElim(Match completedMatch, User winner, SingleElimEvent singleElim) {
        List<Round> rounds = singleElim.getRounds();

        // Find which round and position this match is in
        Round currentRound = null;
        int currentRoundIndex = -1;
        int matchPositionInRound = -1;

        for (int i = 0; i < rounds.size(); i++) {
            Round round = rounds.get(i);
            List<Match> matches = round.getMatches();
            for (int j = 0; j < matches.size(); j++) {
                if (matches.get(j).getId().equals(completedMatch.getId())) {
                    currentRound = round;
                    currentRoundIndex = i;
                    matchPositionInRound = j;
                    break;
                }
            }
            if (currentRound != null) break;
        }

        if (currentRound == null) {
            throw new IllegalArgumentException("Could not find the completed match in any round");
        }

        // Check if there's a next round (not the final)
        if (currentRoundIndex < rounds.size() - 1) {
            Round nextRound = rounds.get(currentRoundIndex + 1);
            List<Match> nextRoundMatches = nextRound.getMatches();

            // Calculate which match in the next round this winner advances to
            // In a standard bracket, two adjacent matches feed into one match in the next round
            int nextMatchIndex = matchPositionInRound / 2;

            if (nextMatchIndex < nextRoundMatches.size()) {
                Match nextMatch = nextRoundMatches.get(nextMatchIndex);

                // Determine if winner goes to playerA or playerB slot
                // Even positioned matches (0, 2, 4...) feed playerA, odd positioned feed playerB
                if (matchPositionInRound % 2 == 0) {
                    nextMatch.setPlayerA(winner);
                } else {
                    nextMatch.setPlayerB(winner);
                }

                // Save the updated next round match
                matchRepo.save(nextMatch);
            }
        }
    }
}