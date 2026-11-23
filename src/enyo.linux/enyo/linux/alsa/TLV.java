package enyo.linux.alsa;

import enyo.util.Either;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;

/**
 * A tag-length-value pairing.
 */
public final class TLV {
    /**
     * The type in a tag-length-value pairing.
     * @see TLV#type()
     */
    public enum Type {
        /** {@code SNDRV_CTL_TLVT_CONTAINER} */
        CONTAINER(0),

        /** {@code SNDRV_CTL_TLVT_DB_SCALE} */
        DB_SCALE(1),

        /** {@code SNDRV_CTL_TLVT_DB_LINEAR} */
        DB_LINEAR(2),

        /** {@code SNDRV_CTL_TLVT_DB_RANGE} */
        DB_RANGE(3),

        /** {@code SNDRV_CTL_TLVT_DB_MINMAX} */
        DB_MINMAX(4),

        /** {@code SNDRV_CTL_TLVT_DB_MINMAX_MUTE} */
        DB_MINMAX_MUTE(5),

        /** {@code SNDRV_CTL_TLVT_CHMAP_FIXED} */
        CHMAP_FIXED(0x101),

        /** {@code SNDRV_CTL_TLVT_VAR} */
        CHMAP_VAR(0x102),

        /** {@code SNDRV_CTL_TLVT_PAIRED} */
        CHMAP_PAIRED(0x103),
        ;

        /**
         * Returns the encoded value of this type.
         * @return the value of the corresponding integer constant
         */
        public int constant() {
            return constant;
        }

        final int constant;

        private Type(int constant) {
            this.constant = constant;
        }

        private static final Map<Integer, Type> MAP;
        static {
            HashMap<Integer, Type> map = new HashMap<>();
            for (Type value : values())
                map.put(value.constant(), value);
            MAP = Map.copyOf(map);
        }
    }

    /**
     * Returns the type of this value.
     * @return the {@linkplain Type type} if available, or the
     *   raw integer value in the error case
     */
    public Either<Type, Integer> type() {
        Type t = Type.MAP.get(type);
        if (t != null)
            return Either.ofLeft(t);
        else
            return Either.ofRight(type);
    }

    /**
     * Returns the contents of this value.
     * @return a byte buffer in native byte order
     */
    public MemorySegment contents() {
        return contents;
    }

    @Override
    public String toString() {
        return String.format("TLV(type=%s, length=%d)",
            type(), contents.byteSize());
    }

    private final int type;
    private final MemorySegment contents;

    // contents should have native byte order
    TLV(int type, MemorySegment contents) {
        this.type = type;
        this.contents = contents;
    }
}
