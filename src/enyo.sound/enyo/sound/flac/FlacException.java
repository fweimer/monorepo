package enyo.sound.flac;

/**
 * Errors during FLAC processing.
 */
public final class FlacException extends RuntimeException {
    /**
     * Creates a new FLAC exception with the specified message.
     * @param message the message for the new exception
     */
    public FlacException(String message) {
        super(message);
    }

    /**
     * Creates a new FLAC exception with the specified message and cause.
     * @param message the message for the new exception
     * @param cause the cause of the new exception
     */
    public FlacException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0xaca7fa33178ca077L;
}
