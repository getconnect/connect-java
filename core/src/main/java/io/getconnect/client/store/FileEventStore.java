package io.getconnect.client.store;

import io.getconnect.client.Event;
import io.getconnect.client.JsonSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileEventStore implements EventStore {
    private final File root;
    private final JsonSerializer serializer;

    public FileEventStore(String projectId, File root, JsonSerializer serializer) throws IOException {
        if (!root.exists() || !root.isDirectory()) {
            throw new IOException("The root directory '" + root + "' does not exist or is not a directory.");
        }

        this.root = new File(root, projectId);
        this.serializer = serializer;
    }

    protected File getCollectionDir(String collection) throws IOException {
        File collectionDir = new File(root, collection);
        if (collectionDir.exists())
            return collectionDir;

        if (!collectionDir.mkdirs())
            throw new IOException("Could not create collection directory '" + collectionDir + "'");

        return collectionDir;
    }

    @Override
    public void add(String collection, Event event) throws IOException {
        File collectionDir = getCollectionDir(collection);

        File eventFile = new File(collectionDir, event.getEventStoreId() + ".json");

        Writer writer = null;
        try {
            OutputStream stream = new FileOutputStream(eventFile);
            writer = new OutputStreamWriter(stream, "UTF-8");
            serializer.serialize(writer, event.getEventData());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public Event[] read(String collection) throws IOException {
        File collectionDir = getCollectionDir(collection);

        File[] files = collectionDir.listFiles();
        ArrayList<Event> events = new ArrayList<Event>();

        for (File file : files) {
            Reader reader = null;
            try {
                InputStream stream = new FileInputStream(file);
                reader = new InputStreamReader(stream, "UTF-8");
                Map<String, Object> eventData = serializer.deserialize(reader);
                events.add(Event.fromEventStore(eventData, file.getName().replace(".json", "")));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        // Ignore
                    }
                }
            }
        }

        return events.toArray(new Event[events.size()]);
    }

    @Override
    public Map<String, Event[]> readAll() throws IOException {
        HashMap<String, Event[]> events = new HashMap<String, Event[]>();

        File[] collections = root.listFiles();

        for (File collection : collections) {
            String collectionName = collection.getName();
            events.put(collectionName, this.read(collectionName));
        }

        return events;
    }

    @Override
    public void acknowledge(String collection, Event event) throws IOException {
        File collectionDir = getCollectionDir(collection);

        new File(collectionDir, event.getEventStoreId() + ".json").delete();
    }
}
