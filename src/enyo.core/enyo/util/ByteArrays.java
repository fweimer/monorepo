package enyo.util;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Byte array extensions.
 *
 * This class complements {@link java.util.Arrays} with additional
 * functionality for byte arrays.
 *
 * @see java.util.Arrays
 */
public final class ByteArrays {
    /**
     * Quotes a byte array.  The result can be parsed as a C string
     * (by implementations with an 8-bit {@code char} type) or Python
     * string.  Surrounding quotes are not included in the returned
     * string.  The quoting is compatible with both single and double
     * quotes.
     *
     * <p>The returned string is empty if the array is empty.  Actual
     * quoting (as opposed to mere conversion to ASCII) happened if
     * the length of the result is greater than the input array.
     *
     * <p><b>Note:</b> The quoting is not suitable for passing the
     * string to a shell.
     *
     * @param array the data to quote.
     * @return a string representing the byte array.
     * @see #quote(byte[], int, int)
     */
    public static String quote(byte[] array) {
        return quote0(array, 0, array.length);
    }

    /**
     * Quotes a byte array.  The bytes in the range {@code fromIndex}
     * (inclusive) to {@code toIndex} (exclusive) are are returned
     * in the result, quoted as if with the {@link #quote(byte[])} method.
     * The returned string equals the string
     * {@code quote(Arrays.copyOfRange(array, fromIndex, toIndex))}.
     *
     * @param array the data to quote.
     * @param fromIndex index of the first byte to include in the
     *  quoted string.
     * @param toIndex one past the last index to include in the
     *  quoted string.
     * @return a string representing the specified elements in the byte array.
     * @see #quote(byte[])
     */
    public static String quote(byte[] array, int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, array.length);
        return quote0(array, fromIndex, toIndex);
    }

    /**
     * Returns a sequential stream with the elements of the array.
     * The elements are converted to {@code int}s as with the implicit
     * conversion.
     *
     * @param array  the array with the source elements
     * @return an integer stream whose elements are between -128 and 127
     *   (inclusive)
     * @throws NullPointerException  @{code array} is null
     */
    public static IntStream signedStream(byte[] array) {
        return signedStream(array, 0, array.length);
    }

    /**
     * Returns a sequential stream with the elements of the array
     * whose indices range from {@code fromIndex} (inclusive) to
     * {@code toIndex} (exclusive).
     * The elements are converted to {@code int}s as with the implicit
     * conversion.
     *
     * @param array  the array with the source elements
     * @param fromIndex  the start index (inclusive)
     * @param toIndex  the end index (exclusive)
     * @return an integer stream whose elements are between -128 and 127
     *   (inclusive)
     * @throws NullPointerException  @{code array} is null
     * @throws ArrayIndexOutOfBoundsException  {@code fromIndex} or
     *   {@code toIndex} are out of bounds for {@code array}
     */
    public static IntStream signedStream(byte[] array,
        int fromIndex, int toIndex) {
        return StreamSupport.intStream(signedSpliterator(
                array, fromIndex, toIndex), false);
    }

    /**
     * Returns a spliterator with elements of the array.
     * The elements are converted to {@code int}s as with the implicit
     * conversion.
     *
     * @param array  the array with the source elements
     * @param fromIndex  the start index (inclusive)
     * @param toIndex  the end index (exclusive)
     * @return an integer spliterator whose elements are between -128 and 127
     *   (inclusive)
     * @throws NullPointerException  @{code array} is null
     * @throws ArrayIndexOutOfBoundsException  {@code fromIndex} or
     *   {@code toIndex} are out of bounds for {@code array}
     */
    public static Spliterator.OfInt signedSpliterator(byte[] array,
        int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex
            || toIndex > array.length)
            throw new ArrayIndexOutOfBoundsException();
        return new SignedByteArraySpliterator(array, fromIndex, toIndex);
    }

    /**
     * Returns a sequential stream with the elements of the array.
     * The elements are converted using {@link Byte#toUnsignedInt(byte)}.
     *
     * @param array  the array with the source elements
     * @return an integer stream whose elements are between -128 and 127
     *   (inclusive)
     * @throws NullPointerException  @{code array} is null
     */
    public static IntStream unsignedStream(byte[] array) {
        return unsignedStream(array, 0, array.length);
    }

    /**
     * Returns a sequential stream with the elements of the array
     * whose indices range from {@code fromIndex} (inclusive) to
     * {@code toIndex} (exclusive).
     * The elements are converted using {@link Byte#toUnsignedInt(byte)}.
     *
     * @param array  the array with the source elements
     * @param fromIndex  the start index (inclusive)
     * @param toIndex  the end index (exclusive)
     * @return an integer stream whose elements are between 0 and 255
     *   (inclusive)
     * @throws NullPointerException  @{code array} is null
     * @throws ArrayIndexOutOfBoundsException  {@code fromIndex} or
     *   {@code toIndex} are out of bounds for {@code array}
     */
    public static IntStream unsignedStream(byte[] array,
        int fromIndex, int toIndex) {
        return StreamSupport.intStream(unsignedSpliterator(
                array, fromIndex, toIndex), false);
    }

    /**
     * Returns a spliterator with elements of the array.
     * The elements are converted using {@link Byte#toUnsignedInt(byte)}.
     *
     * @param array  the array with the source elements
     * @param fromIndex  the start index (inclusive)
     * @param toIndex  the end index (exclusive)
     * @return an integer spliterator whose elements are between 0 and 255
     *   (inclusive)
     * @throws NullPointerException  @{code array} is null
     * @throws ArrayIndexOutOfBoundsException  {@code fromIndex} or
     *   {@code toIndex} are out of bounds for {@code array}
     */
    public static Spliterator.OfInt unsignedSpliterator(byte[] array,
        int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex
            || toIndex > array.length)
            throw new ArrayIndexOutOfBoundsException();
        return new UnsignedByteArraySpliterator(array, fromIndex, toIndex);
    }

    private static String quote0(byte[] array, int fromIndex, int toIndex) {
        if (fromIndex == toIndex)
            return "";
        else {
            // The result object is created on demand, once it is
            // evident that quoting is required.
            StringBuilder result = null;
            for (int i = fromIndex; i < toIndex; ++i) {
                byte b = array[i];
                if (b < ' ' || b == 127 || b == '\\' || b == '\''
                    || b == '"') {
                    if (result == null) {
                        result = new StringBuilder(toIndex - fromIndex + 4);
                        // Copy the bytes previously examined, which
                        // are all ASCII.
                        for (int j = fromIndex; j < i; ++j)
                            result.append((char) array[j]);
                    }
                    result.append('\\');
                    switch (b) {
                    case '\n':
                        result.append('n');
                        break;
                    case '\r':
                        result.append('r');
                        break;
                    case '\t':
                        result.append('t');
                        break;
                    case '\"':
                    case '\'':
                    case '\\':
                        result.append((char) b);
                        break;
                    default:
                        // Use an octal escape sequence because it is
                        // always three digits long, even if more
                        // digits follow.
                        result.append(
                            (char) ('0' + (Byte.toUnsignedInt(b) >> 6)));
                        result.append((char) ('0' + ((b >> 3) & 7)));
                        result.append((char) ('0' + (b & 7)));
                        break;
                    }
                } else {
                    // No quoting is required.
                    if (result != null)
                        result.append((char) b);
                }
            }

            if (result == null)
                // Use Latin-1 so that no further charset checks are
                // needed.  (The string is actually printable ASCII.)
                return new String(array, fromIndex, toIndex - fromIndex,
                    StandardCharsets.ISO_8859_1);
            else
                return result.toString();
        }
    }

    /**
     * Parent class of signed and unsigned byte array spliterators.
     */
    private static abstract class ByteArraySpliterator
        implements Spliterator.OfInt {
        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public long estimateSize() {
            return limit - position;
        }

        @Override
        public void forEachRemaining(IntConsumer consumer) {
            while (position < limit) {
                int b = fetch();
                ++position;
                consumer.accept(b);
            }
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            if (position < limit) {
                int b = fetch();
                ++position;
                consumer.accept(b);
                return true;
            }
            return false;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            int middle = position + (limit - position) / 2;
            if (middle > position) {
                var prefix = split(middle);
                position = middle;
                return prefix;
            } else
                return null;
        }

        abstract int fetch();
        abstract Spliterator.OfInt split(int middle);

        private ByteArraySpliterator(
            byte[] array, int fromIndex, int toIndex) {
            this.array = array;
            this.position = fromIndex;
            this.limit = toIndex;
        }

        final byte[] array;
        int position;
        final int limit;
    }

    /**
     * Signed byte array spliterator.
     */
    private static final class SignedByteArraySpliterator
        extends ByteArraySpliterator {
        @Override
        int fetch() {
            return array[position];
        }

        @Override
        Spliterator.OfInt split(int middle) {
            return new SignedByteArraySpliterator(array, position, middle);
        }

        private SignedByteArraySpliterator(
            byte[] array, int fromIndex, int toIndex) {
            super(array, fromIndex, toIndex);
        }
    }

    /**
     * Unsigned byte array spliterator.
     */
    private static final class UnsignedByteArraySpliterator
        extends ByteArraySpliterator {
        @Override
        int fetch() {
            return array[position] & 0xff;
        }

        @Override
        Spliterator.OfInt split(int middle) {
            return new UnsignedByteArraySpliterator(array, position, middle);
        }

        private UnsignedByteArraySpliterator(
            byte[] array, int fromIndex, int toIndex) {
            super(array, fromIndex, toIndex);
        }
    }

    // No instances.
    private ByteArrays() { }
}
