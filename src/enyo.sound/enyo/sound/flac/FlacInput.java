package enyo.sound.flac;

import enyo.io.BufferInput;
import enyo.io.SimpleBufferInput;
import enyo.sound.pcm.FrameFormat;
import enyo.sound.pcm.PCMFormat;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Turning a FLAC stream into a byte stream containing 16-bit or
 * 24-bit little-endian samples.
 *
 * @see <a href="https://xiph.org/flac/format.html#stream">STREAM</a>
 */
public final class FlacInput extends SimpleBufferInput {
    /**
     * Starts reading a FLAC file.
     * @param in the source of the FLAC data
     * @throws FlacException if a FLAC format violation is detected
     * @throws IOException if an input error occured
     * @throws NullPointerException if {@code in} is null
     */
    public FlacInput(BufferInput in) throws IOException {
        this.in = in;
        this.bits = new BitInput(in);
        int magic = in.fillBuffer(4).getInt();
        if (magic != 0x664c6143) // "fLaC"
            throw new FlacException(String.format(
                    "Invalid FLAC magic: %08x", magic));
        bits.unwrap();

        currentMetadata = new MetadataInput(in);
        if (!currentMetadata.type().equals(BlockType.E.STREAMINFO))
            throw new FlacException(
                "FLAC file does not start with STREAMINFO: "
                + currentMetadata.type());
        streaminfo = new Streaminfo(currentMetadata);
    }

    /**
     * Returns the next metadata stream.
     * Reading sample data implicitly consumes all metadata streams.
     * @return the next metadata stream, or null if all such streams
     *   have been consumed
     * @throws FlacException if a FLAC format violation is detected
     * @throws IOException if an input error occurs
     */
    public MetadataInput nextMetadata() throws IOException {
        if (currentMetadata == null)
            return null;
        currentMetadata.skipToEnd();
        if (currentMetadata.moreBlocks()) {
            currentMetadata = new MetadataInput(in);
            return currentMetadata;
        } else {
            currentMetadata = null;
            return null;
        }
    }

