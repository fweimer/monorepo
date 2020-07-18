package enyo.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Resource consumption of a test.
 *
 * These data are used to avoid overscheduling and running out of
 * resources.  For an estimate of test execution time, see {@link Cost}.
 *
 * If this annotation is applied to a class, it applies to all test
 * methods in that class.  If no annotation applies, the default
 * property values are used.
 *
 * @see Cost
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Resources {
    /**
     * Approximation of the number of threads the test runs in parallel.
     * @return the thread count
     */
    int threads() default THREADS;

    /**
     * Approximation of the maximum live memory used by the test.
     *
     * The default value is arbitrary, but it should
     * cover most tests.  The value is small enough not to impede
     * parallelism with reasonable heap sizes on systems with many
     * processors.
     *
     * @return the memory used, in mibibytes
     */
    int memory() default MEMORY;

    /**
     * Default value of the {@link #threads()} property.
     */
    public static final int THREADS = 1;

    /**
     * Default value of the {@link #memory()} property.
     */
    public static final int MEMORY = 10;
}
