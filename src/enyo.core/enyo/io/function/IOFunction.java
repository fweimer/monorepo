package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * A function which can throw {@link IOException}.  Typical use
 * involves {@link #of(IOFunction)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * List<Path> files;
 * ...
 * List<byte[]> fileContents = files.stream()
 *   .map(IOFunction.of(Files::readAllBytes))
 *   .collect(Collectors.toList());
 * }
 * </pre>
 *
 * @param <T> the argument type of the function
 * @param <R> the return type of the function
 */
@FunctionalInterface
public interface IOFunction<T, R> extends Function<T, R> {
    /**
     * Performs the operation that turns the argument into the return
     * value.
     *
     * @param t the function argument
     * @return the function return value
     *
     * @throws IOException if an I/O error occurs
     */
    R applyIO(T t) throws IOException;

    /**
     * Applies the function to the value.
     *
     * The default implementation calls {@link #applyIO(Object)} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     *
     * @param t the function argument
     * @return the function return value
     */
    default R apply(T t) {
        try {
            return applyIO(t);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its function argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param <T> the argument type of the function
     * @param <R> the return type of the function
     * @param function the function, usually a lambda expression
     * @return the argument to this function
     */
    static <T, R> IOFunction<T, R> of(IOFunction<T, R> function) {
        return function;
    }
}
