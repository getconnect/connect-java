package io.getconnect.client.http;

/**
 * Represents a response to a {@link HttpRequest} sent by the {@link HttpClient}.
 */
public class HttpResponse {
    private final int statusCode;
    private final String response;

    /**
     * Create an HTTP Response.
     * @param statusCode The HTTP status code in the response.
     * @param response The payload of the response.
     */
    public HttpResponse(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    /**
     * Get the status code of the response.
     * @return The status code of the response.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the response payload.
     * @return The payload of the response.
     */
    public String getResponse() {
        return response;
    }
}
