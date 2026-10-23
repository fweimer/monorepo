package enyo.lang;

/**
 * An exception that can be used to terminate the process.
 *
 * The advantage over calling {@link System#exit(int)} directly is
 * that this approach allows catching exit requests without a security
 * manager.
 */
public final class SystemExit extends RuntimeException {
    /**
     * Creates an new system exit exception.
     * @param status the exit status requested to be passed to
     *   {@link System#exit(int)}.
     */
    public SystemExit(int status) {
        this.status = status;
    }

    /**
     * Returns the requested exit status.
     * This exit status should be passed to {@link System#exit(int)}.
     * @return the exit status
     */
    public int status() {
        return status;
    }

    @Override
    public String getMessage() {
        return "Status: " + status;
    }

    /**
     * A callback function that can throw {@link SystemExit}.
     * @see CallableSubprocess
     * @see SystemExit#run(Subprocess)
     * @param <E> the type of the exceptions thrown by the callback
     */
    public interface Subprocess<E extends Throwable> {
        /**
         * Callback function invoked by {@link SystemExit#run(Subprocess)}.
         * @throws E an error occurred while running the callback
         */
        void run() throws E;
    }

    /**
     * Invokes an action which can terminate the virtual machine by
     * throwing {@code SystemExit}.
     * @param action the callback function to invoke
     * @param <E> the type of the exceptions thrown by the callback
     * @throws E an error occurred while running the callback
     * @see #call(CallableSubprocess)
     */
    public static <E extends Throwable> void run(Subprocess<E> action)
        throws E {
        try {
            action.run();
        } catch (SystemExit e) {
            System.exit(e.status);
        }
    }

    /**
     * A callback function that can throw {@link SystemExit} and
     * that returns a value.
     * @see Subprocess
     * @see SystemExit#call(CallableSubprocess)
     * @param <R> the return type of the callback
     * @param <E> the type of the exceptions thrown by the callback
     */
    public interface CallableSubprocess<R, E extends Throwable> {
        /**
         * Callback function invoked by
         * {@link SystemExit#call(CallableSubprocess)}.
         * @return the value that should be returned from
         *   {@link SystemExit#call(CallableSubprocess)}
         * @throws E an error occurred while running the callback
         */
        R call() throws E;
    }

    /**
     * Invokes an action which can terminate the virtual machine by
     * throwing {@code SystemExit}.
     * @param action the callback function to invoke
     * @param <R> the return type of the callback
     * @param <E> the type of the exceptions thrown by the callback
     * @return the value returned by {@code action.call()}
     * @throws E an error occurred while running the callback
     * @see #run(Subprocess)
     */
    public static <R, E extends Throwable> R call(
        CallableSubprocess<R, E> action)
        throws E {
        try {
            return action.call();
        } catch (SystemExit e) {
            System.exit(e.status);
            return null;        // Not reachable.
        }
    }

    /**
     * The requested exit status.
     * @see #status()
     */
    private final int status;

    private static final long serialVersionUID = 0xe9f9dcc1a3c91f09L;
}
