package enyo.lang;

import java.util.Objects;

/**
 * Unchecked wrapper for @link{IllegalAccessException} exceptions.
 */
public final class UncheckedIllegalAccessException extends RuntimeException {
    /**
     * Creates a new wrapping exception.
     * @param cause the exception to wrap
     * @throws NullPointerException if {@code cause} is null
     */
    public UncheckedIllegalAccessException(IllegalAccessException cause) {
        super(Objects.requireNonNull(cause, "cause"));
    }

    private static final long serialVersionUID = 0xbeccff25d126ba87L;
}
