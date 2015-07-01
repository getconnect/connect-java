package io.getconnect.client;

/**
 * Represents a general server exception.
 */
public class ServerException extends RuntimeException {
    public ServerException() {
        super();
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
