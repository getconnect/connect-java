package io.getconnect.client.android;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import io.getconnect.client.JsonSerializer;

public class AndroidJsonSerializer implements JsonSerializer {
    @Override
    public Map<String, Object> deserialize(Reader reader) throws IOException {
        String json = toString(reader);

        try {
            JSONObject object = new JSONObject(json);
            return JsonHelper.toMap(object);
        } catch (JSONException ex) {
            throw new IOException("An error occurred parsing the JSON.", ex);
        }
    }

    private String toString(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        try {
            char[] buffer = new char[4096];
            while (reader.read(buffer, 0, 4096) > 0) {
                writer.write(buffer);
            }
            return writer.toString();
        }
        finally {
            writer.close();
        }
    }

    @Override
    public void serialize(Writer writer, Map<String, ?> value) throws IOException {
        JSONObject json;
        try {
            json = (JSONObject) JsonHelper.toJSON(value);
        } catch (JSONException ex) {
            throw new IOException("An error occurred writing the JSON.", ex);
        }

        writer.write(json.toString());
    }
}
