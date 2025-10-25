package com.tournamenthost.connect.frontend.with.backend.DTO;

import java.util.Date;

public class TournamentUpdateRequest {
    private String message;
    private Date begin;
    private Date end;
    private String location;

    public TournamentUpdateRequest() {}

    public TournamentUpdateRequest(String message, Date begin, Date end, String location) {
        this.message = message;
        this.begin = begin;
        this.end = end;
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
