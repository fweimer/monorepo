package enyo.util;

import java.util.BitSet;
import java.util.stream.Stream;

final class UnmodifiableEnumBitSet<E extends Enum<E>>
    extends AbstractEnumBitSet<E> {
    UnmodifiableEnumBitSet(EnumBitSet<E> base) {
        super(base.type());
        this.base = base;
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean contains(E e) {
        return base.contains(e);
    }

    @Override
    public boolean contains(int value) {
        return base.contains(value);
    }

    @Override
    public int bitLength() {
        return base.bitLength();
    }

    @Override
    public Stream<Object> stream() {
        return base.stream();
    }

    @Override
    public BitSet toBitSet() {
        return base.toBitSet();
    }

    private final EnumBitSet<E> base;
}
