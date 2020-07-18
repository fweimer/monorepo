package enyo.testing;

/**
 * An {@linkplain java.util.function.Supplier supplier} which is expected
 * to throw an exception.
 * @param <T> the type of the value produced (if the supplier would succeed)
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    /**
     * A callback throwing an arbitrary exception.
     * The test framework calls this method to throw the exception
     * expected by the test.  It is expected that this method
     * never returns normally.
     * @return a value (which is never actually produced)
     * @see Testing#expect(ThrowingSupplier,Class)
     * @see Testing#expect(ThrowingSupplier,Class,java.util.funcion.Consumer)
     * @see Testing#expect(String,ThrowingSupplier,Class)
     * @see Testing#expect(String,ThrowingSupplier,Class,
     *   java.util.funcion.Consumer)
     * @throws Throwable the expected exception
     */
    T get() throws Throwable;
}
