package org.unipi.mpsp2343.SmartAlert.dtos.event;

import org.unipi.mpsp2343.SmartAlert.model.Report;

import java.util.List;

//Model for returning the details of an event to an employee for review.
public class GetEventResponseDto {
    private String id;
    private int eventType;
    private Location location;
    private long timestamp;
    private List<Report> reports;
    private long numberOfReports;
    private int eventStatus;

    public GetEventResponseDto() {
    }

    public GetEventResponseDto(String id, int eventType, Location location, long timestamp, List<Report> reports, long numberOfReports, int eventStatus) {
        this.id = id;
        this.eventType = eventType;
        this.location = location;
        this.timestamp = timestamp;
        this.reports = reports;
        this.numberOfReports = numberOfReports;
        this.eventStatus = eventStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public long getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(long numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

    public int getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(int eventStatus) {
        this.eventStatus = eventStatus;
    }
}
