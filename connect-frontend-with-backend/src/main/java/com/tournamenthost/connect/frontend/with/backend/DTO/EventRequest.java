package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    private String name;
    private String eventType;
    private Long tournamentId;
}