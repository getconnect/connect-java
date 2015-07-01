package io.getconnect.client;

import java.util.Map;

/**
 * Represents an exception when trying to create an event with invalid properties.
 */
public class InvalidEventException extends RuntimeException {
    public InvalidEventException() {
        super();
    }

    public InvalidEventException(Throwable cause) {
        super(cause);
    }

    public InvalidEventException(String message) {
        super(message);
    }

    public InvalidEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidEventException create(Map<String, String> invalidProperties) {
        StringBuilder message = new StringBuilder();
        message.append("The following properties were not valid:\n");
        for (String key : invalidProperties.keySet()) {
            message.append(key);
            message.append(": ");
            message.append(invalidProperties.get(key));
            message.append("\n");
        }

        return new InvalidEventException(message.toString());
    }
}
