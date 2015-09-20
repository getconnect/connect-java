package io.getconnect.client.exceptions;

/**
 * Represents a generic Connect exception.
 */
public class ConnectException extends RuntimeException {
    public ConnectException() {
        super();
    }

    public ConnectException(Throwable cause) {
        super(cause);
    }

    public ConnectException(String message) {
        super(message);
    }

    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
