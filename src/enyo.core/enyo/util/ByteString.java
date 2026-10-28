package enyo.util;

import enyo.io.ExtendedCharsets;

import java.io.Serial;
import java.io.Serializable;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Strings of bytes.
 *
 * This class is modeled after {@link String}, but stores bytes
 * instead of UCS-2 code points.  Byte strings can contain UTF-8
 * sequences, but do not have to.  (Like strings may contain UTF-16.)
 *
 * <p>Byte Strings are constructed using one of the {@code of(…)}
 * methods.
 *
 * <p>{@linkplain #compareTo(ByteString) Comparison} uses the unsigned
 * order (matching most network protocols that depend on ordering).
 * Due to the different handling of surrogate pairs in UTF-8 and
 * UTF-16, the ordering for UTF-8 encoded byte strings and UTF-16
 * encoded {@link String}s is not fully equivalent.
 */
public final class ByteString implements Comparable<ByteString>, Serializable {
    /**
     * Returns the empty byte string.
     * @return an empty byte string
     */
    public static ByteString of() {
        return Constants.EMPTY;
    }

    /**
     * Returns a byte string consisting of a single byte.
     * @param b the byte in the string
     * @return a single-byte byte string
     */
    public static ByteString of(byte b) {
        return Constants.ONE_BYTE[b & 0xff];
    }

    /**
     * Returns a byte string consisting of two bytes.
     * @param b0 the first byte in the string
     * @param b1 the second byte in the string
     * @return a two-byte byte string
     */
    public static ByteString of(byte b0, byte b1) {
        return new ByteString(new byte[] { b0, b1 });
    }

    /**
     * Returns a byte string containing three bytes.
     * @param b0 the first byte in the string
     * @param b1 the second byte in the string
     * @param b2 the third byte in the string
     * @return a three-byte byte string
     */
    public static ByteString of(byte b0, byte b1, byte b2) {
        return new ByteString(new byte[] { b0, b1, b2 });
    }

    /**
     * Returns a byte string containing the specified bytes
     * @param b0 the first byte
     * @param b1 the second byte
     * @param b2 the third byte
     * @param rest the remaining bytes
     * @return a byte string containing all the bytes in order
     * @throws NullPointerException if {@code rest} is null
     */
    public static ByteString of(byte b0, byte b1, byte b2, byte... rest) {
        var data = new byte[Math.addExact(rest.length, 3)];
        data[0] = b0;
        data[1] = b1;
        data[2] = b2;
        System.arraycopy(rest, 0, data, 3, rest.length);
        return new ByteString(data);
    }

    /**
     * Returns a byte string containing a copy of the bytes.
     * @param b the contents of the string
     * @return a byte string with the same length and contents
     * @throws NullPointerException if {@code b} is null
     */
    public static ByteString of(byte[] b) {
        switch (b.length) {
        case 0:
            return Constants.EMPTY;
        case 1:
            return Constants.ONE_BYTE[Byte.toUnsignedInt(b[0])];
        default:
            return new ByteString(b.clone());
        }
    }

    /**
     * Returns a string containing the bytes from the active buffer region.
     * The first byte included in the result is at the buffer's
     * {@linkplain ByteBuffer#position()}.
     * The buffer object is not modified (not even temporarily).
     * @param b the buffer containing the bytes
     * @return a byte string of length
     *   {@link ByteBuffer#remaining() b.remaining()}
     */
    public static ByteString of(ByteBuffer b) {
        switch (b.remaining()) {
        case 0:
            return Constants.EMPTY;
        case 1:
            return Constants.ONE_BYTE[Byte.toUnsignedInt(b.get(b.position()))];
        default:
            var copy = new byte[b.remaining()];
            b.get(b.position(), copy);
            return new ByteString(copy);
        }
    }

    /**
     * Returns a byte string containing the encoded character sequence.
     * @param s the character sequence to encode
     * @param cs the charset to use for the encoding
     * @return a byte string containing the encoded bytes
     * @throws NullPointerException if any of the arguments are null
     * @throws IllegalArgumentException if
     *   {@link CharsetEncoder#encode(CharBuffer)} throws
     *   {@link CharacterCodingException}; that exception is available as
     *   the cause
     */
    public static ByteString of(CharSequence s, Charset cs) {
        CharsetEncoder encoder = cs.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer buffer;
        try {
            buffer = encoder.encode(CharBuffer.wrap(s));
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(
                "Invalid input to byte string conversion", e);
        }
        return of(buffer);
    }

    /**
     * Returns a byte string containing the UTF-8-encoded character sequence.
     * @param s the character sequence to encode
     * @return a byte string containing the encoded bytes
     * @throws NullPointerException if {@code s} is null
     * @throws IllegalArgumentException if
     *   {@link CharsetEncoder#encode(CharBuffer)} throws
     *   {@link CharacterCodingException}; that exception is available as
     *   the cause
     */
    public static ByteString ofUTF8(CharSequence s) {
        switch (s.length()) {
        case 0:
            return Constants.EMPTY;
        case 1:
            char ch = s.charAt(0);
            if (ch < 128)
                return of((byte) ch);
            // Fall through.
        }
        return of(s, StandardCharsets.UTF_8);
    }

    /**
     * Returns a byte string containing bytes from an array.
     * @param b the source of bytes
     * @param fromIndex the index of the first byte in {@code b}
     *   to include in the result
     * @param toIndex one past the index of the last byte in {@code b}
     *   to include in the result
     * @return a byte string with the same length and contents
     * @throws IndexOutOfBoundsException if {@code fromIndex} and
     *   {@code toIndex} to not form a valid index range for {@code b}
     * @throws NullPointerException if {@code b} is null
     */
    public static ByteString ofRange(byte[] b, int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, b.length);
        switch (toIndex - fromIndex) {
        case 0:
            return Constants.EMPTY;
        case 1:
            return Constants.ONE_BYTE[Byte.toUnsignedInt(b[fromIndex])];
        default:
            return new ByteString(Arrays.copyOfRange(b, fromIndex, toIndex));
        }
    }

    /**
     * Returns the length of the string in bytes.
     * @return the length of the string, a non-negative integer
     * @see String#length()
     */
    public int length() {
        return data.length;
    }

    /**
     * Returns true if the string does not contain any bytes.
     * @return true if the {@linkplain #length() length} is zero,
     *  false otherwise
     * @see String#isEmpty()
     */
    public boolean isEmpty() {
        return data.length == 0;
    }

    /**
     * Returns a copy of the underlying byte array.
     * @return a new byte array with the same bytes as this string
     * @see String#getBytes()
     */
    public byte[] getBytes() {
        return data.clone();
    }

    /**
     * Returns the contents of this string wrapped in a byte buffer.
     * The buffer's position is zero, and its limit and capacity are
     * {@link length()}.  The byte order is
     * {@link java.nio.ByteOrder#BIG_ENDIAN}.
     * @return a read-only byte buffer
     * @see ByteBuffer#asReadOnlyBuffer()
     */
    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(data).asReadOnlyBuffer();
    }


    /**
     * Returns a memory segment covering this string's byte array.
     * The memory segment is read-only.  It must not be used to
     * mutate the underlying byte array.
     * @return a read-only memory segment
     * @see MemorySegment#ofArray(byte[])
     * @see MemorySegment#asReadOnly()
     */
    public MemorySegment asMemorySegment() {
	return MemorySegment.ofArray(data).asReadOnly();
    }

    /**
     * Returns a stream of unsigned integers with the bytes in the string.
     * @return a new stream
     * @see String#chars()
     */
    public IntStream bytes() {
        return ByteArrays.unsignedStream(data);
    }

    /**
     * Returns the byte at the specified index.
     * @param index the index of the byte to return (starting at 0)
     * @return the value of the byte at the index
     * @throws IndexOutOfBoundsException if {@code index} is negative
     *   or not less than the {@linkplain #length() length}
     */
    public byte byteAt(int index) {
        return data[index];
    }

    /**
     * Returns a string containing the remaining bytes starting at a index.
     * @param index the zero-based index of the first byte to include
     *   in the result
     * @return a string containing the bytes starting at {@code index}
     * @throws IndexOutOfBoundsException if {@code index} is negative
     *   or greater than the {@linkplain #length() length}
     * @see String#substring(int)
     */
    public ByteString substring(int index) {
        if (index == 0)
            return this;
        return ofRange(data, index, data.length);
    }

    /**
     * Returns a string containing the remaining bytes starting at a index.
     * @param fromIndex the zero-based index of the first byte to include
     *   in the result
     * @param toIndex one past the zero-based index of the last byte
     *   to include
     * @return a string containing bytes starting at index {@code fromIndex},
     *   up (but excluding) to index {@code toIndex}
     * @throws IndexOutOfBoundsException if {@code fromIndex}, {@code toIndex}
     *   are not valid for this string
     * @see String#substring(int)
     */
    public ByteString substring(int fromIndex, int toIndex) {
        if (fromIndex == 0 && toIndex == data.length)
            return this;
        return ofRange(data, fromIndex, toIndex);
    }

    /**
     * Returns true if both strings contain the same bytes.
     * @return true if the length and contents of the strings are equal,
     *   false if not
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ByteString o)
            return Arrays.equals(data, o.data);
        return false;
    }

    /**
     * Returns a hash code for the contents of this byte string.
     * The hash code matches that what is computed by
     * {@link Arrays#hashCode(byte[])}.
     * @return an integer
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Compares the strings lexicographically, as unsigned byte sequences.
     * @param other the other byte string in the comparison
     * @return less than zero if this string is less than the other string
     *   in the comparison, zero if the strings are
     *   {@linkplain #equals(Object)} equal, greater than zero if this string
     *   is greater than the other string
     */
    @Override
    public int compareTo(ByteString other) {
        int mismatch = Arrays.mismatch(data, other.data);
        if (mismatch < 0)
            return 0;
        if (mismatch >= data.length)
            return -1;
        if (mismatch >= other.data.length)
            return 1;
        return Byte.toUnsignedInt(data[mismatch])
            - Byte.toUnsignedInt(other.data[mismatch]);
    }

    /**
     * Returns this string decoded as
     * {@linkplain ExtendedCharsets#UTF_8S UTF-8S}.
     * @return a string containing the same characters as this string;
     *   UTF-8 decoding errors are resolved through surrogate encoding
     */
    @Override
    public String toString() {
        return new String(data, ExtendedCharsets.UTF_8S);
    }

    /**
     * The underlying byte storage.
     */
    @Serial
    private byte[] data;

    private ByteString(byte[] data) {
        this.data = data;
    }

    private static final class Constants {
        static final ByteString EMPTY = new ByteString(new byte[0]);

        static final ByteString[] ONE_BYTE;
        static {
            ONE_BYTE = new ByteString[256];
            for (int i = 0; i < 256; ++i)
                ONE_BYTE[i] = new ByteString(new byte[] { (byte) i });
        }

        private Constants() { } // No instances.
    }

    private static final long serialVersionUID = 0x4dc7bcbc77e63869L;

    /**
     * Returns a defensive copy of the byte string if necessary.
     * @return a byte string with an inaccessible backing array
     */
    @Serial
    private Object readResolve() {
        return ByteString.of(data);
    }
}
