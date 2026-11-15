package enyo.sound.flac;

import java.io.IOException;
import java.util.Arrays;

/**
 * A mutable class for decoding FLAC subframes.
 *
 * Instances are not thread-safe or reentrant.
 */
public final class SubframeDecoder {
    /**
     * Creates a new decoder for a FLAC subframe.
     * A frame typically needs a subframe decoder per channel/subframe,
     * but decoders can be reused for decoding the next frame.
     */
    public SubframeDecoder() {
    }

    /**
     * Decode an entire subframe of samples.
     * This method must be called right after
     * {@linkplain SubframeHeader#SubframeHeader(int,BitInput) decoding
     * the subframe header}.  It must be invoked for all subframes
     * in the frame.  After that the alignment and frame check
     * sequence follows.
     *
     * <p>The decoded samples are written to an internal array
     * returned by {@code #getSamples()}.
     *
     * @param hdr the header of the frame in which the to-be-decoded subframe
     *   resides
     * @param shdr the header of the subframe which is to be decoded
     * @param bits the source of bits for the subframe
     * @throws FlacException if a FLAC format violation is detected
     * @throws IOException if an input error occurred
     * @throws NullPointerException if any of the arguments is null
     */
    public void decode(FrameHeader hdr, SubframeHeader shdr, BitInput bits)
        throws IOException {
        int[] samples = this.samples;
        if (samples == null || samples.length < hdr.blockSize()) {
            samples = new int[hdr.blockSize()];
            this.samples = samples;
        }

        switch (shdr.type()) {
        case CONSTANT:
            {
                // Treat the sample as signed.  This contradicts the
                // specification, which says that numbers are unsigned
                // unless otherwise specified.
                int value = bits.getSignedBits(shdr.bitsPerSample(hdr))
                    << shdr.sampleShift();
                Arrays.fill(samples, 0, hdr.blockSize(), value);
                return;
            }
        case VERBATIM:
            readUnencodedSamples(hdr, shdr, bits, hdr.blockSize());
            applyLeftShift(shdr, hdr.blockSize());
            return;
        case FIXED:
        case LPC:
            int order = shdr.order();

            // Read the unencoded warmup samples.
            readUnencodedSamples(hdr, shdr, bits, order);

            setupCoefficients(order);

            int shift;
            int computeWidth;
            if (shdr.type() == SubframeHeader.Type.LPC) {
                LPCParameters lpc = new LPCParameters(bits);
                for (int i = 0; i < order; ++i)
                    coefficients[i] = lpc.readCoefficient(bits);
                shift = lpc.shift();
                computeWidth = computeWidth(hdr, shdr, order, lpc.precision());
            } else {
                // Type is FIXED.
                fixedCoefficients(order);
                shift = 0;
                computeWidth = computeWidth(hdr, shdr, order, FIXED_PRECISION);
            }
            if (computeWidth > 64)
                throw new FlacException(String.format(
                        "Required sample precision exceeds limit: %d",
                        computeWidth));
            int sampleCheckShift;
            if (computeWidth <= 32)
                sampleCheckShift = 32 - shdr.bitsPerSample(hdr);
            else
                sampleCheckShift = 64 - shdr.bitsPerSample(hdr);
            int[] coefficients = this.coefficients;

            // The current write offset in the samples array.
            int samplesPos = order;

            // Read the residual encoded data and restore the signal.
            Residual residual = new Residual(bits);
            int partitions = residual.partitions();
            for (int partition = 0; partition < partitions; ++partition) {
                int partitionLength =
                    residual.partitionLength(hdr, shdr, partition);
                Residual.Parameter resParam =
                    new Residual.Parameter(residual, bits);
                int parameter = resParam.parameter();

                int sampleIndex = 0;
                try {
                    // Dispatch on the encoding type outside the loop.
                    // This results in some code duplication, but is
                    // presumably a bit faster.
                    if (resParam.isUnencoded())
                        for (; sampleIndex < partitionLength;
                             ++sampleIndex) {
                            int value = bits.getSignedBits(parameter);
                            samples[samplesPos] = value;
                            ++samplesPos;
                        }
                    else if (parameter == 0)
                        for (; sampleIndex < partitionLength;
                             ++sampleIndex) {
                            int value = bits.getRiceUnaryOnly();
                            samples[samplesPos] = value;
                            ++samplesPos;
                        }
                    else
                        for (; sampleIndex < partitionLength;
                             ++sampleIndex) {
                            int value = bits.getRice(parameter);
                            samples[samplesPos] = value;
                            ++samplesPos;
                        }
                } catch (FlacException e) {
                    throw new FlacException(
                        "In sample " + sampleIndex, e);
                }

                if (computeWidth <= 32)
                    computePrediction(
                        samples, samplesPos - partitionLength,
                        samplesPos, coefficients, order, shift,
                        sampleCheckShift);
                else
                    computePredictionLong(
                        samples, samplesPos - partitionLength,
                        samplesPos, coefficients, order, shift,
                        sampleCheckShift);
            }

            applyLeftShift(shdr, hdr.blockSize());
        }
    }

