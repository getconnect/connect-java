package io.getconnect.client;

import io.getconnect.client.exceptions.ConnectException;
import io.getconnect.client.exceptions.InvalidEventException;

public interface ConnectCallback {

    /**
     * Called when a request to Connect has been successful.
     */
    public void onSuccess();

    /**
     * Called when a request to Connect has failed.
     * @param e A ConnectException outlining the error that occurred.
     *         Will be {@link InvalidEventException}, {@Link ServerException}
     *         or a generic {@link ConnectException} with an inner exception.
     */
    public void onFailure(ConnectException e);

}
