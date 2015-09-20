package io.getconnect.client.java;

import io.getconnect.client.ConnectClient;
import io.getconnect.client.store.FileEventStore;
import io.getconnect.client.store.MemoryEventStore;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JavaConnectClient extends ConnectClient {

    /**
     * Creates a new {@link ConnectClient} for Java with an in memory event queue.
     * @param projectId ID of the project to which to push events.
     * @param apiKey API key used to access the project (this must be a push or push/query key).
     */
    public JavaConnectClient(String projectId, String apiKey) {
        super(projectId, apiKey, null, new MemoryEventStore());
    }

    /**
     * Creates a new {@link ConnectClient} for Java with an persistent file event store.
     * @param projectId ID of the project to which to push events.
     * @param apiKey API key used to access the project (this must be a push or push/query key).
     * @param eventStoreDir A directory that will be used to store events pending push.
     */
    public JavaConnectClient(String projectId, String apiKey, String eventStoreDir) throws IOException {
        super(projectId, apiKey, null, new FileEventStore(projectId, new File(eventStoreDir)));
    }

    /**
     * Encrypt filtered key for use with the Connect API.
     * @param key the definition of the filtered key.
     * @param masterKey the master key for the Connect project
     * @return The encrypted filtered key.
     * @throws FilteredKeyException
     */
    public static String generateFilteredKey(final Map<String, Object> key, String masterKey) throws FilteredKeyException {
        return FilteredKey.encrypt(key, masterKey);
    }

}
