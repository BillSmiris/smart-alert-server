package org.unipi.mpsp2343.SmartAlert.enums;

//This enum contains the valid statuses for a reported event.
public enum EventStatus {
    OPEN, //Event that has not went through review by an employee.
    REJECTED, //Event rejected by an employee.
    CONFIRMED //Event that has been confirmed as valid by an employee.
}
