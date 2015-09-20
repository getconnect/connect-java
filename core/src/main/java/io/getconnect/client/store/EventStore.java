package io.getconnect.client.store;

import io.getconnect.client.Event;
import io.getconnect.client.exceptions.InvalidEventException;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for an object that can store events.
 */
public interface EventStore {
    /**
     * Add a single event to the store.
     * @param collection Collection for which to add the event.
     * @param event Event to add.
     * @throws IOException If there is a problem adding the event to the store.
     */
    void add(String collection, Event event) throws IOException;

    /**
     * Read all events for a specific collection.
     * @param collection Collection for which to retrieve the events.
     * @return An {@link Iterable} of events under that collection.
     * @throws IOException If there is a problem reading the events from the store.
     */
    Iterable<Event> read(String collection) throws IOException;

    /**
     * Read all events in the store.
     * @return A {@link Map} of collection names to a list of events in the store.
     * @throws IOException If there is a problem reading the events from the store.
     */
    Map<String, Iterable<Event>> readAll() throws IOException;

    /**
     * Acknowledge a single event has been sent.
     * @param collection The collection in which the event belongs.
     * @param event The event to acknowledge.
     * @throws IOException If there is a problem acknowledging the event.
     */
    void acknowledge(String collection, Event event) throws IOException;
}
