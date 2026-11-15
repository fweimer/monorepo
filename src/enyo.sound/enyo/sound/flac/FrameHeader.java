package enyo.sound.flac;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Objects;

/**
 * FLAC frame header information.
 *
 * This class corresponds to the {@code FRAME_HEADER} structure.
 *
 * @see <a href="https://xiph.org/flac/format.html#frame_header">FRAME_HEADER</a>
 */
public final class FrameHeader {
    /**
     * Reads a FLAC frame header from the stream.
     * @param bits the data source
     * @param info fallback information that is used if the frame header
     *   does not encode it
     * @throws FlacException if a FLAC format violation is detected
     * @throws EOFException if no frame is available or the frame
     *   header is truncated
     * @throws IOException if an input error occurred
     * @throws NullPointerException if {@code in} is null
     */
    public FrameHeader(BitInput bits, Streaminfo info) throws IOException {
        Objects.requireNonNull(info);
        int sync = bits.getBits(14);
        if (sync != (1 << 14) - 2)
            throw new FlacException(String.format(
                    "Invalid FLAC sync bits: 0x%04x", sync));
        if (bits.getBits(1) != 0)
            throw new FlacException("Reserved bit 0 set in frame header");
        variableBlockSize = bits.getBits(1) != 0;

        int blockSize0 = bits.getBits(4);
        int sampleRate0 = bits.getBits(4);

        channelAssignment = bits.getBits(4);
        if (channelAssignment >= 0b1011)
            throw new FlacException(
                "Reserved channel assignment: " + channelAssignment);

        {
            int b = bits.getBits(3);
            switch (b) {
            case 0:
                bitsPerSample = info.bitsPerSample();
                break;
            case 1:
                bitsPerSample = 8;
                break;
            case 2:
                bitsPerSample = 12;
                break;
            case 4:
                bitsPerSample = 16;
                break;
            case 5:
                bitsPerSample = 20;
                break;
            case 6:
                bitsPerSample = 24;
                break;
            default:
                throw new FlacException(
                    "Reserved sample size indicator: " + b);
            }
        }

        if (bits.getBits(1) != 0)
            throw new FlacException("Reserved bit 1 set in frame header");

        sampleOrFrameNumber = bits.getUTF8Like();

        blockSize = adjustBlockSize(bits, blockSize0);
        sampleRate = adjustSampleRate(bits, info, sampleRate0);

        int crc8 = bits.get8(); // TODO: check CRC
    }

    /**
     * Returns whether this frame (and all others) has variable block size.
     * @return true if the frame has variable block size, false if it has
     *   a fixed block size
     * @see #blockSize()
     */
    public boolean variableBlockSize() {
        return variableBlockSize;
    }

    /**
     * Returns the block size for this frame.  For files with a fixed
     * block size, this is redundant with the information in the
     * {@link Streaminfo} header.
     * @return the block size in bytes, a positive integer
     * @see #variableBlockSize()
     * @see Streaminfo#minimumBlockSize()
     * @see Streaminfo#maximumBlockSize()
     */
    public int blockSize() {
        return blockSize;
    }

    /**
     * Returns the sample rate for this frame.
     * @return the sample rate, in Hertz; a positive integer
     * @see Streaminfo#sampleRate()
     */
    public int sampleRate() {
        return sampleRate;
    }

    /**
     * Returns the channel assignment for this frame.
     * @return a number that describes which channels are in this frame
     */
    public int channelAssignment() {
        return channelAssignment;
    }

    /**
     * Returns the number of channels (subframes) in this frame.
     * @return the channel/subframe count, a positive integer
     */
    public int subframes() {
        if (channelAssignment <= 0b0111)
            return channelAssignment + 1;
        else
            // The other channels use non-independent encoding.
            return 2;
    }

    /**
     * Returns the baseline number of bits per sample (for each channel).
     * Some subframes will have adjusted bit numbers, see
     * {@link SubframeHeader#bitsPerSample(FrameHeader)}.
     * @return the number of bits, a positive integer
     * @see Streaminfo#bitsPerSample()
     * @see SubframeHeader#bitsPerSample(FrameHeader)
     */
    public int bitsPerSample() {
        return bitsPerSample;
    }

    /**
     * Returns the sample or frame number at the start of the frame.
     * @return under {@linkplain #variableBlockSize() variable block size},
     *   the sample number; with a fixed block size, the frame number
     */
    public long sampleOrFrameNumber() {
        return sampleOrFrameNumber;
    }

    private final boolean variableBlockSize;
    private final int blockSize;
    private final int sampleRate;
    private final int channelAssignment;
    private final int bitsPerSample;
    private final long sampleOrFrameNumber;

    private static int adjustBlockSize(BitInput bits, int blockSize)
        throws IOException {
        switch (blockSize) {
        case 0:
            throw new FlacException("Reserved block size zero used");
        case 1:
            return 192;
        case 2:
            return 576;
        case 3:
            return 1152;
        case 4:
            return 2304;
        case 5:
            return 4608;
        case 6:
            return bits.get8() + 1;
        case 7:
            return bits.get16() + 1;
        default:
            return 1 << blockSize;
        }
    }

    private static int adjustSampleRate(BitInput bits, Streaminfo info,
        int sampleRate) throws IOException {
        switch (sampleRate) {
        case 0:
            return info.sampleRate();
        case 1:
            return 88200;
        case 2:
            return 176400;
        case 3:
            return 192000;
        case 4:
            return 8000;
        case 5:
            return 1600;
        case 6:
            return 22050;
        case 7:
            return 24000;
        case 8:
            return 32000;
        case 9:
            return 44100;
        case 10:
            return 48000;
        case 11:
            return 96000;
        case 12:
            return bits.get8() * 1000;
        case 13:
            return bits.get16();
        case 14:
            return bits.get16() * 10;
        default:
            throw new FlacException(
                "Invalid sample rate in frame header:" + sampleRate);
        }
    }
}
