package enyo.util.argparse;

/**
 * Configuration error detected during argument parser construction.
 *
 * The constructors of the {@link ArgParser} class throw this exception
 * when they detect that the specified record type does not allow
 * the construction of a valid argument parser.
 *
 * @see ArgParser
 */
public final class ConfigurationException extends RuntimeException {
    ConfigurationException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 0xa33fd4acd639a2aeL;
}
