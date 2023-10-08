package enyo.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * An alternative between a result value and an exceptional value.
 *
 * This class is losely modeled on the Rust type {@code std::result::Result}
 * and Haskell's {@code Data.Either}.
 * However, the method names follow the names in the {@link Optional} class.
 *
 * <p>{@code Either} values can be created usint the
 * {@link #ofLeft(Object)} and {@link #ofRight(Object)} functions,
 * or directly using the {@link Left#Left(Object)}
 * and {@link Right#Right(Object)}
 * constructors.  {@code Either}  values cannot contain null values.
 *
 * <p>{@code Either} values can be used in pattern matching, using the
 * {@link Left} and {@link Right} types:
 * {@snippet lang="java" :
 * switch (e) {
 * case Either.Left(var value) -> System.out.format("left: %s%n", value);
 * case Either.Right(var value) -> System.out.format("right: %s%n", value);
 * }
 * }
 *
 * @param <L> the type of the left-hand value
 * @param <R> the type of the right-hand value
 *
 * @see <a href="https://doc.rust-lang.org/std/result/enum.Result.html"><code>std::result::Result</code></a>
 * @see <a href="https://hackage.haskell.org/package/base/docs/Data-Either.html"><code>Data.Either</code></a>
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {
    /**
     * Right-hand values types.
     * @param value the left-hand value
     * @param <L> type of the left-hand value
     * @param <R> type of right-hand values (not used by this object)
     */
    public record Left<L, R>(L value) implements Either<L, R> {
        /**
         * Creates a new left-hand value.
         * @throws NullPointerException if {@code value} is null
         */
        public Left {
            Objects.requireNonNull(value);
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public Optional<L> toLeft() {
            return Optional.of(value);
        }

        @Override
        public Optional<R> toRight() {
            return Optional.empty();
        }

        @Override
        public L leftOrElse(L other) {
            return value;
        }

        @Override
        public R rightOrElse(R other) {
            return other;
        }

        @Override
        public L leftOrElseGet(Supplier<? extends L> supplier) {
            Objects.requireNonNull(supplier);
            return value;
        }

        @Override
        public R rightOrElseGet(Supplier<? extends R> supplier) {
            return supplier.get();
        }

        @Override
        public L leftOrElseThrow() {
            return value;
        }

        @Override
        public R rightOrElseThrow() {
            throw EitherException.wrap(value);
        }

        @Override
        public boolean equalsOfLeft(Object other) {
            return value.equals(other);
        }

        @Override
            public boolean equalsOfRight(Object other) {
            return false;
        }

        @Override
        public final <L1, R1> Left<L1, R1> map(
            Function<? super L, ? extends L1> mapLeft,
            Function<? super R, ? extends R1> mapRight) {
            Objects.requireNonNull(mapLeft, "mapLeft");
            Objects.requireNonNull(mapRight, "mapRight");
            return new Left<L1, R1>(mapLeft.apply(value()));
        }

        @Override
        public final <L1> Either<L1, R> mapLeft(
            Function<? super L, ? extends L1> mapLeft) {
            return new Left<>(mapLeft.apply(value));
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <R1> Either<L, R1> mapRight(
            Function<? super R, ? extends R1> mapRight) {
            Objects.requireNonNull(mapRight, "mapRight");
            return (Left) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <L1> Either<L1, R> flatMapLeft(
            Function<? super L, ? extends Either<? extends L1, ? extends R>>
            mapLeft) {
            return (Either) mapLeft.apply(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <R1> Either<L, R1> flatMapRight(
            Function<? super R, ? extends Either<? extends L, ? extends R1>>
            mapRight) {
            Objects.requireNonNull(mapRight, "mapRight");
            return (Left) this;
        }

        @Override
        public final <T> T compute(Function<? super L, ? extends T> fromLeft,
            Function<? super R, ? extends T> fromRight) {
            Objects.requireNonNull(fromLeft, "fromLeft");
            Objects.requireNonNull(fromRight, "fromRight");
            return fromLeft.apply(value());
        }

        @Override
        public final Stream<L> leftStream() {
            return Stream.of(value);
        }

        @Override
        public final Stream<R> rightStream() {
            return Stream.of();
        }
    }

    /**
     * Right-hand values types.
     * @param value the right-hand value
     * @param <L> type of left-hand values (not used by this object)
     * @param <R> type of the right hand value
     */
    public record Right<L, R>(R value) implements Either<L, R> {
        /**
         * Creates a new right-hand value.
         * @throws NullPointerException if {@code value} is null
         */
        public Right {
            Objects.requireNonNull(value);
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public Optional<L> toLeft() {
            return Optional.empty();
        }

        @Override
        public Optional<R> toRight() {
            return Optional.of(value);
        }

        @Override
        public L leftOrElse(L other) {
            return other;
        }

        @Override
        public R rightOrElse(R other) {
            return value;
        }

        @Override
        public L leftOrElseGet(Supplier<? extends L> supplier) {
            return supplier.get();
        }

        @Override
        public R rightOrElseGet(Supplier<? extends R> supplier) {
            Objects.requireNonNull(supplier);
            return value;
        }

        @Override
        public L leftOrElseThrow() {
            throw EitherException.wrap(value);
        }

        @Override
        public R rightOrElseThrow() {
            return value;
        }

        @Override
        public boolean equalsOfLeft(Object other) {
            return false;
        }

        @Override
        public boolean equalsOfRight(Object other) {
            return value.equals(other);
        }

        @Override
            public final <L1, R1> Right<L1, R1> map(
            Function<? super L, ? extends L1> mapLeft,
            Function<? super R, ? extends R1> mapRight) {
            Objects.requireNonNull(mapLeft, "mapLeft");
            Objects.requireNonNull(mapRight, "mapRight");
            return new Right<L1, R1>(mapRight.apply(value()));
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <L1> Either<L1, R> mapLeft(
            Function<? super L, ? extends L1> mapLeft) {
            Objects.requireNonNull(mapLeft, "mapLeft");
            return (Right) this;
        }

        @Override
        public final <R1> Either<L, R1> mapRight(
            Function<? super R, ? extends R1> mapRight) {
            return new Right<>(mapRight.apply(value));
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <L1> Either<L1, R> flatMapLeft(
            Function<? super L, ? extends Either<? extends L1, ? extends R>>
            mapLeft) {
            Objects.requireNonNull(mapLeft, "mapLeft");
            return (Right) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <R1> Either<L, R1> flatMapRight(
            Function<? super R, ? extends Either<? extends L, ? extends R1>>
            mapRight) {
            return (Either) mapRight.apply(value);
        }

        @Override
        public final <T> T compute(Function<? super L, ? extends T> fromLeft,
            Function<? super R, ? extends T> fromRight) {
            Objects.requireNonNull(fromLeft, "fromLeft");
            Objects.requireNonNull(fromRight, "fromRight");
            return fromRight.apply(value());
        }

        @Override
        public final Stream<L> leftStream() {
            return Stream.of();
        }

        @Override
        public final Stream<R> rightStream() {
            return Stream.of(value);
        }
    }

    /**
     * Creates a new left-hand value.
     * @param value the value to be wrapped
     * @param <L> the left-hand value type
     * @param <R> the type of right-hand values (an arbitrary choice,
     *   since no value is present)
     * @return a left-hand value
     * @throws NullPointerException if {@code value} is null
     */
    public static <L, R> Left<L, R> ofLeft(L value) {
        return new Left<L, R>(value);
    }

    /**
     * Creates a new right-hand value.
     * @param value the value to be wrapped
     * @param <L> the type for  left-hand values (an arbitrary choice,
     *   since no value is present)
     * @param <R> the right-hand value type
     * @return a right-hand value
     * @throws NullPointerException if {@code value} is null
     */
    public static <L, R> Right<L, R> ofRight(R value) {
        return new Right<L, R>(value);
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
    public static <T extends Enum<T>> Either<T, Integer> ofEnumOrdinal(
        Class<T> type, int value) {
        if (!type.isEnum())
            throw new IllegalArgumentException(
                "Not an enum: " + type.getName());
        T[] values = type.getEnumConstants();
        if (value >= 0 && value < values.length)
            return ofLeft(values[value]);
        else
            return ofRight(value);
    }

    /**
     * Returns true if this value is a left-hand value.
     * @return if this value is an instance of {@link Left}
     */
    public boolean isLeft();

    /**
     * Returns true if this value is a right-hand value.
     * @return if this value is an instance of {@link Right}
     */
    public boolean isRight();

    /**
     * Returns an optional value wrapping the left value.
     * @return the left value, or an empty value if it is missing
     */
    public Optional<L> toLeft();

    /**
     * Returns an optional value wrapping the right value.
     * @return the right value, or an empty value if it is missing
     */
    public Optional<R> toRight();

    /**
     * Returns the left value, or a default value.
     * @param other the default value to return
     *   if this object contains a right value
     * @return the left value or default
     */
    public L leftOrElse(L other);

    /**
     * Returns the right value, or a default value.
     * @param other the default value to return
     *   if this object contains a left value
     * @return the right value or default
     */
    public R rightOrElse(R other);

    /**
     * Returns the left value, or a supplied value.
     * @param supplier for obtaining the value in case this is a right value
     * @return the left value or the supplied value
     */
    public L leftOrElseGet(Supplier<? extends L> supplier);

    /**
     * Returns the right value, or a supplied value.
     * @param supplier for obtaining the value in case this is a left value
     * @return the right value or the supplied value
     */
    public R rightOrElseGet(Supplier<? extends R> supplier);

    /**
     * Returns the left value, or throws an exception.
     * @return the left value
     * @throws EitherException if the object contains a right value
     */
    public L leftOrElseThrow();

    /**
     * Returns the right value, or throws an exception.
     * @return the right value
     * @throws EitherException if the object contains a left value
     */
    public R rightOrElseThrow();

    /**
     * Returns true if this object contains a matching left-hand value.
     * A call {@code obj.equalsOfLeft(other)} is equivalent to
     * {@code obj.equals(Either.leftOf(other))}.
     * @param other the left-hand value to compare to
     * @return true if the values are equal
     */
    public boolean equalsOfLeft(Object other);

    /**
     * Returns true if this object contains a matching right-hand value.
     * A call {@code obj.equalsOfRight(other)} is equivalent to
     * {@code obj.equals(Either.rightOf(other))}.
     * @param other the right-hand value to compare to
     * @return true if the values are equal
     */
    public boolean equalsOfRight(Object other);

    /**
     * Produces a new result by applying a mapping.
     * @param mapLeft the mapping function for left-hand values
     * @param mapRight the mapping function for right-hand values
     * @param <L1> the new left type
     * @param <R1> the new right type
     * @return the transformed result
     * @throws NullPointerException if any of the arguments is null
     */
    public <L1, R1> Either<L1, R1> map(
        Function<? super L, ? extends L1> mapLeft,
        Function<? super R, ? extends R1> mapRight);

    /**
     * Produces a new result by applying a mapping to a left-hand value.
     * Right-hand values are returned unchanged.
     * @param mapLeft the mapping function for left-hand values
     * @param <L1> the new left-hand value type
     * @return the new left-hand value, or the old right-hand value
     * @throws NullPointerException if {@code mapLeft} is null
     */
    public <L1> Either<L1, R> mapLeft(
        Function<? super L, ? extends L1> mapLeft);

    /**
     * Produces a new result by applying a mapping to a right-hand value.
     * Left-hand values are returned unchanged.
     * @param mapRight the mapping function for right-hand values
     * @param <R1> the new right-hand value type
     * @return the old left-hand value, or the new right-hand value
     * @throws NullPointerException if {@code mapRight} is null
     */
    public <R1> Either<L, R1> mapRight(
        Function<? super R, ? extends R1> mapRight);

    /**
     * Produces a new result by applying a mapping to a left-hand value.
     * Right-hand values are returned unchanged.
     * @param mapLeft the mapping function for left-hand values
     * @param <L1> the new left-hand value type
     * @return the new left-hand value, or the old right-hand value
     * @throws NullPointerException if {@code mapLeft} is null
     */
    public <L1> Either<L1, R> flatMapLeft(
        Function<? super L, ? extends Either<? extends L1, ? extends R>>
        mapLeft);

    /**
     * Produces a new result by applying a mapping to a right-hand value.
     * Left-hand values are returned unchanged.
     * @param mapRight the mapping function for right-hand values
     * @param <R1> the new right-hand value type
     * @return the old left-hand value, or the new right-hand value
     * @throws NullPointerException if {@code mapRight} is null
     */
    public <R1> Either<L, R1> flatMapRight(
        Function<? super R, ? extends Either<? extends L, ? extends R1>>
        mapRight);

    /**
     * Computes a result of a shared type.
     * @param fromLeft the mapping function for left-hand values
     * @param fromRight the mapping function for right-hand values
     * @return the result of applying the appropriate mapping function
     * @param <T> the common result type
     */
    public <T> T compute(Function<? super L, ? extends T> fromLeft,
        Function<? super R, ? extends T> fromRight);

    /**
     * Returns a stream with the left-hand value (if available(.
     * If this value contains a left-hand value, return a
     * stream with the value, otherwise an empty stream is returned.
     * @return the stream
     */
    public Stream<L> leftStream();

    /**
     * Returns a stream with the right-hand value (if available(.
     * If this value contains a right-hand value, return a
     * stream with the value, otherwise an empty stream is returned.
     * @return the stream
     */
    public Stream<R> rightStream();

    /**
     * Returns a comparator which sort error values last.
     * @param <L> the type of the left-hand values (must be comparable)
     * @param <R> the type of the right-hand values (must be comparable)
     * @return a comparator
     */
    @SuppressWarnings("unchecked")
    public static
        <L extends Comparable<? super L>, R extends Comparable<? super R>>
        Comparator<Either<L, R>> leftFirst() {
        return EitherNaturalOrder.LEFT_FIRST;
    }

    /**
     * Returns a comparator which sort values before errors.
     * The comparison uses the specified comparators.
     * @param left the comparator for left-hand values
     * @param right the comparator for right-hand values
     * @param <L> the type of the left-hand values
     * @param <R> the type of the right-hand values
     * @return a comparator
     */
    public static <L, R> Comparator<Either<L, R>> leftFirst(
        Comparator<? super L> left, Comparator<? super R> right) {
        return EitherNaturalOrder.comparator(left, right, -1);
    }

    /**
     * Returns a comparator which sort errors before values.
     * The comparison uses the natural ordering of values and errors.
     * @param <L> the type of the left-hand values (must be comparable)
     * @param <R> the type of the right-hand values (must be comparable)
     * @return a comparator
     */
    @SuppressWarnings("unchecked")
    public static
        <L extends Comparable<? super L>, R extends Comparable<? super R>>
        Comparator<Either<L, R>> rightFirst() {
        return EitherNaturalOrder.RIGHT_FIRST;
    }

    /**
     * Returns a comparator which sort errors before values.
     * The comparison uses the specified comparators.
     * @param left the comparator for left-hand values
     * @param right the comparator for right-hand values
     * @param <L> the type of the left-hand values
     * @param <R> the type of the right-hand values
     * @return a comparator
     */
    public static <L, R> Comparator<Either<L, R>> rightFirst(
        Comparator<? super L> left, Comparator<? super R> right) {
        return EitherNaturalOrder.comparator(left, right, 1);
    }

    /**
     * Result of a collector operation on {@link Either} objects.
     *
     * In general, collectors operating on streams of {@link Either}s
     * produce errors and values at the same time.  Such results are
     * represented by this class.
     *
     * @param <L> the type of left-hand values
     * @param <R> the type of right-hand values
     * @see Collectors
     */
    public static final class Collected<L, R> {
        /**
         * Creates new pair of collected values.
         * @param leftValues left-hand values
         * @param rightValues he right-hand values
         * @throws NullPointerException if any of the arguments is null
         */
        public Collected(L leftValues, R rightValues) {
            this.leftValues = Objects.requireNonNull(
                leftValues, "leftValues");
            this.rightValues = Objects.requireNonNull(
                rightValues, "rightValues");
        }

        /**
         * Returns the values.
         * @return the values, as passed to the constructor
         */
        public L leftValues() {
            return leftValues;
        }

        /**
         * Returns the errors.
         * @return the errors, as passed to the constructor
         */
        public R rightValues() {
            return rightValues;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Collected<?, ?> other))
                return false;
            return leftValues.equals(other.leftValues)
                && rightValues.equals(other.rightValues);
        }

        @Override
        public int hashCode() {
            return leftValues.hashCode() * 17 + rightValues.hashCode();
        }

        @Override
        public String toString() {
            return String.format("[left=%s, right=%s]", leftValues, rightValues);
        }

        private final L leftValues;
        private final R rightValues;
    }

    /**
     * Collectors for use with streams of {@link Either} objects.
     * @see Collector
     */
    public static final class Collectors {
        /**
         * Returns a collector producing a pair of collections.
         *
         * For example, to obtain a collector which produces a set of
         * left-hand and a list of right-hand values (which could be
         * errors), this invocation can be used:
         *
         * <pre>
         *     Collectors.toCollection(HashSet::new, ArrayList::new)
         * </pre>
         * @param leftFactory the supplier for collections
         *   storing left-hand values
         * @param rightFactory the supplier for collections
         *   storing right-hand values
         * @param <L> the type of left-hand values
         * @param <R> the type of right-hand values
         * @param <CL> the collection type for left-hand values
         * @param <CR> the collection type for right-hand values
         * @return a collector producing a pair of collections
         */
        public static <L, R, CL extends Collection<L>, CR extends Collection<R>>
            Collector<Either<L, R>, ?, Collected<CL, CR>> toCollection(
                Supplier<CL> leftFactory, Supplier<CR> rightFactory) {
            return new EitherCollectionCollector<>(leftFactory, rightFactory);
        }

        private Collectors() { } // No instances.
    }
}
