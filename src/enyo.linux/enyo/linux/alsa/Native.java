package enyo.linux.alsa;

import enyo.io.ExtendedCharsets;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import java.lang.foreign.Linker;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class Native {
    static ByteBuffer wrap(byte[] buffer) {
        return ByteBuffer.wrap(buffer)
            .order(ByteOrder.nativeOrder());
    }

    static ByteBuffer wrapReadOnly(byte[] buffer) {
        return ByteBuffer.wrap(buffer)
            // Order matters here!  (Reverts to big endian.)
            .asReadOnlyBuffer()
            .order(ByteOrder.nativeOrder());
    }

    static ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size)
            .order(ByteOrder.nativeOrder());
    }

    static ByteBuffer duplicate(ByteBuffer buffer) {
        return buffer.duplicate()
            .order(ByteOrder.nativeOrder());
    }

    static long getPlatformLong(ByteBuffer buffer) {
        if (LONG_SIZE == 4)
            return buffer.getInt();
        else
            return buffer.getLong();
    }

    static long getPlatformLong(ByteBuffer buffer, int offset) {
        if (LONG_SIZE == 4)
            return buffer.getInt(offset);
        else
            return buffer.getLong(offset);
    }

    static ByteBuffer putPlatformLong(
        ByteBuffer buffer, int offset, long value) {
        if (LONG_SIZE == 4)
            return buffer.putInt(offset, (int) value);
        else
            return buffer.putLong(offset, value);
    }

    static String getString(ByteBuffer buffer, int size) {
        int limit = buffer.position() + size;
        int i;
        for (i = buffer.position(); i < limit; ++i)
            if (buffer.get(i) == 0)
                break;
        int totalLimit = buffer.limit();
        buffer.limit(i);
        CharsetDecoder decoder = ExtendedCharsets.UTF_8S.newDecoder();
        String result;
        try {
            result = decoder.decode(buffer).toString();
        } catch (CharacterCodingException e) {
            // UTF-8S decoding is designed to always succeed.
            throw new InternalError(e);
        }
        buffer.limit(totalLimit);
        buffer.position(limit);
        return result;
    }

    private static final int LONG_SIZE = (int) Linker.nativeLinker()
        .canonicalLayouts()
        .get("long")
        .byteSize();
}
