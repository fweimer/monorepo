package enyo.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Accessing the contents of data source using byte buffers.
 *
 * The {@link #fillBuffer(int)} method opportunistically fills the
 * returned buffer with bytes from the input source.
 *
 * <pre>
 * BufferInput <i>inp</i> = ...;
 *
 * <b>while</b> (<b>true</b>) {
 *     ByteBuffer <i>buffer</i> = <i>inp</i>.fillBuffer(0);
 *     <b>if</b> (!<i>buffer</i>.hasRemaining())
 *         <b>break</b>; // End of file.
 *
 *     <b>while</b> (<i>buffer</i>.hasRemaining()) {
 *       // Consume the buffer contents here.
 *       ...
 *     }
 * }
 * </pre>
 *
 * It is important to bring the number of remaining bytes in the
 * returned buffer down to zero because otherwise, the call
 * {@code buffer.fillBuffer(0)} might not replenish the buffer.
 *
 * <p>It is also possible to request that a certain minimum number of
 * bytes must be available.  For example, if a data packet is preceded
 * by an eight-byte header containing a magic number and a length,
 * processing of the packet might start like this:
 *
 * <pre>
 * BufferInput <i>inp</i> = ...;
 *
 * ByteBuffer <i>header</i> = <i>inp</i>.fillBuffer(8);
 * <b>int</b> <i>magic</i> = <i>header</i>.getInt();
 * <b>int</b> <i>length</i> = <i>header</i>.getInt();
 * </pre>
 *
 * If {@link #fillBuffer(int)} is called like this, the method throws
 * {@link EOFException} unless enough bytes are available in the
 * input.
 *
 * <p>The interface provides unbounded lookahead capability, limited
 * only by available memory for the buffer allocation.
 *
 * <pre>
 * BufferInput <i>inp</i> = ...;
 *
 * ByteBuffer <i>buffer</i>;
 * <b>try</b> {
 *     // A WAVE RIFF header has 44 bytes.
 *     <i>buffer</i> = <i>inp</i>.fillBuffer(44);
 * } <b>catch</b> (EOFException <i>e</i>) {
 *     // Not a WAVE RIFF file.
 *     ...
 * }
 *
 * WavHeader <i>wavHeader</i>;
 * {
 *     <b>int</b> <i>oldPosition</i> = <i>buffer</i>.position(0);
 *     <b>try</b> {
 *         <i>wavHeader</i> = WavHeader.read(<i>buffer</i>);
 *     } <b>catch</b> (WavHeaderFormatException <i>e</i>) {
 *         // Not a WAVE RIFF file.  Reset the buffer position,
 *         // so that the same bytes can be read again.
 *         <i>buffer</i>.position(<i>oldPosition</i>);
 *         ...
 *     }
 * }
 * </pre>
 *
 * <p>It is expected that implementations of this interface
 * automatically select the most appropriate byte buffer
 * implementation (heap-based,
 * {@linkplain java.nio.Buffer#isDirect() direct},
 * or {@linkplain java.nio.MappedByteBuffer mapped}).  In some cases,
 * a hint may be needed if the entire data source is to be consumed
 * or not.  (For example, input using a file mapping is usually only
 * beneficial if the file is sufficiently large, and the data is actually
 * read via the {@code BitInput} interface.)
 */
public interface BufferInput extends Closeable {
    /**
     * Returns a byte buffer with some bytes from the input source.
     *
     * The bytes are available as
     * {@linkplain ByteBuffer#remaining() remaining bytes}
     * in the returned buffer.  Callers are expected to consume bytes
     * in the buffer and call this method again to fetch more data
     * from the input source.  Here, consuming data from the buffer
     * means increasing the buffer position while leaving its limit
     * unchanged.
     *
     * <p>A call {@code fillBuffer(minRemaining)} with positive
     * {@code minRemaining} returns a buffer that has at least
     * this many bytes remaining.
     *
     * <p>A call {@code fillBuffer(0)} attempts to read some bytes
     * from the data source.  In this case, the returned buffer has no
     * bytes remaining if no more bytes are currently available in the
     * input, that is, if an end-of-file condition has been reached.
     * (More bytes may become available subsequently, depending on the
     * underlying byte source.)  No {@link EOFException} is raised
     * (unless propagated from an underlying read operation).
     *
     * <p>If the caller has not consumed all remaining bytes in the
     * buffer returned by the last call to this method, this method
     * will preserve these remaining bytes in the returned buffer (but
     * not necessarily at the same position).  If the last call
     * returned a non-empty buffer, and the caller has not since
     * updated the buffer to have fewer than {@code minRemaining}
     * remaining bytes (or increased {@code minRemaining} compared
     * to the previous invocation), it is expected that this method
     * returns the same {@code ByteBuffer} instance.
     * (If {@code minRemaining} is increased, it may be necessary to
     * allocate a new {@code ByteBuffer} object with sufficient
     * capacity.  The byte order will be preserved in this case.)
     *
     * <p>In cases where new remaining bytes are added to the buffer,
     * it is expected that implementations make an attempt to provide
     * reasonable amounts of extra data in the buffer (beyond what is
     * requested via {@code minRemaining}).
     *
     * <p>Implementations are encouraged to throw
     * {@link IllegalArgumentException} if {@code minRemaining}
     * is negative.  It is unspecified whether the returned buffer is
     * read-only.  The returned buffer may be array-backed.
     * Modification of the buffer contents is discouraged.
     *
     * @param minRemaining the minimum number of remaining bytes in the
     *   returned buffer; if zero, an attempt is made to read at least
     *   some bytes
     * @return a byte buffer, possibly with no remaining bytes if
     *   {@code minRemaining} is zero and no further bytes are
     *   available in the input
     * @throws EOFException if not enough data is available in the input
     *   to produce a buffer with {@code minRemaining} remaining bytes
     * @throws IOException an input error occurred (propagated from
     *   the actual input source)
     */
    public ByteBuffer fillBuffer(int minRemaining)
        throws EOFException, IOException;

    /**
     * Discards bytes from the input source.
     *
     * Like {@link InputStream#skipNBytes(long)}, this method throws
     * {@link EOFException} if an end-of-file condition is reached
     * before the requested number of bytes could be skipped
     *
     * @param n the number of bytes to skip
     * @throws EOFException the requested number of bytes is not
     *   available in the input
     * @throws IOException an input error occurred (propagated from
     *   the actual input source)
     * @see InputStream#skipNBytes(long)
     */
    public default void skipNBytes(long n) throws IOException {
        while (n > 0) {
            ByteBuffer buffer = fillBuffer(0);
            if (!buffer.hasRemaining())
                // n was positive, so we expected some more bytes.
                throw new EOFException("Expected " + n + " more bytes");
            if (n > buffer.remaining()) {
                n -= buffer.remaining();
                // Discard the bytes in the buffer.
                buffer.position(buffer.limit());
            } else {
                // This is the final buffer.  Consume part of it.
                buffer.position(buffer.position() + (int) n);
                break;
            }
        }
    }

    /**
     * Wraps the input stream in a {@code BufferInput} object.
     *
     * @param in the input stream containing the bytes
     * @return a {@code BufferInput} object that obtains its bytes
     *   from the specified input stream
     * @throws NullPointerException if {@code in} is null
     */
    public static BufferInput wrap(InputStream in) {
        return new BufferInputForStream(in);
    }

    /**
     * Wraps the channel in a {@code BufferInput} object.
     *
     * @param channel the channel providing the bytes
     * @return a {@code BufferInput} object that obtains its bytes
     *   from the specified byte channel
     * @throws NullPointerException if {@code channel} is null
     */
    public static BufferInput wrap(ReadableByteChannel channel) {
        return new BufferInputForChannel(channel);
    }

    /**
     * Wraps the channel in a {@code BufferInput} object.
     *
     * @param array the array providing the bytes
     * @return a {@code BufferInput} object that obtains its bytes
     *   from the specified array
     * @throws NullPointerException if the array parameter is null
     */
    public static BufferInput wrap(byte[] array) {
        return new BufferInputForBuffer(ByteBuffer.wrap(array));
    }

    /**
     * Wraps the channel in a {@code BufferInput} object.
     *
     * @param array the array providing the bytes
     * @param offset the index of the first byte in {@code array}
     *   that will be read by the returned object
     * @param length the number of bytes that can be read through the
     *   returned object
     * @return a {@code BufferInput} object that obtains its bytes
     *   from the specified array
     * @throws NullPointerException if the array parameter is null
     */
    public static BufferInput wrap(byte[] array, int offset, int length) {
        return new BufferInputForBuffer(
            ByteBuffer.wrap(array, offset, length));
    }

    /**
     * Wraps a shallow copy of the byte buffer.  Only the active
     * region of the buffer (the remaining bytes in the buffer) will
     * be used.  The byte order of the buffer initially returned by
     * {@link #fillBuffer(int)} will be big endian, ignoring the byte
     * order of the provided buffer.
     *
     * @param buffer the buffer providing the bytes
     * @return a {@code BufferInput} object that obtains its bytes
     *   from the specified buffer
     * @throws NullPointerException if the buffer parameter is null
     */
    public static BufferInput wrap(ByteBuffer buffer) {
        return new BufferInputForBuffer(buffer);
    }
}
