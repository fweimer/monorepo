package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.IntConsumer;

/**
 * A consumer which can throw {@link IOException}.  Typical use
 * involves {@link #of(IOIntConsumer)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * Path path;
 * ...
 * // Create files named "0", ..., "9".
 * IntStream.range(0, 10)
 *     .forEach(IOIntConsumer.of(i ->
 *             Files.write(path.resolve(Integer.toString(i)),
 *                 new byte[0])));
 * }
 * </pre>
 */
@FunctionalInterface
public interface IOIntConsumer extends IntConsumer {
    /**
     * Performs an I/O operation on a value.
     *
     * @param value the accepted value
     * @throws IOException if an I/O error occurs
     */
    void acceptIO(int value) throws IOException;

    /**
     * Consumes the value.
     *
     * The default implementation calls {@link #acceptIO(int)} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     *
     * @param value the value
     */
    default void accept(int value) {
        try {
            acceptIO(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its consumer argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param consumer the consumer, usually a lambda expression
     * @return the argument to this function
     */
    static IOIntConsumer of(IOIntConsumer consumer) {
        return consumer;
    }
}
