package enyo.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Helper for implementing pass-through serialization checks.
 */
final class SerializationHelper {
    private SerializationHelper() {} // No instances.

    /**
     * Passes the object through serialization.
     * No checks are performed on the deserialized object.
     * @param obj the object to serialize and deserialize
     * @param writeException called on a write exception to inject
     *   the exception into the result type
     * @param readException called on a read exception to inject
     *   the exception into the result type
     * @param result called after a complete round-trip to inject
     *   the deserialized object into the result type
     */
    static <T extends Serializable, U> U passThrough(T obj,
        Function<Throwable, U> writeException,
        Function<Throwable, U> readException,
        Function<Object, U> result) {
        Sentinel sentinel = new Sentinel();
        Object backSentinel;
        Object backObj;
        try {
            var out = new ByteArrayOutputStream();
            try (var objOut = new ObjectOutputStream(out)) {
                try {
                    objOut.writeObject(obj);
                    objOut.writeObject(sentinel);
                } catch (IOException e) {
                    return writeException.apply(e);
                }
            }
            try (var objIn = new PassThroughInputStream(
                    new ByteArrayInputStream(out.toByteArray()),
                    obj.getClass().getClassLoader())) {
                try {
                    backObj = objIn.readObject();
                    backSentinel = objIn.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    return readException.apply(e);
                }
            }
        } catch (IOException e) {
            // These errors cannot happen in practice, so they are not
            // represented in the interface..
            throw new UncheckedIOException(e);
        }
        if (!sentinel.equals(backSentinel))
            // This is difficult to trigger, so it is not represented
            // in the interface.
            throw new AssertionError("Sentinel mismatch: " + backSentinel);
        return result.apply(backObj);
    }

    /**
     * Marker class for checking serialization stream state in
     * {@link SerializationHelper#passThrough(Serializable,Function,Function,Function)}.
     */
    private static final class Sentinel extends Object
        implements Serializable {
        private final long id = COUNTER.getAndIncrement();

        @Override
        public boolean equals(Object o) {
            return o instanceof Sentinel obj && obj.id == id;
        }

        @Override
        public int hashCode() {
            return (int) id;
        }

        private static final AtomicLong COUNTER = new AtomicLong();
        private static final long serialVersionUID = 0xfca6399a876ff5ebL;
    }

    /**
     * Object input stream for serialization checking.
     *
     * The class loader needs to be specified explicitly, otherwise
     * {@link SerializationHelper#passThrough(Serializable,Function,Function,Function)}
     * will pick the wrong class loader because the first non-system
     * class loader on the call stack belongs to {@link
     * SerializationHelper} and not the class under test.
     */
    private static final class PassThroughInputStream
        extends ObjectInputStream {
        private final ClassLoader loader;

        private PassThroughInputStream(InputStream in, ClassLoader loader)
            throws IOException {
            super(in);
            this.loader = loader;
        }

        @Override
        public Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
            if (loader != null) {
                try {
                    return Class.forName(desc.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                    // fall through
                }
            }
            return super.resolveClass(desc);
        }
    }
}
