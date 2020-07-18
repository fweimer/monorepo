package enyo.testing;

/**
 * A callback which is expected to throw an exception.
 */
@FunctionalInterface
public interface ThrowingAction {
    /**
     * A callback throwing an arbitrary exception.
     * The test framework calls this method to throw the exception
     * expected by the test.  It is expected that this method
     * never returns normally.
     * @see Testing#expect(ThrowingAction,Class)
     * @see Testing#expect(ThrowingAction,Class,java.util.funcion.Consumer)
     * @see Testing#expect(String,ThrowingAction,Class)
     * @see Testing#expect(String,ThrowingAction,Class,
     *   java.util.funcion.Consumer)
     * @throws Throwable the expected exception
     */
    void perform() throws Throwable;
}
