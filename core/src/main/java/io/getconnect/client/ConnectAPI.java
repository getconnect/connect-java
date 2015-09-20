package io.getconnect.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.getconnect.client.exceptions.ConnectException;
import io.getconnect.client.exceptions.InvalidEventException;
import io.getconnect.client.exceptions.ServerException;

/**
 * API Client for pushing events to Connect API.
 */

public class ConnectAPI {
    protected static final String API_BASE_URL = "https://api.getconnect.io";
    protected static final Gson gson = GsonUTCDateAdapter.createSerializer();
    protected static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    protected final String projectId;
    protected final String apiKey;
    protected final String eventsUrl;
    protected final OkHttpClient client = new OkHttpClient();

    /**
     * Creates a new Connect API client.
     * @param projectId     ID of the project to which to push events.
     * @param apiKey        API key used to access the project (this must be a push or push/query key).
     */
    public ConnectAPI(String projectId, String apiKey) {
        this(projectId, apiKey, null);
    }

    /**
     * Creates a new Connect API client.
     * @param projectId     ID of the project to which to push events.
     * @param apiKey        API key used to access the project (this must be a push or push/query key).
     * @param baseUrl       Base URL of the Connect API.
     */
    public ConnectAPI(String projectId, String apiKey, String baseUrl) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        String base = baseUrl == null ? API_BASE_URL : baseUrl;
        this.eventsUrl = base + "/events/";
    }

    /**
     * Pushes a single event to the Connect API synchronously.
     * @param collection    The name of the collection to push to.
     * @param event         The {@link Event} to send.
     * @throws ConnectException When an error occurs.
     *         Will be {@link InvalidEventException}, {@Link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public void pushEvent(String collection, Event event) throws ConnectException {
        String url = this.eventsUrl + collection;
        String eventJSON = event.getEventJSON();
        Request request = this.generatePostRequest(url, eventJSON);

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new ConnectException(e);
        }

        if (!response.isSuccessful()) {
            throw ConnectAPI.getExceptionForResponse(response);
        }
    }

    /**
     * Pushes a single event to the Connect API asynchronously.
     * @param collection    The name of the collection to push to.
     * @param event         The {@link Event} to send.
     * @param callback      A {@link ConnectCallback} that will be invoked with the results of the request
     */
    public void pushEvent(String collection, Event event, final ConnectCallback callback) {
        String url = this.eventsUrl + collection;
        String eventJSON = event.getEventJSON();
        Request request = this.generatePostRequest(url, eventJSON);

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Request request, IOException e) {
                if (callback != null) {
                    callback.onFailure(new ConnectException(e));
                }
            }
            @Override public void onResponse(Response response) {
                if (callback != null) {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        ConnectException exception = ConnectAPI.getExceptionForResponse(response);
                        callback.onFailure(exception);
                    }
                }
            }
        });
    }

    /**
     * Pushes a batch of events to the Connect API synchronously.
     * @param batch     The event batch as a Map keyed by collection name containing a collection of @{link Event}s.
     * @throws ConnectException When an error occurs.
     *         Will be {@link InvalidEventException}, {@Link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public Map<String, Iterable<EventPushResponse>> pushEventBatch(final Map<String, Iterable<Event>> batch) throws ConnectException {
        String batchJSON = Event.getJSONForEventBatch(batch);
        Request request = this.generatePostRequest(this.eventsUrl, batchJSON);

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new ConnectException(e);
        }

        if (!response.isSuccessful()) {
            throw ConnectAPI.getExceptionForBatchResponse(response);
        }

        Map<String, Iterable<EventPushResponse>> batchResponse = null;
        try {
            batchResponse = ConnectAPI.buildResponseForBatch(batch, response);
        } catch (IOException e) {
            throw new ConnectException(e);
        }

        return batchResponse;
    }

    /**
     * Pushes a batch of events to the Connect API asynchronously.
     * @param batch         The event batch as a Map keyed by collection name containing a collection of @{link Event}s.
     * @param callback      A {@link ConnectBatchCallback} that will be invoked with the results of the request
     */
    public void pushEventBatch(final Map<String, Iterable<Event>> batch, final ConnectBatchCallback callback) {
        String batchJSON = Event.getJSONForEventBatch(batch);

        Request request = this.generatePostRequest(this.eventsUrl, batchJSON);

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Request request, IOException e) {
                if (callback != null) {
                    callback.onFailure(new ConnectException(e));
                }
            }
            @Override public void onResponse(Response response) {
                if (callback != null) {
                    if (response.isSuccessful()) {
                        Map<String, Iterable<EventPushResponse>> batchResponse = null;
                        try {
                            batchResponse = ConnectAPI.buildResponseForBatch(batch, response);
                        } catch (IOException e) {
                            callback.onFailure(new ConnectException(e));
                        }
                        callback.onSuccess(batchResponse);
                    } else {
                        ConnectException exception = ConnectAPI.getExceptionForBatchResponse(response);
                        callback.onFailure(exception);
                    }
                }
            }
        });
    }

    protected Request generatePostRequest(String url, String json) {
        return new Request.Builder()
                .url(url)
                .addHeader("X-Project-Id", projectId)
                .addHeader("X-Api-Key", apiKey)
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(JSON, json))
                .build();
    }

    protected static ConnectException getExceptionForResponse(Response response) {
        if (response.code() == 200)
            return null;

        Map<String, Object> responseData = null;
        try {
            responseData = ConnectAPI.getDeserializedResponseBody(response);
        } catch (IOException e) {
            return new ConnectException(e);
        }

        switch (response.code()) {
            case 422:
                Map<String, String> errors = new HashMap<String, String>();
                for (Map<String, Object> fieldError : (Iterable<Map<String, Object>>)responseData.get("errors")) {
                    errors.put((String) fieldError.get("field"), (String) fieldError.get("description"));
                }
                return InvalidEventException.create(errors);
            default:
                return new ServerException((String)responseData.get("errorMessage"));
        }
    }

    protected static ConnectException getExceptionForBatchResponse(Response response) {
        if (response.isSuccessful())
            return null;
        return new ServerException(response.message());
    }

    protected static Map<String, Object> getDeserializedResponseBody(Response response) throws IOException {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(response.body().charStream(), type);
    }

    protected static Map<String, Iterable<EventPushResponse>> buildResponseForBatch(final Map<String, Iterable<Event>> batch, final Response response) throws IOException {
        Map<String, Object> responseData = ConnectAPI.getDeserializedResponseBody(response);

        Map<String, Iterable<EventPushResponse>> result = new HashMap<String, Iterable<EventPushResponse>>();

        for (String collection : batch.keySet()) {
            Iterable<Event> pushedEvents = batch.get(collection);
            ArrayList<Map<String, Object>> individualResponses = (ArrayList<Map<String, Object>>) responseData.get(collection);
            ArrayList<EventPushResponse>  individualResults = new ArrayList<EventPushResponse>();

            int i=0;
            for (Event event : pushedEvents) {
                Map<String, Object> individualResponse = individualResponses.get(i++);
                individualResults.add(new EventPushResponse(individualResponse, event));
            }

            result.put(collection, individualResults);
        }

        return result;
    }
}
