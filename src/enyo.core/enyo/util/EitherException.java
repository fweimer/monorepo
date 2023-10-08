package enyo.util;

import java.util.NoSuchElementException;

/**
 * An attempt to unwrap a mismatching {@linkplain Either} value.
 *
 * This exception is thrown if {@link Either#leftOrElseThrow()}
 * or {@link Either#rightOrElseThrow()} is called
 * on a result which does not contain  the appropriate value.
 */
public final class EitherException extends NoSuchElementException {
    /**
     * Creates a new exception with the default message.
     */
    EitherException() {
    }

    /**
     * Creates a new exception with the specified message.
     * @param message the message to use for the new exception
     */
    EitherException(String message) {
        super(message);
    }

    static EitherException wrap(Object obj) {
        String message;
        try {
            message = obj.toString();
        } catch (Throwable e) {
            return new EitherException();
        }
        return new EitherException(message);
    }

    private static final long serialVersionUID = 0x5d8e45ee8d30fca7L;
}
