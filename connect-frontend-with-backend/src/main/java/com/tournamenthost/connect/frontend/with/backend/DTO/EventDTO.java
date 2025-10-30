package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private String name;
    private int id;
    private String eventType;

}