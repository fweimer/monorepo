package enyo.util.argparse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-variable name used in option documentation.
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaVar {
    /**
     * The meta-variable name for the option's argument value.
     * @return the meta-variable name; upper-case letters should be used
     */
    String value();
}
