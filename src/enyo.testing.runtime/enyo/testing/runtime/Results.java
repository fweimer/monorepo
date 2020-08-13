package enyo.testing.runtime;

import enyo.testing.introspect.TestMethodLocator;

import java.util.List;

/**
 * Receiving reports of test result from the testing run time.
 *
 * Implementations must be thread safe.
 */
public interface Results {
    /**
     * Reports a sub-test failure.
     * Failures involving thrown exceptions should use
     * {@link #failure(TestMethodLocator,ExceptionPhase,String,Throwable,Class)}
     * instead.
     * @param test the test that failed (not null)
     * @param context a string providing contextual information
     *   (can be null)
     * @param check the type of failed check, a short string (not null)
     * @param values the objects involved in the check; can be empty
     *   (but not null)
     * @param stackTrace a pruned stack trace of the failure; can be empty
     *   (but not null)
     */
    void failure(TestMethodLocator test, String context, String check,
        List<? extends Object> values, List<StackWalker.StackFrame> stackTrace);

    /**
     * Reports a failure related to an unexpected exception.
     * @param test the test that failed (not null)
     * @param phase when the exception was encountered (not null)
     * @param context a string providing contextual information
     *   (can be null)
     * @param exception the exception encountered (not null)
     * @param expected if another exception was expected instead
     *   (can be null)
     */
    void failure(TestMethodLocator test, ExceptionPhase phase, String context,
        Throwable exception, Class<? extends Throwable> expected);

    /**
     * Invoked at the end of the test to indicate success or failure.
     * However, if the testing object is used incorrectly by
     * still-running threads, further test failures maybe reported.
     * @param test the test that passed (not null)
     * @param success true if the test passed, false if there were failes
     */
    void completed(TestMethodLocator test, boolean success);
}
