package enyo.sound.flac;

import enyo.io.BufferInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * The {@code STREAMINFO} FLAC header.
 *
 * Instances of this class are immutable.
 */
public final class Streaminfo {
    /**
     * Reads stream information from the input stream.
     * @param in the data source
     * @throws IOException if an input error occurred
     * @throws FlacException if an invalid header is detected
     * @throws NullPointerException if {@code in} is null
     */
    public Streaminfo(BufferInput in) throws IOException {
        BitInput bits = new BitInput(in);

        minimumBlockSize = bits.get16();
        if (minimumBlockSize == 0)
            throw new FlacException("Minimum block size is 0");

        maximumBlockSize = bits.get16();
        if (minimumBlockSize > maximumBlockSize)
            throw new FlacException(String.format(
                    "Minimum block size greater than maximum: %d > %d",
                    minimumBlockSize, maximumBlockSize));

        minimumFrameSize = bits.get24();
        maximumFrameSize = bits.get24();
        if (maximumFrameSize != 0 && minimumFrameSize > maximumFrameSize)
            throw new FlacException(String.format(
                    "Minimum frame size greater than maximum: %d > %d",
                    minimumFrameSize, maximumFrameSize));

        sampleRate = bits.getBits(20);
        if (sampleRate == 0 || sampleRate > 655350)
            throw new FlacException("Invalid sample rate: " + sampleRate);

        channels = bits.getBits(3) + 1;
        bitsPerSample = bits.getBits(5) + 1;
        totalSamples = bits.getBitsLong(36);
        digest = bits.getBytes(16);

        bits.unwrap();
    }

    /**
     * Returns the minimum block size.
     * @return a positive integer, counted in samples
     */
    public int minimumBlockSize() {
        return minimumBlockSize;
    }

    /**
     * Resturns the maximum block size.
     * @return a positive integer, counted in samples
     */
    public int maximumBlockSize() {
        return maximumBlockSize;
    }

    /**
     * Returns the minimum block size.
     * @return zero if the minimum block size is unknown, or a positive
     *   byte count
     */
    public int minimumFrameSize() {
        return minimumFrameSize;
    }

    /**
     * Returns the maximum block size.
     * @return zero if the maximum block size is unknown, or a positive
     *   byte count
     */
    public int maximumFrameSize() {
        return maximumFrameSize;
    }

    /**
     * Returns the sample rate.
     * @return the rate, measured in Hertz
     */
    public int sampleRate() {
        return sampleRate;
    }

    /**
     * Returns the number of channels.
     * @return the channel count, a positive integer
     */
    public int channels() {
        return channels;
    }

    /**
     * Returns the number of bits in each sample.
     * @return the bit count, a positive integer; each channel has this
     *   many bits
     */
    public int bitsPerSample() {
        return bitsPerSample;
    }

    /**
     * The total number of samples in the file.  Each sample contains
     * all channels, so the duration of the file in seconds is this
     * number divided by the {@linkplain #sampleRate() sample rate}.
     * @return the total number of samples in the file, or zero if this
     *   value is unknown
     */
    public long totalSamples() {
        return totalSamples;
    }

    /**
     * Returns the MD5 digest of the raw audio data.
     * @return a copy of the digest (16 bytes)
     */
    public byte[] digest() {
        return digest.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FLAC blockSize=");
        sb.append(minimumBlockSize());
        if (minimumBlockSize() != maximumBlockSize()) {
            sb.append('-');
            sb.append(maximumBlockSize());
        }

        if (minimumBlockSize() != 0 || maximumBlockSize() != 0) {
            sb.append(" frameSize=");
            if (minimumFrameSize() != 0)
                sb.append(minimumFrameSize());
            sb.append('-');
            if (maximumFrameSize() != 0)
                sb.append(maximumFrameSize());
        }

        sb.append(" sampleRate=");
        sb.append(sampleRate());

        sb.append(" channels=");
        sb.append(channels());

        sb.append(" bitsPerSample=");
        sb.append(bitsPerSample());

        if (totalSamples() != 0) {
            sb.append(" totalSamples=");
            sb.append(totalSamples());
        }

        return sb.toString();
    }

    private final int minimumBlockSize;
    private final int maximumBlockSize;
    private final int minimumFrameSize;
    private final int maximumFrameSize;
    private final int sampleRate;
    private final int channels;
    private final int bitsPerSample;
    private final long totalSamples;
    private final byte[] digest;
}
