package enyo.testing;

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
    public Examiner<T> context(String context);

    /**
     * Issues a barrier.  If this examiner has recorded any test
     * failures, no further tests are performed through it.
     * @return this object
     */
    public Examiner<T> barrier();

    /**
     * Derives a new examiner from this examiner.
     * @param map the function that converts this examiner's object
     *   to the object for the new examiner
     * @return the new examiner
     * @param <U> the object type of the new examiner
     */
    public <U> Examiner<U> map(Function<? super T, ? extends U> map);

    /**
     * Reports an error if the two values are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * Errors are also reported if the references are null;
     * {@link #nullableIs(Object)} does not report an error in this case.
     * @param other the other value under comparison
     * @return this object
     */
    public Examiner<T> is(T other);

    /**
     * Reports an error if the two values are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * No errors are reported if both references are null.
     * @param other the other value under comparison
     * @return this object
     * @see #is(Object)
     */
    public Examiner<T> nullableIs(T other);

    /**
     * Reports an error if the two values are equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * Errors are also reported if the references are null.
     * @param other the other value under comparison
     * @return this object
     */
    public Examiner<T> isnt(T other);

    /**
     * Reports an error if the two references are not the same.
     * Errors are also reported if the references are null.
     * @param other the other value under comparison
     * @return this object
     */
    public Examiner<T> isSame(T other);

    /**
     * Reports an error if the two references are the same.
     * Errors are also reported if the references are null.
     * @param other the other value under comparison
     * @return this object
     */
    public Examiner<T> isNotSame(T other);

    /**
     * Reports an error if the reference is not null.
     * @return this object
     */
    public Examiner<T> isNull();

    /**
     * Reports an error if the reference is null.
     * @return this object
     */
    public Examiner<T> isNotNull();

    /**
     * Reports an error if the checked object does not have the given
     * class.  It is still a failure if the object's class is a subclass
     * of the given class.
     * @param cls the expected class
     * @return this object
     */
    public Examiner<T> isExactClass(Class<? extends T> cls);

    /**
     * Wrapper for an {@code int} value that is examined during a test.
     */
    public interface OfInt {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        public OfInt context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        public OfInt barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfInt is(int other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfInt isnt(int other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        public Examiner<Integer> boxed();
    }

    /**
     * Wrapper for a {@code long} value that is examined during a test.
     */
    public interface OfLong {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        public OfLong context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        public OfLong barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfLong is(long other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfLong isnt(long other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        public Examiner<Long> boxed();
    }

    /**
     * Wrapper for a {@code double} value that is examined during a test.
     */
    public interface OfDouble {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        public OfDouble context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        public OfDouble barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfDouble is(double other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfDouble isnt(double other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        public Examiner<Double> boxed();
    }

    /**
     * Wrapper for a {@code char} value that is examined during a test.
     */
    public interface OfChar {
        /**
         * Sets the context string for the following checks.
         * @param context a message to be included in test failures
         * @return this object
         * @throws NullPointerException if {@code context} is null
         */
        public OfChar context(String context);

        /**
         * Issues a barrier.  If this examiner has recorded any test
         * failures, no further tests are performed through it.
         * @return this object
         */
        public OfChar barrier();

        /**
         * Reports an error if the two values are not equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfChar is(char other);

        /**
         * Reports an error if the two values are equal.
         * @param other the other value under comparison
         * @return this object
         */
        public OfChar isnt(char other);

        /**
         * Returns an examiner for the boxed value.
         * @return a reference-based examiner
         */
        public Examiner<Character> boxed();
    }
}
