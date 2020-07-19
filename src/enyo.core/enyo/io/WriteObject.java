package enyo.io;

import java.io.IOException;

/**
 * Functional interface for writing an object.
 *
 * @param <S> the type of the stream to which objects are written
 * @param <T> the type of the objects being written
 */
@FunctionalInterface
public interface WriteObject<S, T> {
    /**
     * Writes the object to the stream.
     * @param stream the stream that is written to
     * @param object the object that is written
     * @throws IOException if a write error occurs
     */
    void writeObject(S stream, T object) throws IOException;
}
