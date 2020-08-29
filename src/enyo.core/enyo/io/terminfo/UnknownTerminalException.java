package enyo.io.terminfo;

/**
 * Exception indicating that a terminal description could not be
 * loaded.
 * @see TermFile#load(String)
 */
public final class UnknownTerminalException extends RuntimeException {
    /**
     * Creates a new exception about an unknown terminal.
     * @param message the description the nature of the unknown terminal
     */
    public UnknownTerminalException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 0xc46c1e0ae11c764L;
}
