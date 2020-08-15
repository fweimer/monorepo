package enyo.testing;

import java.util.Arrays;
import java.util.Objects;

/**
 * Factories for {@link Check} objects.
 */
public final class Checks {
    /**
     * Returns a check for an array containing the specified elements.
     * @param objects the expected array elements (which can be null)
     * @param <T> the type of array elements
     * @return a check for arrays
     * @throws NullPointerException if {@code objects} is null
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Check<T[]> arrayEquals(T... objects) {
        Objects.requireNonNull(objects, "objects");
        return (T[] array) -> {
            if (Arrays.equals(array, objects))
                return null;
            else {
                String check;
                if (array == null)
                    check = "arrayEquals (not null)";
                else
                    check = "arrayEquals";
                return new Check.Failure(check,
                    // Make a copy of the array as an Object[] array,
                    // to avoid leaking the type through the test framework.
                    Arrays.copyOf(objects, objects.length, Object[].class));
            }
        };
    }

    private Checks() { }
}
