package io.getconnect.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.getconnect.client.exceptions.InvalidEventException;

/**
 * An object that contains the properties for events that are pushed to Connect.
 */
public class Event {
    private static final Gson gson = GsonUTCDateAdapter.createSerializer();

    protected final String reservedPrefix = "tp_";
    protected final Map<String, Object> eventData;
    protected String eventStoreId;

    /**
     * Create a new event to push to Connect.
     * You can provide both reserved properties "id" and "timestamp" as a {@link java.lang.String} and {@link java.util.Date} respectively.
     * If you do not provide an "id", one will be generated for you.  If you do not provide a "timestamp", the current date is used.
     * @param eventData A {@link java.util.Map} containing the event properties to push to Connect.
     */
    public Event(Map<String, Object> eventData) throws InvalidEventException {
        this(eventData, UUID.randomUUID().toString());
    }

    private Event(Map<String, Object> eventData, String eventStoreId) throws InvalidEventException {
        this.eventData = eventData;
        this.eventStoreId = eventStoreId;
        validateProperties();
        setDefaultProperties();
    }

    public static Event fromEventStore(String eventJSON, String eventStoreId) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> eventData = gson.fromJson(eventJSON, type);
        return new Event(eventData, eventStoreId);
    }

    /**
     * Get the properties of the event.
     * @return A {@link java.util.Map} containing the event properties.
     */
    public Map<String, Object> getEventData() {
        return eventData;
    }

    /**
     * Get the properties of the event as a JSON string.
     * @return A string containing the event properties as JSON.
     */
    public String getEventJSON() {
        return Event.gson.toJson(this.eventData);
    }

    /**
     * Get the ID of the event.
     * @return A {@link java.lang.String} containing the event ID.
     */
    public String getId() {
        return eventData.get("id").toString();
    }

    /**
     * Gets the ID used for storing the event in the event store.
     * @return Event store ID for the event.
     */
    public String getEventStoreId() {
        return eventStoreId;
    }

    protected void setDefaultProperties() {
        if (!eventData.containsKey("id")) {
            eventData.put("id", UUID.randomUUID().toString());
        }
        if (!eventData.containsKey("timestamp")) {
            eventData.put("timestamp", new Date());
        }
    }

    protected void validateProperties() throws InvalidEventException {
        if (this.eventData.isEmpty()) {
            throw new InvalidEventException("The event cannot be sent. It contains no properties");
        }

        HashMap<String, String> invalidProperties = new HashMap<String, String>();

        for (String key : this.eventData.keySet()) {
            if (key.startsWith(this.reservedPrefix))
                invalidProperties.put(key, "Property names cannot start with the reserved prefix '" + this.reservedPrefix + "'");
            if (key.contains("."))
                invalidProperties.put(key, "Property names cannot contain a period (.)");
        }

        if (!invalidProperties.isEmpty()) {
            throw InvalidEventException.create(invalidProperties);
        }
    }

    public static Map<String, Iterable<Event>> buildEventBatch(Map<String, Iterable<Map<String, Object>>> events) throws InvalidEventException {
        Map<String, Iterable<Event>> mappedEvents = new HashMap<String, Iterable<Event>>();

        for (String collection : events.keySet()) {
            ArrayList<Event> newEvents = new ArrayList<Event>();
            for (Map<String, Object> event : events.get(collection)) {
                newEvents.add(new Event(event));
            }
            mappedEvents.put(collection, newEvents);
        }

        return mappedEvents;
    }

    public static String getJSONForEventBatch(Map<String, Iterable<Event>> eventBatch) {
        Map<String, Iterable<Map<String, Object>>> batchEventData = new HashMap<String, Iterable<Map<String, Object>>>();

        for (String collection : eventBatch.keySet()) {
            ArrayList<Map<String, Object>> collectionEventData = new ArrayList<Map<String, Object>>();
            for (Event event : eventBatch.get(collection)) {
                collectionEventData.add(event.getEventData());
            }
            batchEventData.put(collection, collectionEventData);
        }

        return gson.toJson(batchEventData);
    }
}
