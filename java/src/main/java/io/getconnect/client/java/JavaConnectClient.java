package io.getconnect.client.java;

import io.getconnect.client.ConnectClient;
import io.getconnect.client.FilteredKey;
import io.getconnect.client.FilteredKeyException;
import io.getconnect.client.JsonSerializer;
import io.getconnect.client.http.UrlConnectionHttpClient;
import io.getconnect.client.store.FileEventStore;
import io.getconnect.client.store.MemoryEventStore;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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

    /**
     * Encrypt filtered key for use with the Connect API.
     * @param key the definition of the filtered key.
     * @param masterKey the master key for the Connect project
     * @return The encrypted filtered key.
     */
    public static String generateFilteredKey(final Map<String, Object> key, String masterKey) throws FilteredKeyException {
        return generateFilteredKey(new JacksonJsonSerializer(), key, masterKey);
    }

    /**
     * Encrypt filtered key for use with the Connect API.
     * @param serializer the serializer used to serialize the filtered key.
     * @param key the definition of the filtered key.
     * @param masterKey the master key for the Connect project
     * @return The encrypted filtered key.
     */
    public static String generateFilteredKey(final JsonSerializer serializer, final Map<String, Object> key, String masterKey) throws FilteredKeyException {
        return FilteredKey.encrypt(serializer, key, masterKey);
    }
}
