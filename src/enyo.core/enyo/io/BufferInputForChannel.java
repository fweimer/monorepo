package enyo.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Objects;

final class BufferInputForChannel implements BufferInput {
    private final ReadableByteChannel channel;

    // If allocated, this is always a heap-based buffer with a zero
    // array offset.
    private ByteBuffer buffer;

    BufferInputForChannel(ReadableByteChannel channel) {
        this.channel = Objects.requireNonNull(channel);
    }

    @Override
    public ByteBuffer fillBuffer(int minRemaining) throws IOException {
        if (minRemaining < 0)
            throw new IllegalArgumentException(
                "minRemaining: " + minRemaining);

        if (buffer == null) {
            // Allocate a new empty buffer, but only if the requested
            // size is not excessively large.
            if (minRemaining <= BufferInputForStream.LARGE_MIN_REMAINING) {
                buffer = allocate(
                    Math.max(minRemaining, BufferInputForStream.BUFFER_SIZE));
                buffer.limit(0);
            } else
                // Allocate an empty buffer that will be replaced below.
                buffer = ByteBuffer.allocate(0);
        }

        // Special case: Read some bytes even if no bytes have been
        // requested, ideally as many as possible.
        if (minRemaining == 0 && !buffer.hasRemaining()) {
            // We can use the entire buffer.
            buffer.clear();
            // Ignore the return value.  EOF is not an error in this
            // case.
            channel.read(buffer);
            buffer.flip();
            return buffer;
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
            else if (minRemaining
                <= BufferInputForStream.LARGE_MIN_REMAINING) {
                // Compaction did not help, but we can simply increase
                // the size of our buffer.
                buffer = BufferInputForStream.enlargeBuffer(
                    buffer, minRemaining);
            } else {
                // This requires allocation of a substantially larger
                // buffer.
                readIntoLargeBuffer(minRemaining);
                return buffer;
            }
        }

        // Now the buffer can fit the requested amount of data.
        fillBufferUpTo(minRemaining);

        return buffer;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        if (n <= 0)
            // For compatibility with InputStream.skipNBytes(long).
            return;

        if (channel instanceof SeekableByteChannel) {
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

            var seek = (SeekableByteChannel) channel;
            long newPosition = seek.position() + n;
            long seekSize = seek.size();
            if (newPosition < n // Overflow.
                || newPosition > seekSize)
                throw new EOFException("Trying to seek to offset "
                    + Long.toUnsignedString(n)
                    + " with channel length " + seekSize);
            seek.position(newPosition);
        } else
            BufferInput.super.skipNBytes(n);
    }

    private ByteBuffer allocate(int size) {
        boolean direct;
        if (buffer == null)
            direct = channel instanceof FileChannel
                || channel instanceof SocketChannel
                || channel instanceof Pipe.SourceChannel
                || channel instanceof Pipe.SinkChannel;
        else
            direct = buffer.isDirect();
        if (direct)
            return ByteBuffer.allocateDirect(size);
        else
            return ByteBuffer.allocate(size);
    }

    // Switches to a larger buffer and reads the requested amount of
    // data into it.
    private void readIntoLargeBuffer(int minRemaining) throws IOException {
        // Make sure that the requested bytes are actually available
        // in the input before allocating the entire buffer.  That's
        // why InputStream.readNBytes(int) is used.

        var previousData = new byte[buffer.remaining()];
        buffer.get(previousData);
        var order = buffer.order();

        // The buffer is no longer needed.  Do not retain a reference
        // in case it is large.
        buffer = null;

        // Fetch additional data.
        int needed = minRemaining - previousData.length;
        var newData = Channels.newInputStream(channel).readNBytes(needed);
        if (newData.length < needed)
            throw new EOFException();

        // Use a non-direct buffer, under the assumption that this
        // makes it easier to recover a large memory allocation.
        buffer = ByteBuffer.allocate(minRemaining)
            .order(order)
            .put(previousData)
            .put(newData)
            .flip();
    }

    // Adds bytes after the end of the active region until
    // minRemaining bytes are in the active region.  Assumes that
    // buffer already has capacity for minRemaining bytes at its
    // position.
    private void fillBufferUpTo(int minRemaining) throws IOException {
        int originalPosition = buffer.position();

        // This is the position value at which enough data has been
        // read.
        int stopPosition = originalPosition + minRemaining;

        // Switch the active region to the trailing part of the
        // buffer.  Use the entire available buffer space, not just
        // enough to reach stopPosition.
        buffer
            .position(buffer.limit())
            .limit(buffer.capacity());

        // Copy bytes until the minRemaining position has been
        // reached.
        do {
            int count = channel.read(buffer);
            if (count <= 0)
                throw new EOFException();
        } while (buffer.position() < stopPosition);

        // Restore the start of the active region.  Extend it to
        // include the bytes read above.
        buffer
            .limit(buffer.position())
            .position(originalPosition);
    }
}
