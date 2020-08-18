package enyo.testing;

import java.util.Arrays;
import java.util.Objects;

/**
 * A check for an object reference.
 * @param <T> the type of the objects that can be checked
 */
@FunctionalInterface
public interface Check<T> {
    /**
     * Performs a check on an object.
     * @param object the object to check
     * @return null if the check passes, or a {@link Failure} instance
     *   describing the failure
     */
    Failure test(T object);

    /**
     * A check for an {@code int} value.
     */
    @FunctionalInterface
    interface OfInt {
        /**
         * Performs a check on an {@code int} value.
         * @param value the value to check
         * @return null if the check passes, or a {@link Failure} instance
         *   describing the failure
         */
        Failure test(int value);
    }

    /**
     * A check for an {@code long} value.
     */
    @FunctionalInterface
    interface OfLong {
        /**
         * Performs a check on an {@code long} value.
         * @param value the value to check
         * @return null if the check passes, or a {@link Failure} instance
         *   describing the failure
         */
        Failure test(long value);
    }

    /**
     * Description of a {@link Check} failure.
     */
    static final class Failure {
        /**
         * Creates a new failure object.
         * Note that the objects array is not cloned, and may contain
         * null elements.
         *
         * <p>Good check descriptions describe the test expectation
         * (e.g., <i>argument is an even integer</i>), not the cause
         * of the test failure (which might be <i>argument is odd</i>
         * in the example).
         *
         * <p>The object that failed the check should not be included
         * in the {@code objects} array.  The test framework will
         * report the object as part of the test failure context.
         *
         * @param check a short description of the check (which should
         *   not depend on the value that failed the check)
         * @param objects additional objects to associate with the test failure
         * @throws NullPointerException if {@code check}
         *   or {@code objects} is null
         */
        public Failure(String check, Object... objects) {
            this.check = Objects.requireNonNull(check);
            this.objects = Objects.requireNonNull(objects);
        }

        /**
         * Returns a short, descriptive string for this failure.
         * @return a string
         */
        public String check() {
            return check;
        }

        /**
         * Returns additional objects associated with the failure.
         * Note that the array has not been cloned.
         * @return an array (possibly empty, but not null)
         */
        public Object[] objects() {
            return objects;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != Failure.class)
                return false;
            Failure other = (Failure) o;
            return check.equals(other.check)
                && Arrays.equals(objects, other.objects);
        }

        @Override
        public int hashCode() {
            return check.hashCode() * 17 + Arrays.hashCode(objects);
        }

        @Override
        public String toString() {
            return check;
        }

        private final String check;
        private final Object[] objects;
    }
}
