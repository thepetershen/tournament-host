package com.tournamenthost.connect.frontend.with.backend.Model;

import jakarta.persistence.*;
import com.tournamenthost.connect.frontend.with.backend.Model.Event.BaseEvent;

import java.util.HashMap;
import java.util.Map;

@Entity
public class PointsDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private BaseEvent event;

    // Store placement -> points mapping
    // For single elim: "1" (winner), "2" (finalist), "3" (semifinalist), "5" (quarterfinalist), etc.
    // For double elim: Similar to single elim with unique finish positions
    // For round robin: "1", "2", "3", ... for each placement
    @ElementCollection
    @CollectionTable(name = "points_mapping", joinColumns = @JoinColumn(name = "points_distribution_id"))
    @MapKeyColumn(name = "placement")
    @Column(name = "points")
    private Map<String, Integer> pointsMap = new HashMap<>();

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public BaseEvent getEvent() {
        return event;
    }

    public void setEvent(BaseEvent event) {
        this.event = event;
    }

    public Map<String, Integer> getPointsMap() {
        return pointsMap;
    }

    public void setPointsMap(Map<String, Integer> pointsMap) {
        this.pointsMap = pointsMap;
    }

    public void addPlacementPoints(String placement, Integer points) {
        this.pointsMap.put(placement, points);
    }

    public Integer getPointsForPlacement(String placement) {
        return pointsMap.getOrDefault(placement, 0);
    }

    // Constructors
    public PointsDistribution() {
        this.pointsMap = new HashMap<>();
    }

    public PointsDistribution(Tournament tournament, BaseEvent event) {
        this.tournament = tournament;
        this.event = event;
        this.pointsMap = new HashMap<>();
    }
}
