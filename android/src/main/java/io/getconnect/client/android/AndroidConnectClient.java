package io.getconnect.client.android;

import android.content.Context;

import java.io.IOException;
import java.util.Map;

import io.getconnect.client.ConnectClient;
import io.getconnect.client.FilteredKey;
import io.getconnect.client.FilteredKeyException;
import io.getconnect.client.JsonSerializer;
import io.getconnect.client.http.UrlConnectionHttpClient;
import io.getconnect.client.store.FileEventStore;

public class AndroidConnectClient extends ConnectClient {
    public AndroidConnectClient(Context context, String projectId, String apiKey) throws IOException {
        this(context, projectId, apiKey, new AndroidJsonSerializer());
    }

    private AndroidConnectClient(Context context, String projectId, String apiKey, JsonSerializer serializer) throws IOException {
        super(projectId, apiKey, null, new UrlConnectionHttpClient(), serializer, new FileEventStore(projectId, context.getCacheDir(), serializer));
    }

    /**
     * Encrypt filtered key for use with the Connect API.
     * @param key the definition of the filtered key.
     * @param masterKey the master key for the Connect project
     * @return The encrypted filtered key.
     */
    public static String generateFilteredKey(final Map<String, Object> key, String masterKey) throws FilteredKeyException {
        return generateFilteredKey(new AndroidJsonSerializer(), key, masterKey);
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
