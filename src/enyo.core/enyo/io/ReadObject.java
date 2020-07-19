package enyo.io;

import java.io.IOException;

/**
 * Functional interface for reading an object.
 *
 * @param <S> the type of the stream from which objects are read
 * @param <T> the type of the objects being read
 */
@FunctionalInterface
public interface ReadObject<S, T> {
    /**
     * Writes the objectfrom the stream.
     * @param stream the stream that is read from
     * @return the object that has been read
     * @throws IOException if a read error occurs
     */
    T readObject(S stream) throws IOException;
}
