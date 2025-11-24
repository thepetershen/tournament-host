package com.tournamenthost.connect.frontend.with.backend.Model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * creates and stores a match which has two teams and games. Supports both singles and doubles matches.
 *
 */
@Entity
@Data
@Table(name = "matches")
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // NEW: Team-based fields
    @ManyToOne
    @JoinColumn(name = "team_a_id")
    private Team teamA;

    @ManyToOne
    @JoinColumn(name = "team_b_id")
    private Team teamB;

    @ManyToOne
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchType matchType = MatchType.SINGLES;

    @Column(nullable = false)
    private int gamesRequiredToWin = 1; // For best-of-3, this would be 2

    // OLD: Player-based fields (kept for backward compatibility during migration)
    @ManyToOne
    @JoinColumn(name = "player_a_id")
    private User playerA;

    @ManyToOne
    @JoinColumn(name = "player_b_id")
    private User playerB;

    private List<Integer> score;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;

    private boolean completed;

    @ManyToOne
    @JsonIgnore  // Prevent circular reference: Match -> Event -> matches
    private BaseEvent event;

    /**
     * Add a game to this match
     */
    public void addGame(Game game) {
        games.add(game);
        game.setMatch(this);
    }

    /**
     * Remove a game from this match
     */
    public void removeGame(Game game) {
        games.remove(game);
        game.setMatch(null);
    }

    /**
     * Check if match is completed based on games won
     */
    public boolean isMatchCompleted() {
        if (teamA == null || teamB == null) {
            return false;
        }

        int teamAWins = 0;
        int teamBWins = 0;

        for (Game game : games) {
            if (game.getWinnerTeam() != null) {
                if (game.getWinnerTeam().equals(teamA)) {
                    teamAWins++;
                } else if (game.getWinnerTeam().equals(teamB)) {
                    teamBWins++;
                }
            }
        }

        return teamAWins >= gamesRequiredToWin || teamBWins >= gamesRequiredToWin;
    }

    /**
     * Determine and set the winner based on games won
     */
    public void determineWinner() {
        if (!isMatchCompleted()) {
            this.winnerTeam = null;
            this.completed = false;
            return;
        }

        int teamAWins = 0;
        int teamBWins = 0;

        for (Game game : games) {
            if (game.getWinnerTeam() != null) {
                if (game.getWinnerTeam().equals(teamA)) {
                    teamAWins++;
                } else if (game.getWinnerTeam().equals(teamB)) {
                    teamBWins++;
                }
            }
        }

        if (teamAWins >= gamesRequiredToWin) {
            this.winnerTeam = teamA;
            this.completed = true;
        } else if (teamBWins >= gamesRequiredToWin) {
            this.winnerTeam = teamB;
            this.completed = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Match other = (Match) obj;
        return id != null && id.equals(other.id);
    }
}
