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
     * The array type is not checked.  For this, see
     * {@link Examiner#isExactClass(Class)}.
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
     * Returns a check for an {@code byte} array containing the
     * specified elements.
     * @param values the expected array elements
     * @return a check for {@code byte} arrays
     * @throws NullPointerException if {@code values} is null
     */
    public static Check<byte[]> byteArrayEquals(byte... values) {
        Objects.requireNonNull(values, "values");
        return (byte[] array) -> {
            if (Arrays.equals(array, values))
                return null;
            else {
                String check;
                if (array == null)
                    check = "byteArrayEquals (not null)";
                else
                    check = "byteArrayEquals";
                return new Check.Failure(check, values);
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
                return new Check.Failure(check, values);
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

    /**
     * Returns a check that tests if a comparable object is less
     * than a specified object.
     * @param reference the reference object for this check
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if the reference is null
     */
    public static <T extends Comparable<T>> Check<T> lessThan(T reference) {
        Objects.requireNonNull(reference);
        return obj -> {
            if (obj == null)
                return new Check.Failure("lessThan (not null)", reference);
            else if (obj.compareTo(reference) < 0)
                return null;
            else
                return new Check.Failure("lessThan", reference);
        };
    }

    /**
     * Returns a check that tests if a comparable object is less than
     * or equal to a specified object.
     * @param reference the reference object for this check
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if the reference is null
     */
    public static <T extends Comparable<T>> Check<T> lessThanOrEquals(
        T reference) {
        Objects.requireNonNull(reference);
        return obj -> {
            if (obj == null)
                return new Check.Failure(
                    "lessThanOrEquals (not null)", reference);
            else if (obj.compareTo(reference) <= 0)
                return null;
            else
                return new Check.Failure("lessThanOrEquals", reference);
        };
    }

    /**
     * Returns a check that tests if a comparable object is greater
     * than a specified object.
     * @param reference the reference object for this check
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if the reference is null
     */
    public static <T extends Comparable<T>> Check<T> greaterThan(T reference) {
        Objects.requireNonNull(reference);
        return obj -> {
            if (obj == null)
                return new Check.Failure("greaterThan (not null)", reference);
            else if (obj.compareTo(reference) > 0)
                return null;
            else
                return new Check.Failure("greaterThan", reference);
        };
    }

    /**
     * Returns a check that tests if a comparable object is greater
     * than or equal to a specified object.
     * @param reference the reference object for this check
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if the reference is null
     */
    public static <T extends Comparable<T>> Check<T> greaterThanOrEquals(
        T reference) {
        Objects.requireNonNull(reference);
        return obj -> {
            if (obj == null)
                return new Check.Failure(
                    "greaterThanOrEquals (not null)", reference);
            else if (obj.compareTo(reference) >= 0)
                return null;
            else
                return new Check.Failure("greaterThanOrEquals", reference);
        };
    }

    /**
     * Returns a check that tests if a comparable object falls within
     * a range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (exclusive)
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static <T extends Comparable<T>> Check<T> inRange(T low, T high) {
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(high, "high");
        if (low.compareTo(high) > 0)
            throw new IllegalArgumentException(
                "inRange: low end greater than high end");
        return obj -> {
            if (obj == null)
                return new Check.Failure("inRange (not null)", low, high);
            else if (obj.compareTo(low) < 0)
                return new Check.Failure("inRange low", low, high);
            else if (obj.compareTo(high) >= 0)
                return new Check.Failure("inRange high", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if a comparable object falls within
     * a closed range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (inclusive)
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static <T extends Comparable<T>> Check<T> inRangeClosed(
        T low, T high) {
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(high, "high");
        if (low.compareTo(high) > 0)
            throw new IllegalArgumentException(
                "inRangeClosed: low end greater than high end");
        return obj -> {
            if (obj == null)
                return new Check.Failure("inRangeClosed (not null)", low, high);
            else if (obj.compareTo(low) < 0)
                return new Check.Failure("inRangeClosed low", low, high);
            else if (obj.compareTo(high) > 0)
                return new Check.Failure("inRangeClosed high", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if a comparable object does not fall
     * within a range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (exclusive)
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static <T extends Comparable<T>> Check<T> notInRange(T low, T high) {
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(high, "high");
        if (low.compareTo(high) > 0)
            throw new IllegalArgumentException(
                "notInRange: low end greater than high end");
        return obj -> {
            if (obj == null)
                return new Check.Failure("notInRange (not null)", low, high);
            else if (obj.compareTo(low) >= 0 && obj.compareTo(high) < 0)
                return new Check.Failure("notInRange", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if a comparable object does not fall
     * within a closed range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (inclusive)
     * @param <T> the type of the object being checked
     * @return a check for an object reference
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static <T extends Comparable<T>> Check<T> notInRangeClosed(
        T low, T high) {
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(high, "high");
        if (low.compareTo(high) > 0)
            throw new IllegalArgumentException(
                "notInRangeClosed: low end greater than high end");
        return obj -> {
            if (obj == null)
                return new Check.Failure(
                    "notInRangeClosed (not null)", low, high);
            else if (obj.compareTo(low) >= 0 && obj.compareTo(high) <= 0)
                return new Check.Failure("notInRangeClosed", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if a string has the specified prefix.
     * @param prefix the prefix string to check for
     * @return a check for a string
     * @throws NullPointerException if {@code prefix} is null
     */
    public static Check<String> startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix");
        return string -> {
            if (string == null)
                return new Check.Failure("startsWith (not null)", prefix);
            else if (string.startsWith(prefix))
                return null;
            else
                return new Check.Failure("startsWith", prefix);
        };
    }

    /**
     * Returns a check that tests if a string has the specified suffix.
     * @param suffix the suffix string to check for
     * @return a check for a string
     * @throws NullPointerException if {@code suffix} is null
     */
    public static Check<String> endsWith(String suffix) {
        Objects.requireNonNull(suffix, "suffix");
        return string -> {
            if (string == null)
                return new Check.Failure("endsWith (not null)", suffix);
            else if (string.endsWith(suffix))
                return null;
            else
                return new Check.Failure("endsWith", suffix);
        };
    }

    private Checks() { }
}
