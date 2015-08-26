package io.getconnect.client;

import io.getconnect.client.http.HttpClient;
import io.getconnect.client.http.HttpRequest;
import io.getconnect.client.http.HttpResponse;
import io.getconnect.client.http.StreamWriter;
import io.getconnect.client.store.EventStore;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for pushing events to Connect.
 */
public class ConnectClient {
    private final String projectId;
    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final JsonSerializer serializer;
    private final EventStore eventStore;

    /**
     * Creates a new Connect client.
     * @param projectId ID of the project to which to push events.
     * @param apiKey API key used to access the project (this must be a push or push/query key).
     * @param baseUrl Base URL of the Connect API.
     * @param httpClient Client to use to perform HTTP requests.
     * @param serializer Serializer for serializing to/from JSON.
     */
    public ConnectClient(String projectId, String apiKey, String baseUrl, HttpClient httpClient, JsonSerializer serializer, EventStore eventStore) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl == null ? ConnectConstants.API_BASE_URL : baseUrl;
        this.httpClient = httpClient;
        this.serializer = serializer;
        this.eventStore = eventStore;
    }

    /**
     * Push an event to a collection in Connect.
     * @param collection Name of the collection to which to push the event.
     * @param event Event to push to the collection.
     * @throws InvalidCollectionException If the collection name is not valid.
     * @throws DuplicateEventException If the event is a duplicate (i.e. the "id" is the same).
     * @throws IOException If there is an error while sending or receiving the event or response, respectively.
     * @throws InvalidEventException If the server instructs that the event or its properties are invalid.
     * @throws DuplicateEventException If an event being pushed already exists.
     * @throws InvalidEventException If the properties of an event are invalid.
     * @throws ServerException If a server-side errors occurs.
     */
    public void push(String collection, final Map<String, Object> event) throws InvalidCollectionException, DuplicateEventException, IOException, InvalidEventException, ServerException {
        URL url;
        try {
            url = new URL(baseUrl + "/events/" + collection);
        } catch (MalformedURLException ex) {
            throw new InvalidCollectionException("The collection specified is not valid.", ex);
        }

        call(url, new Event(event).getEventData());
    }

    /**
     * Push multiple events to a collection in Connect.
     * @param events A {@link Map} of collection name to events for which to push to Connect.
     * @return A {@link Map} of collection name to an array of responses to individual event pushes in the same order as the request.
     * @throws IOException If there is an error while sending or receiving the event or response, respectively.
     * @throws DuplicateEventException If an event being pushed already exists.
     * @throws InvalidEventException If the properties of an event are invalid.
     * @throws ServerException If a server-side errors occurs.
     */
    public Map<String, EventPushResponse[]> push(final Map<String, Map<String, Object>[]> events) throws DuplicateEventException, IOException, InvalidEventException, ServerException {
        URL url = new URL(baseUrl + "/events");

        HashMap<String, Iterable<Map<String, Object>>> mappedEvents = new HashMap<String, Iterable<Map<String, Object>>>();

        for (String collection : events.keySet()) {
            ArrayList<Map<String, Object>> newEvents = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> event : events.get(collection)) {
                newEvents.add(new Event(event).getEventData());
            }
            mappedEvents.put(collection, newEvents);
        }

        HttpResponse response = call(url, mappedEvents);

        StringReader reader = new StringReader(response.getResponse());
        Map<String, Object> responseBody = serializer.deserialize(reader);
        reader.close();

        HashMap<String, EventPushResponse[]> pushResponse = new HashMap<String, EventPushResponse[]>();
        for (String collection : responseBody.keySet()) {
            Map<String, Object>[] eventResponses = (Map<String, Object>[]) responseBody.get(collection);
            ArrayList<EventPushResponse> mappedResponses = new ArrayList<EventPushResponse>();
            int i = 0;
            for (Map<String, Object> eventResponse : eventResponses) {
                Map<String, Object> event = events.get(collection)[i++];
                mappedResponses.add(new EventPushResponse(eventResponse, event));
            }
            pushResponse.put(collection, mappedResponses.toArray(new EventPushResponse[mappedResponses.size()]));
        }

        return pushResponse;
    }

    /**
     * Call the Connect API with the specified data.
     * @param url URL of the API to call.
     * @param data Data to push to the specified API.
     * @return The response from the API.
     * @throws IOException If there is an error while sending or receiving the request or response, respectively.
     * @throws DuplicateEventException If an event being pushed already exists.
     * @throws InvalidEventException If the properties of an event are invalid.
     * @throws ServerException If a server-side errors occurs.
     */
    protected HttpResponse call(URL url, final Map<String, ?> data) throws DuplicateEventException, IOException, InvalidEventException, ServerException {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Project-Id", projectId);
        headers.put("X-Api-Key", apiKey);

        StreamWriter writer = new StreamWriter() {
            @Override
            public void write(OutputStream stream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
                serializer.serialize(writer, data);
            }
        };

        HttpResponse response = httpClient.send(new HttpRequest(url, "POST", headers, writer));

        if (response.getStatusCode() == 200)
            return response;

        StringReader reader = new StringReader(response.getResponse());
        Map<String, Object> responseBody = serializer.deserialize(reader);
        reader.close();

        switch (response.getStatusCode()) {
            case 409:
                throw new DuplicateEventException((String)responseBody.get("errorMessage"));

            case 422:
                HashMap<String, String> errors = new HashMap<String, String>();
                for (Map<String, Object> fieldError : (Iterable<Map<String, Object>>)responseBody.get("errors")) {
                    errors.put((String)fieldError.get("field"), (String)fieldError.get("description"));
                }
                throw InvalidEventException.create(errors);

            default:
                throw new ServerException((String)responseBody.get("errorMessage"));
        }
    }

    /**
     * Add an event to the event store to be delivered later.
     * @param collection Name of the collection to which to push the event.
     * @param event Event to add to the collection.
     * @throws IOException If there is an error adding the event to the store.
     */
    public synchronized void add(String collection, final Map<String, Object> event) throws IOException {
        eventStore.add(collection, new Event(event));
    }

    /**
     * Push the pending events stored to Connect.
     * @return A {@link Map} of collection name to an array of responses to individual event pushes in the same order as the request.
     * @throws IOException If there is an error while pushing the pending events to Connect.
     */
    public synchronized Map<String, EventPushResponse[]> pushPending() throws IOException {
        URL url = new URL(baseUrl + "/events");

        Map<String, Event[]> events = eventStore.readAll();

        HashMap<String, Iterable<Map<String, Object>>> mappedEvents = new HashMap<String, Iterable<Map<String, Object>>>();

        for (String collection : events.keySet()) {
            ArrayList<Map<String, Object>> newEvents = new ArrayList<Map<String, Object>>();
            for (Event event : events.get(collection)) {
                newEvents.add(event.getEventData());
            }
            mappedEvents.put(collection, newEvents);
        }

        HttpResponse response = call(url, mappedEvents);

        StringReader reader = new StringReader(response.getResponse());
        Map<String, Object> responseBody = serializer.deserialize(reader);
        reader.close();

        HashMap<String, EventPushResponse[]> pushResponse = new HashMap<String, EventPushResponse[]>();
        for (String collection : responseBody.keySet()) {
            Iterable<Map<String, Object>> eventResponses = (Iterable<Map<String, Object>>) responseBody.get(collection);
            ArrayList<EventPushResponse> mappedResponses = new ArrayList<EventPushResponse>();
            int i = 0;
            for (Map<String, Object> eventResponse : eventResponses) {
                Event event = events.get(collection)[i++];
                EventPushResponse mappedResponse = new EventPushResponse(eventResponse, event.getEventData());
                if (mappedResponse.isSuccessful() || mappedResponse.isDuplicate()) {
                    eventStore.acknowledge(collection, event);
                }
                mappedResponses.add(mappedResponse);
            }
            pushResponse.put(collection, mappedResponses.toArray(new EventPushResponse[mappedResponses.size()]));
        }

        return pushResponse;
    }
}