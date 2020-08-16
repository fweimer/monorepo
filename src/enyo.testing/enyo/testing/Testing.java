package enyo.testing;

import java.util.function.Consumer;

/**
 * Testing control objects passed to individual test cases.
 */
public interface Testing {
    /***
     * Throws {@link TestAlreadyFailed} if a test failure has happened.
     * This can be used to prevent the execution of subsequent tests.
     *
     * @throws TestAlreadyFailed if a test failure has already occurred
     */
    void fullBarrier();


    /**
     * Returns an examiner for the specified object reference.
     * @param <T> the type of the object being examined
     * @param object the object reference being examined
     * @return an examiner for the object
     */
    <T> Examiner<T> examine(T object);

    /**
     * Returns an examiner for an {@code int} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    Examiner.OfInt examine(int value);

    /**
     * Returns an examiner for an {@code long} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    Examiner.OfLong examine(long value);

    /**
     * Returns an examiner for an {@code double} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    Examiner.OfDouble examine(double value);

    /**
     * Returns an examiner for an {@code char} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    Examiner.OfChar examine(char value);

    /**
     * Logs a test as unsupported.  Does not act as a barrier, and
     * does not count as a test failure.
     * @param message a brief explanation of what is unsupported about this test
     */
    void unsupported(String message);

    /**
     * Reports a test failure using an exception.
     * It acts as a {@linkplain #fullBarrier() full test barrier}.
     * @param message the message to include in the test failure exception
     * @return nothing; this method never returns normally
     * @param <T> the type of the object that is never returned
     * @see #failedCheck(String,Object[])
     */
    <T> T assertFail(String message);

    /**
     * Checks that a flag is true.
     * An exception is thrown if it is false.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param flag the flag to check
     */
    void assertTrue(boolean flag);

    /**
     * Checks that a flag is true.
     * An exception is thrown if it is false.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param flag the flag to check
     */
    @Deprecated
    void assertTrue(String message, boolean flag);

    /**
     * Checks that a flag is false.
     * An exception is thrown if it is true.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param flag the flag to check
     */
    void assertFalse(boolean flag);

    /**
     * Checks that a flag is false.
     * An exception is thrown if it is true.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param flag the flag to check
     */
    @Deprecated
    void assertFalse(String message, boolean flag);

    /**
     * Checks that the reference is null.
     * An exception is thrown if it is not.
     * This function acts as a {@linkplain #fullBarrier() full test barrier}.
     * @param obj the reference to check
     */
    @Deprecated
    void assertNull(Object obj);

    /**
     * Checks that the reference is null.
     * An exception is thrown if it is not.
     * This function acts as a {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param obj the reference to check
     */
    @Deprecated
    void assertNull(String message, Object obj);

    /**
     * Checks that the two objects are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    void assertEquals(Object left, Object right);

    /**
     * Checks that the two objects are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    void assertEquals(String message, Object left, Object right);

    /**
     * Checks that the two objects are not equal.
     * An exception is thrown if they are equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    void assertNotEquals(Object left, Object right);

    /**
     * Checks that the two arrays are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Arrays#equals(Object[],Object[])}, or if
     * the array arguments are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first array
     * @param right the second array
     */
    void assertArrayEquals(Object[] left, Object[] right);

    /**
     * Checks that the two byte arrays are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Arrays#equals(byte[],byte[])}, or if
     * the array arguments are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first array
     * @param right the second array
     */
    void assertByteArrayEquals(byte[] left, byte[] right);

    /**
     * Checks that the two objects are the same.
     * An exception is thrown if they are not the same, or if
     * they are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    void assertSame(Object left, Object right);

    /**
     * Checks that the two objects are not the same.
     * An exception is thrown if they are the same, or if
     * they are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    void assertNotSame(Object left, Object right);

    /**
     * Checks that an expected exception is thrown.
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param consumer a callback which is invoked for further
     *   checks on the exception object
     * @param <E> the type of the expected exception
     */
    <E extends Throwable> void expect(ThrowingRunnable source,
        Class<E> exception, Consumer<? super E> consumer);

    /**
     * Checks that an expected exception is thrown.
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param <E> the type of the expected exception
     */
    <E extends Throwable> void expect(
        ThrowingRunnable source, Class<E> exception);

    /**
     * Checks that an expected exception is thrown.
     * @param label an informative string included in failure messages
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param consumer a callback which is invoked for further
     *   checks on the exception object
     * @param <E> the type of the expected exception
     */
    <E extends Throwable> void expect(String label, ThrowingRunnable source,
        Class<E> exception, Consumer<? super E> consumer);

    /**
     * Checks that an expected exception is thrown.
     * @param label an informative string included in failure messages
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param <E> the type of the expected exception
     */
    <E extends Throwable> void expect(String label, ThrowingRunnable source,
        Class<E> exception);

    /**
     * Reports a failed check.  Unlike {@link #assertFail(String)},
     * execution continues after this test failure.
     * Good check descriptions describe the test expectation
     * (e.g., <i>argument is an even integer</i>), not the cause of the test
     * failure (which might be <i>argument is odd</i> in the example).
     * @param check a short string identifying the failed check (not null)
     * @param objects object references related to the test failure (not null,
     *   may contain null elements)
     * @see #assertFail(String)
     */
    void failedCheck(String check, Object... objects);

    /**
     * Creates a new counter with the default target, 1.
     * @return the counter
     * @see Counter#Counter()
     * @see #counter(int)
     */
    default Counter counter() {
        return new Counter(this, 1);
    }

     /**
     * Creates a new counter with an explicit target value.
     * @param target the requested target value
     * @return the counter
     * @throws IllegalArgumentException if the argument is negative
     * @see #counter()
     * @see Counter#Counter(int)
     */
    default Counter counter(int target) {
        if (target < 0)
            throw new IllegalArgumentException("target: " + target);
        return new Counter(this, target);
    }
}
