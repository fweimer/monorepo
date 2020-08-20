package enyo.util;

import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Collection;

final class IntBufferEnumBitSet<E extends Enum<E>>
    extends AbstractEnumBitSet<E> {
    IntBufferEnumBitSet(Class<E> type, IntBuffer buffer) {
        super(type);
        this.buffer = buffer;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < buffer.capacity(); ++i)
            if (buffer.get(i) != 0)
                return false;
        return true;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < buffer.capacity(); ++i)
            size += Integer.bitCount(buffer.get(i));
        return size;
    }

    @Override
    public int bitLength() {
        for (int index = buffer.capacity() - 1; index >= 0; --index) {
            int value = buffer.get(index);
            if (value != 0) {
                int clz = Integer.numberOfLeadingZeros(value);
                return BITS * index + (BITS - clz);
            }
        }
        return 0;
    }

    @Override
    public boolean contains(int value) {
        // NB: Open-code parts of index(int).  All non-negative
        // out-of-bounds integers are absent from the set.
        if (value < 0)
            throw new IllegalArgumentException("value: " + value);
        int index = value / BITS;
        int mask = mask(value);
        return (getOrZero(index) & mask) != 0;
    }

    @Override
    public boolean add(int value) {
        int index = index(value);
        int mask = mask(value);
        int old = buffer.get(index);
        try {
            buffer.put(index, old | mask);
        } catch (ReadOnlyBufferException e) {
            throw new UnsupportedOperationException(e);
        }
        return (old & mask) == 0;
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
        int index = index(value);
        int mask = mask(value);
        int old = buffer.get(index);
        try {
            buffer.put(index, old & ~mask);
        } catch (ReadOnlyBufferException e) {
            throw new UnsupportedOperationException(e);
        }
        return (old & mask) != 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < buffer.capacity(); ++i)
            buffer.put(i, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        else if (o.getClass() == IntBufferEnumBitSet.class) {
            IntBufferEnumBitSet<?> other = (IntBufferEnumBitSet<?>) o;
            if (type() != other.type())
                return false;
            int mismatch = buffer.mismatch(other.buffer);
            if (mismatch < 0)
                return true;
            int limit = Math.max(buffer.capacity(), other.buffer.capacity());
            for (int i = mismatch; i < limit; ++i)
                if (getOrZero(i) != other.getOrZero(i))
                    return false;
            return true;
        } else
            return super.equals(o);
    }

    private final IntBuffer buffer;

    // Number of bits in an int.
    private final static int BITS = 32;

    private int index(int value) {
        int index = value / BITS;
        if (value < 0 || index >= buffer.capacity())
            throw new IllegalArgumentException("value: " + value);
        return index;
    }

    private static int mask(int value) {
        return 1 << (value % BITS);
    }

    // Zero-extends the buffer beyond its capacity.
    private int getOrZero(int index) {
        if (index >= buffer.capacity())
            return 0;
        else
            return buffer.get(index);
    }
}
