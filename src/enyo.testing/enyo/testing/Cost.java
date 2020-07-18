package enyo.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Cost of running a test.
 *
 * This information is to run long-running tests first on some test
 * queues if a sufficient number of test queues, using something like
 * Johnson's rule.  See {@link Resources} for ways to avoid
 * overscheduling.
 *
 * If this annotation is applied to a class, it applies to all test
 * methods in that class.
 *
 * @see Resources
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Cost {
    /**
     * Cost of running the test.  The scale is somewhat arbitrary, but
     * as a rule, it shoud correspond to the wall clock time on a
     * reasonably-sized machine.  Note that this refers to wall-clock
     * time, so it includes timeouts.  In the absence of an
     * annotation, {@link #DEFAULT} is assumed.
     *
     * @return a non-negative value indicating how expensive it is to
     *   run the test
     */
    double value();

    /**
     * Default cost of an unannotated test.
     *  The value is {@value #DEFAULT}.
     */
    static final double DEFAULT = 0.125;
}
