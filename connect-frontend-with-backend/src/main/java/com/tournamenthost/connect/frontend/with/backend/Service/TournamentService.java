package com.tournamenthost.connect.frontend.with.backend.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tournamenthost.connect.frontend.with.backend.Model.Match;
import com.tournamenthost.connect.frontend.with.backend.Model.Round;
import com.tournamenthost.connect.frontend.with.backend.Model.Tournament;
import com.tournamenthost.connect.frontend.with.backend.Repository.MatchRepository;
import com.tournamenthost.connect.frontend.with.backend.Repository.TournamentRepository;
import com.tournamenthost.connect.frontend.with.backend.util.TournamentUtil;

@Service
public class TournamentService {
    
    @Autowired
    private TournamentRepository tournamentRepo;

    @Autowired 
    private MatchRepository matchRepo;

    public Tournament addTournament(String name) {
        // Check for duplicate tournament name (case and space insensitive)
        if (tournamentRepo.existsByNameIgnoreCaseAndSpaces(name)) {
            throw new IllegalArgumentException("Tournament with name '" + name + "' already exists");
        }
        Tournament tournament = new Tournament();
        tournament.setName(name);
        return tournamentRepo.save(tournament);
    }

    public Tournament changeTournamentName(Long tournamentId, String newName) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));

        if (tournamentRepo.existsByNameIgnoreCaseAndSpaces(newName)) {
            throw new IllegalArgumentException("Tournament with name '" + newName + "' already exists");
        }
        tournament.setName(newName);
        return tournamentRepo.save(tournament);
    }

    public Tournament getTournament(Long tournamentId){
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        return tournament;
    }

    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        tournamentRepo.findAll().forEach(tournaments::add);
        return tournaments;
    }

    public void addPlayer(Long tournamentId, String name) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));

        if(tournamentRepo.isTournamentInitialized(tournamentId)) {
            throw new IllegalArgumentException("Tournament with id " + tournamentId + "has already be initiated");
        }
        tournament.addPlayer(name);
        tournamentRepo.save(tournament);
    }

    public void addPlayer(Long tournamentId, List<String> names) {
        for(String name: names) {
            this.addPlayer(tournamentId, name);
        }
    }

    public List<String> getPlayers(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        return tournament.getPlayers();
    }


    public void initializeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));
        List<String> players = tournament.getPlayers();

        if (players.size() <= 2) {
            throw new IllegalArgumentException("There are too few players, the begining of this event demands at least 3 players");
        }

        if  (tournamentRepo.isTournamentInitialized(tournamentId)){
            throw new IllegalArgumentException("this tournament is already been initialized, please deinitialize it first");
        }
        //first create the nessesary matches in an nested arraylist

        int matchAmount = TournamentUtil.nextPowerOfTwo(players.size())/2;
        int matchAmountForCurRound = matchAmount;
        List<List<Match>> tournamentInNestedArr = new ArrayList<>();
        List<Match> allMatch = new ArrayList<>();
        
        while(matchAmountForCurRound >= 1) {
            List<Match> curRoundMatches = new ArrayList<>();
            for(int i = 0; i < matchAmountForCurRound; i++) {
                Match curMatch = new Match();
                curMatch.setTournament(tournament);
                curRoundMatches.add(curMatch);
                allMatch.add(curMatch);
            }
            tournamentInNestedArr.add(curRoundMatches);
            matchAmountForCurRound /= 2;
        }


        //assign proper players to the draw, using another method, and then assign them to the first round matches

        ArrayList<String> draw = TournamentUtil.generateDrawUsingSeeding(players, matchAmount);
        List<Match> bottomRoundMatches = tournamentInNestedArr.get(0);
        for(int i = 0; i < draw.size(); i++) {
            if(i%2 == 0) {
                bottomRoundMatches.get(i/2).setPlayerA(draw.get(i));
            } else {
                bottomRoundMatches.get(i/2).setPlayerB(draw.get(i));
            }
        }

        // Create and save rounds
        List<Round> rounds = new ArrayList<>();
        for (int i = 0; i < tournamentInNestedArr.size(); i++) {
            Round round = new Round();
            round.setMatches(tournamentInNestedArr.get(i));
            round.setRoundNumber(i + 1);
            rounds.add(round);
            round.setTournament(tournament);
        }
        // Save rounds (matches will be cascaded)
        // If you have a RoundRepository, you can saveAll(rounds) here, otherwise rely on cascade

        tournament.addRound(rounds);

        // Save all matches to the database
        matchRepo.saveAll(allMatch);

        // Save (update) the tournament in the database (rounds will be cascaded)
        tournamentRepo.save(tournament);


    }

    public void deinitializeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));

        if (!tournamentRepo.isTournamentInitialized(tournamentId)) {
            throw new IllegalArgumentException("This tournament was never initialized, nothing to deinitailize");
        }


        
        // Clear rounds
        tournament.getRounds().clear();
        

        
        // Save tournament
        tournamentRepo.save(tournament);
    }

    /**
     * Returns the tournament draw as a nested list of matches (one list per round).
     */
    public List<List<Match>> getTournamentDraw(Long tournamentId) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament with id " + tournamentId + " not found"));


        List<List<Match>> draw = new ArrayList<>();
        if (tournament.getRounds() != null) {
            for (Round round : tournament.getRounds()) {
                draw.add(round.getMatches());
            }
        }
        return draw;
    }

    

}
