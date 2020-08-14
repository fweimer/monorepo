package enyo.testing.runtime;

/**
 * Describes the phase in which an exception occurred.
 * @see Results#exception(TestMethodLocator,ExceptionPhase,String,Throwable,Class)
 */
public enum ExceptionPhase {
    /**
     * Lookup of the test clase.
     * @see enyo.testing.introspect.TestMethodLocator#className()
     */
    CLASS_LOOKUP,

    /**
     * Lookup of the test clase.
     * @see enyo.testing.introspect.TestMethodLocator#methodName()
     */
    METHOD_LOOKUP,

    /**
     * Construction of the test object instance.
     * (This is not needed for static test methods.)
     */
    SETUP,

    /**
     * Execution of the test itself.
     * Only exceptions that propagate out of the test method
     * are attributed to this phase.
     */
    EXECUTION,

    /**
     * An exception during an expected-exception test.
     * @see enyo.testing.Testing#expect(enyo.testing.ThrowingRunnable,Class)
     */
    EXCEPTION_TEST,
    ;
}
