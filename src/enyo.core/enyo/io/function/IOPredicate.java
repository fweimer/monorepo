package enyo.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Predicate;

/**
 * A predicte which can throw {@link IOException}.  Typical use
 * involves {@link #of(IOPredicate)} to apply the correct type to a
 * lambda expression or method reference, like this:
 *
 * <pre>
 * {@code
 * List<Path> files;
 * ...
 * List<Path> emptyFiles = files.stream()
 *   .filter(IOPredicate.of(path -> Files.size(path) == 0))
 *   .collect(Collectors.toList());
 * }
 * </pre>
 *
 * @param <T> the argument type of the predicate
 */
@FunctionalInterface
public interface IOPredicate<T> extends Predicate<T> {
    /**
     * Checks whether the predicate is true for the argument.
     *
     * @param t the predicate argument
     * @return the predicate result
     *
     * @throws IOException if an I/O error occurs
     */
    boolean testIO(T t) throws IOException;

    /**
     * Performs the predicate check for the value.
     *
     * The default implementation calls {@link #testIO(Object)} and
     * wraps any {@link IOException} in an {@link UncheckedIOException}.
     *
     * @param t the predict argument
     * @return the predicate result
     */
    @Override
    default boolean test(T t) {
        try {
            return testIO(t);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns its function argument.  This can be used to assert the
     * type of lambda expressions.
     *
     * @param <T> the argument type of the predicate
     * @param predicate the predicate, usually a lambda expression
     * @return the argument to this function
     */
    static <T> IOPredicate<T> of(IOPredicate<T> predicate) {
        return predicate;
    }
}
