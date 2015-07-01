package io.getconnect.client.java;

import io.getconnect.client.ConnectClient;
import io.getconnect.client.JsonSerializer;
import io.getconnect.client.http.UrlConnectionHttpClient;
import io.getconnect.client.store.FileEventStore;
import io.getconnect.client.store.MemoryEventStore;

import java.io.File;
import java.io.IOException;

public class JavaConnectClient extends ConnectClient {
    public JavaConnectClient(String projectId, String apiKey) {
        super(projectId, apiKey, null, new UrlConnectionHttpClient(), new JacksonJsonSerializer(), new MemoryEventStore());
    }

    public JavaConnectClient(String projectId, String apiKey, String eventStoreDir) throws IOException {
        this(projectId, apiKey, eventStoreDir, new JacksonJsonSerializer());
    }

    private JavaConnectClient(String projectId, String apiKey, String eventStoreDir, JsonSerializer serializer) throws IOException {
        super(projectId, apiKey, null, new UrlConnectionHttpClient(), serializer, new FileEventStore(projectId, new File(eventStoreDir), serializer));
    }
}