    /**
     * Returns the {@link Streaminfo} data for this FLAC file.  This
     * method can be called at any time and returns the same data.
     * @return information about this FLAC file
     */
    public Streaminfo streaminfo() {
        return streaminfo;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    protected ByteBuffer fillBuffer() throws IOException {
        if (!readNewFrame()) {
            if (decodedBuffer == null)
                decodedBuffer = ByteBuffer.allocate(0);
        }
        return decodedBuffer;
    }

    /**
     * Returns the number of frames that have been decoded so far.
     * @return the number of frames decoded, a non-negative number
     */
    public long frameCounter() {
        return frameCounter;
    }

    /**
     * Returns the description of the decoded data stream.
     * @return the frame format and frame rate
     */
    public PCMFormat format() {
        return new PCMFormat(new FrameFormat(
                streaminfo().bitsPerSample(), streaminfo().channels(),
                ByteOrder.LITTLE_ENDIAN),
            streaminfo().sampleRate());
    }

    private final BufferInput in;
    private final BitInput bits;
    private final Streaminfo streaminfo;
    private MetadataInput currentMetadata;

    // Cached subframe decoders.
    private SubframeDecoder[] decoders;

    // Current block of decoded data.  Reused if possible.
    private ByteBuffer decodedBuffer;
    private ShortBuffer decodedShortBuffer; // Aliases decodedBuffer.

    // Set to true once the end of the data is reached.
    private boolean eofSeen;

    private long frameCounter;

    /**
     * @return true if sample data is available, false if EOF has been
     *   encountered
     */
    private boolean readNewFrame() throws IOException {
        if (eofSeen)
            return false;

        if (streaminfo().bitsPerSample() != 16
            && streaminfo().bitsPerSample() != 24)
            throw new FlacException("Unsupported bits per sample: "
                + streaminfo().bitsPerSample());

        // Make sure that all the metadata headers have been read.
        while (currentMetadata != null)
            nextMetadata();

        // FIXME: Check EOF against the sample count in the header.
        if (!bits.unwrap().fillBuffer(0).hasRemaining()) {
            eofSeen = true;
            return false;
        }

        FrameHeader frame = new FrameHeader(bits, streaminfo);

        try {
            adjustDecodedBuffer(frame);
            decodeFrame(frame);
            if (streaminfo().bitsPerSample() == 24)
                decodedBytes24Bit(frame);
            else
                decodedBytes16Bit(frame);
        } catch (FlacException e) {
            throw new FlacException(
                "FLAC decoding error in frame " + frameCounter, e);
        }

        ++frameCounter;
        return true;
    }

    /**
     * Populates the decoders for all the subframes in this frame with
     * data.
     */
    private void decodeFrame(FrameHeader frame) throws IOException {
        int subframes = frame.subframes();
        adjustDecoders(subframes);

        // Decode all subframes.
        for (int subframeIndex = 0; subframeIndex < subframes;
             ++subframeIndex) {
            SubframeHeader subframe = new SubframeHeader(subframeIndex, bits);
            SubframeDecoder decoder = decoders[subframeIndex];
            decoder.decode(frame, subframe, bits);
        }

        // Process the frame footer.
        bits.getFooter(); // FIXME: Add CRC check.
    }

    /**
     * (Re-)allocate the decoders array to accomodate the requested
     * number of subframes.  Allocate new decoder objects if
     * necessary.
     */
    private void adjustDecoders(int subframes) {
        if (decoders == null) {
            decoders = new SubframeDecoder[subframes];
            for (int i = 0; i < subframes; ++i)
                decoders[i] = new SubframeDecoder();
        } else if (decoders.length < subframes) {
            int oldLength = decoders.length;
            decoders = Arrays.copyOf(decoders, subframes);
            for (int i = oldLength; i < subframes; ++i)
                decoders[i] = new SubframeDecoder();
        }
    }

    /**
     * Copy the data from the decoders to decodedBuffer.  Version with
     * 16 bits per sample.
     */
    private void decodedBytes16Bit(FrameHeader frame) {
        ShortBuffer buffer = decodedShortBuffer;
        int blockSize = frame.blockSize();
        int assignment = frame.channelAssignment();
        int channels = streaminfo().channels();
        if (frame.channelAssignment() == 0 && channels == 1) {
            // Mono frame in mono stream.
            int[] samples = decoders[0].getSamples();
            for (int i = 0; i < blockSize; ++i)
                buffer.put(checkedShortConversion(samples[i]));
        } else if (assignment == 0 && channels == 2) {
            // Mono frame in stereo stream.  Output the frame data on
            // both channels.
            int[] samples = decoders[0].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                short value = checkedShortConversion(samples[i]);
                buffer.put(value);
                buffer.put(value);
            }
        } else if (assignment == 1 && channels == 2) {
            // Stereo frame in stereo stream.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                buffer.put(checkedShortConversion(samples0[i]));
                buffer.put(checkedShortConversion(samples1[i]));
            }
        } else if (assignment == 0b1000 && channels == 2) {
            // Stereo frame with the second channel encoded as
            // difference.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                int sample = samples0[i];
                buffer.put(checkedShortConversion(sample));
                buffer.put(checkedShortConversion(sample - samples1[i]));
            }
        } else if (assignment == 0b1001 && channels == 2) {
            // Stereo frame with the first channel encoded as
            // difference.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                int sample = samples1[i];
                buffer.put(checkedShortConversion(samples0[i] + sample));
                buffer.put(checkedShortConversion(sample));
            }
        } else if (assignment == 0b1010 && channels == 2) {
            // Stereo frame encoded as average and frame difference.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                int average = samples0[i];
                int difference = samples1[i];
                // Undo the rounding of the average.
                average = Math.addExact(average, average) + (difference & 1);
                buffer.put(checkedShortConversion(
                        Math.addExact(average, difference) >> 1));
                buffer.put(checkedShortConversion(
                        Math.subtractExact(average, difference) >> 1));
            }
        } else
            throw new FlacException(String.format(
                    "Subframe channel assignment %d (subframe count: %d)"
                    + " is incompatible with stream channel count %d",
                    assignment, frame.subframes(), channels));


        // Restrict to the region of the samples that were written.
        decodedBuffer.limit(buffer.position() * 2);
    }

    /**
     * Copy the data from the decoders to decodedBuffer.  Version with
     * 16 bits per sample.
     */
    private void decodedBytes24Bit(FrameHeader frame) {
        int blockSize = frame.blockSize();
        int assignment = frame.channelAssignment();
        int channels = streaminfo().channels();
        if (frame.channelAssignment() == 0 && channels == 1) {
            // Mono frame in mono stream.
            int[] samples = decoders[0].getSamples();
            for (int i = 0; i < blockSize; ++i)
                putDecoded24(samples[i]);
        } else if (assignment == 0 && channels == 2) {
            // Mono frame in stereo stream.  Output the frame data on
            // both channels.
            int[] samples = decoders[0].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                putDecoded24(samples[i]);
                putDecoded24(samples[i]);
            }
        } else if (assignment == 1 && channels == 2) {
            // Stereo frame in stereo stream.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                putDecoded24(samples0[i]);
                putDecoded24(samples1[i]);
            }
        } else if (assignment == 0b1000 && channels == 2) {
            // Stereo frame with the second channel encoded as
            // difference.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                int sample = samples0[i];
                putDecoded24(sample);
                putDecoded24(sample - samples1[i]);
            }
        } else if (assignment == 0b1001 && channels == 2) {
            // Stereo frame with the first channel encoded as
            // difference.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                int sample = samples1[i];
                putDecoded24(samples0[i] + sample);
                putDecoded24(sample);
            }
        } else if (assignment == 0b1010 && channels == 2) {
            // Stereo frame encoded as average and frame difference.
            int[] samples0 = decoders[0].getSamples();
            int[] samples1 = decoders[1].getSamples();
            for (int i = 0; i < blockSize; ++i) {
                int average = samples0[i];
                int difference = samples1[i];
                // Undo the rounding of the average.
                average = Math.addExact(average, average) + (difference & 1);
                putDecoded24(Math.addExact(average, difference) >> 1);
                putDecoded24(Math.subtractExact(average, difference) >> 1);
            }
        } else
            throw new FlacException(String.format(
                    "Subframe channel assignment %d (subframe count: %d)"
                    + " is incompatible with stream channel count %d",
                    assignment, frame.subframes(), channels));


        // Restrict to the region of the samples that were written.
        decodedBuffer.flip();
    }

    /**
     * If necessary, (re-)eallocates decodedBuffer to accomodate the
     * data in current frame.
     */
    private void adjustDecodedBuffer(FrameHeader frame) {
        // Use channels() because the decoded frame must always
        // contain the expected number of channels, duplicating
        // channels if necessary.
        int bitCount = frame.blockSize() * frame.bitsPerSample()
            * streaminfo().channels();
        if ((bitCount % 8) != 0)
            throw new FlacException(
                "Decoded frame size in bits is not a multiple of 8: "
                + bitCount);
        int byteCount = bitCount / 8;
        if (decodedBuffer == null || decodedBuffer.capacity() < byteCount) {
            decodedBuffer = ByteBuffer.allocate(byteCount)
                .order(ByteOrder.LITTLE_ENDIAN);
            decodedShortBuffer = decodedBuffer.asShortBuffer();
        } else {
            decodedBuffer.clear(); // Will be adjusted after decoding.
            decodedShortBuffer.clear(); // Only part will be used.
        }
    }

    private static short checkedShortConversion(int original) {
        short truncated = (short) original;
        if (original != truncated)
            throw new FlacException(
                "Out-of-range sample value: " + original);
        return truncated;
    }

    private void putDecoded24(int original) {
        int msb0 = original >> 16;
        byte msb = (byte) msb0;
        if (msb != msb0)
            throw new FlacException(
                "Out-of-range sample value: " + original);
        decodedBuffer.putShort((short) original).put(msb);
    }
}
