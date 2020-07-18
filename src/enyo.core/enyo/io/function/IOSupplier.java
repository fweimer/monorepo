package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * A supplier that can throw {@link IOException}.  Typical use
 * involves {@link #of(IOSupplier)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * Optional<InputStream> optionalStream;
 * ...
 * InputStream in = optionalStream.orElseGet(
 *   IOSupplier.of(() -> Files.newInputStream(path)));
 * }
 * </pre>
 *
 * @param <T> the type of the objects returned
 */
@FunctionalInterface
public interface IOSupplier<T> extends Supplier<T> {
    /**
     * Returns an object of this supplier's type.
     *
     * @return an object
     * @throws IOException if an I/O error occurred
     */
    T getIO() throws IOException;

    /**
     * Returns an object of this supplier's type.
     *
     * The default implementation calls {@link #getIO()} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     *
     * @return an object
     */
    default T get() {
        try {
            return getIO();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its supplier argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param <T> the type of the produced value
     * @param supplier the supplier, usually a lambda expression
     * @return the argument to this function
     */
    static <T> IOSupplier<T> of(IOSupplier<T> supplier) {
        return supplier;
    }
}
