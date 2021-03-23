package enyo.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

final class BufferInputForStream implements BufferInput {
    private final InputStream in;

    // If allocated, this is always a heap-based buffer with a zero
    // array offset.
    private ByteBuffer buffer;

    BufferInputForStream(InputStream in) {
        this.in = Objects.requireNonNull(in);
    }

    @Override
    public ByteBuffer fillBuffer(int minRemaining) throws IOException {
        if (minRemaining < 0)
            throw new IllegalArgumentException(
                "minRemaining: " + minRemaining);

        if (buffer == null) {
            // Allocate a new empty buffer, but only if the requested
            // size is not excessively large.
            if (minRemaining <= LARGE_MIN_REMAINING) {
                buffer = ByteBuffer.allocate(
                    Math.max(minRemaining, BUFFER_SIZE));
                buffer.limit(0);
            } else
                // Allocate an empty buffer that will be replaced below.
                buffer = ByteBuffer.allocate(0);
        }

        // Special case: Read some bytes even if no bytes have been
        // requested, ideally as many as possible.
        if (minRemaining == 0 && !buffer.hasRemaining()) {
            // We can use the entire (heap-based) buffer.
            int count = in.read(buffer.array());
            buffer.position(0);
            if (count <= 0)
                // Treat these read() return values as EOF.
                return buffer.limit(0);
            else
                // Make available the data that has been read.
                return buffer.limit(count);
        }

        // At this point, minRemaining or buffer.remaining() is
        // positive.

        if (minRemaining <= buffer.remaining())
            // The current buffer is already sufficiently filled to
            // meet the request.
            return buffer;

        // Available buffer room: the remaining bytes plus the unused
        // capacity after them.
        int roomInBuffer = buffer.capacity() - buffer.position();
        if (roomInBuffer < minRemaining) {
            // The room does not meet the request.
            if (minRemaining <= buffer.capacity())
                // But we can make enough room by compacting.
                buffer
                    .compact()
                    .limit(buffer.position())
                    .position(0);
            else {
                // Compaction will not help.  Switch to a larger
                // buffer, preserving the remaining bytes.
                if (minRemaining <= LARGE_MIN_REMAINING)
                    buffer = enlargeBuffer(buffer, minRemaining);
                else {
                    // We have a large request.  Make sure that these
                    // bytes are actually available in the input
                    // before allocating the buffer.  This relies on
                    // the InputStream.readNBytes(int) interface
                    // contract.
                    int needed = minRemaining - buffer.remaining();
                    byte[] moreBytes = in.readNBytes(needed);
                    if (moreBytes.length != needed)
                        throw new EOFException(String.format(
                                "%d bytes required, but only %d bytes read",
                                needed, moreBytes.length));
                    buffer = ByteBuffer.allocate(minRemaining)
                        .order(buffer.order())
                        .put(buffer)
                        .put(moreBytes)
                        .flip();
                    return buffer;
                }
            }
        }

        // Now the buffer can fit the requested amount of data.  The
        // read() invocation relies on the buffer invariant
        // (heap-based, zero offset).
        do {
            int count = in.read(buffer.array(),
                buffer.limit(), buffer.capacity() - buffer.limit());
            // There has been some room
            if (count <= 0)
                // We need some data to meet the request.
                throw new EOFException();
            // Record that more bytes have been read.
            buffer.limit(buffer.limit() + count);
        } while (buffer.remaining() < minRemaining);

        return buffer;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        if (n <= 0)
            // For compatibility with InputStream.skipNBytes(long).
            return;

        if (buffer != null) {
            if (n <= buffer.remaining()) {
                // Consume bytes from the current buffer.
                buffer.position(buffer.position() + (int) n);
                return;
            } else {
                // Consume the entire remaining buffer.
                n -= buffer.remaining();
                buffer.position(buffer.limit());
            }
        }

        in.skipNBytes(n);
    }

    // Allocate new buffer that has capacity minRemaining and contains
    // the remaining bytes of the original buffer.
    static ByteBuffer enlargeBuffer(ByteBuffer buffer, int minRemaining) {
        var newBuffer = ByteBuffer.allocate(minRemaining);
        newBuffer
            .order(buffer.order())
            .put(buffer)
            .flip();
        return newBuffer;
    }

    // This is the buffer size used by most I/O libraries.
    static final int BUFFER_SIZE = 8192;

    // If the minRemaining setting is larger than that, do not
    // allocate the byte buffer directly, but try to read bytes first.
    static final int LARGE_MIN_REMAINING = 64 * 1024;
}
