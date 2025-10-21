package com.tournamenthost.connect.frontend.with.backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.EventRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.PointsDistributionRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.LeagueRepository;
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

    public Tournament createTournament(String name, User owner) {
        if (tournamentRepo.existsByNameIgnoreCaseAndSpaces(name)) {
            throw new IllegalArgumentException("Tournament with name '" + name + "' already exists");
        }
        Tournament tournament = new Tournament(name, owner);
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
            ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, matchAmount, event.getPlayerSeeds());
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
            doubleElimEvent.getWinnersBracket().clear();
            doubleElimEvent.getLosersBracket().clear();
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
        }

        // Set match result
        match.setWinner(winner);
        match.setScore(score);
        match.setCompleted(true);

        // Save the match
        matchRepo.save(match);

        
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
     * Initialize a Double Elimination event
     * Creates winners bracket, losers bracket with feed-in structure
     */
    private void initializeDoubleElimEvent(DoubleElimEvent doubleElim, List<User> players) {
        int bracketSize = TournamentUtil.nextPowerOfTwo(players.size());
        int winnersRoundCount = (int) (Math.log(bracketSize) / Math.log(2));

        // Calculate losers rounds: approximately 2 * winnersRounds - 1
        int losersRoundCount = (2 * winnersRoundCount) - 1;

        // Set feed-in cutoff (last 2 rounds of winners don't feed in)
        doubleElim.setFeedInCutoffRound(Math.max(0, winnersRoundCount - 2));

        List<Match> allMatches = new ArrayList<>();

        // 1. Create Winners Bracket (similar to single elim)
        List<DoubleElimRound> winnersRounds = createWinnersBracket(doubleElim, players, bracketSize, allMatches);
        doubleElim.addWinnersRounds(winnersRounds);

        // 2. Create Losers Bracket (empty matches, filled as feed-ins occur)
        List<DoubleElimRound> losersRounds = createLosersBracket(doubleElim, losersRoundCount, winnersRoundCount, allMatches);
        doubleElim.addLosersRounds(losersRounds);

        // Save all matches and event
        matchRepo.saveAll(allMatches);
        eventRepo.save(doubleElim);
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

        return winnersRounds;
    }

    /**
     * Create the losers bracket structure
     */
    private List<DoubleElimRound> createLosersBracket(DoubleElimEvent event, int losersRoundCount,
                                                       int winnersRoundCount, List<Match> allMatches) {
        List<DoubleElimRound> losersRounds = new ArrayList<>();

        // Losers bracket has alternating rounds:
        // - Feed-in rounds (receive losers from winners)
        // - Progression rounds (winners of losers matches advance)

        int matchCount = event.getPlayers().size() / 4; // Start with half the first round losers

        for (int round = 0; round < losersRoundCount; round++) {
            DoubleElimRound doubleElimRound = new DoubleElimRound(round, BracketType.LOSERS);
            doubleElimRound.setEvent(event);

            // Determine if this is a feed-in round
            int winnersRoundFeeding = round / 2;
            if (winnersRoundFeeding < winnersRoundCount && round % 2 == 0) {
                doubleElimRound.setFeedsFromWinnersRound(winnersRoundFeeding);
            }

            List<Match> roundMatches = new ArrayList<>();
            for (int i = 0; i < Math.max(1, matchCount); i++) {
                Match match = new Match();
                match.setEvent(event);
                // Players will be filled in via feed-in or progression
                roundMatches.add(match);
                allMatches.add(match);
            }

            doubleElimRound.setMatches(roundMatches);
            losersRounds.add(doubleElimRound);

            // Adjust match count for next round
            if (round % 2 == 0) {
                // Feed-in round: match count stays same or increases
                matchCount = Math.max(1, matchCount);
            } else {
                // Progression round: match count halves
                matchCount = Math.max(1, matchCount / 2);
            }
        }

        return losersRounds;
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
}