    /**
     * Returns the sample buffer.
     * Must be called after
     * {@link #decode(FrameHeader,SubframeHeader,BitInput)}.
     * Only samples corresponding to the
     * last decoded frame's {@linkplain FrameHeader#blockSize() block size}
     * have been written.
     * @return an array of samples potentially shared with this
     *   decoder instance
     * @throws IllegalStateException
     *   if {@link #decode(FrameHeader,SubframeHeader,BitInput)}
     *   has not been called
     */
    public int[] getSamples() {
        if (samples == null)
            throw new IllegalStateException();
        return samples;
    }

    // Decoded samples.
    private int[] samples;

    // Data used by the LPC predictor.
    private int[] coefficients;

    private void readUnencodedSamples(FrameHeader hdr, SubframeHeader shdr,
        BitInput bits, int count) throws IOException {
        // Treat the samples as signed (see the Arrays.fill() call above).
        int bitsPerSample = shdr.bitsPerSample(hdr);
        for (int i = 0; i < count; ++i)
            samples[i] = bits.getSignedBits(bitsPerSample);
    }

    /**
     * (Re-)allocate the coefficients array if it is missing or not
     * long enough.
     */
    private void setupCoefficients(int count) {
        if (coefficients == null || coefficients.length < count)
            coefficients = new int[count];
    }

    /**
     * Set the (already sized) coefficients array so that the fixed
     * predictors are used.
     */
    private void fixedCoefficients(int order) {
        // For the coefficients, see Tony Robinson, SHORTEN: Simple
        // lossless and near-loss waveform compression, 1994.  They
        // stem from fitting a polynomial to the warmup samples.  It
        // would be possible to open-code four predictors, but the
        // fixed predictors are rarely used (at least at higher
        // compression levels), so this does not seem useful.
        // Likewise for switching between pre-allocated arrays.
        switch (order) {
        case 0:
            break;
        case 1:
            coefficients[0] = 1;
            break;
        case 2:
            coefficients[0] = 2;
            coefficients[1] = -1;
            break;
        case 3:
            coefficients[0] = 3;
            coefficients[1] = -3;
            coefficients[2] = 1;
            break;
        case 4:
            coefficients[0] = 4;
            coefficients[1] = -6;
            coefficients[2] = 4;
            coefficients[3] = -1;
            break;
        default:
            throw new FlacException("Invalid fixed predictor order: " + order);
        }
    }

    // Worst case for the fixedCoefficients(int) parameters.
    private static final int FIXED_PRECISION = 4;

