package enyo.util.argparse;

import java.util.function.Function;

/**
 * A converter from an argument string to an option value.
 */
public interface Converter extends Function<String, Object> {
}
