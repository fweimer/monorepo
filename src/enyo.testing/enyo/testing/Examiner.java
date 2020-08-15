package enyo.testing;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wrapper for an object reference that is being examined during a test.
 *
 * Examiners provide a more type-safe way to do object comparisons
 * because the {@link Testing#examine(Object)} function freezes the
 * type parameter, and the compiler will not infer a common base class
 * such as {@code Object} on a type mismatch, but rather report an
 * error.
 *
 * @param <T> the type of the object being examined
 * @see Testing#examine(Object)
 */
public interface Examiner<T> {
    /**
     * Sets the context string for the following checks.
     * @param context a message to be included in test failures
     * @return this object
     * @throws NullPointerException if {@code context} is null
     */
    Examiner<T> context(String context);

    /**
     * Issues a barrier.  If this examiner has recorded any test
     * failures, no further tests are performed through it.
     * @return this object
     */
    Examiner<T> barrier();

    /**
     * Derives a new examiner from this examiner.
     * @param map the function that converts this examiner's object
     *   to the object for the new examiner
     * @return the new examiner
     * @param <U> the object type of the new examiner
     * @see #map(Function,Consumer)
     */
    <U> Examiner<U> map(Function<? super T, ? extends U> map);

    /**
     * Derives a new examiner from this examiner.
     * Examination of the old value continues after the consumer
     * for the new examiner returns.
     * @param map the function that converts this examiner's object
     *   to the object for the new examiner
      *@param consumer the consumer that receives the new examiner
     * @return this object
     * @param <U> the object type of the new examiner
     * @see #map(Function)
     */
    <U> Examiner<T> map(Function<? super T, ? extends U> map,
        Consumer<Examiner<U>> consumer);

    /**
     * Derives a new examiner from this examiner, for a specific type.
     * If the current object cannot be assigned to {@code cls},
     * a test failure is reported.
     * Examination of the old value continues after the consumer
     * for the new examiner returns.
     * @param cls the expected type for this examiner's value
      *@param consumer the consumer that receives the new examiner
     * @return this object
     * @param <U> the object type of the new examiner
     * @see #map(Function)
     */
    <U extends T> Examiner<T> map(Class<U> cls, Consumer<Examiner<U>> consumer);

    /**
     * Inspects this examiner's value using a consumer.
     * Examination of the value continues after the consumer
     * returns.
     * @param consumer the consumer that receives this examiner's value
     * @return this object
     */
    Examiner<T> inspect(Consumer<? super T> consumer);

    /**
     * Inspects this examiner's value using a consumer.
     * Examination of the value continues after the consumer
     * returns.  If the value does not have type {@code cls}, a test
     * failure occurs.
     * @param cls the reified type for the consumer input
     * @param consumer the consumer that receives this examiner's value
     * @param <U> the expected type of the value
     * @return this object
     */
    <U extends T> Examiner<T> inspect(
        Class<U> cls, Consumer<? super U> consumer);

    /**
     * Reports an error if the two values are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * Errors are also reported if the references are null;
     * {@link #nullableIs(Object)} does not report an error in this case.
     * @param other the other value under comparison
     * @return this object
     */
    Examiner<T> is(T other);

    /**
     * Reports an error if the two values are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * No errors are reported if both references are null.
     * @param other the other value under comparison
     * @return this object
     * @see #is(Object)
     */
    Examiner<T> nullableIs(T other);

    /**
     * Reports an error if the two values are equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * Errors are also reported if the references are null.
     * @param other the other value under comparison
     * @return this object
     */
    Examiner<T> isnt(T other);

    /**
     * Reports an error if the two references are not the same.
     * Errors are also reported if the references are null.
     * @param other the other value under comparison
     * @return this object
     */
    Examiner<T> isSame(T other);

    /**
     * Reports an error if the two references are the same.
     * Errors are also reported if the references are null.
     * @param other the other value under comparison
     * @return this object
     */
    Examiner<T> isNotSame(T other);

    /**
     * Reports an error if the reference is not null.
     * @return this object
     */
    Examiner<T> isNull();

    /**
     * Reports an error if the reference is null.
     * @return this object
     */
    Examiner<T> isNotNull();

    /**
     * Reports an error if the checked object does not have the given
     * class.  It is still a failure if the object's class is a subclass
     * of the given class.
     * @param cls the expected class
     * @return this object
     */
    Examiner<T> isExactClass(Class<? extends T> cls);

    /**
     * Wrapper for an {@code int} value that is examined during a test.
     */
    interface OfInt {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        OfInt context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        OfInt barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfInt is(int other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfInt isnt(int other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        Examiner<Integer> boxed();
    }

    /**
     * Wrapper for a {@code long} value that is examined during a test.
     */
    interface OfLong {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        OfLong context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        OfLong barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfLong is(long other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfLong isnt(long other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        Examiner<Long> boxed();
    }

    /**
     * Wrapper for a {@code double} value that is examined during a test.
     */
    interface OfDouble {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        OfDouble context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        OfDouble barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfDouble is(double other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfDouble isnt(double other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        Examiner<Double> boxed();
    }

    /**
     * Wrapper for a {@code char} value that is examined during a test.
     */
    interface OfChar {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        OfChar context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        OfChar barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfChar is(char other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        OfChar isnt(char other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        Examiner<Character> boxed();
    }
}
