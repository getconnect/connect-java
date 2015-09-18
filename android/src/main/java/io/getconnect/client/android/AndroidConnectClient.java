package io.getconnect.client.android;

import android.content.Context;

import java.io.IOException;

import io.getconnect.client.ConnectClient;
import io.getconnect.client.store.FileEventStore;

public class AndroidConnectClient extends ConnectClient {

    /**
     * Creates a new {@link ConnectClient} for Android.
     * @param context A {@link Context} that is used to access the cache directory for the persistent store.
     * @param projectId ID of the project to which to push events.
     * @param apiKey API key used to access the project (this must be a push or push/query key).
     */
    public AndroidConnectClient(Context context, String projectId, String apiKey) throws IOException {
        super(projectId, apiKey, null, new FileEventStore(projectId, context.getCacheDir()));
    }

}
