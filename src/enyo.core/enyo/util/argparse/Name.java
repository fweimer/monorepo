package enyo.util.argparse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the name of an option.
 *
 * See  {@linkplain ArgParser##naming the default naming conventions}
 * for option naming in the absence of this annotation.
 *
 * This annotation can be repeated.  Positional arguments can only
 * have a single name.
 */
@Target(ElementType.RECORD_COMPONENT)
@Repeatable(Names.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {
    /**
     * The option name.  The name should start with {@code "--"} for
     * long options, and {@code "-"} for short options, and neither
     * for positional arguments.
     *
     * @return the option name
     */
    String value();
}
