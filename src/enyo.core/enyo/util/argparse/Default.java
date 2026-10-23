package enyo.util.argparse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The default value of an option.
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
    /**
     * The default value of the option.
     * @return a string that can be parsed as the field's type
     */
    String value();
}
