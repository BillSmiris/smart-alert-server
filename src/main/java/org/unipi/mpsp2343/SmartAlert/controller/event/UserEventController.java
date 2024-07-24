package org.unipi.mpsp2343.SmartAlert.controller.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.unipi.mpsp2343.SmartAlert.configuration.security.FirebaseAuthenticationToken;
import org.unipi.mpsp2343.SmartAlert.dtos.event.PostEventDto;
import org.unipi.mpsp2343.SmartAlert.dtos.event.SendAlertDto;
import org.unipi.mpsp2343.SmartAlert.service.EventService;

import java.util.List;

//Controller for handling requests related to the events for users with the USER role.
@RestController
@RequestMapping("api/v1/user/event")
public class UserEventController {
    private static final Logger logger = LoggerFactory.getLogger(UserEventController.class);

    private final EventService eventService;
    public UserEventController(EventService eventService) {
        this.eventService = eventService;
    }

    //Posts the info of a reported event
    @PostMapping()
    private ResponseEntity<?> login(@RequestBody PostEventDto postEventDto, FirebaseAuthenticationToken principal) {
        try {
            eventService.saveEvent(postEventDto, (String) principal.getEmail());
            return ResponseEntity.ok().body("");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    //Check for new disaster alerts. Takes as parameters the user's location.
    @GetMapping("alerts")
    private List<SendAlertDto> checkForAlerts(@RequestParam double lat, @RequestParam double lon, FirebaseAuthenticationToken principal) {
        return eventService.checkForAlerts(lat, lon, (String) principal.getPrincipal());
    }
}
