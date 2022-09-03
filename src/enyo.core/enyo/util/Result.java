package enyo.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * An alternative between a result value and an exceptional value.
 *
 * This class is losely modeled on the Rust type {@code std::result::Result}.
 * However, the method names follow the names in the {@link Optional} class.
 *
 * @param <T> the type of the successful case
 * @param <E> the type of the error case
 *
 * @see <a href="https://doc.rust-lang.org/std/result/enum.Result.html"><code>std::result::Result</code></a>
 */
public class Result<T, E> {
    /**
     * Creates a new result which contains a value.
     * @param value the value to be wrapped in the result
     * @param <T> the type of the value
     * @param <E> the type of errors (an arbitrary choice,
     *   since no error is present)
     * @return the result
     * @throws NullPointerException if {@code value} is null
     */
    public static <T, E> Result<T, E> of(T value) {
        Objects.requireNonNull(value);
        return new Result<T, E>(value);
    }

    /**
     * Creates a new result which contains an error.
     * @param error the error to be wrapped in the result
     * @param <T> the type of values (an arbitrary choice,
     *   since no value is present)
     * @param <E> the type of the error
     * @return the result
     * @throws NullPointerException if {@code error} is null
     */
    public static <T, E> Result<T, E> ofError(E error) {
        Objects.requireNonNull(error);
        return new ResultError<T, E>(error);
    }


    /**
     * Returns either n enum constant or the original integer value.
     * If possible, an enum is return which has a matching
     * {@link Enum#ordinal()} value.
     *
     * @param <T> the enum type
     * @param type the reified enum type
     * @param value the enum ordinal value to convert
     * @return a result with an enum constant whose ordinal is equal
     *   to {@code value}, or an error with the integer {@code value}
     *   if there is no match
     * @throws IllegalArgumentException if {@code type} does not
     *   refer to an enum type
     * @throws NullPointerException if {@code type} is null
     */
    public static <T extends Enum<T>> Result<T, Integer> ofEnumOrdinal(
        Class<T> type, int value) {
        if (!type.isEnum())
            throw new IllegalArgumentException(
                "Not an enum: " + type.getName());
        T[] values = type.getEnumConstants();
        if (value >= 0 && value < values.length)
            return Result.of(values[value]);
        else
            return Result.ofError(value);
    }

    /**
     * Returns true if this result contains a non-exceptional value.
     * @return true if a value is present, false if an error is present
     */
    public final boolean isPresent() {
        // Error values use the ResultError class.
        return getClass() == Result.class;
    }

    /**
     * Returns true if this result contains an error.
     * @return true if an error is present, false if a value is present
     */
    public final boolean isError() {
        return getClass() != Result.class;
    }

    /**
     * Returns the result value stored in this result.
     * This method raises {@link ResultException} if and only if
     * {@link #isPresent()} is false.
     * @return the value
     * @throws ResultException if no value is present
     * @see #value()
     */
    @SuppressWarnings("unchecked")
    public final T orElseThrow() {
        if (isPresent())
            return (T) object;
        else {
            String message;
            try {
                message = object.toString();
            } catch (Throwable t) {
                throw new ResultException();
            }
            throw new ResultException(message);
        }
    }

    /**
     * Returns the value if available.
     * This method returns an empty result if and only if
     * {@link #isPresent()} is false.
     * @return the value, if available
     * @see #orElseThrow()
     * @see #stream()
     * @see #error()
     */
    public final Optional<T> value() {
        if (isPresent())
            return Optional.of(orElseThrow());
        else
            return Optional.empty();
    }

    /**
     * Consumes the value if it is available.
     * Nothing happens if this result is an error.
     * @param action the consumer to invoke with the value if it is available
     * @throws NullPointerException if {@code action} is null
     */
    public final void ifPresent(Consumer<? super T> action) {
        if (isPresent())
            action.accept(orElseThrow());
    }

    /**
     * Consumes the value if it is available.
     * Nothing happens if this result is an error.
     * @param action the consumer to invoke with the value
     * @param errorAction the consumer to invoke with the error
     * @throws NullPointerException if any argument is null
     */
    public final void ifPresentOrElse(Consumer<? super T> action,
        Consumer<? super E> errorAction) {
        if (isPresent())
            action.accept(orElseThrow());
        else
            errorAction.accept(error().orElseThrow());
    }

