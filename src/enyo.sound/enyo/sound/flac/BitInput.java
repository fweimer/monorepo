package enyo.sound.flac;

import enyo.io.BufferInput;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Reading variably-sized integers from an input stream.
 *
 * The integers are stored in big-endian format, and in each byte, the
 * most-significant bit is read first.
 */
public final class BitInput {
    /**
     * Wraps the input stream for bit decoding.
     * @param in the data source
     * @throws NullPointerException if {@code in} is null
     */
    public BitInput(BufferInput in) {
        this.in = Objects.requireNonNull(in);
    }

    /**
     * Returns the underlying buffer input.
     * The buffer input is updated to make sure that exactly those bytes
     * that have been read through this {@code BitInput} object
     * are also marked as read in the {@link BufferInput} object.
     *
     * <p>It is possible to continue reading from this {@code BitInput}
     * object after consuming some bytes directly from the underlying
     * buffer input.
     *
     * @return the buffer input passed to the constructor
     * @throws IllegalStateException if this bit input is not
     *   {@linkplain #isAligned() aligned on a byte boundary}
     */
    public BufferInput unwrap() {
        if (!isAligned())
            throw new IllegalStateException(
                "Bit alignment is " + (remaining % 8));
        if (input != null) {
            // Back up by the remaining bytes.
            input.position(input.position() - remaining / 8);
            // And nothing is now buffered on this side.
            buffer = 0;
            remaining = 0;
        }
        return in;
    }

    /**
     * Returns true if the bit stream is at a byte boundary.
     * @return true if the next bit is located at a byte boundary,
     *   false if not
     */
    public boolean isAligned() {
        return (remaining % 8) == 0;
    }

    /**
     * Returns 8 consecutive bits read from the source.
     * @return an integer from 0 to 255 (inclusive)
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     */
    public int get8() throws IOException {
        return getBits0(8);
    }

    /**
     * Returns 16 consecutive bits read from the source.
     * @return an integer from 0 to 65535 (inclusive)
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     */
    public int get16() throws IOException {
        return getBits0(16);
    }

    /**
     * Returns 24 consecutive bits read from the source.
     * @return an integer from 0 to 16777215 (inclusive)
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     */
    public int get24() throws IOException {
        return getBits0(24);
    }

    /**
     * Returns 32 consecutive bits read from the source.
     * @return an integer; the value is returned is as signed, so that
     *   it fits into an {@code int}
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     */
    public int get32() throws IOException {
        return getBits0(32);
    }

    /**
     * Returns the specified number of bits read from the source.
     * @param count the number of bits to read; from 0 to 32 (inclusive)
     * @return an integer;
     *   if {@code count} is 32, the value is returned is as signed,
     *   so that it fits into an {@code int}
     * @throws IllegalArgumentException if {@code count} is negative
     *   or larger than 32
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     * @see #getSignedBits(int)
     * @see #getBitsLong(int)
     */
    public int getBits(int count) throws IOException {
        if (count < 0 || count >= 32)
            throw new IllegalArgumentException("count: " + count);
        return getBits0(count);
    }

    /**
     * Returns the specified number of bits read from the source.
     * The value is read as a two's complement integer with a sign bit.
     * @param count the number of bits to read; from 0 to 32 (inclusive)
     * @return a signed integer
     * @throws IllegalArgumentException if {@code count} is negative
     *   or larger than 32
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     * @see #getBits(int)
     */
    public int getSignedBits(int count) throws IOException {
        int toSignPosition = 32 - count;
        return getBits(count) << toSignPosition >> toSignPosition;
    }

    /**
     * Returns the byte-aligned 16-bit frame footer.
     * @return an integer from 0 to 65535 (inclusive)
     * @throws FlacException if the padding is not zero
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     */
    public int getFooter() throws IOException {
        int paddingSize = remaining & 7;
        int padding = getBits0(paddingSize);
        if (padding != 0)
            throw new FlacException(String.format(
                    "Frame %d-bit frame footer padding is not zero: %d",
                    paddingSize, padding));

        return get16();
    }

    /**
     * Returns the specified number of bits read from the source.
     * @param count the number of bits to read; from 0 to 64 (inclusive)
     * @return an long;
     *   if {@code count} is 64, the value is returned is as signed,
     *   so that it fits into a {@code long}
     * @throws IllegalArgumentException if {@code count} is negative
     *   or larger than 64
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     * @see #getBits(int)
     */
    public long getBitsLong(int count) throws IOException {
        if (count < 0 || count > 64)
            throw new IllegalArgumentException("count: " + count);
        if (count > 32) {
            int result1 = getBits0(count - 32);
            int result2 = get32(); // Buffer could now be aligned.
            return ((long) result1 << 32) | Integer.toUnsignedLong(result2);
        } else
            return Integer.toUnsignedLong(getBits0(count));
    }

    /**
     * Returns an UTF-8-style integer read from the source.
     * @return an unsigned long
     * @throws EOFException if the stream ends before all bits can be read
     * @throws IOException if an input error occured
     */
    public long getUTF8Like() throws IOException {
        int b = get8();
        if (b < 128)
            return b;
        if ((b & 0xc0) == 0x80)
            throw new FlacException("Malformed leader byte: " + b);
        int count = Integer.numberOfLeadingZeros(~(b << 25));
        long result = b & ((1 << (7 - count)) - 1);
        do {
            b = get8();
            if ((b & 0xc0) != 0x80)
                throw new FlacException("Malformed follower byte: " + b);
            result = (result << 6) | (b & 0x3f);
            --count;
        } while (count > 0);
        return result;
    }

