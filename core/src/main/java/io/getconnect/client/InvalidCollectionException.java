package io.getconnect.client;

/**
 * Represents an exception when the collection is invalid.
 */
public class InvalidCollectionException extends RuntimeException {
    public InvalidCollectionException() {
        super();
    }

    public InvalidCollectionException(Throwable cause) {
        super(cause);
    }

    public InvalidCollectionException(String message) {
        super(message);
    }

    public InvalidCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
