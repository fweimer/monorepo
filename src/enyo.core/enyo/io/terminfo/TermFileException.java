package enyo.io.terminfo;

/**
 * Parsing error in a terminfo file.
 *
 * @see TermFile
 */
public final class TermFileException extends RuntimeException {
    /**
     * Creates a new terminal file parsing exception.
     * @param message the description of the parsing problem
     */
    public TermFileException(String message) {
        super(message);
    }

    /**
     * Creates a new terminal file parsing exception.
     * @param message the description of the parsing problem
     * @param cause the underlying exception
     */
    public TermFileException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0xd9cc2b7a8b2ce251L;
}
