package enyo.util;

import java.util.NoSuchElementException;

/**
 * An attempt to unwrap a non-value {@linkplain Result result}.
 *
 * This exception is thrown if {@link Result#orElseThrow()} is called
 * on a result which does not contain an error.
 */
public final class ResultException extends NoSuchElementException {
    /**
     * Creates a new exception with the default message.
     */
    ResultException() {
    }

    /**
     * Creates a new exception with the specified message.
     * @param message the message to use for the new exception
     */
    ResultException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 0x5d8e45ee8d30fca6L;
}
