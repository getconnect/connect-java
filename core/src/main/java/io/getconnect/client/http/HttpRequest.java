package io.getconnect.client.http;

import java.net.URL;
import java.util.Map;

/**
 * An HTTP request to be sent by the {@link HttpClient}.
 */
public class HttpRequest {
    private final URL url;
    private final String method;
    private final Map<String, String> headers;
    private final StreamWriter payloadWriter;

    /**
     * Create a new HTTP request.
     * @param url The {@link URL} to which to send the request.
     * @param method The HTTP method for the request (e.g. GET, POST, PUT).
     * @param headers A {@link java.util.Map} containing the headers to add to the request.
     * @param payloadWriter The writer for the payload for which to send, if applicable.
     */
    public HttpRequest(URL url, String method, Map<String, String> headers, StreamWriter payloadWriter) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.payloadWriter = payloadWriter;
    }

    /**
     * Get the URL of the request.
     * @return A {@link URL} for the request.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Get the HTTP method for the request.
     * @return The HTTP method for the request.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get the headers for the HTTP request.
     * @return A {@link java.util.Map} containing the headers to add to the request.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get the writer for the payload of the HTTP request.
     * @return The writer for the payload of the request.
     */
    public StreamWriter getPayloadWriter() {
        return payloadWriter;
    }
}