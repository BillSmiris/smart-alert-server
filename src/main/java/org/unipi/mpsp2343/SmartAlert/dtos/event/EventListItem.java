package org.unipi.mpsp2343.SmartAlert.dtos.event;

//Model for returning the reported events to the employee, with enough information for them to be displayed in a list format.
public class EventListItem {
    private String id;
    private int eventType;
    private long timestamp;
    private long numberOfReports;

    public EventListItem() {
    }

    public EventListItem(String id, int eventType, long timestamp, long numberOfReports) {
        this.id = id;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.numberOfReports = numberOfReports;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(long numberOfReports) {
        this.numberOfReports = numberOfReports;
    }
}
