package io.getconnect.client.store;

import org.apache.commons.codec.binary.Base32;

import io.getconnect.client.Event;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileEventStore implements EventStore {
    private final File root;
    private final Base32 base32;

    public FileEventStore(String projectId, File root) throws IOException {
        if (!root.exists() || !root.isDirectory()) {
            throw new IOException("The root directory '" + root + "' does not exist or is not a directory.");
        }

        this.root = new File(root, projectId);
        this.base32 = new Base32();
    }

    protected File getCollectionDir(String collection) throws IOException {
        String fileName = base32.encodeAsString(collection.getBytes());
        File collectionDir = new File(root, fileName);
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
            writer.write(event.getEventJSON());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException ex) { }
            }
        }
    }

    @Override
    public Iterable<Event> read(String collection) throws IOException {
        File collectionDir = getCollectionDir(collection);

        File[] files = collectionDir.listFiles();
        ArrayList<Event> events = new ArrayList<Event>();

        for (File file : files) {
            BufferedReader reader = null;
            try {
                FileInputStream fin = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                String eventJSON = sb.toString();

                events.add(Event.fromEventStore(eventJSON, file.getName().replace(".json", "")));
            } catch (Exception e){
                if (reader != null) {
                    try { reader.close(); } catch (IOException ex) { }
                }
            }
        }

        return events;
    }

    @Override
    public Map<String, Iterable<Event>> readAll() throws IOException {
        HashMap<String, Iterable<Event>> events = new HashMap<String, Iterable<Event>>();

        File[] collections = root.listFiles();

        for (File collection : collections) {
            String collectionName = new String(base32.decode(collection.getName()));
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
