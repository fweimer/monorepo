package enyo.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates whether the virtual machine may exit during testing.
 *
 * Some tests run for an extended period of time, but perform
 * computations only which do not involve externally visible effects
 * such as file system changes (for temporary test files and
 * directories).  This means that it is safe to terminate the virtual
 * machine while the testing is running.  A test with this annotation
 * with a true {@linkplain #value() value} indicates to the test
 * framework that it should not wait for the test to terminate before
 * exiting.
 *
 * <p>If this annotation is applied to a class, it applies to all
 * tests defined in the class, unless these tests have their own
 * annotations.
 *
 * @see System#exit(int)
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ExitDuringTest {
    /**
     * If true, the virtual machine may exit during testing.
     *
     * @return false if this test prevents the virtual machine from
     *   terminating while the test is executing, false if not
     */
    boolean value() default true;
}
