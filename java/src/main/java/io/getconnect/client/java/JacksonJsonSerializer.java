package io.getconnect.client.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.getconnect.client.JsonSerializer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public class JacksonJsonSerializer implements JsonSerializer {
    private final ObjectMapper mapper;

    private static final MapType MAP_TYPE = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
    private static final MapType MAP_ARRAY_TYPE = TypeFactory.defaultInstance().constructMapType(Map.class,
            TypeFactory.defaultInstance().constructType(String.class), MAP_TYPE);

    public JacksonJsonSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public Map<String, Object> deserialize(Reader reader) throws IOException {
        return mapper.readValue(reader, MAP_TYPE);
    }

    @Override
    public void serialize(Writer writer, Map<String, ?> value) throws IOException {
        mapper.writeValue(writer, value);
    }
}
