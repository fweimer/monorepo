package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;

/**
 * A biconsumer which can throw {@link IOException}.  Typical use
 * involves {@link #of(IOBiConsumer)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * Map<Path, Long> files = Map.of();
 * ...
 * files.forEach(IOBiConsumer.of((path, size) -> {
 *         try (var ch = FileChannel.open(
 *                 path, StandardOpenOption.WRITE)) {
 *             ch.truncate(size);
 *         }
 *     }));
 *}</pre>
 *
 * @param <T> the type of the first consumed value
 * @param <U> the type of the second consumed value
 */
@FunctionalInterface
public interface IOBiConsumer<T, U> extends BiConsumer<T, U> {
    /**
     * Performs an I/O operation on a value.
     *
     * @param t the first accepted value
     * @param u the second accepted value
     * @throws IOException if an I/O error occurs
     */
    void acceptIO(T t, U u) throws IOException;

    /**
     * Consumes the value.
     *
     * The default implementation calls {@link #acceptIO(Object,Object)} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     *
     * @param t the first accepted value
     * @param u the second accepted value
     */
    default void accept(T t, U u) {
        try {
            acceptIO(t, u);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its consumer argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param <T> the type of the first consumed value
     * @param <U> the type of the second consumed value
     * @param consumer the consumer, usually a lambda expression
     * @return the argument to this function
     */
    static <T, U> IOBiConsumer<T, U> of(IOBiConsumer<T, U> consumer) {
        return consumer;
    }
}