    /**
     * Implementation of LPC.
     *
     *
     * @param residual the value of the residual
     * @param sampleCheckShift shift amount to check that the result
     *   does not overflow the estimated sample width
     */
    private static void computePrediction(
        int[] samples, int first, int last,
        int[] coefficients, int order, int shift, int sampleCheckShift) {
        switch (order)
            {
            case 0:
                break;
            case 1:
                computePrediction1(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 2:
                computePrediction2(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 3:
                computePrediction3(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 4:
                computePrediction4(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 5:
                computePrediction5(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 6:
                computePrediction6(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 7:
                computePrediction7(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 8:
                computePrediction8(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 9:
                computePrediction9(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 10:
                computePrediction10(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 11:
                computePrediction11(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            case 12:
                computePrediction12(samples, first, last,
                    coefficients, shift, sampleCheckShift);
                break;
            default:
                computePredictionFallback(samples, first, last,
                    coefficients, order, shift, sampleCheckShift);
            }
    }

    private static void computePredictionFallback(
        int[] samples, int first, int last,
        int[] coefficients, int order, int shift, int sampleCheckShift) {
        for (int i = first; i < last; ++i) {
            int prediction = 0;
            for (int j = 0; j < order; ++j)
                prediction += coefficients[j] * samples[i - 1 - j];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction1(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction2(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction3(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction4(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction5(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction6(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction7(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        int c6 = coefficients[6];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6]
                + c6 * samples[i - 7];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction8(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        int c6 = coefficients[6];
        int c7 = coefficients[7];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6]
                + c6 * samples[i - 7]
                + c7 * samples[i - 8];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction9(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        int c6 = coefficients[6];
        int c7 = coefficients[7];
        int c8 = coefficients[8];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6]
                + c6 * samples[i - 7]
                + c7 * samples[i - 8]
                + c8 * samples[i - 9];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction10(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        int c6 = coefficients[6];
        int c7 = coefficients[7];
        int c8 = coefficients[8];
        int c9 = coefficients[9];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6]
                + c6 * samples[i - 7]
                + c7 * samples[i - 8]
                + c8 * samples[i - 9]
                + c9 * samples[i - 10];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction11(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        int c6 = coefficients[6];
        int c7 = coefficients[7];
        int c8 = coefficients[8];
        int c9 = coefficients[9];
        int c10 = coefficients[10];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6]
                + c6 * samples[i - 7]
                + c7 * samples[i - 8]
                + c8 * samples[i - 9]
                + c9 * samples[i - 10]
                + c10 * samples[i - 11];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    private static void computePrediction12(
        int[] samples, int first, int last,
        int[] coefficients, int shift, int sampleCheckShift) {
        int c0 = coefficients[0];
        int c1 = coefficients[1];
        int c2 = coefficients[2];
        int c3 = coefficients[3];
        int c4 = coefficients[4];
        int c5 = coefficients[5];
        int c6 = coefficients[6];
        int c7 = coefficients[7];
        int c8 = coefficients[8];
        int c9 = coefficients[9];
        int c10 = coefficients[10];
        int c11 = coefficients[11];
        for (int i = first; i < last; ++i) {
            int prediction
                = c0 * samples[i - 1]
                + c1 * samples[i - 2]
                + c2 * samples[i - 3]
                + c3 * samples[i - 4]
                + c4 * samples[i - 5]
                + c5 * samples[i - 6]
                + c6 * samples[i - 7]
                + c7 * samples[i - 8]
                + c8 * samples[i - 9]
                + c9 * samples[i - 10]
                + c10 * samples[i - 11]
                + c11 * samples[i - 12];
            int result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 32 - sampleCheckShift);
            samples[i] = result;
        }
    }

    /**
     * Implementation of LPC.  Version with 64-bit intermediate results, and
     * integrated with residual reading.
     */
    private static void computePredictionLong(
        int[] samples, int first, int last,
        int[] coefficients, int order, int shift, int sampleCheckShift) {
        for (int i = first; i < last; ++i) {
            long prediction = 0;
            for (int j = 0; j < order; ++j)
                prediction += (long) coefficients[j] * samples[i - 1 - j];
            long result = (prediction >> shift) + samples[i];
            if ((result << sampleCheckShift) >> sampleCheckShift != result)
                throwComputeOverflow(result, 64 - sampleCheckShift);
            samples[i] = (int) result;
        }
    }

    private static void throwComputeOverflow(long sample, int width) {
        throw new FlacException(String.format(
                "Sample value %d exceeds computation width %d",
                sample, width));
    }

    private void applyLeftShift(SubframeHeader shdr, int limit) {
        int shift = shdr.sampleShift();
        if (shift != 0)
            for (int i = 0; i < limit; ++i)
                samples[i] <<= shift;
    }

    /**
     * Returns the number of bits that sample values need.
     */
    private static int computeWidth(FrameHeader hdr,
        SubframeHeader shdr, int order, int precision) {
        int bps = shdr.bitsPerSample(hdr);
        if (order > 0) {
            int log2 = 32 - Integer.numberOfLeadingZeros(order - 1);
            return bps + precision - 1 + log2;
        } else
            return bps + precision - 1;
    }
}
