package org.unipi.mpsp2343.SmartAlert.controller.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.unipi.mpsp2343.SmartAlert.dtos.event.EventListItem;
import org.unipi.mpsp2343.SmartAlert.dtos.event.GetEventResponseDto;
import org.unipi.mpsp2343.SmartAlert.enums.EventStatus;
import org.unipi.mpsp2343.SmartAlert.service.EventService;

import java.util.List;

//Controller for handling requests related to the events for users with the ADMIN role.
@RestController
@RequestMapping("api/v1/admin/event")
public class AdminEventController {
    private static final Logger logger = LoggerFactory.getLogger(AdminEventController.class);
    private final EventService eventService;
    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    //Gets a lightweight list of OPEN events.
    @GetMapping(path = "list")
    private List<EventListItem> getEventList() {
        return eventService.getEventList();
    }

    //Gets the details of a single event, based on eventId
    @GetMapping(path = "{eventId}")
    public GetEventResponseDto getEventById(@PathVariable("eventId") String eventId){
        return eventService.getEventById(eventId);
    }

    //Sets the status of an event to REJECTED
    @PutMapping(path = "reject/{eventId}")
    public ResponseEntity<?> rejectEvent(@PathVariable("eventId") String eventId) {
        try {
            eventService.updateEventStatus(eventId, EventStatus.REJECTED);
            return ResponseEntity.ok().body("");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    //Sets the status of an event to CONFIRMED
    @PutMapping(path = "confirm/{eventId}")
    public ResponseEntity<?> confirmEvent(@PathVariable("eventId") String eventId) {
        try {
            eventService.updateEventStatus(eventId, EventStatus.CONFIRMED);
            return ResponseEntity.ok().body("");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
