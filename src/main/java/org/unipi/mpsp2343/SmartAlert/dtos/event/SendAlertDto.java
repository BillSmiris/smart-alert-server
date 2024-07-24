package org.unipi.mpsp2343.SmartAlert.dtos.event;

//Model for sending a disaster alert to the user.
public class SendAlertDto {
    private int eventType;
    private Location location;

    public SendAlertDto() {
    }

    public SendAlertDto(int eventType, Location location) {
        this.eventType = eventType;
        this.location = location;
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
}
