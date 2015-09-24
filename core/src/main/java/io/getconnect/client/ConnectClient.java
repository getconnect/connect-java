package io.getconnect.client;

import io.getconnect.client.exceptions.ConnectException;
import io.getconnect.client.exceptions.ServerException;
import io.getconnect.client.exceptions.InvalidEventException;
import io.getconnect.client.store.EventStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for pushing events to Connect.
 */
public class ConnectClient {
    private final String projectId;
    private final String apiKey;
    private final EventStore eventStore;
    private final ConnectAPI connectAPI;

    /**
     * Creates a new Connect client.
     * @param projectId ID of the project to which to push events.
     * @param apiKey API key used to access the project (this must be a push or push/query key).
     * @param baseUrl Base URL of the Connect API.
     * @param eventStore EventStore used for persistence.
     */
    public ConnectClient(String projectId, String apiKey, String baseUrl, EventStore eventStore) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        this.eventStore = eventStore;
        this.connectAPI = new ConnectAPI(projectId, apiKey, baseUrl);
    }

    /**
     * Synchronously push an event to a collection in Connect.
     * @param collection Name of the collection to which to push the event.
     * @param event Event to push to the collection.
     * @throws InvalidEventException If the server instructs that the event or its properties are invalid.
     * @throws ConnectException When an error occurs.
     *         Will be {@link InvalidEventException}, {@link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public void push(final String collection, final Map<String, Object> event) throws ConnectException {
        this.connectAPI.pushEvent(collection, new Event(event));
    }

    /**
     * Asynchronously Push an event to a collection in Connect.
     * @param collection    Name of the collection to which to push the event.
     * @param event         {@link Event} to push to the collection.
     * @param callback      A {@link ConnectCallback} that will be invoked with the result of the request.
     */
    public void pushAsync(final String collection, final Map<String, Object> event, final ConnectCallback callback) {
        Event mappedEvent = null;
        try {
            mappedEvent = new Event(event);
        } catch (ConnectException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
            return;
        }

        this.connectAPI.pushEvent(collection, mappedEvent, callback);
    }

    /**
     * Synchronously push a batch of events to Connect.
     * @param batch A {@link Map} of collection name to events for which to push to Connect.
     * @return A {@link Map} of collection name to an array of responses to individual event pushes in the same order as the request.
     * @throws ConnectException When an error occurs.
     *         Will be {@link InvalidEventException}, {@Link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public Map<String, Iterable<EventPushResponse>> pushBatch(final Map<String, Iterable<Map<String, Object>>> batch) throws ConnectException {
        Map<String, Iterable<Event>> eventBatch = Event.buildEventBatch(batch);
        return this.connectAPI.pushEventBatch(eventBatch);
    }

    /**
     * Asynchronously push a batch of events to Connect.
     * @param batch A Map with collection name as a key and event Maps as values.
     * @param callback  A {@link ConnectBatchCallback} that will be invoked with the result of the request.
     */
    public void pushBatchAsync(final Map<String, Iterable<Map<String, Object>>> batch, final ConnectBatchCallback callback) {
        Map<String, Iterable<Event>> eventBatch = null;
        try {
            eventBatch = Event.buildEventBatch(batch);
        } catch (ConnectException e) {
            if (callback != null) {
                callback.onFailure(e);
            }
            return;
        }

        this.connectAPI.pushEventBatch(eventBatch, callback);
    }

    /**
     * Add an event to the event store to be delivered later.
     * @param collection Name of the collection to which to push the event.
     * @param event Event to add to the collection.
     * @throws ConnectException When an error occurs.
     *         Will be {@link InvalidEventException} or a generic {@link ConnectException} with an inner exception.
     */
    public synchronized void add(String collection, final Map<String, Object> event) throws ConnectException {
        try {
            eventStore.add(collection, new Event(event));
        } catch (IOException e) {
            throw new ConnectException(e);
        }
    }

    /**
     * Push the pending events stored to Connect synchronously.
     * @return A {@link Map} of collection name to an array of responses to individual events pushed from the queue.
     * @throws ConnectException When an error occurs.
     *         Will be {@link InvalidEventException}, {@Link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public synchronized Map<String, Iterable<EventPushResponse>> pushPending() throws ConnectException {

        Map<String, Iterable<Event>> eventBatch = null;
        try {
            eventBatch = this.eventStore.readAll();
        } catch (IOException e) {
            throw new ConnectException(e);
        }

        if (eventBatch.size() < 1) {
            return new HashMap<String, Iterable<EventPushResponse>>();
        }

        Map<String, Iterable<EventPushResponse>> details = this.connectAPI.pushEventBatch(eventBatch);
        this.updateStoreWithResponse(details);
        return details;
    }

    /**
     * Push the pending events stored to Connect asynchronously.
     * @param callback A {@link ConnectBatchCallback} that will be invoked with the result of the request.
     */
    public synchronized void pushPendingAsync(final ConnectBatchCallback callback) {

        Map<String, Iterable<Event>> eventBatch = null;
        try {
            eventBatch = this.eventStore.readAll();
        } catch (IOException e) {
            if (callback != null) {
                callback.onFailure(new ConnectException(e));
            }
            return;
        }

        if (eventBatch.size() < 1) {
            if (callback != null) {
                callback.onSuccess(new HashMap<String, Iterable<EventPushResponse>>());
            }
            return;
        }

        this.connectAPI.pushEventBatch(eventBatch, new ConnectBatchCallback() {
            @Override
            public void onSuccess(Map<String, Iterable<EventPushResponse>> details) {
                ConnectClient.this.updateStoreWithResponse(details);
                if (callback != null) {
                    callback.onSuccess(details);
                }
            }

            @Override
            public void onFailure(ConnectException e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    protected synchronized void updateStoreWithResponse(Map<String, Iterable<EventPushResponse>> details) {
        for (String collection : details.keySet()) {
            for (EventPushResponse eventResponse : details.get(collection)) {
                if (eventResponse.isSuccessful()) {
                    try {
                        eventStore.acknowledge(collection, eventResponse.getEvent());
                    } catch (IOException e) {
                        // ignore, it will try again on the next pass.
                    }
                }
            }
        }
    }
}