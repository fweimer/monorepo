package enyo.sound.tests;

import enyo.sound.pcm.FrameFormat;
import enyo.sound.pcm.PCMFormat;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class WaveFormat implements Serializable {
    /**
     * The number of bytes in a minimum-sized WAVE header.
     */
    public static final int MINIMUM_HEADER_SIZE = 44;

    public int channels() {
        return channels;
    }

    public int sampleRate() {
        return sampleRate;
    }

    public int bitsPerSample() {
        return bitsPerSample;
    }

    public long sampleDataSize() {
        return sampleDataSize;
    }

    public int sampleSize() {
        return channels() * bitsPerSample() / 8;
    }

    public long sampleCount() {
        return sampleDataSize() / sampleSize();
    }

    public long expectedFileSize() {
        return sampleDataSize + headerSize;
    }

    public PCMFormat format() {
        return new PCMFormat(
            new FrameFormat(bitsPerSample(), channels(),
                ByteOrder.LITTLE_ENDIAN),
            sampleRate());
    }

    public static WaveFormat getHeader(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < MINIMUM_HEADER_SIZE)
            throw new BufferUnderflowException();

        int oldPosition = buffer.position();
        ByteOrder oldOrder = buffer.order();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        boolean good = false;
        try {
            var result = getHeader0(buffer);
            good = true;
            return result;
        } finally {
            buffer.order(oldOrder);
            if (!good)
                buffer.position(oldPosition);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "WaveFormat(channels=%d, sampleRate=%d, bitsPerSample=%d,"
            + " sampleDataSize=%d)",
            channels, sampleRate, bitsPerSample, sampleDataSize);
    }

    private final int channels;
    private final int sampleRate;
    private final int bitsPerSample;
    private final long sampleDataSize;
    private final int headerSize;

    private WaveFormat(int channels, int sampleRate, int bitsPerSample,
        long sampleDataSize, int headerSize) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.sampleDataSize = sampleDataSize;
        this.headerSize = headerSize;
    }

    private static WaveFormat getHeader0(ByteBuffer buffer)
        throws IOException {
        int value = buffer.getInt();
        if (value != 0x46464952)
            throw new IOException(
                "Invalid RIFF magic: 0x" + Integer.toHexString(value));

        int riffSize = buffer.getInt();

        value = buffer.getInt();
        if (value != 0x45564157)
            throw new IOException(
                "Invalid WAVE magic: 0x" + Integer.toHexString(value));

        value = buffer.getInt();
        if (value != 0x20746d66)
            throw new IOException(
                "Invalid fmt magic: 0x" + Integer.toHexString(value));

        int fmtSize = buffer.getInt();
        if (fmtSize >= 0 && fmtSize < 16)
            throw new IOException("Invalid fmt size: " + fmtSize);
        if (fmtSize != 16)
            throw new IOException("Unsupported fmtSize: " + fmtSize);

        int headerSize = MINIMUM_HEADER_SIZE - 16 + fmtSize;

        value = buffer.getShort();
        if (value != 1)
            throw new IOException("Unrecognized audio format: " + value);

        int channels = Short.toUnsignedInt(buffer.getShort());
        if (channels < 1)
            throw new IOException("Invalid channel count: " + channels);

        int sampleRate = buffer.getInt();
        if (sampleRate < 0)
            throw new IOException("Invalid sample rate: " + sampleRate);

        int byteRate = buffer.getInt();
        if (byteRate < 0)
            throw new IOException("Invalid byte rate: " + byteRate);

        int frameSizeInBytes = Short.toUnsignedInt(buffer.getShort());
        int bitsPerSample = Short.toUnsignedInt(buffer.getShort());

        if (frameSizeInBytes != channels * bitsPerSample / 8)
            throw new IOException(String.format(
                    "Invalid frame size: %d != %d * %d / 8",
                    frameSizeInBytes, channels, bitsPerSample));

        if (byteRate != sampleRate * frameSizeInBytes)
            throw new IOException(String.format(
                    "Invalid byte rate: %d != %d * %d",
                    byteRate, sampleRate, frameSizeInBytes));

        value = buffer.getInt();
        if (value != 0x61746164)
            throw new IOException(
                "Invalid data magic: 0x" + Integer.toHexString(value));

        value = buffer.getInt();
        if (riffSize != 36 + value)
            throw new IOException(String.format(
                    "RIFF size and data size do not match: %d, %d",
                    riffSize, value));

        long sampleBytes = Integer.toUnsignedLong(value);
        if ((sampleBytes % frameSizeInBytes) != 0)
            throw new IOException(String.format(
                    "Data size %d is not a multiple of frame size %d",
                    sampleBytes, frameSizeInBytes));

        return new WaveFormat(channels, sampleRate, bitsPerSample,
            sampleBytes, headerSize);
    }

    public static WaveFormat readHeader(InputStream in) throws IOException {
        byte[] header = in.readNBytes(MINIMUM_HEADER_SIZE);
        if (header.length < MINIMUM_HEADER_SIZE)
            throw new EOFException("WAVE file too short: " + header);
        ByteBuffer buffer = ByteBuffer.wrap(header)
            .order(ByteOrder.LITTLE_ENDIAN);

        int value = buffer.getInt();
        if (value != 0x46464952)
            throw new IOException(
                "Invalid RIFF magic: 0x" + Integer.toHexString(value));

        int riffSize = buffer.getInt();

        value = buffer.getInt();
        if (value != 0x45564157)
            throw new IOException(
                "Invalid WAVE magic: 0x" + Integer.toHexString(value));

        value = buffer.getInt();
        if (value != 0x20746d66)
            throw new IOException(
                "Invalid fmt magic: 0x" + Integer.toHexString(value));

        int fmtSize = buffer.getInt();
        if (fmtSize >= 0 && fmtSize < 16)
            throw new IOException("Invalid fmt size: " + fmtSize);

        int headerSize = MINIMUM_HEADER_SIZE - 16 + fmtSize;

        value = buffer.getShort();
        if (value != 1)
            throw new IOException("Unrecognized audio format: " + value);

        int channels = Short.toUnsignedInt(buffer.getShort());
        if (channels < 1)
            throw new IOException("Invalid channel count: " + channels);

        int sampleRate = buffer.getInt();
        if (sampleRate < 0)
            throw new IOException("Invalid sample rate: " + sampleRate);

        int byteRate = buffer.getInt();
        if (byteRate < 0)
            throw new IOException("Invalid byte rate: " + byteRate);

        int frameSizeInBytes = Short.toUnsignedInt(buffer.getShort());
        int bitsPerSample = Short.toUnsignedInt(buffer.getShort());

        if (frameSizeInBytes != channels * bitsPerSample / 8)
            throw new IOException(String.format(
                    "Invalid frame size: %d != %d * %d / 8",
                    frameSizeInBytes, channels, bitsPerSample));

        if (byteRate != sampleRate * frameSizeInBytes)
            throw new IOException(String.format(
                    "Invalid byte rate: %d != %d * %d",
                    byteRate, sampleRate, frameSizeInBytes));

        if (fmtSize != 16) {
            // Skip over the tail of the format section.  8 bytes
            // following the initial 16 bytes have already been read.
            if (fmtSize > 24) {
                in.skipNBytes(Integer.toUnsignedLong(fmtSize) - 24);
                buffer.clear();
            } else {
                for (int i = 16; i < fmtSize; ++i)
                    buffer.get();
                buffer.compact();
            }
            // Replenish the data chunk header.
            while (buffer.position() < 8) {
                int b = in.read();
                if (b < 0)
                    throw new EOFException();
                buffer.put((byte) b);
            }
        }

        value = buffer.getInt();
        if (value != 0x61746164)
            throw new IOException(
                "Invalid data magic: 0x" + Integer.toHexString(value));

        value = buffer.getInt();
        if (riffSize != 36 + value)
            throw new IOException(String.format(
                    "RIFF size and data size do not match: %d, %d",
                    riffSize, value));

        long sampleBytes = Integer.toUnsignedLong(value);
        if ((sampleBytes % frameSizeInBytes) != 0)
            throw new IOException(String.format(
                    "Data size %d is not a multiple of frame size %d",
                    sampleBytes, frameSizeInBytes));

        return new WaveFormat(channels, sampleRate, bitsPerSample,
            sampleBytes, headerSize);
    }

    private static final long serialVersionUID = 0x4626358af4b9b5efL;
}
