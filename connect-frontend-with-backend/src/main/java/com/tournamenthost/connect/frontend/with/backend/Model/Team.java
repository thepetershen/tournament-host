package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player1_id", nullable = false)
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id", nullable = true)
    private User player2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamType teamType;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private BaseEvent event;

    // Constructors
    public Team() {
    }

    public Team(User player1, TeamType teamType, BaseEvent event) {
        this.player1 = player1;
        this.teamType = teamType;
        this.event = event;
    }

    public Team(User player1, User player2, TeamType teamType, BaseEvent event) {
        this.player1 = player1;
        this.player2 = player2;
        this.teamType = teamType;
        this.event = event;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPlayer1() {
        return player1;
    }

    public void setPlayer1(User player1) {
        this.player1 = player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public void setTeamType(TeamType teamType) {
        this.teamType = teamType;
    }

    public BaseEvent getEvent() {
        return event;
    }

    public void setEvent(BaseEvent event) {
        this.event = event;
    }

    /**
     * Get formatted team name for display
     * Singles: "Player A"
     * Doubles: "Player A / Player B"
     */
    public String getTeamName() {
        if (teamType == TeamType.SINGLES) {
            return player1.getName() != null && !player1.getName().isEmpty()
                ? player1.getName()
                : player1.getUsername();
        } else {
            String player1Name = player1.getName() != null && !player1.getName().isEmpty()
                ? player1.getName()
                : player1.getUsername();
            String player2Name = player2 != null
                ? (player2.getName() != null && !player2.getName().isEmpty()
                    ? player2.getName()
                    : player2.getUsername())
                : "TBD";
            return player1Name + " / " + player2Name;
        }
    }

    /**
     * Check if team contains a specific player
     */
    public boolean hasPlayer(User player) {
        if (player == null) return false;
        return (player1 != null && player1.getId().equals(player.getId())) ||
               (player2 != null && player2.getId().equals(player.getId()));
    }
}
