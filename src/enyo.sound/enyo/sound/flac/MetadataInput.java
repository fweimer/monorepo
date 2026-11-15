package enyo.sound.flac;

import enyo.io.BufferInput;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Reading data in a metadata block.
 */
public final class MetadataInput implements BufferInput {
    /**
     * Returns the type of the metadata block.
     * @return a result with the {@link BlockType} enum constant if available,
     *   or an integer in the error part if it is not
     */
    public BlockType type() {
        return BlockType.T.ofInt(type);
    }

    /**
     * Returns true if more metadata blocks follow.
     * @return true if more metadata blocks follow, false if this is
     *   the last metadata block
     */
    public boolean moreBlocks() {
        return moreBlocks;
    }

    /**
     * Overall length of the stream.
     * @return the number of metadata bytes available in this stream.
     */
    public int length() {
        return length;
    }

    /**
     * Skips to the end of this metadata stream.  After that, all
     * attempts to read data return an end-of-file condition.
     * @throws IOException if an input error occurs
     */
    public void skipToEnd() throws IOException {
        if (onFinalBuffer) {
            // All data has been read if we have reached the final
            // buffer.
            buffer.position(buffer.limit());
            return;
        }

        try {
            in.skipNBytes(remaining);
        } finally {
            remaining = 0;
            onFinalBuffer = true;
            buffer = null;
        }
    }

    @Override
    public ByteBuffer fillBuffer(int minRemaining) throws IOException {
        if (minRemaining <= 0)
            throw new IllegalArgumentException(
                "minRemaining: " + minRemaining);

        // The final buffer may have been duplicated, and its limit
        // has been adjusted.
        if (onFinalBuffer) {
            if (buffer == null)
                // Can happen after skipToEnd().
                buffer = ByteBuffer.allocate(0);
            if (minRemaining > buffer.remaining())
                throw new EOFException();
            return buffer;
        }

        // Consume the bytes taken from the buffer that was returned
        // the last time.
        if (buffer != null) {
            int br = buffer.remaining();
            if (br > previousBufferRemaining)
                throw new IllegalStateException(
                    "Previous buffer size too large");
            remaining -= previousBufferRemaining - br;
        }


        if (minRemaining > remaining)
            throw new EOFException();

        buffer = in.fillBuffer(minRemaining);
        if (buffer.remaining() > remaining) {
            // Limit the size of the last buffer.
            onFinalBuffer = true;
            var newBuffer = buffer
                .duplicate()
                .limit(buffer.position() + remaining)
                .slice()
                .order(buffer.order());
            // All bytes have been consumed.
            buffer.position(buffer.position() + remaining);
            remaining = 0;
            buffer = newBuffer;
            return newBuffer;
        } else {
            previousBufferRemaining = buffer.remaining();
            return buffer;
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    MetadataInput(BufferInput in) throws IOException {
        ByteBuffer b = in.fillBuffer(4);
        int header = b.getInt();

        this.in = in;
        moreBlocks = header >= 0;
        type = (byte) ((header >> 24) & 0x7f);
        length = header & 0xff_ffff;
        remaining = length;
    }

    private final BufferInput in;
    private final boolean moreBlocks;
    private final byte type;
    private final int length;

    // This counts the number of bytes that still need to be consumed
    // from the underlying input.
    private int remaining;

    // Cached buffer from in.refillBuffer(), or own buffer created for
    // the final buffer.
    private ByteBuffer buffer;
    // The buffer.remaining() value when that buffer was returned.
    private int previousBufferRemaining;

    // This is set to true once the final buffer (with changed limit)
    // has been reached.
    private boolean onFinalBuffer;
}
