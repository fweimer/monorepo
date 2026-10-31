package enyo.testing;

/**
 * A callback which can throw any exception.
 * @see ThrowingCallable
 * @see Runnable
 */
@FunctionalInterface
public interface ThrowingRunnable {
    /**
     * A callback throwing an arbitrary exception.
     * @throws Throwable an arbitrary exception
     */
    void run() throws Throwable;
}
