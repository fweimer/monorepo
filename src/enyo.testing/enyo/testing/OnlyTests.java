package enyo.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restrict the set of tests in class that are run.
 *
 * Only the {@linkplain Test annotated tests} whose name matches any
 * of the {@linkplain #value() listed names} are actually run.  This
 * can be used during test debugging, to temporarily focus on a subset
 * of the tests.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface OnlyTests {
    /**
     * Restrict the set of executed tests to the tests named in the
     * array.
     * @return an array of test names
     * @see Test
     */
    String[] value();
}
