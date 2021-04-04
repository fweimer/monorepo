package enyo.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Simplified version of {@link BufferInput}.
 *
 * This class can be used to implement buffer inputs that
 * have strict buffer boundaries.  The supplied implementation of
 * {@link BufferInput#fillBuffer(int)} automatically allocates a temporary
 * buffer if a request crosses a buffer boundary.
 */
public abstract class SimpleBufferInput implements BufferInput {
    /**
     * Creates a new simple buffer input.
     */
    protected SimpleBufferInput() {
    }

    /**
     * Returns a filled buffer if data is available.
     *
     * This method is called by {@link #fillBuffer(int)} after the
     * previously returned buffer has been fully consumed.  Once this
     * method is called, the {@link BufferInput} protocoll ensures
     * that the previously returned buffer is only used again if it is
     * returned once more from this method.
     *
     * @return a byte buffer; if there are more bytes in the source,
     *   it must have remaining bytes
     * @throws IOException an input error occurred
     */
    protected abstract ByteBuffer fillBuffer() throws IOException;

    /**
     * Returns a filled buffer after skipping.
     *
     * This method is called from {@link #skipNBytes(long)} after all
     * pending bytes have been consumed.  An implementation must skip
     * exactly {@code n} bytes.
     *
     * <p>The default implementation calls {@link #fillBuffer()} to
     * obtain more bytes and discards buffers until the skip target
     * lies within a buffer, which is then returned.
     *
     * @param n the number of bytes to skip; it is always positive
     *  if called from {@link #skipNBytes(long)}
     * @return a (possibly empty) buffer that contains left-over bytes
     *  that have not been skipped; null also indicates an empty buffer
     * @throws EOFException if the requested bytes are not available
     *   in the input data
     * @throws IOException if an input error occurrs
     */
    protected ByteBuffer skipNBytesImpl(long n) throws IOException {
        while (true) {
            ByteBuffer buffer = fillBuffer();
            if (n <= buffer.remaining())
                // The skip target lies within this buffer, so we are
                // done.
                return buffer.position(buffer.position() + (int) n);
            if (n > 0 && !buffer.hasRemaining())
                // An empty buffer signals EOF, but there are more
                // bytes to skip.
                throw new EOFException();
            n -= buffer.remaining();
        }
    }

    @Override
    public final ByteBuffer fillBuffer(int minRemaining) throws IOException {
        if (minRemaining < 0)
            throw new IllegalArgumentException(
                "minRemaining: " + minRemaining);

        if (mainBuffer == null)
            mainBuffer = fillBuffer();

        if (minRemaining == 0) {
            if (!mainBuffer.hasRemaining())
                auxOrRefill();
            return mainBuffer;
        }

        if (minRemaining <= mainBuffer.remaining())
            return mainBuffer;

        if (!mainBuffer.hasRemaining()) {
            auxOrRefill();
            if (minRemaining <= mainBuffer.remaining())
                return mainBuffer;
        }

        // Allocate auxBuffer because mainBuffer does not have enough
        // data.
        if (auxBuffer == null) {
            auxBuffer = mainBuffer;
            mainBuffer = ByteBuffer.allocate(
                Math.min(minRemaining,
                    BufferInputForStream.LARGE_MIN_REMAINING))
                .order(auxBuffer.order());
        } else
            // auxBuffer is not null, so mainBuffer is a buffer
            // allocated by this class, and can be changed at will.
            mainBuffer.compact();

        // Copy bytes from auxBuffer to mainBuffer.
        do {
            // Make sure that there are more bytes to copy.
            if (!auxBuffer.hasRemaining()) {
                auxBuffer = fillBuffer()
                    .order(auxBuffer.order());
                if (!auxBuffer.hasRemaining())
                    throw new EOFException();
            }

            // Make sure that there is room in mainBuffer for more bytes.
            if (!mainBuffer.hasRemaining()) {
                int newCapacity;
                if (minRemaining <= BufferInputForStream.LARGE_MIN_REMAINING)
                    // Directly allocate the requested buffer size.
                    newCapacity = minRemaining;
                else
                    // Implement an exponential resizing policy to avoid
                    // excessive allocations for short, corrupted streams.
                    newCapacity = Math.min(
                        Math.max(BufferInputForStream.LARGE_MIN_REMAINING,
                            Math.addExact(mainBuffer.capacity(),
                                mainBuffer.capacity() / 2)),
                        minRemaining);
                // Copy the old mainBuffer contents to a larger buffer.
                mainBuffer = ByteBuffer.allocate(newCapacity)
                    .order(mainBuffer.order())
                    .put(mainBuffer.flip());
            }

            int needed = minRemaining - mainBuffer.position();
            int toCopy = Math.min(needed,
                Math.min(auxBuffer.remaining(), mainBuffer.remaining()));
            int auxBufferLimit = auxBuffer.limit();
            auxBuffer.limit(auxBuffer.position() + toCopy);
            mainBuffer.put(auxBuffer);
            auxBuffer.limit(auxBufferLimit);
        } while (mainBuffer.position() < minRemaining);

        return mainBuffer.flip();
    }

    @Override
    public final void skipNBytes(long n) throws IOException {
        if (n <= 0)
            return;

        if (mainBuffer != null) {
            if (n <= mainBuffer.remaining()) {
                // It is possible to satisfy the skip request
                // from mainBuffer alone.
                mainBuffer.position(mainBuffer.position() + (int) n);
                return;
            }

            // Consume the skipped bytes from mainBuffer.
            n -= mainBuffer.remaining();
            mainBuffer.position(mainBuffer.limit());

            // Check to see if there is data in the auxiliary buffer.
            if (auxBuffer != null) {
                mainBuffer = auxBuffer;
                auxBuffer = null;

                if (n <= mainBuffer.remaining()) {
                    mainBuffer.position(mainBuffer.position() + (int) n);
                    return;
                }

                n -= mainBuffer.remaining();
                mainBuffer.position(mainBuffer.limit());
            }
        }

        // Now mainBuffer is at its limit, and there is no auxBuffer.
        // The simplified implementation can be used.
        mainBuffer = skipNBytesImpl(n);
    }

    // Previous return value of fillBuffer(), or locally allocated
    // buffer if auxBuffer != null.
    private ByteBuffer mainBuffer;

    // Used by fillBuffer(int) to store data coming after mainBuffer.
    // Always a buffer returned by fillBuffer().
    private ByteBuffer auxBuffer;

    // Switch to auxBuffer, or fill mainBuffer via fillBuffer().
    private void auxOrRefill() throws IOException {
        if (auxBuffer == null || !auxBuffer.hasRemaining())
            mainBuffer = fillBuffer()
                .order(mainBuffer.order());
        else
            // There is some leftover from a previous
            // boundary-crossing request.
            mainBuffer = auxBuffer;
        auxBuffer = null;
    }
}
