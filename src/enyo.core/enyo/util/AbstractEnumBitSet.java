package enyo.util;

import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Abstract base class for {@link EnumBitSet} implementations.
 *
 * An implementation of a read-only set only needs to override the
 * following methods:
 * <ul>
 * <li>{@link #contains(int)}
 * <li>{@link #bitLength()}
 * </ul>
 * Modifiable sets need to override the following additional methods:
 * <ul>
 * <li>{@link #add(int)}
 * <li>{@link #addAll(Collection)}
 * <li>{@link #clear()}
 * <li>{@link #remove(int)}
 * </ul>
 * The default implementations provided by this class throw
 * {@link UnsupportedOperationException}.
 *
 * @param <E> the underlying {@linkplain Enum enum type} of this bit set
 */
public abstract class AbstractEnumBitSet<E extends Enum<E>>
    implements EnumBitSet<E> {
    /**
     * Creates a new bit set for the specified enum type.
     * @param type the reified enum type
     * @throws IllegalArgumentException if {@code type} is not an enum type
     * @throws NullPointerException if {@code type} is null
     */
    protected AbstractEnumBitSet(Class<E> type) {
        if (!type.isEnum())
            throw new IllegalArgumentException(
                "Not an enum type: " + type.getName());
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public final Class<E> type() {
        return type;
    }

    @Override
    public int size() {
        int size = 0;
        int bitLength = bitLength();
        for (int i = 0; i < bitLength; ++i)
            if (contains(i))
                ++size;
        return size;
    }

    @Override
    public boolean isEmpty() {
        return bitLength() == 0;
    }

    @Override
    public boolean contains(E e) {
        checkType(e);
        return contains(e.ordinal());
    }

    @Override
    public Stream<Object> stream() {
        int bitLength = bitLength();
        if (bitLength == 0)
            return Stream.of();

        E[] values = type.getEnumConstants();
        return IntStream.range(0, bitLength)
            .filter(this::contains)
            .mapToObj(i -> i < values.length ? values[i] : i);
    }

    @Override
    public BitSet toBitSet() {
        int bitLength = bitLength();
        BitSet result = new BitSet(bitLength);
        for (int i = 0; i < bitLength; ++i)
            if (contains(i))
                result.set(i);
        return result;
    }

    @Override
    public Set<Object> toSet() {
        return stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean add(E e) {
        checkType(e);
        return add(e.ordinal());
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(E e) {
        checkType(e);
        return remove(e.ordinal());
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof EnumBitSet<?>))
            return false;
        EnumBitSet<?> other = (EnumBitSet<?>) o;
        if (type() != other.type())
            return false;
        int bitLength = bitLength();
        if (bitLength != other.bitLength())
            return false;
        for (int i = 0; i < bitLength; ++i)
            if (contains(i) != other.contains(i))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        // This gives the same results as BitSet#hashCode().
        int bitLength = bitLength();
        int wordCount = (bitLength + 63) / 64;
        long h = 1234;
        for (int i = wordCount; --i >= 0; ) {
            long word = 0;
            for (int j = 0; j < 64; ++j)
                if (contains(i * 64 + j))
                    word |= 1L << j;
            h ^= word * (i + 1);
        }
        return (int) ((h >> 32) ^ h);
    }

    @Override
    public String toString() {
        return '['
            + stream().map(Object::toString).collect(Collectors.joining(", "))
            + ']';
    }

    private final Class<E> type;

    private void checkType(Enum<?> e) {
        checkType(e, type);
    }

    private static void checkType(Enum<?> e, Class<?> type) {
        Class<?> enumClass = e.getDeclaringClass();
        if (enumClass != type)
            throw new ClassCastException(
                "Enum constant " + enumClass.getName() + '.' + e.name()
                + " is not of type " + type.getName());
    }
}
