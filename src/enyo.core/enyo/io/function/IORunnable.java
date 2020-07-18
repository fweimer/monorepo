package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A runnable which can throw {@link IOException}.  Typical use
 * involves {@link #of(IORunnable)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * Cleaner cleaner = Cleaner.create();
 * Path path;
 * ...
 * Cleaner.Cleanable cleanable = cleaner.register(
 *     path, IORunnable.of(() -> Files.deleteIfExists(path)));
 * }
 * </pre>
 */
@FunctionalInterface
public interface IORunnable extends Runnable {
    /**
     * Performs an I/O operation.
     *
     * @throws IOException if an I/O error occurs
     */
    void runIO() throws IOException;

    /**
     * Performs the action.
     *
     * The default implementation calls {@link #runIO()} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     */
    default void run() {
        try {
            runIO();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its runnable argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param runnable the runnable, usually a lambda expression
     * @return the argument to this function
     */
    static IORunnable of(IORunnable runnable) {
        return runnable;
    }
}
