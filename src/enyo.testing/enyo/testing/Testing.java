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
    public void fullBarrier();


    /**
     * Returns an examiner for the specified object reference.
     * @param <T> the type of the object being examined
     * @param object the object reference being examined
     * @return an examiner for the object
     */
    public <T> Examiner<T> examine(T object);

    /**
     * Returns an examiner for an {@code int} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    public Examiner.OfInt examine(int value);

    /**
     * Returns an examiner for an {@code long} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    public Examiner.OfLong examine(long value);

    /**
     * Returns an examiner for an {@code double} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    public Examiner.OfDouble examine(double value);

    /**
     * Returns an examiner for an {@code char} value.
     * @param value the value that is being examined
     * @return an examiner for the value
     */
    public Examiner.OfChar examine(char value);

    /**
     * Logs a test as unsupported.  Does not act as a barrier, and
     * does not count as a test failure.
     * @param message a brief explanation of what is unsupported about this test
     */
    public void unsupported(String message);

    /**
     * Reports a test failure using an exception.
     * It acts as a {@linkplain #fullBarrier() full test barrier}.
     * @param message the message to include in the test failure exception
     * @return nothing; this method never returns normally
     * @param <T> the type of the object that is never returned
     */
    public <T> T assertFail(String message);

    /**
     * Checks that a flag is true.
     * An exception is thrown if it is false.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param flag the flag to check
     */
    public void assertTrue(boolean flag);

    /**
     * Checks that a flag is true.
     * An exception is thrown if it is false.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param flag the flag to check
     */
    public void assertTrue(String message, boolean flag);

    /**
     * Checks that a flag is false.
     * An exception is thrown if it is true.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param flag the flag to check
     */
    public void assertFalse(boolean flag);

    /**
     * Checks that a flag is false.
     * An exception is thrown if it is true.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param flag the flag to check
     */
    public void assertFalse(String message, boolean flag);

    /**
     * Checks that the reference is null.
     * An exception is thrown if it is not.
     * This function acts as a {@linkplain #fullBarrier() full test barrier}.
     * @param obj the reference to check
     */
    @Deprecated
    public void assertNull(Object obj);

    /**
     * Checks that the reference is null.
     * An exception is thrown if it is not.
     * This function acts as a {@linkplain #fullBarrier() full test barrier}.
     * @param message an information string to include in the error message
     * @param obj the reference to check
     */
    @Deprecated
    public void assertNull(String message, Object obj);

    /**
     * Checks that the two objects are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    public void assertEquals(Object left, Object right);

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
    public void assertEquals(String message, Object left, Object right);

    /**
     * Checks that the two objects are not equal.
     * An exception is thrown if they are equal according to
     * {@link java.util.Objects#equals(Object,Object)}.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    public void assertNotEquals(Object left, Object right);

    /**
     * Checks that the two arrays are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Arrays#equals(Object[],Object[])}, or if
     * the array arguments are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first array
     * @param right the second array
     */
    public void assertArrayEquals(Object[] left, Object[] right);

    /**
     * Checks that the two byte arrays are equal.
     * An exception is thrown if they are not equal according to
     * {@link java.util.Arrays#equals(byte[],byte[])}, or if
     * the array arguments are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first array
     * @param right the second array
     */
    public void assertByteArrayEquals(byte[] left, byte[] right);

    /**
     * Checks that the two objects are the same.
     * An exception is thrown if they are not the same, or if
     * they are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    public void assertSame(Object left, Object right);

    /**
     * Checks that the two objects are not the same.
     * An exception is thrown if they are the same, or if
     * they are null.
     * This function acts as {@linkplain #fullBarrier() full test barrier}.
     * @param left the first object
     * @param right the second object
     */
    @Deprecated
    public void assertNotSame(Object left, Object right);

    /**
     * Checks that an expected exception is thrown.
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param consumer a callback which is invoked for further
     *   checks on the exception object
     * @param <E> the type of the expected exception
     */
    public <E extends Throwable> void expect(
        ThrowingRunnable source, Class<E> exception,
        Consumer<? super E> consumer);

    /**
     * Checks that an expected exception is thrown.
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param <E> the type of the expected exception
     */
    public <E extends Throwable> void expect(
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
    public <E extends Throwable> void expect(String label,
        ThrowingRunnable source, Class<E> exception,
        Consumer<? super E> consumer);

    /**
     * Checks that an expected exception is thrown.
     * @param label an informative string included in failure messages
     * @param source the callback that is expected to throw
     * @param exception the type of the expected exception
     * @param <E> the type of the expected exception
     */
    public <E extends Throwable> void expect(String label,
        ThrowingRunnable source, Class<E> exception);
}
