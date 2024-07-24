package org.unipi.mpsp2343.SmartAlert.service;

import com.google.firebase.database.DatabaseReference;
import org.springframework.stereotype.Service;
import org.unipi.mpsp2343.SmartAlert.dtos.event.EventListItem;
import org.unipi.mpsp2343.SmartAlert.dtos.event.GetEventResponseDto;
import org.unipi.mpsp2343.SmartAlert.dtos.event.PostEventDto;
import org.unipi.mpsp2343.SmartAlert.dtos.event.SendAlertDto;
import org.unipi.mpsp2343.SmartAlert.enums.EventStatus;
import org.unipi.mpsp2343.SmartAlert.enums.EventType;
import org.unipi.mpsp2343.SmartAlert.model.Event;
import org.unipi.mpsp2343.SmartAlert.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    //Saves an event report
    public void saveEvent(PostEventDto dto, String userEmail){
        try {
            eventRepository.saveEvent(dto, userEmail);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("DB_ERROR");
        }
    }

    //Retrieves a lightweight event list
    public List<EventListItem> getEventList() {
        try {
            return eventRepository.getEventList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("DB_ERROR");
        }
    }

    //Retrieves the details of a single event, based on id
    public GetEventResponseDto getEventById(String eventId) {
        try {
            return eventRepository.getEventById(eventId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("DB_ERROR");
        }
    }

    //Updates the status of an event
    public void updateEventStatus(String eventId, EventStatus newEventStatus) {
        try {
            eventRepository.updateEventStatus(eventId, newEventStatus);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("DB_ERROR");
        }
    }

    //Checks for any possible alerts that have to be sent to a user
    public List<SendAlertDto> checkForAlerts(double lat, double lon, String userId) {
        try {
            return eventRepository.getAlerts(lat, lon, userId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("DB_ERROR");
        }
    }
}