    /**
     * Reads a signed integer in variable-length exponential Golomb
     * encoding.  First, zero bits are read until a one bit
     * is encountered.  The number of zero bits is then shifted
     * left by {@code width}, and {@code width} further
     * bits are copied from the input stream into the lower bits.
     * The resulting integer is then converted into a signed integer
     * by treating the least significant bit as a sign bit.
     * @param width bit length of the base-two encoded part
     * @return a signed integer
     * @throws IOException if an input error occured
     */
    public int getRice(int width) throws IOException {
        if (width < 0 || width >= 32)
            throw new IllegalArgumentException("width: " + width);
        int count = getUnary0();
        int lowerBits = getBits0(width);
        int shifted = count << width;
        if (shifted >> width != count)
            throw new FlacException(String.format(
                "Unary count %d too large for width %d",
                count, width));
        int combined = shifted | lowerBits;
        // The sign bit is in the lowest bit (not the MSB).
        int result = (combined >>> 1) ^ -(combined & 1);
        return result;
    }

    /**
     * Return an unary-only Rice/Golomb-encoded value.
     * Equivalent to {@code getRice(0)}.
     * @return a signed integer
     * @see #getRice(int)
     * @throws IOException if an input error occured
     */
    public int getRiceUnaryOnly() throws IOException {
        int count = getUnary0();
        // The sign bit is in the lowest bit (not the MSB).
        return (count >>> 1) ^ -(count & 1);
    }

    /**
     * Returns the requested number of bytes read from the source.
     * @param count the number of bytes to read
     * @return a byte array with the requested data
     * @throws IllegalArgumentException if {@code count} is negative
     * @throws EOFException if the stream ends before all bytes can be read
     * @throws IOException if an input error occured
     */
    public byte[] getBytes(int count) throws IOException {
        if (count < 0)
            throw new IllegalArgumentException("count: " + count);
        byte[] blob = new byte[count];
        getBytes(blob);
        return blob;
    }

    /**
     * Reads bytes into the passed array.
     * @param b the byte array to fill data read from the source
     * @throws EOFException if the stream ends before all bytes can be read
     * @throws IOException if an input error occured
     * @throws NullPointerException if {@code b} is null
     */
    public void getBytes(byte[] b) throws IOException {
        for (int i = 0; i < b.length; ++i)
            b[i] = (byte) getBits(8);
    }

    private final BufferInput in;

    // Buffered bits are aligned towards the MSB so that the next
    // usable bit is always the MSB.
    private long buffer;

    // Number of buffered bits in buffer.
    private int remaining;

    // The number of bytes that have been copied buffer the last time
    // it was refilled.
    private ByteBuffer input;

    // Size of the buffer in bits.
    private static final int BUFFER_SIZE = 64;

    private int getUnary0() throws IOException {
        // FIXME: This could have some overflow checking.
        int result = 0;
        while (buffer == 0) {
            result += remaining;
            refillBuffer();
        }

        int last = Long.numberOfLeadingZeros(buffer);
        // Avoid shifting by BUFFER_SIZE in case buffer == 1.
        buffer = (buffer << last) << 1;
        remaining -= last + 1;
        return result + last;
    }

    private int getBits0(int count) throws IOException {
        // buffer >>> BUFFER_SIZE shifts by 0, so this case needs
        // special treatment.
        if (count == 0)
            return 0;

        // Needs to be kept in sync with getUnary0().
        if (count <= remaining) {
            // Extract the top count bits from the buffer.
            int result = (int) (buffer >>> (BUFFER_SIZE - count));

            // Remove the extracted bits from the buffer.
            buffer <<= count;
            remaining -= count;

            return result;
        }

        return getBitsSlow(count);
    }

    private int getBitsSlow(int count) throws IOException {
        // Consume the entire remaining buffer.
        int result;
        if (remaining != 0) {
            result = (int) (buffer >>> (BUFFER_SIZE - remaining));
            count -= remaining;
        } else
            // Special case to avoid >>> BUFFER_SIZE, see above.
            result = 0;

        while (true) {
            refillBuffer();

            // The refill may have produced fewer than BUFFER_SIZE
            // bits.  But usually, we can exit the loop after one
            // refill.
            if (remaining >= count) {
                int resultTail = (int) (buffer >>> (BUFFER_SIZE - count));
                buffer <<= count;
                remaining -= count;
                return (result << count) | resultTail;
            }

            // Copy the active portion of the buffer into the result.
            result = (result << remaining)
                | (int) (buffer >>> (BUFFER_SIZE - remaining));
            count -= remaining;
            // Do not bother with updating remaining, it will be
            // overwritten by the refill.
        }
    }

    private void refillBuffer() throws IOException {
        if (input != null && input.remaining() >= BUFFER_SIZE / 8) {
            buffer = input.getLong();
            remaining = BUFFER_SIZE;
        } else
            refillBufferSlow();
    }

    private void refillBufferSlow() throws IOException {
        if (input == null || !input.hasRemaining())
            input = in.fillBuffer(1);
        if (input.remaining() >= BUFFER_SIZE / 8) {
            buffer = input.getLong();
            remaining = BUFFER_SIZE;
        } else {
            buffer = 0;
            remaining = 0;
            while (input.hasRemaining()) {
                buffer = (buffer << 8) | Byte.toUnsignedInt(input.get());
                remaining += 8;
            }
            buffer <<= BUFFER_SIZE - remaining;
        }
    }
}
