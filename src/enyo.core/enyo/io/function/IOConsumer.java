package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * A consumer which can throw {@link IOException}.  Typical use
 * involves {@link #of(IOConsumer)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * List<Path> files;
 * ...
 * files.stream().forEach(IOConsumer.of(Files::delete));
 * }
 * </pre>
 *
 * @param <T> the type of the consumed value
 */
@FunctionalInterface
public interface IOConsumer<T> extends Consumer<T> {
    /**
     * Performs an I/O operation on a value.
     *
     * @param t the accepted value
     * @throws IOException if an I/O error occurs
     */
    void acceptIO(T t) throws IOException;

    /**
     * Consumes the value.
     *
     * The default implementation calls {@link #acceptIO(Object)} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     *
     * @param t the value
     */
    default void accept(T t) {
        try {
            acceptIO(t);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its consumer argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param <T> the type of the consumed value
     * @param consumer the consumer, usually a lambda expression
     * @return the argument to this function
     */
    static <T> IOConsumer<T> of(IOConsumer<T> consumer) {
        return consumer;
    }
}
