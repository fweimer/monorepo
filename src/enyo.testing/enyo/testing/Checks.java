package enyo.testing;

import java.util.Arrays;
import java.util.Collection;
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

    /**
     * Returns a check for an {@code int} array containing the
     * specified elements.
     * @param values the expected array elements
     * @return a check for {@code int} arrays
     * @throws NullPointerException if {@code values} is null
     */
    public static Check<int[]> intArrayEquals(int... values) {
        Objects.requireNonNull(values, "values");
        return (int[] array) -> {
            if (Arrays.equals(array, values))
                return null;
            else {
                String check;
                if (array == null)
                    check = "intArrayEquals (not null)";
                else
                    check = "intArrayEquals";
                Object[] boxed = new Object[values.length];
                for (int i = 0; i < values.length; ++i)
                    boxed[i] = values[i];
                return new Check.Failure(check, boxed);
            }
        };
    }

    /**
     * Returns a check that tests if a collection is null.
     * @param <T> type of the collection elements
     * @param <C> type of the collection
     * @return a check for collections
     */
    public static <T, C extends Collection<T>> Check<C> empty() {
        return collection -> {
            if (collection == null)
                return new Check.Failure("empty (not null)");
            else if (collection.isEmpty())
                return null;
            else
                return new Check.Failure("empty");
        };
    }

    private Checks() { }
}
