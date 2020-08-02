package enyo.testing.introspect;

import java.util.concurrent.Semaphore;

/**
 * Constrains the resources which are used in parallel.
 *
 * The manager determines the available system resources when created,
 * and then tries to avoid overscheduling by limiting parallel
 * resource consumption in {@link #withResources(Resources,Action)}.
 *
 * @see TestResources
 */
public final class ResourcesManager {
    /**
     * Creates a new resources manager.
     * The available resources are the system resources.
     * @see TestResources#systemResources()
     */
    public ResourcesManager() {
        available = TestResources.systemResources();
        threads = new Semaphore(available.threads(), true);
        memory = new Semaphore(available.memory(), true);
    }

    /**
     * Callback functional interface used by
     * {@link #withResources(Resources,Action)}.
     *
     * @param <E> the type of exceptions raised by
     * the {@linkplain #action() action}
     */
    @FunctionalInterface
    public interface Action<E extends Throwable> {
        /**
         * Called by {@link #withResources(Resources,Action)} to
         * perform an action.
         * @throws E if an error occurred
         */
        void action() throws E;
    }

    /**
     * Executes the action, blocking until sufficient resources become
     * available.  For those resources which exceed all the available
     * resources, the {@linkplain #available() maximum amount} is
     * acquired, and then the action is executed opportunistically, in
     * the hope that the resources are sufficient anyway.
     *
     * @param <E> the type of exceptions raised by the
     *   {@linkplain Action#action() action}
     * @param resources the amount of resources needed by the action
     * @param action the action to perform
     * @throws E if an error occurs executing the action
     * @throws InterruptedException if the thread was interrupted while
     *   waiting for resources to become available
     * @throws NullPointerException if any of  the arguments are null
     */
    public <E extends Throwable> void withResources(
        TestResources resources, Action<E> action)
        throws E, InterruptedException {
        int threadsNeeded = Math.min(resources.threads(), available.threads());
        int memoryNeeded = Math.min(resources.memory(), available.memory());

        threads.acquire(threadsNeeded);
        try {
            memory.acquire(memoryNeeded);
            try {
                action.action();
            } finally {
                memory.release(memoryNeeded);
            }
        } finally {
            threads.release(threadsNeeded);
        }
    }

    /**
     * Returns the resources determined at creation time.
     * @return the resources
     * @see TestResources#systemResources()
     */
    public TestResources available() {
        return available;
    }

    @Override
    public String toString() {
        return String.format("ResourcesManager: threads=%d/%d memory=%d/%d",
            available.threads() - threads.availablePermits(),
            available.threads(),
            available.memory() - memory.availablePermits(),
            available.memory());
    }

    private final TestResources available;
    private final Semaphore threads;
    private final Semaphore memory;
}
