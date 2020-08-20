package enyo.util.tests;

import enyo.util.EnumBitSet;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public final class TestIntBufferEnumBitSet {
    enum E3 { C0 , C1, C2 }

    @Test
    public static void testEmpty(Testing t) {
        List.of(0, 1).stream().forEach(i ->
            t.examine(EnumBitSet.wrap(E3.class, IntBuffer.allocate(i)))
                .map(EnumBitSet::isEmpty)
                .is(true));
    }

    @Test
    public static void testOneWord(Testing t) {
        IntBuffer buffer = IntBuffer.allocate(1);
        buffer.put(0, 1);
        EnumBitSet<E3> ebs = EnumBitSet.wrap(E3.class, buffer);
        t.examine(ebs)
            .map(set -> set.contains(0), e -> e.is(true))
            .map(set -> set.contains(1), e -> e.is(false))
            .map(EnumBitSet::size, e -> e.is(1))
            .map(EnumBitSet::bitLength, e -> e.is(1))
            .map(EnumBitSet::toString, e -> e.is("[C0]"));
        buffer.put(0, 2);
        t.examine(ebs)
            .map(set -> set.contains(0), e -> e.is(false))
            .map(set -> set.contains(1), e -> e.is(true))
            .map(set -> set.contains(2), e -> e.is(false))
            .map(EnumBitSet::size, e -> e.is(1))
            .map(EnumBitSet::bitLength, e -> e.is(2))
            .map(EnumBitSet::toString, e -> e.is("[C1]"));
        buffer.put(0, Integer.MIN_VALUE);
        t.examine(ebs)
            .map(set -> set.contains(31), e -> e.is(true))
            .map(set -> set.contains(30), e -> e.is(false))
            .map(EnumBitSet::size, e -> e.is(1))
            .map(EnumBitSet::bitLength, e -> e.is(32))
            .map(EnumBitSet::toString, e -> e.is("[31]"));
        t.examine(ebs)
            .map(set -> set.remove(31), e -> e.is(true))
            .map(set -> set.add(30), e -> e.is(true))
            .map(EnumBitSet::toString, e -> e.is("[30]"));
        t.examine(buffer.get(0))
            .is(0x4000_0000);
    }

    enum E0 { }

    @Test
    public static void testTwoWord(Testing t) {
        IntStream.range(0, 32).forEach(i -> {
                IntBuffer buffer = IntBuffer.allocate(2);
                EnumBitSet<E0> ebs = EnumBitSet.wrap(E0.class, buffer);
                buffer.put(0, 1 << i);
                t.examine(ebs)
                    .map(EnumBitSet::size, e -> e.is(1))
                    .map(EnumBitSet::bitLength, e -> e.is(i + 1))
                    .map(EnumBitSet::toSet, e -> e.is(Set.of(i)));
            });
        IntStream.range(32, 64).forEach(i -> {
                IntBuffer buffer = IntBuffer.allocate(2);
                EnumBitSet<E0> ebs = EnumBitSet.wrap(E0.class, buffer);
                buffer.put(1, 1 << (i - 32));
                t.examine(ebs)
                    .map(EnumBitSet::size, e -> e.is(1))
                    .map(EnumBitSet::bitLength, e -> e.is(i + 1))
                    .map(EnumBitSet::toSet, e -> e.is(Set.of(i)));
            });
    }
}
