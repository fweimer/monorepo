package enyo.sound.flac;

import java.io.IOException;

/**
 * Liner predictive coding parameters.
 *
 * These are found in an {@link SubframeHeader.Type#LPC} subframe.
 */
public final class LPCParameters {
    /**
     * Reads the parameters from the bit source.  This must be done
     * after reading the unencoded warmup samples in the subframe.
     * @param bits the data source
     * @throws FlacException if malformed data is detected
     * @throws IOException if an input error occurred
     */
    public LPCParameters(BitInput bits) throws IOException {
        precision = (byte) (bits.getBits(4) + 1);
        if (precision == 16)
            throw new FlacException("Invalid LPC coefficient precision");
        shift = (byte) bits.getSignedBits(5);
        if (shift < 0)
            // The specification says that the shift value is signed,
            // but this implementation cannot handle it.  It might be
            // possible to turn it into a left shift, if this is the
            // intent of the specification.  But RFC 9639 bans negative
            // shift values.
            throw new FlacException("Invalid LPC coefficient shift: " + shift);
    }

    /**
     * Returns the number of bits in the coefficients that follow the
     * LPC parameters.
     * @return the bit count, a positive number
     */
    public int precision() {
        return precision;
    }

    /**
     * Returns the right-shift amount to be applied after computing
     * the LPC linear combination.
     *
     * This is a fixed-point scale factor.  The shift is an arithmetic
     * right shift ({@code >>}), not a logical right shift ({@code >>>}).
     * @return an integer in the range from 0 to 15
     */
    public int shift() {
        return shift;
    }

    /**
     * Reads an LPC coefficient from the bit source.
     * @param bits the source of the bits being read
     * @return the bits that have been read; a signed integer
     * @throws IOException if an input error occurred
     */
    public int readCoefficient(BitInput bits) throws IOException {
        return bits.getSignedBits(precision);
    }

    private final byte precision;
    private final byte shift;
}
