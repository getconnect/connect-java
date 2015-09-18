package io.getconnect.client;

import java.util.Map;

import io.getconnect.client.exceptions.ConnectException;
import io.getconnect.client.exceptions.InvalidEventException;

public interface ConnectBatchCallback {
    /**
     * Called when a request to Connect has been successful.
     * @param details A Map of key/value pairs of collection name and a list of {@link EventPushResponse}s respectively.
     */
    public void onSuccess(Map<String, Iterable<EventPushResponse>> details);

    /**
     * Called when a request to Connect has failed.
     * @param e A ConnectException outlining the error that occurred.
     *         Will be {@link InvalidEventException}, {@Link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public void onFailure(ConnectException e);
}
