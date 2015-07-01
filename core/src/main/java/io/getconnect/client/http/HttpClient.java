package io.getconnect.client.http;

import java.io.IOException;

/**
 * An interface to abstract the sending of HTTP requests.
 */
public interface HttpClient {
    /**
     * Send an HTTP request and receive a response.
     * @param request A {@link HttpRequest} containing the request to send.
     * @return A {@link HttpResponse} containing the response to the request.
     * @throws IOException If there is an error sending the request or reading the response.
     */
    HttpResponse send(HttpRequest request) throws IOException;
}