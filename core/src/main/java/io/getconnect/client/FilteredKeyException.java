package io.getconnect.client;

/**
 * Represents an exception when the filtered key generation fails.
 */
public class FilteredKeyException extends RuntimeException {
    public FilteredKeyException() {
        super();
    }

    public FilteredKeyException(Throwable cause) {
        super(cause);
    }

    public FilteredKeyException(String message) {
        super(message);
    }

    public FilteredKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}