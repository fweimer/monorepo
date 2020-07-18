package enyo.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A set of strings with membership check based on hashcodes.
 *
 * This is similar to a {@link java.util.Set Set&lt;String>}, but it is
 * possible to look up up strings based on {@link ByteBuffer} slices.
 *
 * <p>For the The {@link ByteBuffer}-based lookup methods to work in
 * all cases, the string filter should contain only ASCII strings.
 */
public final class StringFilter {
    /**
     * Creates a new string filter containing the passed strings.
     * @param strings the strings which become members of the filter
     * @return a string filter containing these strings
     */
    public static StringFilter of(String... strings) {
        return new StringFilter(strings);
    }

    /**
     * Creates a new string filter containing the passed collection of
     * strings.
     * @param collection the strings which become members of the filter
     * @return a string filter containing these strings
     */
    public static StringFilter of(Collection<String> collection) {
        return new StringFilter(
            collection.toArray(new String[collection.size()]));
    }

    /**
     * Returns true if the filter contains the passed strings.
     * @param s the string to check
     * @return true if the filter contains the string, false if not
     */
    public boolean contains(String s) {
        return lookup(s) != null;
    }

    /**
     * Returns true if the bytes are contained in the string.
     * The buffer slice to check must be in an ASCII-transparent encoding
     * (such as {@linkplain java.nio.charset.StandardCharsets#UTF_8 UTF-8}).
     * @param buffer the source of the bytes
     * @param from first byte index to check
     * @param to one past the last byte to check
     * @return true if the filter contains the sub-string of the buffer,
     *   false if not
     */
    public boolean contains(ByteBuffer buffer, int from, int to) {
        return lookup(buffer, from, to) != null;
    }

    /**
     * Returns the stored string matching the passed string.
     * This is an interning operation.
     * @param s the string to look up
     * @return the string equal to the string argument, or null if the filter
     *   does not contain a matching string
     */
    public String lookup(String s) {
        int hashCode = s.hashCode();
        Object bucket = table[hashCode & (table.length - 1)];
        if (bucket == null)
            return null;
        if (bucket.getClass() == String.class) {
            String candidate = (String) bucket;
            if (candidate.hashCode() == hashCode && candidate.equals(s))
                return candidate;
        } else
            for (String candidate : (String[]) bucket)
                if (candidate.hashCode() == hashCode && candidate.equals(s))
                    return candidate;
        return null;
    }

    /**
     * Returns the stored string matching a buffer slice.
     * This is an interning operation.
     * The buffer slice to check must be in an ASCII-transparent encoding
     * (such as {@linkplain java.nio.charset.StandardCharsets#UTF_8 UTF-8}).
     * @param buffer the source of the bytes
     * @param from first byte index to look up
     * @param to one past the last byte to look up
     * @return the string matching the buffer slice, or null if the filter
     *   does not contain a matching string
     */
    public String lookup(ByteBuffer buffer, int from, int to) {
        Objects.checkFromToIndex(from, to, buffer.limit());
        int hashCode = 0;
        for (int i = from; i < to; ++i)
            hashCode = hashCode * 31 + buffer.get(i);
        Object bucket = table[hashCode & (table.length - 1)];
        if (bucket == null)
            return null;
        if (bucket.getClass() == String.class) {
            String candidate = (String) bucket;
            if (candidate.hashCode() == hashCode
                && matches(buffer, from, to, candidate))
                return candidate;
        } else
            for (String candidate : (String[]) bucket)
                if (candidate.hashCode() == hashCode
                    && matches(buffer, from, to, candidate))
                    return candidate;
        return null;
    }

    private final Object[] table;

    private StringFilter(String[] strings) {
        int log2 = 32 - Integer.numberOfLeadingZeros(strings.length);
        table = new Object[1 << log2];
        for (String s : strings) {
            int index = s.hashCode() & (table.length - 1);
            Object bucket = table[index];
            if (bucket == null)
                table[index] = s;
            else if (bucket.getClass() == String.class)
                table[index] = new String[] { (String) bucket, s };
            else {
                String[] oldBucket = (String[]) bucket;
                String[] newBucket = Arrays.copyOf(
                    oldBucket, oldBucket.length + 1);
                newBucket[oldBucket.length] = s;
                table[index] = newBucket;
            }
        }
    }

    private static boolean matches(ByteBuffer buffer, int from, int to,
        String candidate) {
        int length = candidate.length();
        for (int i = 0; i < length; ++i)
            // Non-ASCII bytes are negative and therefore never match.
            if (candidate.charAt(i) != buffer.get(from + i))
                return false;
        return true;
    }
}
