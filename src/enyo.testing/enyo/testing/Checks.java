package enyo.testing;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
     * Returns a check for the active region of a {@link ByteBuffer}
     * containing the specified elements.
     * @param values the expected buffer contents
     * @return a check for {@link ByteBuffer} objects
     * @throws NullPointerException if {@code values} is null
     */
    public static Check<ByteBuffer> byteBufferEquals(byte... values) {
        Objects.requireNonNull(values, "values");
        return (ByteBuffer buffer) -> {
	    if (buffer == null)
		return new Check.Failure("byteBufferEquals (not null)", values);
	    boolean ok = values.length == buffer.remaining();
	    if (ok)
		for (int i = 0; i < values.length; ++i)
		    if (buffer.get(buffer.position() + i) != values[i]) {
			ok = false;
			break;
		    }
	    if (ok)
		return null;
	    else
		return new Check.Failure("byteBufferEquals", values);
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
     * Returns a check that tests if a collection contains a specific object.
     * @param reference the object whose presence in the collection is checked
     * @param <T> type of the collection elements
     * @param <C> type of the collection
     * @return a check for collections
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T, C extends Collection<T>> Check<C> contains(T reference) {
        Objects.requireNonNull(reference);
        return collection -> {
            if (collection == null)
                return new Check.Failure("contains (not null)", reference);
            else if (collection.contains(reference))
                return null;
            else
                return new Check.Failure("contains", reference);
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
     * Returns a check that tests if an {@code int} value is less
     * than a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code int} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfInt lessThan(int reference) {
        return value -> {
            if (value < reference)
                return null;
            else
                return new Check.Failure("lessThan", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code int} value is less than
     * or equal to a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code int} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfInt lessThanOrEquals(int reference) {
        return value -> {
            if (value <= reference)
                return null;
            else
                return new Check.Failure("lessThanOrEquals", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code int} value is greater
     * than a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code int} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfInt greaterThan(int reference) {
        return value -> {
            if (value > reference)
                return null;
            else
                return new Check.Failure("greaterThan", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code int} value is greater
     * than or equal to a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code int} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfInt greaterThanOrEquals(int reference) {
        return value -> {
            if (value >= reference)
                return null;
            else
                return new Check.Failure("greaterThanOrEquals", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code int} value falls within
     * a range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (exclusive)
     * @return a check for an {@code int} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfInt inRange(int low, int high) {
        if (low > high)
            throw new IllegalArgumentException(
                "inRange: low end greater than high end");
        return value -> {
            if (value < low)
                return new Check.Failure("inRange low", low, high);
            else if (value >= high)
                return new Check.Failure("inRange high", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code int} value falls within
     * a closed range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (inclusive)
     * @return a check for an {@code int} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfInt inRangeClosed(int low, int high) {
        if (low > high)
            throw new IllegalArgumentException(
                "inRangeClosed: low end greater than high end");
        return value -> {
            if (value < low)
                return new Check.Failure("inRangeClosed low", low, high);
            else if (value > high)
                return new Check.Failure("inRangeClosed high", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code int} value does not fall
     * within a range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (exclusive)
     * @return a check for an {@code int} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfInt notInRange(int low, int high) {
        if (low > high)
            throw new IllegalArgumentException(
                "notInRange: low end greater than high end");
        return value -> {
            if (low <= value && value < high)
                return new Check.Failure("notInRange", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code int} value does not fall
     * within a closed range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (inclusive)
     * @return a check for an {@code int} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfInt notInRangeClosed(int low, int high) {
        if (low > high)
            throw new IllegalArgumentException(
                "notInRangeClosed: low end greater than high end");
        return value -> {
            if (low <= value && value <= high)
                return new Check.Failure("notInRangeClosed", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code long} value is less
     * than a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code long} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfLong lessThan(long reference) {
        return value -> {
            if (value < reference)
                return null;
            else
                return new Check.Failure("lessThan", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code long} value is less than
     * or equal to a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code long} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfLong lessThanOrEquals(long reference) {
        return value -> {
            if (value <= reference)
                return null;
            else
                return new Check.Failure("lessThanOrEquals", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code long} value is greater
     * than a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code long} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfLong greaterThan(long reference) {
        return value -> {
            if (value > reference)
                return null;
            else
                return new Check.Failure("greaterThan", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code long} value is greater
     * than or equal to a specified value.
     * @param reference the reference value for this check
     * @return a check for an {@code long} value
     * @throws NullPointerException if the reference is null
     */
    public static Check.OfLong greaterThanOrEquals(long reference) {
        return value -> {
            if (value >= reference)
                return null;
            else
                return new Check.Failure("greaterThanOrEquals", reference);
        };
    }

    /**
     * Returns a check that tests if an {@code long} value falls within
     * a range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (exclusive)
     * @return a check for an {@code long} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfLong inRange(long low, long high) {
        if (low > high)
            throw new IllegalArgumentException(
                "inRange: low end greater than high end");
        return value -> {
            if (value < low)
                return new Check.Failure("inRange low", low, high);
            else if (value >= high)
                return new Check.Failure("inRange high", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code long} value falls within
     * a closed range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (inclusive)
     * @return a check for an {@code long} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfLong inRangeClosed(long low, long high) {
        if (low > high)
            throw new IllegalArgumentException(
                "inRangeClosed: low end greater than high end");
        return value -> {
            if (value < low)
                return new Check.Failure("inRangeClosed low", low, high);
            else if (value > high)
                return new Check.Failure("inRangeClosed high", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code long} value does not fall
     * within a range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (exclusive)
     * @return a check for an {@code long} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfLong notInRange(long low, long high) {
        if (low > high)
            throw new IllegalArgumentException(
                "notInRange: low end greater than high end");
        return value -> {
            if (low <= value && value < high)
                return new Check.Failure("notInRange", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an {@code long} value does not fall
     * within a closed range.
     * @param low the lower bound of the range (inclusive)
     * @param high the lower bound of the range (inclusive)
     * @return a check for an {@code long} value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException
     *   if {@code low} is greater than {@code high}
     */
    public static Check.OfLong notInRangeClosed(long low, long high) {
        if (low > high)
            throw new IllegalArgumentException(
                "notInRangeClosed: low end greater than high end");
        return value -> {
            if (low <= value && value <= high)
                return new Check.Failure("notInRangeClosed", low, high);
            else
                return null;
        };
    }

    /**
     * Returns a check that tests if an array's elements are in
     * increasing order.
     * The natural ordering according to {@link Comparable} is used.
     * Elements which are equal are permitted.
     * @param <T> the type of the array elements
     * @return a check for arrays
     * @see #strictlyMonotonicArray()
     * @see #monotonicArray(Comparator)
     * @see #monotonic()
     */
    public static <T extends Comparable<T>> Check<T[]> monotonicArray() {
        return value -> {
            for (int i = 1; i < value.length; ++i)
                if (value[i - 1].compareTo(value[i]) > 0)
                    return new Check.Failure("monotonicArray",
                        i - 1, value[i - 1], value[i]);
            return null;
        };
    }

    /**
     * Returns a check that tests if an array's elements are in
     * strictly increasing order.
     * The natural ordering according to {@link Comparable} is used.
     * Elements which are equal not permitted.
     * @param <T> the type of the array elements
     * @return a check for arrays
     * @see #monotonicArray()
     * @see #strictlyMonotonicArray(Comparator)
     * @see #strictlyMonotonic()
     */
    public static <T extends Comparable<T>> Check<T[]>
        strictlyMonotonicArray() {
        return value -> {
            for (int i = 1; i < value.length; ++i)
                if (value[i - 1].compareTo(value[i]) >= 0)
                    return new Check.Failure("strictlyMonotonicArray",
                        i - 1, value[i - 1], value[i]);
            return null;
        };
    }

    /**
     * Returns a check that tests if an array's elements are in
     * increasing order.
     * Elements which are equal are permitted.
     * @param <T> the type of the array elements
     * @param comparator the comparator that determines the ordering
     * @return a check for arrays
     * @throws NullPointerException if the argument is null
     * @see #strictlyMonotonicArray(Comparator)
     * @see #monotonicArray()
     * @see #monotonic(Comparator)
     */
    public static <T> Check<T[]> monotonicArray(
        Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return value -> {
            for (int i = 1; i < value.length; ++i)
                if (comparator.compare(value[i - 1], value[i]) > 0)
                    return new Check.Failure("monotonicArray",
                        i - 1, value[i - 1], value[i]);
            return null;
        };
    }

    /**
     * Returns a check that tests if an array's elements are in
     * strictly increasing order.
     * Elements which are equal are not permitted.
     * @param <T> the type of the array elements
     * @param comparator the comparator that determines the ordering
     * @return a check for arrays
     * @throws NullPointerException if the argument is null
     * @see #monotonicArray(Comparator)
     * @see #strictlyMonotonicArray()
     * @see #strictlyMonotonic(Comparator)
     */
    public static <T> Check<T[]> strictlyMonotonicArray(
        Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return value -> {
            for (int i = 1; i < value.length; ++i)
                if (comparator.compare(value[i - 1], value[i]) >= 0)
                    return new Check.Failure("strictlyMonotonicArray",
                        i - 1, value[i - 1], value[i]);
            return null;
        };
    }

    /**
     * Returns a check that tests if an iterable is in increasing order.
     * The natural ordering according to {@link Comparable} is used.
     * Elements which are equal are permitted.
     * @param <T> the type of elements over which the iteration happens
     * @return a check for iterables
     * @see #strictlyMonotonic()
     * @see #monotonic(Comparator)
     * @see #monotonicArray()
     */
    public static <T extends Comparable<T>> Check<Iterable<T>> monotonic() {
        return value -> {
            Iterator<T> it = value.iterator();
            if (!it.hasNext())
                return null;
            T previous = it.next();
            int i = 0;
            while (it.hasNext()) {
                T current = it.next();
                if (previous.compareTo(current) > 0)
                    return new Check.Failure("monotonic",
                        i, previous, current);
                previous = current;
                ++i;
            }
            return null;
        };
    }

    /**
     * Returns a check that tests if an iterable is in strictly
     * increasing order.  The natural ordering according to {@link
     * Comparable} is used.  Elements which are equal are not
     * permitted.
     * @param <T> the type of elements over which the iteration happens
     * @return a check for iterables
     * @see #monotonic()
     * @see #strictlyMonotonic(Comparator)
     * @see #strictlyMonotonicArray()
     */
    public static <T extends Comparable<T>> Check<Iterable<T>>
        strictlyMonotonic() {
        return value -> {
            Iterator<T> it = value.iterator();
            if (!it.hasNext())
                return null;
            T previous = it.next();
            int i = 0;
            while (it.hasNext()) {
                T current = it.next();
                if (previous.compareTo(current) >= 0)
                    return new Check.Failure("strictlyMonotonic",
                        i, previous, current);
                previous = current;
                ++i;
            }
            return null;
        };
    }

    /**
     * Returns a check that tests if an iterable is in increasing order.
     * Elements which are equal are permitted.
     * @param <T> the type of elements over which the iteration happens
     * @param comparator the comparator that determines the ordering
     * @return a check for iterables
     * @throws NullPointerException if the argument is null
     * @see #strictlyMonotonic(Comparator)
     * @see #monotonic()
     * @see #monotonicArray(Comparator)
     */
    public static <T> Check<Iterable<T>> monotonic(
        Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return value -> {
            Iterator<T> it = value.iterator();
            if (!it.hasNext())
                return null;
            T previous = it.next();
            int i = 0;
            while (it.hasNext()) {
                T current = it.next();
                if (comparator.compare(previous, current) > 0)
                    return new Check.Failure("monotonic",
                        i, previous, current);
                previous = current;
                ++i;
            }
            return null;
        };
    }

    /**
     * Returns a check that tests if an iterable is in strictly
     * increasing order.  Elements which are equal are not permitted.
     * @param <T> the type of elements over which the iteration happens
     * @param comparator the comparator that determines the ordering
     * @return a check for iterables
     * @throws NullPointerException if the argument is null
     * @see #monotonic(Comparator)
     * @see #strictlyMonotonic()
     * @see #strictlyMonotonicArray(Comparator)
     */
    public static <T> Check<Collection<T>> strictlyMonotonic(
        Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return value -> {
            Iterator<T> it = value.iterator();
            if (!it.hasNext())
                return null;
            T previous = it.next();
            int i = 0;
            while (it.hasNext()) {
                T current = it.next();
                if (comparator.compare(previous, current) >= 0)
                    return new Check.Failure("strictlyMonotonic",
                        i, previous, current);
                previous = current;
                ++i;
            }
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

    /**
     * Returns a check that verifies that objects are serializable.
     * @param <T> the type of the object to serialize
     * @return a check that verifies that the object comes back as
     *   {@linkplain Object#equals(Object) equal} to the original object,
     *   and that its {@linkplain Object#hashCode() hash code} is unchanged
     */
    public static <T extends Serializable> Check<T> serialize() {
        return obj -> {
            if (obj == null)
                return null;
            return SerializationHelper.passThrough(obj,
                e -> new Check.Failure("serialize (write)", e),
                e -> new Check.Failure("serialize (read)", e),
                backObj -> {
                    if (!obj.equals(backObj))
                        return new Check.Failure(
                            "serialize (equals)", backObj);
                    if (obj.hashCode() != backObj.hashCode())
                        return new Check.Failure(
                            "serialize (hashCode)", backObj);
                    return null;
                });
        };
    }

    private Checks() { }
}
