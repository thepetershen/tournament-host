package com.tournamenthost.connect.frontend.with.backend.Model;

import java.util.ArrayList;
import java.util.List;

import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.SingleElimEvent;

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

    @ManyToOne
    @JoinColumn(name = "player_a_id")
    private User playerA;

    @ManyToOne
    @JoinColumn(name = "player_b_id")
    private User playerB;

    private List<Integer> score;


    @ManyToOne
    private BaseEvent event;


}
