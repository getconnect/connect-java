package io.getconnect.client;

import java.util.*;

/**
 * An object that contains the properties for events that are pushed to Connect.
 */
public class Event {
    protected final String reservedPrefix = "tp_";
    protected final Map<String, Object> eventData;
    protected String eventStoreId;

    /**
     * Create a new event to push to Connect.
     * You can provide both reserved properties "id" and "timestamp" as a {@link java.lang.String} and {@link java.util.Date} respectively.
     * If you do not provide an "id", one will be generated for you.  If you do not provide a "timestamp", the current date is used.
     * @param eventData A {@link java.util.Map} containing the event properties to push to Connect.
     */
    public Event(Map<String, Object> eventData) {
        this(eventData, UUID.randomUUID().toString());
    }

    private Event(Map<String, Object> eventData, String eventStoreId) {
        this.eventData = eventData;
        this.eventStoreId = eventStoreId;
        setDefaultProperties();
        validateProperties();
    }

    public static Event fromEventStore(Map<String, Object> eventData, String eventStoreId) {
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
        HashMap<String, String> invalidProperties = new HashMap<String, String>();

        for (String key : eventData.keySet()) {
            if (key.startsWith(reservedPrefix))
                invalidProperties.put(key, "Property names cannot start with the reserved prefix '" + reservedPrefix + "'");
            if (key.contains("."))
                invalidProperties.put(key, "Property names cannot contain a period (.)");
        }

        if (!invalidProperties.isEmpty()) {
            throw InvalidEventException.create(invalidProperties);
        }
    }
}
