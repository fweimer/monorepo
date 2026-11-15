package enyo.sound.pcm;

import java.nio.ByteOrder;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description of the frame format in a PCM data stream.
 *
 * This describes layout properties of individual frames, such the
 * number and size of individual samples in it.
 *
 * <p>The sample rate (or frame rate) has to be tracked separately.
 */
public final class FrameFormat {
    /**
     * Creates a new frame format.
     *
     * @param bitsPerSample
     *   the {@linkplain #bitsPerSample() number of bits per sample};
     *   must be positive
     * @param channels
     *   the {@linkplain #channels() number of channels}; must be positive
     * @param order the {@linkplain #order() byte order of the samples}
     * @throws IllegalArgumentException if {@code bitsPerSample},
     *   {@code channels} are not positive
     * @throws NullPointerException if {@code order} is null
     */
    public FrameFormat(int bitsPerSample, int channels, ByteOrder order) {
        if (bitsPerSample < 1)
            throw new IllegalArgumentException(
                "bitsPerSample: " + bitsPerSample);
        if (channels < 1)
            throw new IllegalArgumentException("channels: " + channels);
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.order = Objects.requireNonNull(order, "order");
    }

    /**
     * Returns a collection of well-known PCM formats.  For example,
     * the returned collection includes the stereo, 16-bit, little-endian
     * format commonly used by CDDA.
     * @return a non-empty collection
     */
    public static Collection<FrameFormat> standardFormats() {
        var formatsLocal = formats;
        if (formatsLocal == null)
            return computeFormats();
        return formatsLocal;
    }

    /**
     * The number of bits in each sample.
     *
     * @return the bit count, a positive number
     */
    public int bitsPerSample() {
        return bitsPerSample;
    }

    /**
     * Returns the number of channels in the PCM data stream.
     * @return the number of channels, a positive number
     *   (1 is mono, 2 is stereo)
     */
    public int channels() {
        return channels;
    }

    /**
     * Returns the byte order of the samples.
     * @return the byte order
     */
    public ByteOrder order() {
        return order;
    }

    /**
     * Returns the frame size in bytes.
     * @return the frame size, a positive integer
     * @throws IllegalStateException if the frame size is not an integral
     *   number of bytes
     * @throws IllegalStateException if the frame size overflows the size
     *   of an integer
     */
    public int frameSizeInBytes() {
        long bits = bitsPerSample() * (long) channels();
        if ((bits & 7) != 0)
            throw new IllegalStateException(
                "Frame size in bits is not a multiple of 8:" + bits);
        long bytes = bits >>> 3;
        if (bytes > Integer.MAX_VALUE)
            throw new IllegalStateException(
                "Frame size is too large:" + bits);
        return (int) bytes;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != FrameFormat.class)
            return false;
        var o = (FrameFormat) other;
        return bitsPerSample == o.bitsPerSample && channels == o.channels
             && order == o.order;
    }

    @Override
    public int hashCode() {
        int orderCode = order == ByteOrder.BIG_ENDIAN ? 0 : 47;
        return bitsPerSample * 1009 + channels * 17 + orderCode;
    }

    public String toString() {
        return String.format(
            "FrameFormat(bitsPersample=%d, channels=%d, order=%s)",
            bitsPerSample(), channels(), order());
    }

    private final int bitsPerSample;
    private final int channels;
    private final ByteOrder order;

    private static Collection<FrameFormat> formats;

    private static Collection<FrameFormat> computeFormats() {
        var formatsLocal = new ArrayList<FrameFormat>();
        for (int channels = 1; channels <= 2; ++channels)
            for (var order : new ByteOrder[] { ByteOrder.BIG_ENDIAN,
                                               ByteOrder.LITTLE_ENDIAN })
                for (int bits = 8; bits <= 32; bits += 8)
                    formatsLocal.add(new FrameFormat(
                            bits, channels, order));
        formats = List.copyOf(formatsLocal);
        return formatsLocal;
    }
}
