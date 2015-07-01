package io.getconnect.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Scanner;

public class UrlConnectionHttpClient implements HttpClient {
    private static final int TIMEOUT = 30000;

    @Override
    public HttpResponse send(HttpRequest request) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) request.getUrl().openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        connection.setRequestMethod(request.getMethod());
        connection.setRequestProperty("Accept", "application/json");
        for (String header : request.getHeaders().keySet()) {
            connection.setRequestProperty(header, request.getHeaders().get(header));
        }

        if (request.getPayloadWriter() != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            request.getPayloadWriter().write(connection.getOutputStream());
        } else {
            connection.connect();
        }

        InputStream responseStream;
        try {
            responseStream = connection.getInputStream();
        } catch (IOException ex) {
            responseStream = connection.getErrorStream();
        }

        String response = null;
        if (responseStream != null) {
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            response = s.hasNext() ? s.next() : "";

            try {
                responseStream.close();
            } catch (IOException ex) {
                // Ignore
            }
        }

        return new HttpResponse(connection.getResponseCode(), response);
    }
}
