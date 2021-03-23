package enyo.io;

import java.io.EOFException;
import java.nio.ByteBuffer;

final class BufferInputForBuffer implements BufferInput {
    private final ByteBuffer buffer;

    BufferInputForBuffer(ByteBuffer buffer) {
        this.buffer = buffer.slice();
    }

    @Override
    public ByteBuffer fillBuffer(int minRemaining) throws EOFException {
        if (minRemaining < 0)
            throw new IllegalArgumentException(
                "minRemaining: " + minRemaining);

        if (minRemaining > buffer.remaining())
            throw new EOFException();
        return buffer;
    }

    @Override
    public void close() {
    }
}
