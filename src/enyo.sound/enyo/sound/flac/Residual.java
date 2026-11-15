package enyo.sound.flac;

import java.io.IOException;
import java.util.Objects;

/**
 * The encoded residual of a signal in a FLAC subframe.
 * @see SubframeHeader
 */
public final class Residual {
    /**
     * Creates a new residual header.
     * The residual follows the {@linkplain LPCParameters LPC parameters}.
     * Several {@linkplain #partitions() partitions} follow, which
     * have to be read separately after this object.  These
     * partitions have their own {@linkplain Parameter header}.
     * @param bits the source of the data
     * @throws FlacException if a FLAC format violation is detected
     * @throws IOException if an input error occurred
     * @throws NullPointerException if the argument is null
     */
    public Residual(BitInput bits) throws IOException {
        type = Type.read(bits);
        order = (byte) bits.getBits(4);
    }

    /**
     * Returns the base-2 logarithm of the number of encoding partitions.
     * @return a non-negative integer
     * @see #partitions()
     */
    public int order() {
        return order;
    }

    /**
     * Returns the number of encoded partitions.
     * @return a positive integer (a power of two)
     * @see #order()
     */
    public int partitions() {
        return 1 << order;
    }

    /**
     * The encoding type for the partitions.
     */
    public enum Type {
        // Order matters here; ordinal() + 4 is the parameter bit size.
        /**
         * Encoding with 4 bits for the partition parameter.
         */
        RICE,
        /**
         * Encoding with 5 bits for the partition parameter.
         */
        RICE2,
        ;

        /**
         * Reads a residual encoding type from the input.
         * @param bits the source of the data
         * @return the partition encoding type
         * @throws FlacException if a FLAC format violation is detected
         * @throws IOException if an input error occurred
         * @throws NullPointerException if the argument is null
         */
        public static Type read(BitInput bits) throws IOException {
            int type = bits.getBits(2);
            switch (type) {
            case 0:
                return RICE;
            case 1:
                return RICE2;
            default:
                throw new FlacException("Invalid residual type: " + type);
            }
        }
    }

    /**
     * Returns the encoding type of the partitions.
     * @return the encoding type
     */
    public Type type() {
        return type;
    }

    /**
     * Encoding parameter for residual partitions.
     */
    public static final class Parameter {
        /**
         * Creates a new partition paramaeter.
         * The parameters are not yet read.  The caller has to call
         * {@link BitInput#getBits(int)} or
         * {@link BitInput#getRice(int)}, as indicated by
         * {@link #isUnencoded()} and {@link #parameter()}.
         * @param res the residual in which this partition resides
         * @param bits the source of the data
         * @throws IOException if an input error occurred
         * @throws NullPointerException if an argument is null
         */
        public Parameter(Residual res, BitInput bits) throws IOException {
            int parameterBits = 4 + res.type().ordinal();
            int parameter0 = bits.getBits(parameterBits);
            if (parameter0 < (1 << parameterBits) - 1) {
                parameter = (byte) parameter0;
                unencoded = false;
            } else {
                parameter = (byte) bits.getBits(5);
                unencoded = true;
            }
        }

        /**
         * Returns the partition parter.  It has to be passed to the
         * {@linkplain #isUnencoded() appropriate decoding function}.
         * @return the decoding parameter, a non-negative integer
         * @see BitInput#getBits(int)
         * @see BitInput#getRice(int)
         */
        public int parameter() {
            return parameter;
        }

        /**
         * Returns whether the partition is unencoded.  If this method
         * returns true, encoded values should be read using
         * {@link BitInput#getBits(int)}, otherwise
         * {@link BitInput#getRice(int)} must be used.  In both
         * cases, the {@linkplain #parameter() partition parameter}
         * must be passed as the bit count parameter.
         * @return true if the partitions is unencoded, if false
         *   if it uses Rice/Golomb-style encoding.
         * @see BitInput#getBits(int)
         * @see BitInput#getRice(int)
         */
        public boolean isUnencoded() {
            return unencoded;
        }

        private final byte parameter;
        private final boolean unencoded;
    }

    /**
     * Returns the length of the specified partition.
     * @param frame the frame which contains this residual
     * @param subframe the subframe which contains this residual
     * @param partition the index of the residual partition whose
     *   length should be returned (0 for the first partition)
     * @return the length of the partition, measured in samples;
     *   a positive integer
     * @throws NullPointerException if {@code frame} or {@code subframe}
     *   is null
     */
    public int partitionLength(FrameHeader frame, SubframeHeader subframe,
        int partition) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(subframe, "subframe");
        if (partition < 0 || partition >= partitions())
            throw new IllegalArgumentException("partition: " + partition);
        if (partition > 0)
            return frame.blockSize() >> order();
        else
            // First partition is shorter.
            return (frame.blockSize() >> order()) - subframe.order();
    }

    private final byte order;
    private final Type type;
}
