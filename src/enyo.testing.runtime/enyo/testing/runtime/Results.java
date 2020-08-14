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
     * Reports an unsupported test.
     * @param test the test that is unsupported or has an unsupported aspect
     * @param context a short, explanatory message
     */
    void unsupported(TestMethodLocator test, String context);

    /**
     * Reports a sub-test failure.
     * Failures involving thrown exceptions should use
     * {@link #exception(TestMethodLocator,List,ExceptionPhase,Throwable,Class)}
     * instead.
     * @param test the test that failed (not null)
     * @param check the type of failed check, a short string (not null)
     * @param examined the examination context for this check; can be empty
     *   (but not null)
     * @param otherObjects other objects involved in the check; can be empty
     *   (but not null), elements can be null
     * @param stackTrace a pruned stack trace of the failure; can be empty
     *   (but not null)
     */
    void failure(TestMethodLocator test, List<Examined> examined,
        String check, Object[] otherObjects,
        List<StackWalker.StackFrame> stackTrace);

    /**
     * Reports a failure related to an unexpected exception.
     * @param test the test that failed (not null)
     * @param context a list of examined values providing contextual
     *   information (not null)
     * @param phase when the exception was encountered (not null)
     * @param exception the exception encountered (not null)
     * @param expected if another exception was expected instead
     *   (can be null)
     */
    void exception(TestMethodLocator test, List<Examined> context,
        ExceptionPhase phase, Throwable exception,
        Class<? extends Throwable> expected);

    /**
     * Invoked at the end of the test to indicate success or failure.
     * However, if the testing object is used incorrectly by
     * still-running threads, further test failures maybe reported.
     * @param test the test that passed (not null)
     * @param success true if the test passed, false if there were failes
     */
    void completed(TestMethodLocator test, boolean success);
}
