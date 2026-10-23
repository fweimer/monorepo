package enyo.util.argparse;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/**
 * Configuration error detected during argument parser construction.
 *
 * The constructors of the {@link ArgParser} class throw this exception
 * when they detect that the specified record type does not allow
 * the construction of a valid argument parser.
 *
 * @see ArgParser
 */
public final class ArgumentListException extends RuntimeException {
    ArgumentListException(
        ArgParser<?> parser, List<String> args, List<String> errors) {
        super(errors.get(0));
        this.parser = Objects.requireNonNull(parser, "parser");
        this.args = List.copyOf(args);
        this.errors = List.copyOf(errors);
    }

    /**
     * Returns the parser object that produced the parse failure.
     * @return a parser object
     */
    public ArgParser<?> getParser() {
        return parser;
    }

    /**
     * Returns the arugment list that led to the parse failure.
     * @return an immutable list of strings
     */
    public List<String> args() {
        return args;
    }

    /**
     * Returns the list of errors that led to the parse failure.
     * @return a non-empty immutable list of strings
     */
    public List<String> errors() {
        return errors;
    }

    /**
     * Writes a diagnostic message to the specified output stream.
     *
     * The process is not terminated after writing the messages.
     *
     * @param out the stream to which the diagnostic messages are written
     * @throws NullPointerException if {@code out} is null
     */
    public void write(PrintStream out) {
        for (String message : errors()) {
            out.print("error: ");
            out.println(message);
        }
    }

    private transient final ArgParser<?> parser;
    /**
     * The argument list that was parsed.
     */
    @SuppressWarnings("serial")
    private final List<String> args;
    /**
     * The errors that occured.
     */
    @SuppressWarnings("serial")
    private final List<String> errors;

    private static final long serialVersionUID = 0x1b78fd3333d13c18L;
}
