package enyo.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Throwable wrapper of a {@link ThrowableErasure} object.
 *
 * This class allows to re-throw an erased throwable
 * (albeit not with the correct type).
 */
public final class ErasedThrowable extends RuntimeException {
    /**
     * Wraps the erasure in a throwable with similar characteristics.
     *
     * The new throwable shares some characteristics with the
     * erased throwable.
     * @param erasure the erasure to wrap
     * @throws NullPointerException if {@code erasure} is null
     */
    public ErasedThrowable(ThrowableErasure erasure) {
        super(erasure.message());
        this.erasure = erasure;
    }

    /**
     * Returns the erasure.
     * @return the erasure passed to
     *   {@link #ErasedThrowable(ThrowableErasure)}
     */
    public ThrowableErasure erasure() {
        return erasure;
    }

    /**
     * Returns the stack trace of the erasure.
     * @return the stack trace elements returned by
     *   {@link ThrowableErasure#stackTrace() erasure().stackTrace()}
     */
    @Override
    public StackTraceElement[] getStackTrace() {
        return erasure.stackTrace().toArray(new StackTraceElement[0]);
    }

    /**
     * Prints the stack trace using the erased throwable.
     *
     * @see ThrowableErasure#printStackTrace(PrintWriter)
     */
    @Override
    public void printStackTrace() {
        PrintWriter wr = new PrintWriter(System.err);
        erasure.printStackTrace(wr);
        wr.flush();
    }

    /**
     * Prints the stack trace using the erased throwable.
     *
     * @param s the stream to print to
     * @see ThrowableErasure#printStackTrace(PrintWriter)
     */
    @Override
    public void printStackTrace(PrintStream s) {
        PrintWriter wr = new PrintWriter(s);
        erasure.printStackTrace(wr);
        wr.flush();
    }

    /**
     * Prints the stack trace using the erased throwable.
     *
     * @param s the writer to print to
     * @see ThrowableErasure#printStackTrace(PrintWriter)
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        erasure.printStackTrace(s);
    }

    /**
     * Returns the erasued throwable as a string.
     *
     * @return the string
     *   {@link ThrowableErasure#toString() erasure().toString()}
     */
    @Override
    public String toString() {
        return erasure.toString();
    }

    /**
     * @see #erasure()
     */
    private final ThrowableErasure erasure;

    private static final long serialVersionUID = 0x775b89443bfe7585L;
}
