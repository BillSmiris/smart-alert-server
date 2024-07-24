package org.unipi.mpsp2343.SmartAlert.model;

import org.unipi.mpsp2343.SmartAlert.dtos.event.Location;
import org.unipi.mpsp2343.SmartAlert.enums.EventStatus;
import org.unipi.mpsp2343.SmartAlert.enums.EventType;

import java.util.List;

//Class that represents a reported event in the db.
public class Event {
    private EventType eventType; //Type of the event
    private Location location; //Location of the first report of the event.
    private long timestamp; //Timestamp of the first report of the event.
    private List<Report> reports; //Reports of the event.
    private long numberOfReports; //Number of times the event has been reported.
    private EventStatus eventStatus; //Status of the event.

    public Event() {
    }

    public Event(EventType eventType, Location location, long timestamp, List<Report> reports, long numberOfReports, EventStatus eventStatus) {
        this.eventType = eventType;
        this.location = location;
        this.timestamp = timestamp;
        this.reports = reports;
        this.numberOfReports = numberOfReports;
        this.eventStatus = eventStatus;
    }

    public Event(EventType eventType, long timestamp, long numberOfReports) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.numberOfReports = numberOfReports;
    }

    public Event(EventType eventType, Location location, long numberOfReports) {
        this.eventType = eventType;
        this.location = location;
        this.numberOfReports = numberOfReports;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
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

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public void addReport(Report report) {
        this.reports.add(report);
    }
}
