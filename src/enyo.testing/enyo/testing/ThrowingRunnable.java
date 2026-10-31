package enyo.testing;

/**
 * A callback which is expected to throw an exception.
 * @see Runnable
 */
@FunctionalInterface
public interface ThrowingRunnable {
    /**
     * A callback throwing an arbitrary exception.
     * The test framework calls this method to throw the exception
     * expected by the test.  It is expected that this method
     * never returns normally.
     * @see Testing#expect(ThrowingRunnable,Class)
     * @see Testing#expect(ThrowingRunnable,Class)
     * @see Testing#expect(String,ThrowingRunnable,Class)
     * @see Testing#expect(String,ThrowingRunnable,Class)
     * @throws Throwable the expected exception
     */
    void run() throws Throwable;
}
