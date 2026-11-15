package enyo.sound.flac;

import java.io.IOException;

/**
 * FLAC subframe header information.
 *
 * This class corresponds to the {@code SUBFRAME_HEADER} structure.
 *
 * @see <a href="https://xiph.org/flac/format.html#subframe_header">SUBFRAME_HEADER</a>
 */
public final class SubframeHeader {
    /**
     * Reads a FLAC subframe header from the stream.
     *
     * For {@linkplain Type#LPC parameterized LPC subframes},
     * the coefficient layout is automatically read
     * (but not the coefficients themselves).
     *
     * @param bits the data source
     * @param index the number of the subframe (0 is the first subframe)
     * @throws FlacException if a FLAC format violation is detected
     * @throws IOException if an input error occurred
     * @throws IllegalArgumentException if {@code index} is negative or too
     *   large
     * @throws NullPointerException if {@code bits} is null
     */
    public SubframeHeader(int index, BitInput bits) throws IOException {
        this.index = (byte) index;
        if (index < 0 || this.index != index)
            throw new IllegalArgumentException("index: " + index);

        if (bits.getBits(1) != 0)
            throw new FlacException(String.format(
                    "Subframe %d does not start with zero bit", index));

        int type0 = bits.getBits(6);
        if (type0 == 0) {
            type = Type.CONSTANT;
            order = -1;
        } else if (type0 == 1) {
            type = Type.VERBATIM;
            order = -1;
        } else if (type0 >= 8 && type0 <= 12) {
            type = Type.FIXED;
            order = (byte) (type0 - 8);
        } else if (type0 >= 32) {
            type = Type.LPC;
            order = (byte) (type0 - 31);
        } else {
            throw new FlacException(String.format(
                "Reserved type indicator in subframe %d: %s",
                index, type0));
        }

        if (bits.getBits(1) == 0)
            sampleShift = 0;
        else {
            int count = 1;
            while (bits.getBits(1) == 0) {
                ++count;
                if (count > 32)
                    throw new FlacException(
                        "Sample shift out of range in subframe " + index);
            }
            sampleShift = (byte) count;
        }
    }

    /**
     * Returns the subframe number.
     * @return a non-negative integer
     */
    public int index() {
        return index;
    }

    /**
     * Returns the bits per sample for this subframe.
     * @return the bit count, a positive integer
     * @param frame the frame that contains this subframe
     */
    public int bitsPerSample(FrameHeader frame) {
        if (index() >= frame.subframes())
            throw new IllegalStateException(String.format(
                    "Subframe index %d does not match subframe count %d",
                    index(), frame.subframes()));

        switch (frame.channelAssignment()) {
        case 0b1000:
        case 0b1010:
            // Non-indepenent channel encoding: The second subframe
            // has more bits.
            if (index() == 1)
                return frame.bitsPerSample() + 1 - sampleShift();
            break;
        case 0b1001:
            // Non-indepenent channel encoding: The first subframe has
            // more bits.
            if (index() == 0)
                return frame.bitsPerSample() + 1 - sampleShift();
            break;
        }
        return frame.bitsPerSample() - sampleShift();
    }

    /**
     * The FLAC subframe type.
     * @see SubframeHeader#type()
     */
    public enum Type {
        /**
         * Subframe of a constant value.
         * The encoded size of the subframe is determined by the
         * {@linkplain FrameHeader#bitsPerSample() sample size}.
         * @see <a href="https://xiph.org/flac/format.html#subframe_constant">SUBFRAME_CONSTANT</a>
         */
        CONSTANT(false),

        /**
         * Subframe of verbatim data.  The encoded size of the
         * subframe is the product of the
         * {@linkplain FrameHeader#blockSize() block size}
         * and the {@linkplain FrameHeader#bitsPerSample() sample size}.
         * @see <a href="https://xiph.org/flac/format.html#subframe_verbatim">SUBFRAME_VERBATIM</a>
         */
        VERBATIM(false),

        /**
         * Subframe using a fixed predictor.  The subframe starts with
         * a number of samples determined by the
         * {@linkplain SubframeHeader#order() predictor order},
         * each using the number of bits indicated by
         * {@link FrameHeader#bitsPerSample()}.
         * The residual data then follows.
         *
         * <p>Subframes of this type have a
         * {@linkplain SubframeHeader#order() predictor order}.
         *
         * @see <a href="https://xiph.org/flac/format.html#subframe_fixed">SUBFRAME_FIXED</a>
         */
        FIXED(true),

        /**
         * Subframe using a parameterized predictor.  The subframe starts
         * with a number of samples determined by the
         * {@linkplain SubframeHeader#order() predictor order},
         * each using the number of bits indicated by
         * {@link FrameHeader#bitsPerSample()}.
         *
         * <p>The coefficient precision and shift follows
         * (automatically decoded by {@link SubframeHeader}), followed
         * by the coefficients themselves.  The residual data
         * concludes the frame.
         *
         * <p>Subframes of this type have a
         * {@linkplain SubframeHeader#order() predictor order}.
         *
         * @see <a href="https://xiph.org/flac/format.html#subframe_lpc">SUBFRAME_LPC</a>
         */
        LPC(true),
        ;

        /**
         * Returns true if subframes of this type of a
         * {@linkplain SubframeHeader#order() predictor order}.
         * @return true if {@link SubframeHeader#order()} returns a value,
         *   false if it will throw an exception for subframes of this type
         */
        public boolean hasOrder() {
            return hasOrder;
        }

        private final boolean hasOrder;

        private Type(boolean hasOrder) {
            this.hasOrder = hasOrder;
        }
    }

    /**
     * Returns the type of this subframe.
     * @return the subframe type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the predictor order of this subframe.
     *
     * The predictor order determines the number of coefficients
     * (for the {@link Type#LPC} type) and the number of unencoded
     * samples that precede the residual encoding.
     *
     * <p>This method should only be called if {@code type().hasOrder()}
     *   is true.
     * @return the predictor order, a non-negative integer
     * @throws IllegalStateException if {@link Type#hasOrder()} is false
     *   for this subframe's {@linkplain #type() type}.
     */
    public int order() {
        if (order >= 0)
            return order;
        else
            throw new IllegalStateException(
                "Type " + type() + " does not have order");
    }

    /**
     * Returns the shift amount for decoding samples.
     * This is also called <em>wasted bits per sample</em>.
     * @return the number of bit positions samples should be shifted left
     *   during decoding (zero for no shifting)
     */
    public int sampleShift() {
        return sampleShift;
    }

    private final byte index;
    private final Type type;
    private final byte order;
    private final byte sampleShift;
}