    /**
     * Returns the error if available.
     * This method returns an empty result if and only if
     * {@link #isPresent()} is true.
     * @return the error, if available
     * @see #orElseThrow()
     * @see #value()
     */
    @SuppressWarnings("unchecked")
    public final Optional<E> error() {
        if (isPresent())
            return Optional.empty();
        else
            return Optional.of((E) object);
    }

    /**
     * Returns true if the present value equals the specified value.
     * The return value is equal to {@code equals(Result.of(other))}.
     * @param other the value to test against
     * @return true if the value is {@linkplain #isPresent()} and
     *   {@linkplain Object#equals(Object)} equal to {@code other},
     *   false if the value is absent or not equal
     */
    public final boolean equalsOf(Object other) {
        return isPresent() && object.equals(other);
    }

    /**
     * Returns true if the error equals the specified value.
     * The return value is equal to {@code equals(Result.ofError(other))}.
     * @param other the value to test against
     * @return true if this object is an {@linkplain #isError() error} and
     *   the error is {@linkplain Object#equals(Object) equal to}
     *   {@code other}, false if a value is present or the error is not equal
     */
    public final boolean equalsOfError(Object other) {
        return isError() && object.equals(other);
    }

    /**
     * Returns a stream with this result's value (if available).
     * If {@link #isPresent()} is true, this method returns a one-element
     * stream with the value, otherwise an empty stream is returned.
     * @return the stream
     * @see #value()
     */
    public final Stream<T> stream() {
        if (isPresent())
            return Stream.of(orElseThrow());
        else
            return Stream.of();
    }

    /**
     * Returns this result's value if present, or a default value.
     * If {@link #isPresent()} is true, this method returns the
     * value of this result, otherwise {@code other} is returned.
     * This is equivalent to {@code value().orElse(other)}.
     * @param other the default value for error cases
     * @return the value
     * @see #value()
     */
    public final T orElse(T other) {
        if (isPresent())
            return orElseThrow();
        else
            return other;
    }

