package enyo.io.terminfo;

/**
 * Parsing errors in terminal script.
 *
 * @see ScriptParser
 */
public final class TerminalScriptException extends RuntimeException {
    /**
     * Creates a new script parser exception.
     * @param message the description of the parsing problem
     */
    public TerminalScriptException(String message) {
        super(message);
    }

    /**
     * Creates a new script parser exception.
     * @param message the description of the parsing problem
     * @param cause the underlying exception
     */
    public TerminalScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0x69780b8c65ef4b59L;
}
