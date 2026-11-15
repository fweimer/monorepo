package enyo.linux.alsa;

/**
 * An exception for operations in the sound system.
 */
public class SoundException extends RuntimeException {
    /**
     * Creates a new sound subsystem exception.
     */
    public SoundException() {
    }

    /**
     * Creates a new sound subsystem exception with the specified message.
     * @param message a short message explaining the error
     */
    public SoundException(String message) {
        super(message);
    }

    /**
     * Creates a new sound subsystem exception with the specified
     * message and cause.
     * @param message a short message explaining the error
     * @param cause the exception that caused this exception
     */
    public SoundException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0x8ebe881e046dd29fL;
}
