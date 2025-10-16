package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.Map;

public class PointsDistributionDTO {
    private Long id;
    private Long tournamentId;
    private Integer eventIndex;
    private Map<String, Integer> pointsMap;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Integer getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(Integer eventIndex) {
        this.eventIndex = eventIndex;
    }

    public Map<String, Integer> getPointsMap() {
        return pointsMap;
    }

    public void setPointsMap(Map<String, Integer> pointsMap) {
        this.pointsMap = pointsMap;
    }
}