    /**
     * Returns this result's value if present, or a supplied value.
     * If {@link #isPresent()} is true, this method returns the
     * value of this result, otherwise {@code other} is returned.
     * This is equivalent to {@code value().orElseGet(supplier)}.
     * @param supplier the source of the value in case of an error
     * @return the value
     * @see #value()
     */
    public final T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent())
            return orElseThrow();
        else
            return supplier.get();
    }

    /**
     * Produces a new result by applying a mapping to the value.
     * If the result is an error, this object is returned.
     * @param mapValue the mapping function for values
     * @param <U> the new value type
     * @return the transformed result if a value was present, or this
     *   result (with an error value)
     * @throws NullPointerException if {@code ok} is null
     */
    public final <U> Result<U, E> map(
        Function<? super T, ? extends U> mapValue) {
        Objects.requireNonNull(mapValue, "value");
        if (isPresent())
            return of(mapValue.apply(orElseThrow()));
        else
            return castError();
    }

    /**
     * Produces a new result by applying a mapping.
     * @param mapValue the mapping function for values
     * @param mapError the mapping function for errors
     * @param <U> the new value type
     * @param <F> the new error type
     * @return the transformed result
     * @throws NullPointerException if any of the arguments is null
     */
    public final <U, F> Result<U, F> map(
        Function<? super T, ? extends U> mapValue,
        Function<? super E, ? extends F> mapError) {
        Objects.requireNonNull(mapValue, "mapValue");
        Objects.requireNonNull(mapError, "mapError");
        if (isPresent())
            return of(mapValue.apply(value().get()));
        else
            return ofError(mapError.apply(error().get()));
    }

    /**
     * Produces a new result by applying a mapping to an error.
     * If this result has a value, this object is returned.
     * @param map the mapping function for errors
     * @param <F> the new error type
     * @return the transformed result if an error was present, or this
     *   result (with a non-error value)
     * @throws NullPointerException if {@code error} is null
     */
    public final <F> Result<T, F> mapError(
        Function<? super E, ? extends F> map) {
        Objects.requireNonNull(map, "map");
        if (isPresent())
            return castOk();
        else
            return ofError(map.apply(error().get()));
    }

    /**
     * Computes a new result if a value is present.
     * If this result has value, apply {@code} map to it produce a new result,
     * otherwise return this result.
     * @param map the mapping operation
     * @param <U> the new result type
     * @return the new result (if this result has a value), or this result
     * @throws NullPointerException if {@code map} is null
     */
    public final <U> Result<U, E> flatMap(
        Function<? super T, ? extends Result<? extends U, ? extends E>> map) {
        Objects.requireNonNull(map, "map");
        if (isPresent()) {
            var r = map.apply(orElseThrow());
            if (r.isPresent())
                return Result.of(r.orElseThrow());
            else
                return Result.ofError(r.error().orElseThrow());
        }
        else
            return castError();
    }

    /**
     * Computes a result of a shared type for values and errors.
     * @param fromValue the mapping function for values
     * @param fromError the mapping function for errors
     * @return the result of applying the appropriate mapping function
     * @param <R> the common result type
     */
    public final <R> R compute(Function<? super T, ? extends R> fromValue,
        Function<? super E, ? extends R> fromError) {
        Objects.requireNonNull(fromValue, "fromValue");
        Objects.requireNonNull(fromError, "fromError");
        if (isPresent())
            return fromValue.apply(orElseThrow());
        else
            return fromError.apply(error().orElseThrow());
    }

    /**
     * Returns a comparator which sort errors before values.
     * The comparison uses the natural ordering of values and errors.
     * @param <T> the type of the values (must be comparable)
     * @param <E> the type of the errors (must be comparable)
     * @return a comparator
     */
    @SuppressWarnings("unchecked")
    public static
        <T extends Comparable<? super T>, E extends Comparable<? super E>>
        Comparator<Result<T, E>> errorsFirst() {
        return NaturalOrder.ERRORS_FIRST;
    }

    /**
     * Returns a comparator which sort errors before values.
     * The comparison uses the specified comparators.
     * @param values the comparator for the values
     * @param errors the comparator for the errors
     * @param <T> the type of the values
     * @param <E> the type of the errors
     * @return a comparator
     */
    public static <T, E> Comparator<Result<T, E>> errorsFirst(
        Comparator<? super T> values, Comparator<? super E> errors) {
        return comparator(values, errors, 1);
    }

    /**
     * Returns a comparator which sort error values last.
     * @param <T> the type of the values (must be comparable)
     * @param <E> the type of the errors (must be comparable)
     * @return a comparator
     */
    @SuppressWarnings("unchecked")
    public static
        <T extends Comparable<? super T>, E extends Comparable<? super E>>
        Comparator<Result<T, E>> valuesFirst() {
        return NaturalOrder.VALUES_FIRST;
    }

    /**
     * Returns a comparator which sort values before errors.
     * The comparison uses the specified comparators.
     * @param values the comparator for the values
     * @param errors the comparator for the errors
     * @param <T> the type of the values
     * @param <E> the type of the errors
     * @return a comparator
     */
    public static <T, E> Comparator<Result<T, E>> valuesFirst(
        Comparator<? super T> values, Comparator<? super E> errors) {
        return comparator(values, errors, -1);
    }

    /**
     * Result of a collector operation on {@link Result}s.
     *
     * In general, collectors operating on streams of {@link Result}s
     * produce errors and values at the same time.  Such results are
     * represented by this class.
     *
     * @param <T> the type of the errors
     * @param <E> the type of the values
     * @see Collectors
     */
    public static final class Collected<T, E> {
        /**
         * Creates new pair of collected values.
         * @param values the values
         * @param errors he errors
         * @throws NullPointerException if any of the arguments is null
         */
        public Collected(T values, E errors) {
            this.values = Objects.requireNonNull(values, "values");
            this.errors = Objects.requireNonNull(errors, "errors");
        }

        /**
         * Returns the values.
         * @return the values, as passed to the constructor
         */
        public T values() {
            return values;
        }

        /**
         * Returns the errors.
         * @return the errors, as passed to the constructor
         */
        public E errors() {
            return errors;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != Collected.class)
                return false;
            Collected<?, ?> other = (Collected<?, ?>) o;
            return values.equals(other.values) && errors.equals(other.errors);
        }

        @Override
        public int hashCode() {
            return values.hashCode() * 17 + errors.hashCode();
        }

        @Override
        public String toString() {
            return String.format("[values=%s, errors=%s]", values, errors);
        }

        private final T values;
        private final E errors;
    }

    /**
     * Collectors for use with streams of {@link Result}s.
     * @see Collector
     */
    public static final class Collectors {
        /**
         * Returns a collector producing a pair of collections.
         *
         * For example, to obtain a collector which produces a set of
         * values and a list of errors, this invocation can be used:
         *
         * <pre>
         *     Collectors.toCollection(HashSet::new, ArrayList::new)
         * </pre>
         * @param valuesFactory the supplier for collections storing values
         * @param errorsFactory the supplier for collections storing errors
         * @param <T> the type of values
         * @param <E> the type of errors
         * @param <CT> the collection type for values
         * @param <CE> the collection type for errors
         * @return a collector producing a pair of collections
         */
        public static <T, E, CT extends Collection<T>, CE extends Collection<E>>
            Collector<Result<T, E>, ?, Collected<CT, CE>> toCollection(
                Supplier<CT> valuesFactory, Supplier<CE> errorsFactory) {
            return new CollectionCollector<>(valuesFactory, errorsFactory);
        }

        private Collectors() { } // No instances.
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        return object.equals(((Result) o).object);
    }

    @Override
    public final int hashCode() {
        return object.hashCode() ^ getClass().hashCode();
    }

    /**
     * Returns the string representation of the underlying object.
     * This is the unadorned string for either the value or error
     * object, whichever is present.
     * @return the string, as returned by {@link Object#toString}
     */
    @Override
    public final String toString() {
        return object.toString();
    }

    private final Object object;

    private Result(Object object) {
        this.object = object;
    }

    @SuppressWarnings("unchecked")
    private <F> Result<T, F> castOk() {
        if (isPresent())
            return (Result<T, F>) this;
        else
            throw new IllegalStateException("Not a value");
    }

    @SuppressWarnings("unchecked")
    private <U> Result<U, E> castError() {
        if (isPresent())
            throw new IllegalStateException("Not an error");
        else
            return (Result<U, E>) this;
    }

    // Different class to mark error values.
    private static final class ResultError<T, E> extends Result<T, E> {
        ResultError(Object value) {
            super(value);
        }
    }

    private static final class NaturalOrder
        <T extends Comparable<? super T>, E extends Comparable<? super E>>
        implements Comparator<Result<T, E>> {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compare(Result<T, E> left, Result<T, E> right) {
            Comparable l = (Comparable) left.object;
            Comparable r = (Comparable) right.object;
            if (left.isPresent()) {
                if (right.isPresent())
                    return l.compareTo(r);
                else
                    return presentAndError;
            } else {
                if (right.isPresent())
                    return -presentAndError;
                else
                    return l.compareTo(r);
            }
        }

        private final int presentAndError;

        private NaturalOrder(int presentAndError) {
            this.presentAndError = presentAndError;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        static final NaturalOrder ERRORS_FIRST = new NaturalOrder(1);
        @SuppressWarnings({"unchecked", "rawtypes"})
        static final NaturalOrder VALUES_FIRST = new NaturalOrder(-1);
    }

    private static <T, E> Comparator<Result<T, E>> comparator(
        Comparator<? super T> values, Comparator<? super E> errors,
        int presentAndError) {
        return (left, right) -> {
            if (left.isPresent()) {
                if (right.isPresent())
                    return values.compare(left.orElseThrow(),
                        right.orElseThrow());
                else
                    return presentAndError;
            } else {
                if (right.isPresent())
                    return -presentAndError;
                else
                    return errors.compare(left.error().orElseThrow(),
                        right.error().orElseThrow());
            }
        };
    }

    private static final class CollectionCollector
        <T, E, CT extends Collection<T>, CE extends Collection<E>>
        implements Collector<Result<T, E>, Collected<CT, CE>,
        Collected<CT, CE>> {
        private final Supplier<CT> valuesFactory;
        private final Supplier<CE> errorsFactory;

        private static final Set<Characteristics> CHARACTERISTICS =
            Set.of(Characteristics.IDENTITY_FINISH);

        CollectionCollector(Supplier<CT> valuesFactory,
            Supplier<CE> errorsFactory) {
            this.valuesFactory = valuesFactory;
            this.errorsFactory = errorsFactory;
        }

        @Override
        public BiConsumer<Collected<CT, CE>, Result<T, E>> accumulator() {
            return (mut, t) ->
                t.ifPresentOrElse(mut.values::add, mut.errors::add);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }

        @Override
        public BinaryOperator<Collected<CT, CE>> combiner() {
            return (left, right) -> {
                left.values.addAll(right.values);
                left.errors.addAll(right.errors);
                return left;
            };
        }

        @Override
        public Function<Collected<CT, CE>, Collected<CT, CE>> finisher() {
            return Function.identity();
        }

        @Override
        public Supplier<Collected<CT, CE>> supplier() {
            return () ->
                new Collected<>(valuesFactory.get(), errorsFactory.get());
        }
    }
}
