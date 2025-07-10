package com.tournamenthost.connect.frontend.with.backend.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * creates and stores aa match which has two players and a score. THis current implementation is that of a single elim tournament
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

    private String playerA;//currently just using strings to make it simple, will change later

    private String playerB;

    private List<Integer> score;

    @OneToOne
    @JoinColumn(name = "previous_match_a_id")
    private Match previousMatchA;

    @OneToOne
    @JoinColumn(name = "previous_match_b_id")
    private Match previousMatchB;

    // Optional: track this match's "next" (where the winner goes)
    @ManyToOne
    @JoinColumn(name = "next_match_id")
    private Match nextMatch;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return id != null && id.equals(match.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
