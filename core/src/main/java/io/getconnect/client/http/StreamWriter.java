package io.getconnect.client.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface to write data to a stream.
 * This is used in the HTTP client to write the data contained in the request.
 */
public interface StreamWriter {
    /**
     * Write the data to the specified {@link OutputStream}.
     * @param stream The output stream to which to write.
     * @throws IOException If there is an error writing to the stream.
     */
    void write(OutputStream stream) throws IOException;
}
