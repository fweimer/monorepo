package enyo.lang;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Compute a class value with lookup restrictions.
 *
 * This class is similar to {@link ClassValue}, except that computing the
 * value depends on a lookup object for access.
 *
 * <h2 id="safety">Safety restrictions</h2>
 *
 * If {@link #get(MethodHandles.Lookup,Class)} returns a previously
 * computed value, it only checks that the type is accessible using
 * the specified lookup.  This means the computed value should only
 * give access to public elements of the class.  For example, the
 * computed value could be a serialization function that eventually
 * invokes the canonical constructor of the specified record type.
 *
 * <p>If further access checks are needed, they need to be performed
 * before calling {@link #get(MethodHandles.Lookup,Class)}, and access
 * to the {@code LookupClassValue} needs to be restricted.
 *
 * @param <T> the type of the computed value
 * @see ClassValue
 */
public final class LookupClassValue<T> {
    /**
     * A callback for computing class values with lookup restrictions.
     *
     * @param <T> the type of the computed value
     * @see LookupClassValue#of(Supplier)
     * @see ClassValue#computeValue(Class)
     */
    public interface Supplier<T> {
        /**
         * Compute the class value for the specified type.
         *
         * The implementation should use the lookup for privileged access.
         *
         * <p>This method can be called multiple times for a single class
         * if {@link LookupClassValue#get(MethodHandles.Lookup,Class)}
         * is invoked concurrently.
         *
         * <p>See the @{link LookupClassValue}
         * {@linkplain LookupClassValue##safet safety requirements}
         * for how access conveyed by the lookup should be reflected
         * in the returned value.
         *
         * @param lookup the access capability for processing the type
         * @param type the type for which the value is computed
         * @return the computed value, which should not be null
         * @throws UncheckedIllegalAccessException if the lookup does not grant
         *   access to the type
         * @throws NullPointerException any of the arguments is null
         * @see ClassValue#computeValue(Class)
         */
        T computeValue(MethodHandles.Lookup lookup, Class<?> type);
    }

    /**
     * Create a new set of cached class values, using the specified supplier.
     *
     * @param <T> the type of the computed values
     * @param supplier the source of the computed values
     * @return a new class value computation object
     * @throws NullPointerException if {@code supplier} is null
     */
    public static <T> LookupClassValue<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return new LookupClassValue<>(supplier);
    }

    /**
     * Compute the value of the specified class.
     *
     * @param lookup the access capability for processing the type
     * @param type the type for which the value is computed
     * @return the computed value, potentially cached from a previous
     *   invocation
     * @throws UncheckedIllegalAccessException if the lookup does not grant
     *   access to the type
     * @throws NullPointerException any of the arguments is null, or if
     *   the supplier specified with {@link #of(Supplier)} returned null
     * @throws RuntimeException any exception thrown by
     *   {@link Supplier#computeValue(MethodHandles.Lookup,Class)}.
     * @see ClassValue#get(Class)
     */
    public T get(MethodHandles.Lookup lookup, Class<?> type) {
        try {
            lookup.accessClass(type);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException(e);
        }

        // The implementation uses an AtomicReference as a placeholder/
        // A CAS is used to choose the unique winner among concurrently
        // executing computations.
        var ref = classValue.get(type);
        T val = ref.getAcquire();
        if (val != null)
            return val;
        val = supplier.computeValue(lookup, type);
        if (val == null)
            throw new NullPointerException("Supplier returned null");
        T witness = ref.compareAndExchange(null, val);
        if (witness != null)
            // CAS failed.  Return the other value.
            return witness;
        else
            // CAS succeeded.  Use the value computed above.
            return val;
    }

    private static final class RefClassValue<T>
        extends ClassValue<AtomicReference<T>> {
        @Override
        protected AtomicReference<T> computeValue(Class<?> type) {
            return new AtomicReference<T>();
        }
    }

    private LookupClassValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    private final RefClassValue<T> classValue = new RefClassValue<>();
    private final Supplier<T> supplier;
}
