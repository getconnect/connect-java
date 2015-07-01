package io.getconnect.client;

import java.util.Map;

/**
 * Represents a response to pushing a batch of events.
 */
public class EventBatchPushResponse {
    private final int statusCode;
    private final String errorMessage;
    private final Map<String, Iterable<EventPushResponse>> responses;

    /**
     * Create a response to pushing a batch of events.
     * @param statusCode Status code of the response.
     * @param errorMessage Error message of the response, if applicable.
     * @param responses
     */
    public EventBatchPushResponse(int statusCode, String errorMessage, Map<String, Iterable<EventPushResponse>> responses) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.responses = responses;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, Iterable<EventPushResponse>> getResponses() {
        return responses;
    }
}
