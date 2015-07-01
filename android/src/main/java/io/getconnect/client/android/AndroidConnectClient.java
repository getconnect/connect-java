package io.getconnect.client.android;

import android.content.Context;

import java.io.IOException;

import io.getconnect.client.ConnectClient;
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
}
