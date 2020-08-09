package enyo.lang;

import java.io.InvalidObjectException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Type-less exception information.
 *
 * This class extracts information from a throwable object and
 * discards its type.  No references to the original throwable object
 * or its class is retained.  As a result, objects of type
 * {@code ThrowableErasure} can safely be serialized and deserialized,
 * without depending on other non-platform classes, even for custom
 * exception types.
 *
 * <p>{@code ThrowableErasure} objects are immutable after
 * construction.  They have an identity and can form cycles (if the
 * original throwable object has cycles involving causes and
 * suppressed objects).
 *
 * @see Throwable
 */
public final class ThrowableErasure implements Serializable {
    /**
     * Creates a new erased throwable.
     *
     * @param throwable the object whose data is extracted
     */
    public ThrowableErasure(Throwable throwable) {
        this(new IdentityHashMap<>(), throwable);
    }

    /**
     * Returns the name of the throwable class.
     *
     * @return the class name
     * @see Class#getName()
     */
    public String className() {
        return className;
    }


    /**
     * Returns the stack trace for this throwable.
     *
     * The stack trace is empty if the call to
     * {@link Throwable#getStackTrace()} threw an exception.
     *
     * @return a list of stack trace elements (possibly empty)
     * @see Throwable#getStackTrace()
     */
    public List<StackTraceElement> stackTrace() {
        return stackTrace;
    }

    /**
     * Returns the message of the throwable.  If the message could not
     * be computed, {@code null} is returned, and {@link #messageError()}
     * returns true.
     *
     * @return the message, or {@code null} if it is not available
     * @see Throwable#getMessage()
     * @see #messageError()
     */
    public String message() {
        return message;
    }

    /**
     * Returns true if there was an error computing the message.
     *
     * @return true if an exception was raised during
     *   {@link Throwable#getMessage()}
     * @see #message()
     */
    public boolean messageError() {
        return messageError;
    }

    /**
     * Returns the cause of the throwable.  If the cause could not
     * be determined, {@code null} is returned, and {@link #causeError()}
     * returns true.
     *
     * @return the cause, or {@code null} if it is not available
     * @see Throwable#getCause()
     * @see #causeError()
     */
    public ThrowableErasure cause() {
        return cause;
    }

    /**
     * Returns true if there was an error computing the cause.
     *
     * @return true if an exception was raised during
     *   {@link Throwable#getCause()}
     * @see #cause()
     */
    public boolean causeError() {
        return causeError;
    }

    /**
     * Returns the suppressed throwable objects.
     * @return a list of throwables (possibly empty)
     * @see Throwable#getSuppressed()
     */
    public List<ThrowableErasure> suppressed() {
        return suppressed;
    }

    /**
     * Prints a stack trace for this throwable.
     *
     * The output format follows the format of
     * {@link Throwable#printStackTrace()}.
     *
     * @param s the writer to which the output is written
     * @see Throwable#printStackTrace(PrintWriter)
     */
    public void printStackTrace(PrintWriter s) {
        // Initial portion of the stack trace.
        s.println(toString());
        for (StackTraceElement e : stackTrace)
            printStackTraceElement(s, 1, e);

        if (cause != null || !suppressed.isEmpty()) {
            // Used to avoid printing repeated stack trace elements.
            HashSet<List<StackTraceElement>> stacks = new HashSet<>();
            for (int i = 0; i < stackTrace.size(); ++i)
                stacks.add(stackTrace.subList(i, stackTrace.size()));

            // Used to detected cycles.
            HashSet<ThrowableErasure> seen = new HashSet<>();
            seen.add(this);

            printCauseAndSuppressed(s, seen, 1, stacks);
        }
    }

    /**
     * Converts the throwable to a string.
     *
     * @return the original string conversion if available, otherwise
     *   a string comprised of the {@linkplain #className() class name}
     *   and the {@linkplain #message() message} (if available) is used
     * @see Throwable#toString()
     */
    @Override
    public String toString() {
        if (toString == null)
            return toStringDefault();
        else
            return toString;
    }

    /**
     * @see #className()
     */
    private final String className;

    /**
     * @see #stackTrace()
     */
    private final List<StackTraceElement> stackTrace;

    /**
     * @see #message()
     */
    private final String message;


    /**
     * @see #messageError()
     */
    private final boolean messageError;

    /**
     * @see #toString()
     */
    private final String toString;

    /**
     * @see #cause()
     */
    private final ThrowableErasure cause;

    /**
     * @see #causeError()
     */
    private final boolean causeError;

