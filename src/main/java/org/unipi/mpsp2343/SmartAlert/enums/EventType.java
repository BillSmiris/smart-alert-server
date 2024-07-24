package org.unipi.mpsp2343.SmartAlert.enums;

//This enum contains the available event types and relative information.
public enum EventType {
    FLOOD,
    FIRE,
    EARTHQUAKE,
    TORNADO;

    //For a reported event to be considered the same as another reported event, it has to be the same type as the other event,
    //be within a certain range of the other event and be reported within a certain timeframe from the other event.
    //Time ranges, per event type, for a reported event to be considered the same as another event.
    private static final long[] timeRange = {7200000, 7200000, 7200000, 7200000};

    //Geographical ranges, per event type, for a reported event to be considered the same as another event.
    private static final long[] geoRange = {200, 200, 200, 200};

    //For a user to be notified for an event, they have to be in a certain range of the event in a certain timeframe after the reporting of the event.
    //Time ranges, per event type, for a user to be notified for a confirmed event.
    private static final long[] alertTimeRange = {600000, 600000, 600000, 600000};

    //Geographical ranges, per event type, for a user to be notified for a confirmed event.
    private static final long[] alertGeoRange = {300, 300, 300, 300};

    public static long getTimeRange(EventType eventType) {
        return timeRange[eventType.ordinal()];
    }

    public static long getGeoRange(EventType eventType) {
        return geoRange[eventType.ordinal()];
    }

    public static long getAlertTimeRange(EventType eventType) {
        return alertTimeRange[eventType.ordinal()];
    }

    public static long getAlertGeoRange(EventType eventType) {
        return alertGeoRange[eventType.ordinal()];
    }

}
