package org.unipi.mpsp2343.SmartAlert.repository;

import com.google.api.Documentation;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.unipi.mpsp2343.SmartAlert.dtos.event.*;
import org.unipi.mpsp2343.SmartAlert.enums.EventStatus;
import org.unipi.mpsp2343.SmartAlert.enums.EventType;
import org.unipi.mpsp2343.SmartAlert.model.Event;
import org.unipi.mpsp2343.SmartAlert.model.Report;
import org.unipi.mpsp2343.SmartAlert.model.Role;
import org.unipi.mpsp2343.SmartAlert.service.FirebaseUserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class EventRepository {
    private static final Logger logger = LoggerFactory.getLogger(EventRepository.class);
    Firestore firestore;
    private final CollectionReference events;
    private final CollectionReference alerts;

    public EventRepository(Firestore firestore) {
        this.firestore = firestore;
        events = firestore.collection("events");
        alerts = firestore.collection("alerts");
    }

    //Saves an event to firebase.
    public void saveEvent(PostEventDto dto, String userEmail) throws ExecutionException, InterruptedException {
        //Checks if an event exists and then it either creates a new event with all the info provided by the user or adds
        // the report specific user provided info as a "comment" to the existing event.
        String existingEventId = checkExistingEvent(dto);

        if(existingEventId == null) {
            createEvent(dto, userEmail);
        }
        else {
            updateEvent(existingEventId, dto, userEmail);
        }
    }

    //Checks if a newly reported event already exists in firebase.
    private String checkExistingEvent(PostEventDto dto) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        EventType eventType = dto.getEventType();
        Location eventLocation = dto.getLocation();
        //For an event to be considered existing the following criteria must be true:
        Query query = events.whereEqualTo("eventType", dto.getEventType()) //1.Be of the same type as the newly reported event
                .whereGreaterThanOrEqualTo("timestamp", dto.getTimestamp() - EventType.getTimeRange(eventType)) //2. Its first report must be within a certain timeframe of the new report.
                //3. It must be within a certain geographical range of the newly reported event
                //For example, we can say that for two reports to be for the same fire, the new report has to be made within 5 hours of the first report
                //and within 20 km of the first report. If the second report is done more than five hours or more than 20km far from the first report, it is considered another fire.
                .whereGreaterThanOrEqualTo("location.lat", eventLocation.getLat() - EventType.getGeoRange(eventType))
                .whereLessThanOrEqualTo("location.lat", eventLocation.getLat() + EventType.getGeoRange(eventType))
                .whereGreaterThanOrEqualTo("location.lon", eventLocation.getLon() - EventType.getGeoRange(eventType))
                .whereLessThanOrEqualTo("location.lon", eventLocation.getLon() + EventType.getGeoRange(eventType));

        ApiFuture<QuerySnapshot> querySnapshotApiFuture = query.get();
        querySnapshotApiFuture.addListener(() -> {
            try {
                QuerySnapshot querySnapshot = querySnapshotApiFuture.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
                if (!documents.isEmpty()) {
                    //If the event exists, its id is returned, else null is returned.
                    String docId = documents.get(0).getId();
                    if (docId != null) {
                        future.complete(docId);
                    } else {
                        future.complete(null);
                    }
                } else {
                    future.complete(null);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.info("--!" + e.getMessage());
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Creates a new event in  firebase
    private void createEvent(PostEventDto dto, String userEmail) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future  = new CompletableFuture<>();
        DocumentReference newRole = events.document();
        ApiFuture<WriteResult> writeResultApiFuture = newRole.set(new Event(
                dto.getEventType(), //Type of the event
                dto.getLocation(), //Location of the event. The location of the first report is used to determine if a following report is for the same event.
                dto.getTimestamp(), //Date and time of the event. The timestamp of the first report is used to determine if a following report is for the same event
                //A list of all the report for the event. For every report, the user's email, their comment and the photo they took of the event are kept.
                //They work similar to social media comments.
                List.of(new Report(
                        dto.getComments(),
                        dto.getPhotoBase64(),
                        userEmail
                )),
                1, //The number of reports is set to 1 by default
                EventStatus.OPEN //The event's status is set to OPEN by default
        ));

        //The event is posted to the db
        writeResultApiFuture.addListener(() -> {
            try {
                writeResultApiFuture.get();
                future.complete(null);
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Updates an existing event with a new report
    private void updateEvent(String existingEventId, PostEventDto dto, String userEmail) throws ExecutionException, InterruptedException{
        CompletableFuture<Void> future  = new CompletableFuture<>();
        DocumentReference eventToUpdate = events.document(existingEventId);

        ApiFuture<WriteResult> writeResultApiFuture = eventToUpdate.update(
                "numberOfReports", FieldValue.increment(1), //Increments the number of reports by one
                "reports", FieldValue.arrayUnion(new Report(dto.getComments(), dto.getPhotoBase64(), userEmail)) //Adds the new report
        );

        writeResultApiFuture.addListener(() -> {
            try {
                writeResultApiFuture.get();
                future.complete(null);
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());


        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Gets a lightweight list of all OPEN events
    public List<EventListItem> getEventList() throws ExecutionException, InterruptedException{
        List<EventListItem> results = new ArrayList<>();
        CompletableFuture<List<EventListItem>> future = new CompletableFuture<>();
        Query query = events
                .whereEqualTo("eventStatus", EventStatus.OPEN) //From all open events
                .select("eventType", "timestamp", "numberOfReports"); //Selects type, timestamp and number of reports(to calculate severity)

        ApiFuture<QuerySnapshot> querySnapshotApiFuture = query.get();
        querySnapshotApiFuture.addListener(() -> {
            try {
                QuerySnapshot querySnapshot = querySnapshotApiFuture.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
                if (!documents.isEmpty()) {
                    for(QueryDocumentSnapshot doc: documents){
                        Event event = doc.toObject(Event.class);
                        //The retrieved information is sent to the user along with the id of each event
                        results.add(new EventListItem(
                                doc.getId(),
                                event.getEventType().ordinal(),
                                event.getTimestamp(),
                                event.getNumberOfReports()
                        ));
                    }
                }
                future.complete(results);
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Retrieves the details of a single event, based on id
    public GetEventResponseDto getEventById(String eventId) throws ExecutionException, InterruptedException {
        CompletableFuture<GetEventResponseDto> future = new CompletableFuture<>();

        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = events.document(eventId).get();

        documentSnapshotApiFuture.addListener(() -> {
            try {
                DocumentSnapshot documentSnapshot = documentSnapshotApiFuture.get();
                Event event = documentSnapshot.toObject(Event.class);
                if(event != null) {
                    //The retrieved details are returned to the user, along with the id of the event
                    future.complete(new GetEventResponseDto(
                        documentSnapshot.getId(),
                        event.getEventType().ordinal(),
                        event.getLocation(),
                        event.getTimestamp(),
                        event.getReports(),
                        event.getNumberOfReports(),
                        event.getEventStatus().ordinal()
                    ));
                }
                else {
                    future.complete(null);
                }
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Updates the status of an event based on a given new status
    public void updateEventStatus(String eventId, EventStatus newEventStatus) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future  = new CompletableFuture<>();
        DocumentReference eventToUpdate = events.document(eventId);

        ApiFuture<WriteResult> writeResultApiFuture = eventToUpdate.update(
                "eventStatus", newEventStatus //Sets the new status
        );

        writeResultApiFuture.addListener(() -> {
            try {
                writeResultApiFuture.get();
                future.complete(null);
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());


        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Checks for any possible alerts that have to be sent to a user
    public List<SendAlertDto> getAlerts(double lat, double lon, String userId) throws ExecutionException, InterruptedException {
        CompletableFuture<List<SendAlertDto>> future = new CompletableFuture<>();

        long currentMillis = System.currentTimeMillis();
        List<Query> queries = new ArrayList<>();
        //Four queries are made to firebase, one for each type of event
        for (EventType eventType : EventType.values()) {
            long timeRange = EventType.getAlertTimeRange(eventType);
            long geoRange = EventType.getAlertGeoRange(eventType);

            //For a user to be notified about an event the following must be true:
            Query query = events
                    .whereEqualTo("eventType", eventType)
                    .whereEqualTo("eventStatus", EventStatus.CONFIRMED) //1. The event must be CONFIRMED
                    //2. The event must have been first been reported within a certain timeframe from the moment the check happens
                    //If too much time has passed, the event is considered irrelevant to the user and an alert is not sent
                    .whereGreaterThanOrEqualTo("timestamp", currentMillis - timeRange)
                    //3.The user must be within a certain geographical range at the moment the check happens
                    .whereGreaterThanOrEqualTo("location.lat", lat - geoRange)
                    .whereLessThanOrEqualTo("location.lat", lat + geoRange)
                    .whereGreaterThanOrEqualTo("location.lon", lon - geoRange)
                    .whereLessThanOrEqualTo("location.lon", lon + geoRange)
                    .select("eventType", "location"); //Only event type and event location are selected

            queries.add(query);
        }

        AtomicInteger completedQueries = new AtomicInteger(0);
        int totalQueries = queries.size();

        //The queries are executed concurrently and their results are combined into a single list.
        List<QueryDocumentSnapshot> combinedResults = new ArrayList<>();
        for(Query query: queries) {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = query.get();
            querySnapshotApiFuture.addListener(() -> {
                try {
                    QuerySnapshot querySnapshot = querySnapshotApiFuture.get();
                    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
                    if (!documents.isEmpty()) {
                        synchronized (combinedResults) {
                            combinedResults.addAll(documents);
                        }
                    }

                    //The program continues once all queries are done
                    if (completedQueries.incrementAndGet() == totalQueries) {
                        //After all queries are completed, the possible alerts are created
                        future.complete(createAlerts(combinedResults, userId));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.info("--!" + e.getMessage());
                    future.completeExceptionally(e);
                }
            }, Executors.newSingleThreadExecutor());
        }

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Creates alerts for the user
    private List<SendAlertDto> createAlerts(List<QueryDocumentSnapshot> combinedResults, String userId) throws ExecutionException, InterruptedException{
        List<SendAlertDto> newAlerts = new ArrayList<>();
        CompletableFuture<List<SendAlertDto>> future = new CompletableFuture<>();

        //Having gotten a list of CONFIRMED events that are relevant to the user, a query is made to the alerts table
        //to check if the user has been already notified for any of the retrieved events
        Query query = alerts
                .whereEqualTo("userId", userId)
                .select("eventId");

        ApiFuture<QuerySnapshot> querySnapshotApiFuture = query.get();
        querySnapshotApiFuture.addListener(() -> {
            try {
                QuerySnapshot querySnapshot = querySnapshotApiFuture.get();
                List<QueryDocumentSnapshot> alertDocuments = querySnapshot.getDocuments();
                List<String> eventIds = new ArrayList<>();
                if (!alertDocuments.isEmpty()) {
                    for(QueryDocumentSnapshot doc: alertDocuments){
                        eventIds.add(doc.get("eventId", String.class));
                    }
                }

                //The events for which the user has already been notified, are removed from the retrieved events.
                List<QueryDocumentSnapshot> filteredDocs = combinedResults.stream().filter(r -> !eventIds.contains(r.getId())).toList();
                List<String> filteredEventIds = new ArrayList<>();
                //Alerts for the remaining events are created
                for(QueryDocumentSnapshot doc: filteredDocs) {
                    Event event = doc.toObject(Event.class);
                    newAlerts.add(new SendAlertDto(
                            event.getEventType().ordinal(),
                            event.getLocation()
                    ));
                    filteredEventIds.add(doc.getId());
                }

                try {
                    //The new alerts are logged in firebase, so the user is not notified again for the same events.
                    insertNewAlerts(filteredEventIds, userId);
                } catch (ExecutionException | InterruptedException e) {
                    future.completeExceptionally(e);
                }

                future.complete(newAlerts);
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }, Executors.newSingleThreadExecutor());

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e;
        }
    }

    //Inserts entries to firebase that indicate that a user has been notified once for a particular event
    private void insertNewAlerts(List<String> eventIds, String userId) throws ExecutionException, InterruptedException{
        WriteBatch batch = firestore.batch();
        for (String eventId : eventIds) {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("eventId", eventId);
            alertData.put("userId", userId);
            batch.set(firestore.collection("alerts").document(), alertData);
        }

        ApiFuture<List<WriteResult>> commitFuture = batch.commit();
        commitFuture.get();
    }

}
