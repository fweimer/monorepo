package enyo.util;

import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A set whose members can be represented by integers and enum constants.
 *
 * Small integers (less than the count of enum constants in {@code E})
 * can be represented by the enum constant with the same
 * {@link Enum#ordinal()}.  Both representations are treated the same.
 * However, the methods returning objects prefer enum constants if they
 * are evailable.  For example, if the integer 0 is added using
 * {@link #add(int)}, it is expected that the first enum constant
 * is contained in the stream returned by {@link #stream()}, not
 * the {@linkplain Integer integer} 0.
 *
 * @param <E> the underlying {@linkplain Enum enum type} of this bit set
 */
public interface EnumBitSet<E extends Enum<E>> {
    /**
     * Returns the refied type of the enum constants in this set.
     * @return an {@linkplain Enum enum type}
     */
    Class<E> type();

    /**
     * Returns true if this set is empty.
     * @return true if this set is empty, false if it has elements.
     * @see Collection#isEmpty()
     */
    boolean isEmpty();

    /**
     * Returns the number of elements in this set.
     * @return the element count, a non-negative integer
     * @see Collection#size()
     * @see BitSet#cardinality()
     */
    int size();

    /**
     * Determines if an enum constant is contained in this set.
     * @param e the enum constant whose presence is to be checked
     * @return true if the constant is contained in this set
     *   (even if its ordinal value has been added using {@link #add(i)}),
     *   or false if it is absent
     * @throws ClassCastException if {@code e} is not of the
     *   {@linkplain #type() enum type of this bit set}
     * @throws NullPointerException if {@code e} is null
     * @see #contains(int)
     * @see Collection#contains(Object)
     */
    boolean contains(E e);

    /**
     * Determines if an enum constant is contained in this set.
     * @param value the integer whose presence is to be checked
     * @return true if the constant is contained in this set
     *   (even if its ordinal value has been added using {@link #add(i)}),
     *   or false if it is absent
     * @throws IllegalArgumentException if {@code value} is negative
     * @see #contains(Enum)
     * @see Collection#contains(Object)
     * @see BitSet#get(int)
     */
    boolean contains(int value);

    /**
     * Returns the largest integer included in this set, plus 1.
     * @return 0 if this set is empty, otherwise the largest integer
     *   contained in this set plus 1 (considering enum constants by their
     *   {@linkplain Enum#ordinal() ordinal values})
     * @see BitSet#length()
     */
    int bitLength();

    /**
     * Returns a stream with the elements of this set.  Enum constants
     * are used if available, instead of integers.  It is unspecified
     * if future modifications are reflected in the stream.
     * @return a stream of {@linkplain Enum enum constants} and
     * {@linkplain Integer boxed integers}
     */
    Stream<Object> stream();

    /**
     * Returns a copy of this set as a bit set.  The bit set contains
     * the same integers as members as this set.  The two sets are
     * independent.  Changes to one set do not affect the other.
     * @return a new bit set
     */
    BitSet toBitSet();

    /**
     * Returns an immutable set that contains the members of this set.
     * The returned set contains enum constants in preference to the
     * integers of their ordinal value.
     * @return an immutable set
     */
    Set<Object> toSet();

    /**
     * Adds an enum constant to this set.
     * @param e the enum constant to add
     * @return true if the constant has been newly added to this set,
     *   false if it was already present (even if it has been added as
     *   an integer via {@link #add(int)})
     * @throws ClassCastException if {@code e} is not of the
     *   {@linkplain #type() enum type of this bit set}
     * @throws UnsupportedOperationException if this set is unmodifiable
     * @throws NullPointerException if {@code e} is null
     * @see #add(int)
     * @see Collection#add(Object)
     */
    boolean add(E e);

    /**
     * Adds an integer to this set.
     * @param value the non-negative integer to add to this set
     * @return true if the value has been newly added to this set,
     *   false if it was already present (even if it has been added as
     *   an enum constant via {@link #add(Enum)})
     * @throws IllegalArgumentException if {@code value} is negative
     *   or too large
     * @throws UnsupportedOperationException if this set is unmodifiable
     * @see #add(Enum)
     * @see Collection#add(Object)
     * @see BitSet#set(int)
     */
    boolean add(int value);

    /**
     * Adds enum values from another collection to this set.
     * @param c the other collection of enum values
     * @return true if any new elements have been added, false otherwise
     * @throws ClassCastException if {@code e} is not of the
     *   {@linkplain #type() enum type of this bit set}
     * @throws UnsupportedOperationException if this set is unmodifiable
     * @see #add(Enum)
     * @see Collection#addAll(Collection)
     */
    boolean addAll(Collection<? extends E> c);

    /**
     * Removes all elements from this set.
     * @throws UnsupportedOperationException if this set is unmodifiable
     * @see Collection#clear()
     */
    void clear();

    /**
     * Removes an enum constant from this set.
     * @param e the enum constant to remove
     * @return true if the constant has been removed from this set
     *   (even if it has been added as an integer via {@link
     *   #add(int)}), false if it was already absent
     * @throws ClassCastException if {@code e} is not of the
     *   {@linkplain #type() enum type of this bit set}
     * @throws UnsupportedOperationException if this set is unmodifiable
     * @throws NullPointerException if {@code e} is null
     * @see #remove(int)
     * @see Collection#remove(Object)
     */
    boolean remove(E e);

    /**
     * Removes an integer from this set.
     * @param value the integer to remove
     * @return true if the integer has been removed from this set
     *   (even if it has been added as an enum constant via {@link
     *   #add(Enum)}), false if it was already absent
     * @throws IllegalArgumentException if {@code value} is negative
     *   or too large
     * @throws UnsupportedOperationException if this set is unmodifiable
     * @see #remove(int)
     * @see Collection#remove(Object)
     * @see BitSet#clear(int)
     */
    boolean remove(int value);

    /**
     * Returns an unmodifiable view of a bit set.
     * Attempts to modified the bit set through the returned object
     * will throw {@link UnsupportedOperationException}.
     * @param <E> the type of the enum constants for this bit set
     * @param set the underlying set for which an unmodifiable view
     *   should be created
     * @return an unmodifiable bit set
     */
    static <E extends Enum<E>> EnumBitSet<E> unmodifiableSet(
        EnumBitSet<E> set) {
        if (set.getClass() == UnmodifiableEnumBitSet.class)
            return set;
        else
            return new UnmodifiableEnumBitSet<>(set);
    }

    /**
     * Wraps an {@link IntBuffer} in an enum set.  If the buffer is
     * not read-only, updates to the buffer are reflected in the set,
     * and vice versa.  After creation, changes to the buffer's
     * position and limit do not affect the operation of the set.  The
     * maximum integer value that can be represented in the set is
     * {@code buffer.remaining() * 32 - 1}.  Attempts to access
     * members outside this range will trhow {@link
     * IllegalArgumentException}.
     * @param <E> the type of the enum constants for this bit set
     * @param type the reified enum type
     * @param buffer the underlying buffer for the set
     * @return an enum bit set
     * @throws NullPointerException if any argument is nul
     * @throws IllegalArgumentException if {@code type} is not an
     *   {@linkplain Enum enum type}
     */
    static <E extends Enum<E>> EnumBitSet<E> wrap(
        Class<E> type, IntBuffer buffer) {
        return new IntBufferEnumBitSet<>(type, buffer.slice());
    }
}
