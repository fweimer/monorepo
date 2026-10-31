package enyo.testing;

/**
 * A callback which is expected to throw an exception.
 * @param <V> the type of the values produced
 * @see ThrowingRunnable
 * @see java.util.concurrent.Callable
 */
@FunctionalInterface
public interface ThrowingCallable<V> {
    /**
     * A callback throwing an arbitrary exception.
     * @return the value produced
     * @throws Throwable an arbitrary exception
     */
    V call() throws Throwable;
}