    /**
     * @see #suppressed()
     */
    private final List<ThrowableErasure> suppressed;

    private static final long serialVersionUID = 0x49466b664f2c1442L;

    private ThrowableErasure(
        IdentityHashMap<Throwable, ThrowableErasure> instances,
        Throwable exception) {
        instances.put(exception, this);
        className = exception.getClass().getName();
        suppressed = Arrays.stream(exception.getSuppressed())
            .map(e -> translate(instances, e))
            .collect(Collectors.toUnmodifiableList());

        // Deal with exceptions from methods that can be overridden.
        // toString() errors which are not getMessage() errors and
        // getStackTrace() errors are supposed to be rare, so there is
        // no error flag for them.

        {
            StackTraceElement[] stackTrace0;
            try {
                stackTrace0 = exception.getStackTrace();
            } catch (Throwable e) {
                stackTrace0 = null;
            }
            if (stackTrace0 == null)
                stackTrace = List.of();
            else
                stackTrace = List.of(stackTrace0);
        }

        {
            String message0;
            boolean error;
            try {
                message0 = exception.getMessage();
                error = false;
            } catch (Throwable e) {
                message0 = null;
                error = true;
            }
            message = message0;
            messageError = error;
        }

        {
            Throwable cause0;
            boolean error;
            try {
                cause0 = exception.getCause();
                error = false;
            } catch (Throwable e) {
                cause0 = null;
                error = true;
            }
            cause = translate(instances, cause0);
            causeError = error;
        }

        {
            String toString0;
            try {
                toString0 = exception.toString();
                if (toStringDefault().equals(toString0))
                    // No need to keep the string if it is canonical.
                    toString0 = null;
            } catch (Throwable e) {
                toString0 = null;
            }
            toString = toString0;
        }
    }

    private static ThrowableErasure translate(
        IdentityHashMap<Throwable, ThrowableErasure> instances,
        Throwable exception) {
        if (exception == null)
            return null;
        ThrowableErasure result = instances.get(exception);
        if (result != null)
            return result;
        return new ThrowableErasure(instances, exception);
    }

    /**
     * Checks that some class invariants are not violated.
     * @return this object
     * @throws InvalidObjectException if the class name is null,
     *   or if the stack traces and suppressed exception lists are
     *   not immutable lists
     */
    private Object readResolve() throws InvalidObjectException {
        Objects.requireNonNull(className, "className");
        if (List.copyOf(stackTrace) != stackTrace)
            throw new InvalidObjectException(
                "Modifiable stackTrace field: " + toString());
        if (List.copyOf(suppressed) != suppressed)
            throw new InvalidObjectException(
                "Modifiable suppressed field: " + toString());
        return this;
    }

    private void printStackTrace0(PrintWriter s,
        HashSet<ThrowableErasure> seen, int indentation,
        HashSet<List<StackTraceElement>> stacks) {
        if (!seen.add(this)) {
            // The line has already been indented by the caller.
            s.print("[CIRCULAR REFERENCE: ");
            s.print(toString());
            s.println(']');
            return;
        }

        s.println(toString());

        for (int i = 0; i < stackTrace.size(); ++i) {
            // Check for an already printed part of the stack trace.
            List<StackTraceElement> stack =
                stackTrace.subList(i, stackTrace.size());
            if (stacks.contains(stack)) {
                indent(s, indentation);
                s.print("... ");
                s.print(stack.size());
                s.println(" more");
                break;
            }
            printStackTraceElement(s, indentation, stackTrace.get(i));
        }

        printCauseAndSuppressed(s, seen, indentation, stacks);
    }

    private void printCauseAndSuppressed(PrintWriter s,
        HashSet<ThrowableErasure> seen, int indentation,
        HashSet<List<StackTraceElement>> stacks) {
        if (cause != null) {
            // Indentation stays the same.
            indent(s, indentation - 1);
            s.print("Caused by: ");
            cause.printStackTrace0(s, seen, indentation, stacks);
        }

        for (ThrowableErasure e : suppressed) {
            indent(s, indentation);
            s.print("Suppressed: ");
            e.printStackTrace0(s, seen, indentation + 1, stacks);
        }
    }

    private static void indent(PrintWriter s, int indentation) {
        while (indentation > 0) {
            s.print('\t');
            --indentation;
        }
    }

    private static void printStackTraceElement(PrintWriter s, int indentation,
        StackTraceElement e) {
        indent(s, indentation);
        s.print("at ");
        s.println(e.toString());
    }

    private String toStringDefault() {
        if (message != null)
            return className + ": " + message;
        else
            return className;
    }
}
