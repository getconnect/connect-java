package io.getconnect.client;

import java.util.Map;

/**
 * Response to a single event being pushed in a batch.
 */
public class EventPushResponse {
    private Boolean success;
    private Boolean duplicate;
    private String message;
    private Map<String, Object> event;

    /**
     * Create a response to an event push.
     */
    public EventPushResponse(Map<String, Object> properties, Map<String, Object> event) {
        success = properties.containsKey("success") ? (Boolean)properties.get("success") : false;
        duplicate = properties.containsKey("duplicate") ? (Boolean)properties.get("duplicate") : false;
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
     * Whether or not the event push was a duplicate.
     * @return Whether or not the event push was a duplicate.
     */
    public Boolean isDuplicate() {
        return duplicate;
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
     * @return The event that was pushed.
     */
    public Map<String, Object> getEvent() {
        return event;
    }
}
