package enyo.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a module as containing tests.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.MODULE)
public @interface TestModule {
}
