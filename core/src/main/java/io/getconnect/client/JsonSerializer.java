package io.getconnect.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * An interface for abstracting the serialization/deserialization of JSON.
 */
public interface JsonSerializer {
    /**
     * Deserialize JSON data provided by {@link java.io.Reader} to a {@link java.util.Map}.
     * @param reader The {@link java.io.Reader} from which to read the JSON data.
     * @return A {@link java.util.Map} containing the deserialized object.
     * @throws IOException If an error occurs while reading the data.
     */
    Map<String, Object> deserialize(Reader reader) throws IOException;

    /**
     * Serializes a {@link java.util.Map} to JSON and writes it to the specified {@link java.io.Writer}.
     * @param writer The {@link java.io.Writer} to which to write the JSON data.
     * @param value A {@link java.util.Map} containing the object to serialize.
     * @throws IOException If an error occurs while writing the data.
     */
    void serialize(Writer writer, Map<String, ?> value) throws IOException;
}
