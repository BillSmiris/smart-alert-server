package org.unipi.mpsp2343.SmartAlert.dtos.event;

import org.unipi.mpsp2343.SmartAlert.enums.EventType;

//Model for posting an event report.
public class PostEventDto {
    private String comments;
    private EventType eventType;
    private Location location;
    private long timestamp;
    private String photoBase64;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }
}
