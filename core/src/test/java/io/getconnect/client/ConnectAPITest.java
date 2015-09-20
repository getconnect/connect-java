package io.getconnect.client;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.HashMap;
import java.util.Map;

import io.getconnect.client.exceptions.ConnectException;
import io.getconnect.client.exceptions.InvalidEventException;

/**
 * Created by chadedrupt on 18/09/15.
 */
public class ConnectAPITest {

    @Test
    public void testThatTheProjectIDHeaderIsSet() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse());
        String projectId = "this-is-a-project-id";
        ConnectAPI connectAPI = new ConnectAPI(projectId, "", server.url("").toString());

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("test", "test");

        connectAPI.pushEvent("test", new Event(event));

        RecordedRequest request = null;
        try {
            request = server.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(projectId, request.getHeader("X-Project-Id"));
    }

    @Test
    public void testThatTheAPIKeyHeaderIsSet() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse());
        String apiKey = "this-is-an-api-key";
        ConnectAPI connectAPI = new ConnectAPI("", apiKey, server.url("").toString());

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("test", "test");

        connectAPI.pushEvent("test", new Event(event));

        RecordedRequest request = null;
        try {
            request = server.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(apiKey, request.getHeader("X-Api-Key"));
    }

    @Test
    public void testThatTheContentTypeHeaderIsSet() {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse());
        ConnectAPI connectAPI = new ConnectAPI("", "", server.url("").toString());

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("test", "test");

        connectAPI.pushEvent("test", new Event(event));

        RecordedRequest request = null;
        try {
            request = server.takeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"));
    }

    @Test
    public void testThatTheExceptionContainsMessageFromAPI() {
        MockWebServer server = new MockWebServer();
        String errorMessage = "Something bad happened.";
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"errorMessage\": \"" + errorMessage + "\"}"));
        ConnectAPI connectAPI = new ConnectAPI("this-is-a-project-id", "this-is-an-api-key", server.url("").toString());

        Map<String, Object> event = new HashMap<String, Object>();
        event.put("test", "test");

        try {
            connectAPI.pushEvent("test", new Event(event));
        } catch (ConnectException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void testThatTheExceptionContainsFieldDetailsFromAPI() {
        MockWebServer server = new MockWebServer();
        String fieldName = "fieldName";
        String errorMessage = "There was an error with this field.";
        String body = "{ \"errors\": [ { \"field\": \"" + fieldName +"\", \"description\": \"" + errorMessage + "\" } ] }";
        server.enqueue(new MockResponse().setResponseCode(422).setBody(body));
        ConnectAPI connectAPI = new ConnectAPI("this-is-a-project-id", "this-is-an-api-key", server.url("").toString());

        Map<String, Object> event = new HashMap<String, Object>();
        event.put(fieldName, "test");

        try {
            connectAPI.pushEvent("test", new Event(event));
        } catch (InvalidEventException e) {
            assertThat(e.getMessage(), containsString(fieldName));
            assertThat(e.getMessage(), containsString(errorMessage));
        }
    }

}
