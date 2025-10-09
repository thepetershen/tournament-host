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
        } else {
            event.unInitiateEvent();
        }
        if (event instanceof SingleElimEvent singleElimEvent) {
            singleElimEvent.getRounds().clear();
        } else if (event instanceof RoundRobinEvent roundRobinEvent) {
            roundRobinEvent.getPlayerSchedules().clear();
        } else if (event instanceof DoubleElimEvent doubleElimEvent) {
            doubleElimEvent.getWinnersBracket().clear();
            doubleElimEvent.getLosersBracket().clear();
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
        ArrayList<User> draw = TournamentUtil.generateDrawUsingSeeding(players, matchCount);

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
}