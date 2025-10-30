package com.tournamenthost.connect.frontend.with.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Model.PointsDistribution;
import com.tournamenthost.connect.frontend.with.backend.Model.League;
import com.tournamenthost.connect.frontend.with.backend.Model.LeaguePlayerRanking;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventRegistration;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.EventRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.PointsDistributionRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.LeagueRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.EventRegistrationRepository;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.EventType;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.Round;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.RoundRobinEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.DoubleElimEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.DoubleElimRound;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BracketType;
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

    @Autowired
    private PointsDistributionRepository pointsDistributionRepo;

    @Autowired
    private LeagueRepository leagueRepo;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepo;

    public Tournament createTournament(String name, User owner, String message, Date begin, Date end, String location) {
        if (tournamentRepo.existsByNameIgnoreCaseAndSpaces(name)) {
            throw new IllegalArgumentException("Tournament with name '" + name + "' already exists");
        }

        // Validate that message, begin, end, and location are provided
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament message is required");
        }
        if (begin == null) {
            throw new IllegalArgumentException("Tournament begin date is required");
        }
        if (end == null) {
            throw new IllegalArgumentException("Tournament end date is required");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament location is required");
        }

        // Validate that begin date is before end date
        if (begin.after(end)) {
            throw new IllegalArgumentException("Begin date must be before end date");
        }

        Tournament tournament = new Tournament(name, owner);
        tournament.setMessage(message);
        tournament.setBegin(begin);
        tournament.setEnd(end);
        tournament.setLocation(location);

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

    public Map<BaseEvent, List<Match>> getPlayerMatchesForTournament(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        User player = userRepo.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + playerId + " not found"));

        Map<BaseEvent, List<Match>> answer = new LinkedHashMap<>();

        for (BaseEvent event : tournament.getEvents()) {
            List<Match> playerMatches = getPlayerMatchesForEvent(event, player);
            if (!playerMatches.isEmpty()) {
                answer.put(event, playerMatches);
            }
        }

        return answer;
    }

    /**
     * Get all matches for a specific player in an event, ordered chronologically
     * For Single/Double Elim: earlier rounds come first (Round of 32 before Round of 16)
     * For Round Robin: matches are returned in their natural order
     */
    private List<Match> getPlayerMatchesForEvent(BaseEvent event, User player) {
        if (!event.isEventInitialized()) {
            return new ArrayList<>();
        }

        List<Match> playerMatches = new ArrayList<>();

        if (event instanceof SingleElimEvent singleElim) {
            // Iterate through rounds in order (earliest round first)
            List<Round> rounds = singleElim.getRounds();
            for (Round round : rounds) {
                for (Match match : round.getMatches()) {
                    if (isPlayerInMatch(match, player)) {
                        playerMatches.add(match);
                    }
                }
            }
        } else if (event instanceof RoundRobinEvent roundRobin) {
            // Get all matches for the player
            for (PlayerSchedule schedule : roundRobin.getPlayerSchedules()) {
                if (schedule.getPlayer().equals(player)) {
                    playerMatches.addAll(schedule.getMatches());
                    break;
                }
            }
        } else if (event instanceof DoubleElimEvent doubleElim) {
            // Add matches from winners bracket first (earliest round first)
            for (DoubleElimRound round : doubleElim.getWinnersBracket()) {
                for (Match match : round.getMatches()) {
                    if (isPlayerInMatch(match, player)) {
                        playerMatches.add(match);
                    }
                }
            }
            // Then add matches from losers bracket (earliest round first)
            for (DoubleElimRound round : doubleElim.getLosersBracket()) {
                for (Match match : round.getMatches()) {
                    if (isPlayerInMatch(match, player)) {
                        playerMatches.add(match);
                    }
                }
            }
        }

        return playerMatches;
    }

    /**
     * Check if a player is a participant in a match
     */
    private boolean isPlayerInMatch(Match match, User player) {
        return (match.getPlayerA() != null && match.getPlayerA().equals(player)) ||
               (match.getPlayerB() != null && match.getPlayerB().equals(player));
    }
    // Event-related methods merged from EventService
    public BaseEvent addEvent(String name, EventType eventType, Long tournamentId) {
        BaseEvent event;
        switch (eventType) {
            case SINGLE_ELIM -> event = new SingleElimEvent();
            case ROUND_ROBIN -> event = new RoundRobinEvent();
            case DOUBLE_ELIM -> event = new DoubleElimEvent();
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
        } else if (event instanceof DoubleElimEvent doubleElim) {
            List<Match> answer = new ArrayList<>();
            // Add all matches from winners bracket
            for (DoubleElimRound round : doubleElim.getWinnersBracket()) {
                answer.addAll(round.getMatches());
            }
            // Add all matches from losers bracket
            for (DoubleElimRound round : doubleElim.getLosersBracket()) {
                answer.addAll(round.getMatches());
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
        }

        // Validate seeding before initialization
        validateSeeding(event);

        event.initializeEvent();
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
            // Ensure playerSeeds is never null (should be empty HashMap if no seeds)
            Map<Long, Integer> seeds = event.getPlayerSeeds();
            if (seeds == null) {
                seeds = new HashMap<>();
            }
            ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, matchAmount, seeds);
            List<Match> bottomRoundMatches = eventInNestedArr.get(0);
            for (int i = 0; i < draw.size(); i++) {
                if (i % 2 == 0) {
                    bottomRoundMatches.get(i / 2).setPlayerA(draw.get(i));
                } else {
                    bottomRoundMatches.get(i / 2).setPlayerB(draw.get(i));
                }
            }

            // Auto-advance players with byes in the first round
            if (eventInNestedArr.size() > 1) {
                List<Match> secondRoundMatches = eventInNestedArr.get(1);
                for (int i = 0; i < bottomRoundMatches.size(); i++) {
                    Match firstRoundMatch = bottomRoundMatches.get(i);
                    User playerA = firstRoundMatch.getPlayerA();
                    User playerB = firstRoundMatch.getPlayerB();

                    // If one player has a bye, auto-advance the other player
                    if (playerA == null && playerB != null) {
                        // PlayerB gets bye, advance to next round
                        firstRoundMatch.setWinner(playerB);
                        firstRoundMatch.setCompleted(true);
                        // Advance to second round
                        Match nextMatch = secondRoundMatches.get(i / 2);
                        if (i % 2 == 0) {
                            nextMatch.setPlayerA(playerB);
                        } else {
                            nextMatch.setPlayerB(playerB);
                        }
                    } else if (playerB == null && playerA != null) {
                        // PlayerA gets bye, advance to next round
                        firstRoundMatch.setWinner(playerA);
                        firstRoundMatch.setCompleted(true);
                        // Advance to second round
                        Match nextMatch = secondRoundMatches.get(i / 2);
                        if (i % 2 == 0) {
                            nextMatch.setPlayerA(playerA);
                        } else {
                            nextMatch.setPlayerB(playerA);
                        }
                    }
                    // If both are null or both are present, do nothing (normal match or error state)
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
        } else if (event instanceof DoubleElimEvent doubleElim) {
            initializeDoubleElimEvent(doubleElim, players);
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
    }

    public void deinitializeEvent(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        if (!event.isEventInitialized()) {
            throw new IllegalArgumentException("This event was never initialized, nothing to deinitialize");
        }

        // Collect all matches to delete
        List<Match> matchesToDelete = new ArrayList<>();

        if (event instanceof SingleElimEvent singleElimEvent) {
            for (Round round : singleElimEvent.getRounds()) {
                matchesToDelete.addAll(round.getMatches());
            }
            singleElimEvent.getRounds().clear();
        } else if (event instanceof RoundRobinEvent roundRobinEvent) {
            for (PlayerSchedule schedule : roundRobinEvent.getPlayerSchedules()) {
                matchesToDelete.addAll(schedule.getMatches());
            }
            // Use distinct to avoid deleting the same match multiple times
            matchesToDelete = matchesToDelete.stream().distinct().toList();
            roundRobinEvent.getPlayerSchedules().clear();
        } else if (event instanceof DoubleElimEvent doubleElimEvent) {
            for (DoubleElimRound round : doubleElimEvent.getWinnersBracket()) {
                matchesToDelete.addAll(round.getMatches());
            }
            for (DoubleElimRound round : doubleElimEvent.getLosersBracket()) {
                matchesToDelete.addAll(round.getMatches());
            }
            // Add bronze match if it exists
            if (doubleElimEvent.getBronzeMatch() != null) {
                matchesToDelete.add(doubleElimEvent.getBronzeMatch());
            }
            doubleElimEvent.getWinnersBracket().clear();
            doubleElimEvent.getLosersBracket().clear();
            doubleElimEvent.setBronzeMatch(null);
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }

        // Delete all matches from database
        matchRepo.deleteAll(matchesToDelete);

        // Mark event as uninitialized
        event.unInitiateEvent();
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
        } else if (event instanceof DoubleElimEvent doubleElimEvent) {
            // For double elim, return a map with both brackets
            Map<String, List<List<Match>>> draw = new TreeMap<>();

            List<List<Match>> winnersDraw = new ArrayList<>();
            for (DoubleElimRound round : doubleElimEvent.getWinnersBracket()) {
                winnersDraw.add(round.getMatches());
            }
            draw.put("winners", winnersDraw);

            List<List<Match>> losersDraw = new ArrayList<>();
            for (DoubleElimRound round : doubleElimEvent.getLosersBracket()) {
                losersDraw.add(round.getMatches());
            }
            draw.put("losers", losersDraw);

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

    /**
     * Record match result with tournament context validation
     * This ensures the match belongs to the specified tournament before recording the result
     */
    public void recordMatchResult(Long tournamentId, Long matchId, Long winnerId, List<Integer> score) {
        // First verify the match belongs to this tournament
        verifyMatchBelongsToTournament(matchId, tournamentId);

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

        // validate that both players are in the match

        if (match.getPlayerA() == null || match.getPlayerB() == null) {
            throw new IllegalStateException("Match with id " + matchId + "doesn't have both of their players yet");
        }
        
        // Handle bracket advancement for single elimination events
        BaseEvent event = match.getEvent();

        // For round robin events, no advancement is needed
        if (event instanceof SingleElimEvent singleElim) {
            advanceWinnerInSingleElim(match, winner, singleElim);
        } else if (event instanceof DoubleElimEvent doubleElim) {
            advanceInDoubleElim(match, winner, doubleElim);
        }

        // Set match result
        match.setWinner(winner);
        match.setScore(score);
        match.setCompleted(true);

        // Save the match
        matchRepo.save(match);

        // For double elim, auto-advance any single players in losers bracket after each result
        if (event instanceof DoubleElimEvent doubleElim) {
            autoAdvanceSinglePlayersInLosersBracket(doubleElim);
            matchRepo.save(match);
        }

        
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
        } else if ((completedMatch.getPlayerA() == null || completedMatch.getPlayerB() == null) && currentRoundIndex == 0) {
            throw new IllegalStateException("The match doesn't have both of its players yet");
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

                // Don't overwrite players in an already-completed match
                // This prevents issues when matches are completed out of order
                if (!nextMatch.isCompleted()) {
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

    // ==================== DOUBLE ELIMINATION METHODS ====================

    /**
     * Advance winner and loser in double elimination
     */
    private void advanceInDoubleElim(Match completedMatch, User winner, DoubleElimEvent doubleElim) {
        List<DoubleElimRound> winnersRounds = doubleElim.getWinnersBracket();
        List<DoubleElimRound> losersRounds = doubleElim.getLosersBracket();

        // Determine the loser
        User loser = completedMatch.getPlayerA().equals(winner) ?
                     completedMatch.getPlayerB() : completedMatch.getPlayerA();

        // Find which bracket and round this match is in
        boolean isWinnersBracket = false;
        int currentRoundIndex = -1;
        int matchPositionInRound = -1;

        // Check winners bracket
        for (int i = 0; i < winnersRounds.size(); i++) {
            DoubleElimRound round = winnersRounds.get(i);
            List<Match> matches = round.getMatches();
            for (int j = 0; j < matches.size(); j++) {
                if (matches.get(j).getId().equals(completedMatch.getId())) {
                    isWinnersBracket = true;
                    currentRoundIndex = i;
                    matchPositionInRound = j;
                    break;
                }
            }
            if (isWinnersBracket) break;
        }

        // Check losers bracket if not found in winners
        if (!isWinnersBracket) {
            for (int i = 0; i < losersRounds.size(); i++) {
                DoubleElimRound round = losersRounds.get(i);
                List<Match> matches = round.getMatches();
                for (int j = 0; j < matches.size(); j++) {
                    if (matches.get(j).getId().equals(completedMatch.getId())) {
                        currentRoundIndex = i;
                        matchPositionInRound = j;
                        break;
                    }
                }
                if (currentRoundIndex != -1) break;
            }
        }

        if (currentRoundIndex == -1) {
            throw new IllegalArgumentException("Could not find the completed match in any round");
        }

        if (isWinnersBracket) {
            // Advance winner to next winners round
            if (currentRoundIndex < winnersRounds.size() - 1) {
                DoubleElimRound nextWinnersRound = winnersRounds.get(currentRoundIndex + 1);
                List<Match> nextRoundMatches = nextWinnersRound.getMatches();
                int nextMatchIndex = matchPositionInRound / 2;

                if (nextMatchIndex < nextRoundMatches.size()) {
                    Match nextMatch = nextRoundMatches.get(nextMatchIndex);
                    if (!nextMatch.isCompleted()) {
                        if (matchPositionInRound % 2 == 0) {
                            nextMatch.setPlayerA(winner);
                        } else {
                            nextMatch.setPlayerB(winner);
                        }
                        matchRepo.save(nextMatch);
                    }
                }
            }

            // Feed loser to losers bracket (if within feed-in cutoff)
            if (currentRoundIndex <= doubleElim.getFeedInCutoffRound()) {
                // Find the corresponding losers bracket round that receives from this winners round
                for (DoubleElimRound losersRound : losersRounds) {
                    Integer feedsFrom = losersRound.getFeedsFromWinnersRound();
                    if (feedsFrom != null && feedsFrom.intValue() == currentRoundIndex) {

                        List<Match> losersMatches = losersRound.getMatches();
                        // Find an empty slot in this losers round
                        for (Match losersMatch : losersMatches) {
                            if (losersMatch.getPlayerA() == null) {
                                losersMatch.setPlayerA(loser);
                                matchRepo.save(losersMatch);
                                break;
                            } else if (losersMatch.getPlayerB() == null) {
                                losersMatch.setPlayerB(loser);
                                matchRepo.save(losersMatch);
                                break;
                            }
                        }
                        break;
                    }
                }
            } else if (currentRoundIndex == winnersRounds.size() - 2) {
                // Semifinals losers go to bronze match (3rd/4th place)
                Match bronzeMatch = doubleElim.getBronzeMatch();
                if (bronzeMatch != null && !bronzeMatch.isCompleted()) {
                    if (bronzeMatch.getPlayerA() == null) {
                        bronzeMatch.setPlayerA(loser);
                        matchRepo.save(bronzeMatch);
                    } else if (bronzeMatch.getPlayerB() == null) {
                        bronzeMatch.setPlayerB(loser);
                        matchRepo.save(bronzeMatch);
                    }
                }
            }
        } else {
            // In losers bracket - only advance winner (loser is eliminated)
            if (currentRoundIndex < losersRounds.size() - 1) {
                DoubleElimRound nextLosersRound = losersRounds.get(currentRoundIndex + 1);
                List<Match> nextRoundMatches = nextLosersRound.getMatches();
                int nextMatchIndex = matchPositionInRound / 2;

                if (nextMatchIndex < nextRoundMatches.size()) {
                    Match nextMatch = nextRoundMatches.get(nextMatchIndex);
                    if (!nextMatch.isCompleted()) {
                        if (matchPositionInRound % 2 == 0) {
                            nextMatch.setPlayerA(winner);
                        } else {
                            nextMatch.setPlayerB(winner);
                        }
                        matchRepo.save(nextMatch);
                    }
                }
            }
        }
    }

    /**
     * Initialize a Double Elimination event
     *
     * Structure:
     * - Winners bracket: Standard single elimination
     * - Losers bracket: Sized at (bracketSize/2), receives losers from winners until semifinals
     * - Bronze match: Semifinal losers from winners bracket play for 3rd/4th place
     * - Feed-ins stop BEFORE semifinals (semifinals and finals don't feed losers)
     */
    private void initializeDoubleElimEvent(DoubleElimEvent doubleElim, List<User> players) {
        int bracketSize = TournamentUtil.nextPowerOfTwo(players.size());

        // Feed-in cutoff: only feed in from quarterfinals (first round only)
        // After quarterfinals, losers bracket plays out independently
        int feedInCutoff = 0;  // Only feed from round 0 (quarterfinals)
        doubleElim.setFeedInCutoffRound(feedInCutoff);

        List<Match> allMatches = new ArrayList<>();

        // 1. Create Winners Bracket (standard single elimination)
        List<DoubleElimRound> winnersRounds = createWinnersBracket(doubleElim, players, bracketSize, allMatches);
        doubleElim.addWinnersRounds(winnersRounds);

        // 2. Create Losers Bracket (half the size of winners bracket)
        int losersBracketSize = bracketSize / 2;
        List<DoubleElimRound> losersRounds = createLosersBracket(doubleElim, losersBracketSize, feedInCutoff, allMatches);
        doubleElim.addLosersRounds(losersRounds);

        // 3. Create Bronze Match (3rd/4th place) for semifinal losers
        Match bronzeMatch = new Match();
        bronzeMatch.setEvent(doubleElim);
        allMatches.add(bronzeMatch);
        doubleElim.setBronzeMatch(bronzeMatch);

        // Save all matches and event
        matchRepo.saveAll(allMatches);
        eventRepo.save(doubleElim);

        // Auto-advance any players in losers bracket who are alone in their match
        autoAdvanceSinglePlayersInLosersBracket(doubleElim);
        matchRepo.saveAll(allMatches);
    }

    /**
     * Create the winners bracket structure
     */
    private List<DoubleElimRound> createWinnersBracket(DoubleElimEvent event, List<User> players,
                                                        int bracketSize, List<Match> allMatches) {
        List<DoubleElimRound> winnersRounds = new ArrayList<>();
        int matchCount = bracketSize / 2;

        // Generate seeded draw
        ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, matchCount, event.getPlayerSeeds());

        // Create rounds from bottom to top
        for (int round = 0; round < Math.log(bracketSize) / Math.log(2); round++) {
            DoubleElimRound doubleElimRound = new DoubleElimRound(round, BracketType.WINNERS);
            doubleElimRound.setEvent(event);

            List<Match> roundMatches = new ArrayList<>();
            for (int i = 0; i < matchCount; i++) {
                Match match = new Match();
                match.setEvent(event);

                // For first round, assign players from draw
                if (round == 0 && i * 2 < draw.size()) {
                    if (i * 2 < draw.size()) match.setPlayerA(draw.get(i * 2));
                    if (i * 2 + 1 < draw.size()) match.setPlayerB(draw.get(i * 2 + 1));
                }

                roundMatches.add(match);
                allMatches.add(match);
            }

            doubleElimRound.setMatches(roundMatches);
            winnersRounds.add(doubleElimRound);

            matchCount /= 2;
        }

        // Auto-advance players with byes in the first round of winners bracket
        if (winnersRounds.size() > 1) {
            List<Match> firstRoundMatches = winnersRounds.get(0).getMatches();
            List<Match> secondRoundMatches = winnersRounds.get(1).getMatches();

            for (int i = 0; i < firstRoundMatches.size(); i++) {
                Match firstRoundMatch = firstRoundMatches.get(i);
                User playerA = firstRoundMatch.getPlayerA();
                User playerB = firstRoundMatch.getPlayerB();

                // If one player has a bye, auto-advance the other player
                if (playerA == null && playerB != null) {
                    // PlayerB gets bye, advance to next round
                    firstRoundMatch.setWinner(playerB);
                    firstRoundMatch.setCompleted(true);
                    // Advance to second round
                    Match nextMatch = secondRoundMatches.get(i / 2);
                    if (i % 2 == 0) {
                        nextMatch.setPlayerA(playerB);
                    } else {
                        nextMatch.setPlayerB(playerB);
                    }
                } else if (playerB == null && playerA != null) {
                    // PlayerA gets bye, advance to next round
                    firstRoundMatch.setWinner(playerA);
                    firstRoundMatch.setCompleted(true);
                    // Advance to second round
                    Match nextMatch = secondRoundMatches.get(i / 2);
                    if (i % 2 == 0) {
                        nextMatch.setPlayerA(playerA);
                    } else {
                        nextMatch.setPlayerB(playerA);
                    }
                }
            }
        }

        return winnersRounds;
    }

    /**
     * Create the losers bracket structure
     *
     * Losers bracket pattern:
     * - Feed-in round: Takes N losers from winners bracket round 0 (quarterfinals) only
     * - Progression rounds: Winners from previous losers rounds advance until one remains
     *
     * After quarterfinals, the losers bracket plays out independently with no more feed-ins.
     * Semifinals and finals losers go to bronze match instead.
     *
     * Example for 8 players (feedInCutoff=0):
     * Winners: 4 matches (R0) → 2 matches (R1) → 1 match (R2)
     * Losers:
     * - L-R0: 2 matches (4 losers from W-R0 feed in) → 4 players remain [FEED-IN]
     * - L-R1: 1 match (4 players → 2 players) [PROGRESSION]
     * - L-R2: 1 match (2 players → 1 player) [PROGRESSION - LOSERS FINALS]
     */
    private List<DoubleElimRound> createLosersBracket(DoubleElimEvent event, int losersBracketSize,
                                                       int feedInCutoff, List<Match> allMatches) {
        List<DoubleElimRound> losersRounds = new ArrayList<>();
        int round = 0;

        // First feed-in round always has losersBracketSize/2 matches
        int progressionMatchCount = losersBracketSize / 2;

        // Create feed-in and progression rounds until we reach the cutoff
        for (int winnersRound = 0; winnersRound <= feedInCutoff; winnersRound++) {
            // Calculate feed-in match count
            // Feed-in needs to accommodate: N losers from winners + M winners from previous progression
            // For first round: just take losers from winners (progressionMatchCount matches)
            // For subsequent rounds: combine losers from winners (half of progressionMatchCount)
            //                        with winners from previous progression (progressionMatchCount)
            int feedInMatchCount;
            if (winnersRound == 0) {
                feedInMatchCount = progressionMatchCount;
            } else {
                // After first round, we need enough matches to pair up:
                // - progressionMatchCount winners from previous round
                // - progressionMatchCount losers from current winners round
                feedInMatchCount = progressionMatchCount;
            }

            // Create feed-in round
            DoubleElimRound feedInRound = new DoubleElimRound(round, BracketType.LOSERS);
            feedInRound.setEvent(event);
            feedInRound.setFeedsFromWinnersRound(winnersRound);

            List<Match> feedInMatches = new ArrayList<>();
            for (int i = 0; i < feedInMatchCount; i++) {
                Match match = new Match();
                match.setEvent(event);
                feedInMatches.add(match);
                allMatches.add(match);
            }
            feedInRound.setMatches(feedInMatches);
            losersRounds.add(feedInRound);
            round++;

            // Create progression round (winners from feed-in advance)
            progressionMatchCount = Math.max(1, feedInMatchCount / 2);

            DoubleElimRound progressionRound = new DoubleElimRound(round, BracketType.LOSERS);
            progressionRound.setEvent(event);

            List<Match> progressionMatches = new ArrayList<>();
            for (int i = 0; i < progressionMatchCount; i++) {
                Match match = new Match();
                match.setEvent(event);
                progressionMatches.add(match);
                allMatches.add(match);
            }
            progressionRound.setMatches(progressionMatches);
            losersRounds.add(progressionRound);
            round++;
        }

        // After feed-ins stop, continue creating progression rounds until we reach 1 player
        while (progressionMatchCount > 1) {
            progressionMatchCount = progressionMatchCount / 2;

            DoubleElimRound progressionRound = new DoubleElimRound(round, BracketType.LOSERS);
            progressionRound.setEvent(event);

            List<Match> progressionMatches = new ArrayList<>();
            for (int i = 0; i < progressionMatchCount; i++) {
                Match match = new Match();
                match.setEvent(event);
                progressionMatches.add(match);
                allMatches.add(match);
            }
            progressionRound.setMatches(progressionMatches);
            losersRounds.add(progressionRound);
            round++;
        }

        return losersRounds;
    }

    /**
     * Auto-advance players in losers bracket who are alone in their match
     * This handles cases where odd numbers of players feed into losers bracket
     */
    private void autoAdvanceSinglePlayersInLosersBracket(DoubleElimEvent event) {
        List<DoubleElimRound> losersRounds = event.getLosersBracket();

        for (int roundIndex = 0; roundIndex < losersRounds.size(); roundIndex++) {
            DoubleElimRound currentRound = losersRounds.get(roundIndex);
            List<Match> matches = currentRound.getMatches();

            for (int matchIndex = 0; matchIndex < matches.size(); matchIndex++) {
                Match match = matches.get(matchIndex);
                User playerA = match.getPlayerA();
                User playerB = match.getPlayerB();

                // If match already completed, skip
                if (match.isCompleted()) {
                    continue;
                }

                // If one player has a bye (is null), auto-advance the other
                User advancingPlayer = null;
                if (playerA != null && playerB == null) {
                    advancingPlayer = playerA;
                } else if (playerB != null && playerA == null) {
                    advancingPlayer = playerB;
                }

                if (advancingPlayer != null) {
                    // Mark match as completed with the single player as winner
                    match.setWinner(advancingPlayer);
                    match.setCompleted(true);

                    // Advance to next round if not the final
                    if (roundIndex < losersRounds.size() - 1) {
                        DoubleElimRound nextRound = losersRounds.get(roundIndex + 1);
                        List<Match> nextRoundMatches = nextRound.getMatches();

                        // Determine which match in next round this player advances to
                        int nextMatchIndex = matchIndex / 2;
                        if (nextMatchIndex < nextRoundMatches.size()) {
                            Match nextMatch = nextRoundMatches.get(nextMatchIndex);

                            // Assign to playerA or playerB slot based on position
                            if (matchIndex % 2 == 0) {
                                if (nextMatch.getPlayerA() == null) {
                                    nextMatch.setPlayerA(advancingPlayer);
                                }
                            } else {
                                if (nextMatch.getPlayerB() == null) {
                                    nextMatch.setPlayerB(advancingPlayer);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recursively check again in case advancing created more single-player matches
        boolean hasMoreSinglePlayerMatches = false;
        for (DoubleElimRound round : losersRounds) {
            for (Match match : round.getMatches()) {
                if (!match.isCompleted() &&
                    ((match.getPlayerA() != null && match.getPlayerB() == null) ||
                     (match.getPlayerA() == null && match.getPlayerB() != null))) {
                    hasMoreSinglePlayerMatches = true;
                    break;
                }
            }
            if (hasMoreSinglePlayerMatches) break;
        }

        if (hasMoreSinglePlayerMatches) {
            autoAdvanceSinglePlayersInLosersBracket(event);
        }
    }

    // ==================== AUTHORIZATION METHODS ====================

    /**
     * Check if a user can edit a tournament (owner or authorized editor)
     */
    public boolean canUserEditTournament(Long tournamentId, User user) {
        Tournament tournament = getTournament(tournamentId);
        return tournament.canUserEdit(user);
    }

    /**
     * Add an authorized editor to a tournament (only owner can do this)
     */
    public void addAuthorizedEditor(Long tournamentId, Long editorId, User currentUser) {
        Tournament tournament = getTournament(tournamentId);

        // Only owner can add editors
        if (!tournament.getOwner().equals(currentUser)) {
            throw new IllegalArgumentException("Only the tournament owner can add editors");
        }

        User editor = userRepo.findById(editorId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + editorId + " not found"));

        tournament.addAuthorizedEditor(editor);
        tournamentRepo.save(tournament);
    }

    /**
     * Remove an authorized editor from a tournament (only owner can do this)
     */
    public void removeAuthorizedEditor(Long tournamentId, Long editorId, User currentUser) {
        Tournament tournament = getTournament(tournamentId);

        // Only owner can remove editors
        if (!tournament.getOwner().equals(currentUser)) {
            throw new IllegalArgumentException("Only the tournament owner can remove editors");
        }

        User editor = userRepo.findById(editorId)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + editorId + " not found"));

        tournament.removeAuthorizedEditor(editor);
        tournamentRepo.save(tournament);
    }

    /**
     * Verify user has edit permission, throw exception if not
     */
    public void verifyEditPermission(Long tournamentId, User user) {
        if (!canUserEditTournament(tournamentId, user)) {
            throw new IllegalArgumentException("You do not have permission to edit this tournament");
        }
    }

    /**
     * Verify user has permission to edit a match (by checking tournament ownership)
     */
    public void verifyMatchEditPermission(Long matchId, User user) {
        Match match = matchRepo.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match with id " + matchId + " not found"));
        Long tournamentId = match.getEvent().getTournament().getId();
        verifyEditPermission(tournamentId, user);
    }

    /**
     * Verify that a match belongs to a specific tournament
     * Throws IllegalArgumentException if the match doesn't belong to the tournament
     */
    public void verifyMatchBelongsToTournament(Long matchId, Long tournamentId) {
        Match match = matchRepo.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match with id " + matchId + " not found"));

        BaseEvent event = match.getEvent();
        if (event == null) {
            throw new IllegalArgumentException("Match with id " + matchId + " is not associated with any event");
        }

        Tournament matchTournament = event.getTournament();
        if (matchTournament == null || !matchTournament.getId().equals(tournamentId)) {
            throw new IllegalArgumentException("Match with id " + matchId + " does not belong to tournament with id " + tournamentId);
        }
    }

    /**
     * Verify that a match belongs to a specific event within a tournament
     * Throws IllegalArgumentException if the match doesn't belong to the specified event
     */
    public void verifyMatchBelongsToEvent(Long matchId, Long tournamentId, int eventIndex) {
        // First verify the match belongs to the tournament
        verifyMatchBelongsToTournament(matchId, tournamentId);

        // Then verify it belongs to the specific event
        Match match = matchRepo.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match with id " + matchId + " not found"));

        BaseEvent matchEvent = match.getEvent();
        BaseEvent expectedEvent = getEventsForTournament(tournamentId).get(eventIndex);

        if (!matchEvent.getId().equals(expectedEvent.getId())) {
            throw new IllegalArgumentException("Match with id " + matchId + " does not belong to event " + eventIndex + " of tournament " + tournamentId);
        }
    }

    // ==================== POINTS DISTRIBUTION METHODS ====================

    /**
     * Set or update points distribution for a specific event
     */
    public PointsDistribution setPointsDistribution(Long tournamentId, int eventIndex, Map<String, Integer> pointsMap) {
        Tournament tournament = getTournament(tournamentId);
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        // Check if points distribution already exists for this event
        PointsDistribution pointsDistribution = pointsDistributionRepo.findByEvent(event).orElse(null);

        if (pointsDistribution == null) {
            pointsDistribution = new PointsDistribution(tournament, event);
        } else {
            pointsDistribution.getPointsMap().clear();
        }

        pointsDistribution.setPointsMap(pointsMap);
        pointsDistributionRepo.save(pointsDistribution);

        return pointsDistribution;
    }

    /**
     * Get points distribution for a specific event
     */
    public PointsDistribution getPointsDistribution(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        return pointsDistributionRepo.findByEvent(event).orElse(null);
    }

    /**
     * Get all points distributions for a tournament
     */
    public List<PointsDistribution> getAllPointsDistributions(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        return pointsDistributionRepo.findAllByTournament(tournament);
    }

    // ==================== TOURNAMENT COMPLETION METHODS ====================

    /**
     * Check if an event is complete (all matches have been played)
     */
    public boolean isEventComplete(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        if (!event.isEventInitialized()) {
            return false;
        }

        List<Match> matches = getMatchesForEvent(tournamentId, eventIndex);

        // Event is complete if all matches are completed
        for (Match match : matches) {
            if (!match.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a tournament is complete (all events are complete)
     */
    public boolean isTournamentComplete(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        List<BaseEvent> events = tournament.getEvents();

        if (events == null || events.isEmpty()) {
            return false;
        }

        // Tournament is complete if all events are complete
        for (BaseEvent event : events) {
            if (!isEventComplete(tournamentId, event.getIndex())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculate event-specific points based on placements
     * Returns a map of User -> Points earned for this specific event
     * Only calculates points when the event is complete
     */
    public Map<User, Integer> calculateEventPoints(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        PointsDistribution pointsDistribution = getPointsDistribution(tournamentId, eventIndex);

        if (pointsDistribution == null) {
            throw new IllegalArgumentException("No points distribution configured for this event");
        }

        if (!event.isEventInitialized()) {
            throw new IllegalArgumentException("Event has not been initialized yet");
        }

        // Only calculate points when the event is complete
        if (!isEventComplete(tournamentId, eventIndex)) {
            throw new IllegalArgumentException("Cannot calculate points - event is not yet complete. All matches must be finished first.");
        }

        Map<User, Integer> userPoints = new TreeMap<>();

        if (event instanceof SingleElimEvent singleElim) {
            userPoints = calculateSingleElimPoints(singleElim, pointsDistribution);
        } else if (event instanceof RoundRobinEvent roundRobin) {
            userPoints = calculateRoundRobinPoints(roundRobin, pointsDistribution);
        } else if (event instanceof DoubleElimEvent doubleElim) {
            userPoints = calculateDoubleElimPoints(doubleElim, pointsDistribution);
        }

        return userPoints;
    }

    /**
     * Calculate cumulative tournament points across all events
     * Returns a map of User -> Total Points earned across all events in the tournament
     */
    public Map<User, Integer> calculateCumulativeTournamentPoints(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        List<BaseEvent> events = tournament.getEvents();
        Map<User, Integer> cumulativePoints = new TreeMap<>();

        // Iterate through each event
        for (BaseEvent event : events) {
            int eventIndex = event.getIndex();

            // Check if the event is complete and has points distribution
            if (!isEventComplete(tournamentId, eventIndex)) {
                continue; // Skip incomplete events
            }

            PointsDistribution pointsDistribution = getPointsDistribution(tournamentId, eventIndex);
            if (pointsDistribution == null) {
                continue; // Skip events without points distribution
            }

            try {
                // Calculate points for this event
                Map<User, Integer> eventPoints = calculateEventPoints(tournamentId, eventIndex);

                // Add to cumulative points
                for (Map.Entry<User, Integer> entry : eventPoints.entrySet()) {
                    User user = entry.getKey();
                    Integer points = entry.getValue();
                    cumulativePoints.put(user, cumulativePoints.getOrDefault(user, 0) + points);
                }
            } catch (IllegalArgumentException e) {
                // Skip events that can't have points calculated
                continue;
            }
        }

        return cumulativePoints;
    }

    /**
     * Calculate points for Single Elimination events
     * Assumes event is complete (all matches finished)
     */
    private Map<User, Integer> calculateSingleElimPoints(SingleElimEvent event, PointsDistribution pointsDistribution) {
        Map<User, Integer> userPoints = new TreeMap<>();
        List<Round> rounds = event.getRounds();

        if (rounds.isEmpty()) {
            return userPoints;
        }

        int totalRounds = rounds.size();

        // Process each round from last to first
        // Losers of each round get points for their placement
        for (int roundIndex = totalRounds - 1; roundIndex >= 0; roundIndex--) {
            Round round = rounds.get(roundIndex);
            String placement = getPlacementForSingleElimRound(totalRounds, roundIndex);
            Integer points = pointsDistribution.getPointsForPlacement(placement);

            for (Match match : round.getMatches()) {
                User loser = getLoser(match);
                if (loser != null && !userPoints.containsKey(loser)) {
                    userPoints.put(loser, points);
                }
            }
        }

        // Award 1st place points to the tournament winner
        Round finalRound = rounds.get(totalRounds - 1);
        if (!finalRound.getMatches().isEmpty()) {
            Match finalMatch = finalRound.getMatches().get(0);
            User winner = finalMatch.getWinner();
            if (winner != null) {
                Integer winnerPoints = pointsDistribution.getPointsForPlacement("1");
                userPoints.put(winner, winnerPoints);
            }
        }

        return userPoints;
    }

    /**
     * Calculate points for Round Robin events
     */
    private Map<User, Integer> calculateRoundRobinPoints(RoundRobinEvent event, PointsDistribution pointsDistribution) {
        Map<User, Integer> userPoints = new TreeMap<>();
        Map<User, Integer> userWins = new TreeMap<>();
        Map<User, Integer> userTotalScore = new TreeMap<>();

        // Initialize all players
        for (User player : event.getPlayers()) {
            userWins.put(player, 0);
            userTotalScore.put(player, 0);
        }

        // Count wins and total scores for each player
        for (PlayerSchedule schedule : event.getPlayerSchedules()) {
            User player = schedule.getPlayer();
            int wins = 0;
            int totalScore = 0;

            for (Match match : schedule.getMatches()) {
                if (match.isCompleted()) {
                    if (match.getWinner() != null && match.getWinner().equals(player)) {
                        wins++;
                    }
                    // Calculate player's score in this match
                    if (match.getScore() != null && !match.getScore().isEmpty()) {
                        totalScore += getPlayerScoreInMatch(match, player);
                    }
                }
            }

            userWins.put(player, wins);
            userTotalScore.put(player, totalScore);
        }

        // Sort players by wins (then by total score as tiebreaker)
        List<User> sortedPlayers = new ArrayList<>(event.getPlayers());
        sortedPlayers.sort((u1, u2) -> {
            int winsCompare = Integer.compare(userWins.get(u2), userWins.get(u1));
            if (winsCompare != 0) return winsCompare;
            return Integer.compare(userTotalScore.get(u2), userTotalScore.get(u1));
        });

        // Assign points based on placement
        for (int i = 0; i < sortedPlayers.size(); i++) {
            User player = sortedPlayers.get(i);
            String placement = String.valueOf(i + 1);
            Integer points = pointsDistribution.getPointsForPlacement(placement);
            userPoints.put(player, points);
        }

        return userPoints;
    }

    /**
     * Calculate points for Double Elimination events
     */
    private Map<User, Integer> calculateDoubleElimPoints(DoubleElimEvent event, PointsDistribution pointsDistribution) {
        Map<User, Integer> userPoints = new TreeMap<>();
        Map<User, String> userPlacements = new TreeMap<>();

        // Determine placements based on when players were eliminated
        List<DoubleElimRound> winnersRounds = event.getWinnersBracket();
        List<DoubleElimRound> losersRounds = event.getLosersBracket();

        // Track eliminations from losers bracket (these determine final placements)
        for (int i = losersRounds.size() - 1; i >= 0; i--) {
            DoubleElimRound round = losersRounds.get(i);
            String placement = getPlacementForDoubleElimLosersRound(losersRounds.size(), i, losersRounds);

            for (Match match : round.getMatches()) {
                if (match.isCompleted() && match.getWinner() != null) {
                    User loser = getLoser(match);
                    if (loser != null && !userPlacements.containsKey(loser)) {
                        userPlacements.put(loser, placement);
                    }
                }
            }
        }

        // Determine winner (grand finals winner)
        if (!losersRounds.isEmpty()) {
            DoubleElimRound lastLosersRound = losersRounds.get(losersRounds.size() - 1);
            if (!lastLosersRound.getMatches().isEmpty()) {
                Match grandFinals = lastLosersRound.getMatches().get(0);
                if (grandFinals.isCompleted() && grandFinals.getWinner() != null) {
                    userPlacements.put(grandFinals.getWinner(), "1");
                    User loser = getLoser(grandFinals);
                    if (loser != null && !userPlacements.containsKey(loser)) {
                        userPlacements.put(loser, "2");
                    }
                }
            }
        }

        // Convert placements to points
        for (Map.Entry<User, String> entry : userPlacements.entrySet()) {
            User player = entry.getKey();
            String placement = entry.getValue();
            Integer points = pointsDistribution.getPointsForPlacement(placement);
            userPoints.put(player, points);
        }

        return userPoints;
    }

    /**
     * Helper: Get placement string for losers of a single elim round
     * totalRounds=3 (8 players): roundIndex 2="2" (finals loser), 1="3" (semis losers), 0="5" (quarters losers)
     */
    private String getPlacementForSingleElimRound(int totalRounds, int roundIndex) {
        if (roundIndex == totalRounds - 1) return "2";  // Finals loser gets 2nd place
        if (roundIndex == totalRounds - 2) return "3";  // Semifinals losers get 3rd place

        // For earlier rounds: quarterfinalist losers=5th, round of 16 losers=9th, etc.
        int playersAtThisLevel = (int) Math.pow(2, roundIndex + 1);
        return String.valueOf(playersAtThisLevel / 2 + 1);
    }

    /**
     * Helper: Get placement string for double elim losers round
     *
     * For a 16-player tournament (7 losers rounds):
     * - LR6 (Grand Finals): 2nd place (1 player eliminated)
     * - LR5 (Losers Finals): 3rd place (1 player eliminated)
     * - LR4: 4th place (1 player eliminated)
     * - LR3: 5th place (2 players eliminated)
     * - LR2: 7th place (2 players eliminated)
     * - LR1: 9th place (4 players eliminated)
     * - LR0: 13th place (4 players eliminated)
     *
     * The placement is based on how many players were eliminated in later rounds.
     */
    private String getPlacementForDoubleElimLosersRound(int totalLosersRounds, int roundIndex, List<DoubleElimRound> losersRounds) {
        if (roundIndex == totalLosersRounds - 1) return "2";  // Grand finals loser (2nd place)
        if (roundIndex == totalLosersRounds - 2) return "3";  // Losers finals loser (3rd place)

        // Calculate how many players have already been eliminated in later rounds
        // This determines the starting placement for this round
        int placementNumber = 4; // Start after 1st, 2nd, 3rd

        // Count from the second-to-last round down to current round
        for (int i = totalLosersRounds - 3; i > roundIndex; i--) {
            // Each losers round eliminates as many players as there are matches
            // (each match has 1 loser)
            int matchesInRound = losersRounds.get(i).getMatches().size();
            placementNumber += matchesInRound;
        }

        return String.valueOf(placementNumber);
    }

    /**
     * Helper: Get the loser of a match
     */
    private User getLoser(Match match) {
        if (match.getWinner() == null) return null;
        if (match.getWinner().equals(match.getPlayerA())) {
            return match.getPlayerB();
        } else {
            return match.getPlayerA();
        }
    }

    /**
     * Helper: Get player's score in a specific match
     */
    private int getPlayerScoreInMatch(Match match, User player) {
        if (match.getScore() == null || match.getScore().isEmpty()) {
            return 0;
        }

        // Assume score list has [playerA score, playerB score]
        if (match.getPlayerA() != null && match.getPlayerA().equals(player)) {
            return match.getScore().get(0);
        } else if (match.getPlayerB() != null && match.getPlayerB().equals(player)) {
            return match.getScore().size() > 1 ? match.getScore().get(1) : 0;
        }

        return 0;
    }

    // ==================== SEEDING METHODS ====================

    /**
     * Validate seeding for an event before initialization
     * @param event The event to validate
     */
    private void validateSeeding(BaseEvent event) {
        Map<Long, Integer> playerSeeds = event.getPlayerSeeds();
        if (playerSeeds == null || playerSeeds.isEmpty()) {
            // No seeds set, that's fine
            return;
        }

        // Validate: Only Single Elim and Double Elim can be seeded
        if (event instanceof RoundRobinEvent) {
            throw new IllegalArgumentException("Round Robin events cannot use seeding");
        }

        List<User> players = event.getPlayers();
        List<Long> playerIds = players.stream().map(User::getId).toList();

        // Validate: all seeded players must be in the event
        for (Long userId : playerSeeds.keySet()) {
            if (!playerIds.contains(userId)) {
                throw new IllegalArgumentException("Seeded user with ID " + userId + " is not registered in this event");
            }
        }

        // Validate: number of seeds cannot exceed number of players
        if (playerSeeds.size() > players.size()) {
            throw new IllegalArgumentException("Number of seeds (" + playerSeeds.size() +
                ") cannot exceed number of players (" + players.size() + ")");
        }

        // Validate: seed numbers must be sequential starting from 1
        List<Integer> seedNumbers = new ArrayList<>(playerSeeds.values());
        seedNumbers.sort(Integer::compareTo);
        for (int i = 0; i < seedNumbers.size(); i++) {
            if (seedNumbers.get(i) != i + 1) {
                throw new IllegalArgumentException("Seed numbers must be sequential starting from 1. Found: " + seedNumbers);
            }
        }

        // Validate: no duplicate seed numbers
        Set<Integer> uniqueSeeds = new HashSet<>(playerSeeds.values());
        if (uniqueSeeds.size() != playerSeeds.values().size()) {
            throw new IllegalArgumentException("Duplicate seed numbers found");
        }
    }

    /**
     * Manually set seeds for players in an event
     * @param tournamentId Tournament ID
     * @param eventIndex Event index
     * @param playerSeeds Map of user ID to seed number (1 = first seed, 2 = second, etc.)
     */
    public void setManualSeeds(Long tournamentId, int eventIndex, Map<Long, Integer> playerSeeds) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        // Validate: cannot set seeds after initialization
        if (event.isEventInitialized()) {
            throw new IllegalStateException("Cannot set seeds after event has been initialized");
        }

        // Validate: all seeded players must be registered in the event
        List<Long> playerIds = event.getPlayers().stream().map(User::getId).toList();
        for (Long userId : playerSeeds.keySet()) {
            if (!playerIds.contains(userId)) {
                throw new IllegalArgumentException("User with ID " + userId + " is not registered in this event");
            }
        }

        // Validate: number of seeds cannot exceed number of players
        if (playerSeeds.size() > event.getPlayers().size()) {
            throw new IllegalArgumentException("Number of seeds (" + playerSeeds.size() +
                ") cannot exceed number of players (" + event.getPlayers().size() + ")");
        }

        // Validate: seed numbers must be sequential starting from 1
        List<Integer> seedNumbers = new ArrayList<>(playerSeeds.values());
        seedNumbers.sort(Integer::compareTo);
        for (int i = 0; i < seedNumbers.size(); i++) {
            if (seedNumbers.get(i) != i + 1) {
                throw new IllegalArgumentException("Seed numbers must be sequential starting from 1. Found: " + seedNumbers);
            }
        }

        // Validate: no duplicate seed numbers
        Set<Integer> uniqueSeeds = new HashSet<>(playerSeeds.values());
        if (uniqueSeeds.size() != playerSeeds.values().size()) {
            throw new IllegalArgumentException("Duplicate seed numbers found");
        }

        // Set the seeds
        event.setPlayerSeeds(playerSeeds);
        eventRepo.save(event);
    }

    /**
     * Automatically set seeds from league rankings
     * @param tournamentId Tournament ID
     * @param eventIndex Event index
     * @param numberOfSeeds Number of top players to seed
     */
    public void setAutoSeeds(Long tournamentId, int eventIndex, int numberOfSeeds) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        Tournament tournament = getTournament(tournamentId);

        // Validate: cannot set seeds after initialization
        if (event.isEventInitialized()) {
            throw new IllegalStateException("Cannot set seeds after event has been initialized");
        }

        // Validate: number of seeds cannot exceed number of players
        if (numberOfSeeds > event.getPlayers().size()) {
            throw new IllegalArgumentException("Number of seeds (" + numberOfSeeds +
                ") cannot exceed number of players (" + event.getPlayers().size() + ")");
        }

        // Validate: numberOfSeeds must be positive
        if (numberOfSeeds <= 0) {
            throw new IllegalArgumentException("Number of seeds must be positive");
        }

        // Find leagues this tournament belongs to
        List<League> leagues = leagueRepo.findByTournamentsContaining(tournament);

        if (leagues.isEmpty()) {
            throw new IllegalArgumentException("Tournament is not part of any league. Cannot auto-seed from rankings.");
        }

        // Use the first league (in practice, a tournament might be in multiple leagues)
        League league = leagues.get(0);

        // Get all rankings for this league, sorted by rank
        List<LeaguePlayerRanking> rankings = league.getPlayerRankings();

        // Create a map of user ID to their league rank
        Map<Long, Integer> userRankMap = new HashMap<>();
        for (LeaguePlayerRanking ranking : rankings) {
            userRankMap.put(ranking.getPlayer().getId(), ranking.getRank());
        }

        // Filter event players who have rankings, and sort by their rank
        List<User> rankedPlayers = event.getPlayers().stream()
            .filter(player -> userRankMap.containsKey(player.getId()))
            .sorted((p1, p2) -> {
                int rank1 = userRankMap.get(p1.getId());
                int rank2 = userRankMap.get(p2.getId());
                return Integer.compare(rank1, rank2);
            })
            .toList();

        if (rankedPlayers.isEmpty()) {
            throw new IllegalArgumentException("No players in this event have rankings in the league");
        }

        // Take the top N ranked players and assign seeds
        Map<Long, Integer> playerSeeds = new HashMap<>();
        int seedsToAssign = Math.min(numberOfSeeds, rankedPlayers.size());

        for (int i = 0; i < seedsToAssign; i++) {
            User player = rankedPlayers.get(i);
            playerSeeds.put(player.getId(), i + 1); // Seed 1, 2, 3, ...
        }

        // Set the seeds
        event.setPlayerSeeds(playerSeeds);
        eventRepo.save(event);
    }

    // ==================== EVENT REGISTRATION METHODS ====================

    /**
     * Player signs up for an event (creates pending registration)
     */
    public EventRegistration signUpForEvent(Long tournamentId, int eventIndex, User user) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        // Validate: Event must not be initialized yet
        if (event.isEventInitialized()) {
            throw new IllegalStateException("Cannot sign up for an event that has already been initialized");
        }

        // Validate: User not already registered
        if (eventRegistrationRepo.existsByEventAndUser(event, user)) {
            throw new IllegalArgumentException("You have already signed up for this event");
        }

        // Validate: User not already a player
        if (event.getPlayers().contains(user)) {
            throw new IllegalArgumentException("You are already a player in this event");
        }

        // Create and save registration
        EventRegistration registration = new EventRegistration(event, user);
        return eventRegistrationRepo.save(registration);
    }

    /**
     * Get all registrations for an event (for moderators)
     */
    public List<EventRegistration> getEventRegistrations(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        return eventRegistrationRepo.findByEvent(event);
    }

    /**
     * Get pending registrations for an event (for moderators)
     */
    public List<EventRegistration> getPendingRegistrations(Long tournamentId, int eventIndex) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);
        return eventRegistrationRepo.findByEventAndStatus(event, EventRegistration.RegistrationStatus.PENDING);
    }

    /**
     * Approve multiple registrations at once
     */
    public void approveRegistrations(Long tournamentId, int eventIndex, List<Long> registrationIds, User reviewer) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        // Validate: Event must not be initialized
        if (event.isEventInitialized()) {
            throw new IllegalStateException("Cannot approve registrations after event has been initialized");
        }

        for (Long registrationId : registrationIds) {
            EventRegistration registration = eventRegistrationRepo.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + registrationId));

            // Verify registration belongs to this event
            if (!registration.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException("Registration " + registrationId + " does not belong to this event");
            }

            // Skip if already approved
            if (registration.getStatus() == EventRegistration.RegistrationStatus.APPROVED) {
                continue;
            }

            // Approve the registration
            registration.setStatus(EventRegistration.RegistrationStatus.APPROVED);
            registration.setReviewedAt(new Date());
            registration.setReviewedBy(reviewer);
            eventRegistrationRepo.save(registration);

            // Add player to event
            event.addPlayer(registration.getUser());
        }

        eventRepo.save(event);
    }

    /**
     * Reject a registration
     */
    public void rejectRegistration(Long tournamentId, int eventIndex, Long registrationId, User reviewer) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        EventRegistration registration = eventRegistrationRepo.findById(registrationId)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Verify registration belongs to this event
        if (!registration.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException("Registration does not belong to this event");
        }

        // Reject the registration
        registration.setStatus(EventRegistration.RegistrationStatus.REJECTED);
        registration.setReviewedAt(new Date());
        registration.setReviewedBy(reviewer);
        eventRegistrationRepo.save(registration);
    }

    /**
     * Cancel your own registration (before it's reviewed)
     */
    public void cancelRegistration(Long tournamentId, int eventIndex, User user) {
        BaseEvent event = getEventsForTournament(tournamentId).get(eventIndex);

        EventRegistration registration = eventRegistrationRepo.findByEventAndUser(event, user)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Can only cancel if still pending
        if (registration.getStatus() != EventRegistration.RegistrationStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending registrations");
        }

        eventRegistrationRepo.delete(registration);
    }

    // ==================== TOURNAMENT METADATA METHODS ====================

    /**
     * Get tournament location
     */
    public String getTournamentLocation(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        return tournament.getLocation();
    }

    /**
     * Set tournament location
     */
    public void setTournamentLocation(Long tournamentId, String location) {
        Tournament tournament = getTournament(tournamentId);
        tournament.setLocation(location);
        tournamentRepo.save(tournament);
    }

    /**
     * Update tournament's message, begin date, end date, and location
     * Allows updating all fields at once with validation
     */
    public Tournament updateTournamentDetails(Long tournamentId, String message, Date begin, Date end, String location) {
        Tournament tournament = getTournament(tournamentId);

        // Validate begin and end dates if both are provided
        if (begin != null && end != null && begin.after(end)) {
            throw new IllegalArgumentException("Begin date must be before end date");
        }

        // Update message if provided
        if (message != null) {
            tournament.setMessage(message);
        }

        // Update begin date if provided
        if (begin != null) {
            // Validate against existing end date if not updating end
            if (end == null && tournament.getEnd() != null && begin.after(tournament.getEnd())) {
                throw new IllegalArgumentException("Begin date must be before end date");
            }
            tournament.setBegin(begin);
        }

        // Update end date if provided
        if (end != null) {
            // Validate against existing begin date if not updating begin
            if (begin == null && tournament.getBegin() != null && end.before(tournament.getBegin())) {
                throw new IllegalArgumentException("End date must be after begin date");
            }
            tournament.setEnd(end);
        }

        // Update location if provided
        if (location != null) {
            tournament.setLocation(location);
        }

        return tournamentRepo.save(tournament);
    }
}