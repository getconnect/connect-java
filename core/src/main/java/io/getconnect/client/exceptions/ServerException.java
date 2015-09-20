package io.getconnect.client.exceptions;

/**
 * Represents a general server exception.
 */
public class ServerException extends ConnectException {
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
