package enyo.util.tests;

import enyo.util.AbstractEnumBitSet;
import enyo.util.BitSetEnumBitSet;
import enyo.util.EnumBitSet;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestAbstractEnumBitSet {
    static class Minimal<E extends Enum<E>> extends AbstractEnumBitSet<E> {
        Minimal(Class<E> type) {
            super(type);
        }

        @Override
        public int bitLength() {
            return 0;
        }

        @Override
        public boolean contains(int value) {
            return false;
        }
    }

    enum E1 { C0 }

    @Test
    public static void testMinimal(Testing t) {
        var ebs = new Minimal<>(E1.class);
        t.examine(ebs)
            .mapToInt(EnumBitSet::size, e -> e.is(0))
            .map(EnumBitSet::isEmpty, e -> e.is(true))
            .map(EnumBitSet::toSet, e -> e.is(Set.of()))
            .map(EnumBitSet::toBitSet, e -> e.is(new BitSet()))
            .map(set -> set.contains(E1.C0), e -> e.is(false))
            .map(set -> set.equals(new Minimal<>(E1.class)), e -> e.is(true))
            .map(set -> set.equals(new BitSetEnumBitSet<>(E1.class)),
                e -> e.is(true))
            .map(set -> new BitSetEnumBitSet<>(E1.class).equals(set),
                e -> e.is(true))
            .map(s -> s.stream().collect(Collectors.toList()),
                e -> e.is(List.of()));
    }
}
