package io.getconnect.client.store;

import io.getconnect.client.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemoryEventStore implements EventStore {
    private final HashMap<String, ArrayList<Event>> events = new HashMap<String, ArrayList<Event>>();

    private ArrayList<Event> getEvents(String collection) {
        ArrayList<Event> collectionEvents = events.get(collection);
        if (collectionEvents == null) {
            collectionEvents = new ArrayList<Event>();
            events.put(collection, collectionEvents);
        }
        return collectionEvents;
    }

    @Override
    public void add(String collection, Event event) throws IOException {
        ArrayList<Event> collectionEvents = getEvents(collection);
        collectionEvents.add(event);
    }

    @Override
    public Event[] read(String collection) throws IOException {
        ArrayList<Event> collectionEvents = getEvents(collection);
        return collectionEvents.toArray(new Event[collectionEvents.size()]);
    }

    @Override
    public Map<String, Event[]> readAll() throws IOException {
        HashMap<String, Event[]> allEvents = new HashMap<String, Event[]>();

        for (String collection : events.keySet()) {
            ArrayList<Event> collectionEvents = events.get(collection);
            allEvents.put(collection, collectionEvents.toArray(new Event[collectionEvents.size()]));
        }

        return allEvents;
    }

    @Override
    public void acknowledge(String collection, Event event) throws IOException {
        ArrayList<Event> collectionEvents = getEvents(collection);
        collectionEvents.remove(event);
    }
}
