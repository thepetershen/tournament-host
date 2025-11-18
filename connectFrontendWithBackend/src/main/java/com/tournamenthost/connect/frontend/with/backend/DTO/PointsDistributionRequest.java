package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.Map;

public class PointsDistributionRequest {
    private Map<String, Integer> pointsMap;

    public Map<String, Integer> getPointsMap() {
        return pointsMap;
    }

    public void setPointsMap(Map<String, Integer> pointsMap) {
        this.pointsMap = pointsMap;
    }
}
