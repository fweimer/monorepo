package enyo.util;

import java.util.BitSet;
import java.util.Collection;

/**
 * An enum bit set backed by a bit set.
 * The sets are modifiable.
 */
public final class BitSetEnumBitSet<E extends Enum<E>>
    extends AbstractEnumBitSet<E> {
    /**
     * Creates a new bit set for the specified enum type.
     * @param type the reified enum type
     * @throws IllegalArgumentException if {@code type} is not an enum type
     * @throws NullPointerException if {@code type} is null
     */
    public BitSetEnumBitSet(Class<E> type) {
        super(type);
        this.bits = new BitSet();
    }

    @Override
    public int bitLength() {
        return bits.length();
    }

    @Override
    public int size() {
        return bits.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return bits.isEmpty();
    }

    @Override
    public BitSet toBitSet() {
        return (BitSet) bits.clone();
    }

    @Override
    public boolean contains(int value) {
        checkValue(value);
        try {
            return bits.get(value);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("value: " + value);
        }
    }

    @Override
    public boolean add(int value) {
        checkValue(value);
        if (bits.get(value))
            return false;
        else {
            bits.set(value);
            return true;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean added = false;
        for (E e : c)
            if (add(e))
                added = true;
        return added;
    }

    @Override
    public boolean remove(int value) {
        checkValue(value);
        if (bits.get(value)) {
            bits.clear(value);
            return true;
        } else
            return false;
    }

    @Override
    public void clear() {
        bits.clear();
    }

    private final BitSet bits;

    private static void checkValue(int value) {
        if (value < 0 || value > Integer.MAX_VALUE / 8)
            throw new IllegalArgumentException("value: " + value);
    }
}
