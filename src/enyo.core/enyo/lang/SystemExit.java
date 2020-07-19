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
     * The requested exit status.
     * @see #status()
     */
    private final int status;

    private static final long serialVersionUID = 0xe9f9dcc1a3c91f09L;
}
