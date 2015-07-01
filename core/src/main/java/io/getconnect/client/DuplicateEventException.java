package io.getconnect.client;

/**
 * Represents an exception when a duplicate event is pushed.
 */
public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException() {
        super();
    }

    public DuplicateEventException(Throwable cause) {
        super(cause);
    }

    public DuplicateEventException(String message) {
        super(message);
    }

    public DuplicateEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
