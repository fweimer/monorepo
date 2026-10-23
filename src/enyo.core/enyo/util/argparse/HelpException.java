package enyo.util.argparse;

/**
 * Exception thrown by {@link ArgParser} to indicate an help request
 * from the user.
 *
 * @see ArgParser#parse(String[])
 */
public final class HelpException extends RuntimeException {
    HelpException() { }

    private static final long serialVersionUID = 0xc22da35094c40612L;
}
