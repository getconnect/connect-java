package io.getconnect.client;

import java.util.Map;

/**
 * Response to a single event being pushed in a batch.
 */
public class EventPushResponse {
    private Boolean success;
    private String message;
    private Event event;

    /**
     * Create a response to an event push.
     */
    public EventPushResponse(Map<String, Object> properties, Event event) {
        success = properties.containsKey("success") ? (Boolean)properties.get("success") : false;
        message = properties.containsKey("message") ? (String)properties.get("message") : null;
        this.event = event;
    }

    /**
     * Whether or not the event push was successful.
     * @return Whether or not the event push was successful.
     */
    public Boolean isSuccessful() {
        return success;
    }

    /**
     * The error message, if applicable, for the event push.
     * @return The error message, if applicable, for the event push.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the event that was pushed.
     * @return The {@link Event} that was pushed.
     */
    public Event getEvent() {
        return event;
    }

}
