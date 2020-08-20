package enyo.util.tests;

import enyo.util.BitSetEnumBitSet;
import enyo.util.EnumBitSet;

import enyo.testing.Checks;
import enyo.testing.Cost;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestBitSetEnumBitSet {
    @Test
    public static void testEmpty(Testing t) {
        test(t, Empty.class);
    }

    enum Empty { }

    @Test
    public static void testOne(Testing t) {
        test(t, One.class);
    }

    enum One { C0 }

    @Test
    public static void testTwo(Testing t) {
        test(t, Two.class);
    }

    enum Two { C0, C1 }

    @Test
    public static void testThree(Testing t) {
        test(t, Three.class);
    }

    enum Three { C0, C1, C2 }

    @Test
    public static void testFour(Testing t) {
        test(t, Four.class);
    }

    enum Four { C0, C1, C2, C3 }

    @Test
    public static void testFive(Testing t) {
        test(t, Five.class);
    }

    enum Five { C0, C1, C2, C3, C4 }

    private static <E extends Enum<E>> void test(Testing t, Class<E> cls) {
        E[] values = cls.getEnumConstants();

        t.examine(new BitSetEnumBitSet<>(cls))
            .test(EnumBitSet::isEmpty)
            .mapToInt(EnumBitSet::size, e -> e.is(0));

        // Check read-only status.
        {
            EnumBitSet<E> ebs = EnumBitSet.unmodifiableSet(
                new BitSetEnumBitSet<>(cls));
            t.expect(() -> ebs.add(0), UnsupportedOperationException.class);
            t.expect(() -> ebs.remove(0), UnsupportedOperationException.class);
            t.expect(() -> ebs.addAll(List.of()),
                UnsupportedOperationException.class);
            if (values.length > 0)
                t.expect(() -> ebs.addAll(List.of(values[0])),
                    UnsupportedOperationException.class);
            t.expect(() -> ebs.clear(), UnsupportedOperationException.class);
        }

        for (E e0 : values) {
            EnumBitSet<E> ebs = new BitSetEnumBitSet<>(cls);
            t.examine(e0).test(ebs::add);
            t.examine(ebs)
                .testNot(EnumBitSet::isEmpty)
                .test(ebs0 -> ebs0.contains(e0))
                .is(of(e0))
                .mapToInt(EnumBitSet::size, e -> e.is(1));
            t.examine(ebs.stream().collect(Collectors.toList()))
                .is(List.of(e0));
        }

        for (E e0 : values)
            for (E e1 : values) {
                EnumBitSet<E> ebs = new BitSetEnumBitSet<>(cls);
                t.examine(e0)
                    .test(ebs::add)
                    .testNot(ebs::add);
                if (e1 == e0) {
                    t.examine(e1).testNot(ebs::add);
                    t.examine(ebs.size()).is(1);
                    t.examine(ebs.stream().collect(Collectors.toList()))
                        .is(List.of(e0));
                } else {
                    t.examine(e1)
                        .testNot(ebs::remove)
                        .test(ebs::add);
                    t.examine(ebs.size()).is(2);
                    t.examine(ebs.stream().collect(Collectors.toSet()))
                        .is(Set.of(e0, e1));
                }
                int baseSize = ebs.size();

                t.examine((Integer) values.length)
                    .test(ebs::add)
                    .testNot(ebs::add);
                t.examine(ebs)
                    .mapToInt(EnumBitSet::size, e -> e.is(baseSize + 1))
                    .map(EnumBitSet::toSet,
                        e -> e.is(setWithDuplicates(e0, e1, values.length)));


                t.examine((Integer) (values.length + 1))
                    .test(ebs::add);
                t.examine(ebs)
                    .mapToInt(EnumBitSet::size, e -> e.is(baseSize + 2))
                    .map(EnumBitSet::toSet,
                        e -> e.is(setWithDuplicates(e0, e1,
                                values.length, values.length + 1)));

                if (e0 != e1) {
                    t.examine(e0)
                        .test(ebs::remove)
                        .testNot(ebs::remove);
                    t.examine(ebs)
                        .mapToInt(EnumBitSet::size, e -> e.is(3))
                        .map(EnumBitSet::toSet,
                            e -> e.is(setWithDuplicates(e1,
                                    values.length, values.length + 1)))
                        .testNot(EnumBitSet::isEmpty);
                }


                t.examine(e1)
                    .test(ebs::remove)
                    .testNot(ebs::remove)
                    .test(ebs::add)
                    .testNot(ebs::add);
                t.examine(ebs.stream().collect(Collectors.toList()))
                    .is(List.of(e1, values.length, values.length + 1));
                t.examine(e1)
                    .test(ebs::remove)
                    .testNot(ebs::remove);
                t.examine(ebs)
                    .mapToInt(EnumBitSet::size, e -> e.is(2))
                    .map(EnumBitSet::toSet,
                        e -> e.is(setWithDuplicates(
                                values.length, values.length + 1)))
                    .testNot(EnumBitSet::isEmpty);

                t.examine((Integer) values.length)
                    .test(ebs::remove)
                    .testNot(ebs::remove);
                t.examine(ebs)
                    .mapToInt(EnumBitSet::size, e -> e.is(1))
                    .map(EnumBitSet::toSet,
                        e -> e.is(setWithDuplicates(values.length + 1)))
                    .testNot(EnumBitSet::isEmpty);
                t.examine((Integer) (values.length + 1))
                    .test(ebs::remove)
                    .testNot(ebs::remove);
                t.examine(ebs)
                    .is(EnumBitSet.unmodifiableSet(ebs))
                    .mapToInt(EnumBitSet::size, e -> e.is(0))
                    .map(s -> s.stream().collect(Collectors.toList()),
                        e -> e.is(List.of()))
                    .map(EnumBitSet::toSet, e -> e.is(Set.of()))
                    .test(EnumBitSet::isEmpty);
            }

        for (E e0 : values)
            for (E e1 : values)
                for (E e2 : values) {
                    EnumBitSet<E> ebs = new BitSetEnumBitSet<>(cls);
                    ebs.add(e0);
                    ebs.add(e1);
                    ebs.add(e2);
                    Set<Object> set = setWithDuplicates(e0, e1, e2);
                    t.examine(ebs)
                        .mapToInt(EnumBitSet::size, e -> e.is(set.size()))
                        .testNot(EnumBitSet::isEmpty)
                        .map(EnumBitSet::toSet, e -> e.is(set));
                }

        for (E e0 : values)
            for (E e1 : values)
                for (E e2 : values)
                    for (int i0 = 0; i0 <= 7; ++i0)
                        for (int i1 = 0; i1 <= 7; ++i1)
                            test5(t, cls, e0, e1, e2, i0, i1);

        {
            EnumBitSet<E> ebs = new BitSetEnumBitSet<>(cls);
            ebs.addAll(Arrays.asList(values));
            t.examine(ebs)
                .map(EnumBitSet::toSet)
                .is(Set.of((Object[]) values));
            t.examine(ebs.size()).is(values.length);
        }
    }

    private static <E extends Enum<E>> void test5(Testing t, Class<E> cls,
        E e0, E e1, E e2, int i0, int i1) {
        EnumBitSet<E> ebs = new BitSetEnumBitSet<>(cls);
        ebs.add(e0);
        ebs.add(e1);
        ebs.add(e2);
        ebs.add(i0);
        ebs.add(i1);

        t.examine(ebs)
            .is(EnumBitSet.unmodifiableSet(ebs));

        Set<Object> set = setWithDuplicates(
            e0.ordinal(), e1.ordinal(), e2.ordinal(),
            i0, i1);

        for (E c : cls.getEnumConstants())
            t.examine(c)
                .map(c0 -> ebs.contains(c0))
                .is(set.contains(c.ordinal()));
        for (int j = 0; j <= 10; ++j)
            t.examine(j)
                .mapToObj(j0 -> ebs.contains(j0))
                .is(set.contains(j));

        t.examine(ebs)
            .mapToInt(EnumBitSet::size, e -> e.is(set.size()))
            .testNot(EnumBitSet::isEmpty)
            .map(EnumBitSet::stream,
                s -> s.map(stream -> stream.map(obj -> {
                            if (obj.getClass() == Integer.class)
                                return obj;
                            else
                                return ((Enum<?>) obj).ordinal();
                        }))
                .map(stream -> stream.collect(Collectors.toSet()))
                .is(set));

        ebs.clear();
        t.examine(ebs)
            .test(EnumBitSet::isEmpty)
            .mapToInt(EnumBitSet::size, e -> e.is(0))
            .mapToInt(EnumBitSet::bitLength, e -> e.is(0))
            .map(EnumBitSet::toSet, e -> e.test(s -> s.isEmpty()))
            .map(EnumBitSet::stream,
                e -> e.map(stream -> stream.collect(Collectors.toList()))
                .is(List.of()));
    }


    private static Set<Object> setWithDuplicates(Object... objects) {
        return Set.copyOf(Arrays.asList(objects));
    }

    @Test
    public static void testNull(Testing t) {
        EnumBitSet<Two> ebs = new BitSetEnumBitSet<>(Two.class);
        t.expect(() -> ebs.add((Two) null), NullPointerException.class);
        t.expect(() -> ebs.addAll((Set<Two>) null), NullPointerException.class);
        t.expect(() -> ebs.contains((Two) null), NullPointerException.class);
        t.expect(() -> EnumBitSet.unmodifiableSet((EnumBitSet<Two>) null),
            NullPointerException.class);
        t.examine(ebs.equals(null)).is(false);
        t.expect(() -> ebs.remove((Two) null), NullPointerException.class);
    }

    @Test
    public static void testIllegalValue(Testing t) {
        List.of(-1, Integer.MAX_VALUE, Integer.MAX_VALUE / 8 + 1).stream()
            .forEach(i -> {
                    EnumBitSet<Two> ebs = new BitSetEnumBitSet<>(Two.class);
                    t.expect(() -> ebs.add(i), IllegalArgumentException.class);
                    t.expect(() -> ebs.contains(i),
                        IllegalArgumentException.class);
                    t.expect(() -> ebs.remove(i),
                        IllegalArgumentException.class);
                    t.examine(ebs)
                        .map(EnumBitSet::isEmpty)
                        .is(true);
                });
    }

    @Test
    public static void testToBitSetIndependent(Testing t) {
        EnumBitSet<Two> ebs = new BitSetEnumBitSet<>(Two.class);
        ebs.add(0);
        ebs.add(17);
        BitSet bs = ebs.toBitSet();
        bs.set(1);
        bs.clear(Two.C0.ordinal());
        t.examine(ebs)
            .map(EnumBitSet::toSet)
            .is(Set.of(Two.C0, 17));
        t.examine(bs)
            .map(BitSet::stream)
            .map(IntStream::boxed)
            .map(s -> s.collect(Collectors.toSet()))
            .is(Set.of(Two.C1.ordinal(), 17));
    }

    @Test
    public static void testBitLength(Testing t) {
        for (Five c : Five.values()) {
            EnumBitSet<Five> ebs = new BitSetEnumBitSet<>(Five.class);
            t.examine(ebs)
                .mapToInt(EnumBitSet::bitLength)
                .is(0);
            ebs.add(c);
            t.examine(ebs)
                .mapToInt(EnumBitSet::bitLength, e -> e.is(c.ordinal() + 1))
                .mapToInt(EnumBitSet::size, e -> e.is(1));
            ebs.add(0);
            if (c != Five.C0)
                t.examine(ebs)
                    .mapToInt(EnumBitSet::bitLength, e -> e.is(c.ordinal() + 1))
                    .mapToInt(EnumBitSet::size, e -> e.is(2));
        }
        IntStream.rangeClosed(0, 100).forEach(i -> {
                EnumBitSet<Five> ebs = new BitSetEnumBitSet<>(Five.class);
                ebs.add(i);
                t.examine(ebs)
                    .mapToInt(EnumBitSet::bitLength, e -> e.is(i + 1))
                    .mapToInt(EnumBitSet::size, e -> e.is(1));
                ebs.add(0);
                if (i != 0)
                    t.examine(ebs)
                        .mapToInt(EnumBitSet::bitLength,
                            e -> e.is(i + 1))
                        .mapToInt(EnumBitSet::size, e -> e.is(2));
            });
    }

    @Test
    @Cost(0.5)
    @Resources(memory=270)
    public static void testLargeInteger(Testing t) {
        int max = Integer.MAX_VALUE / 8;
        EnumBitSet<Empty> ebs = new BitSetEnumBitSet<>(Empty.class);
        ebs.add(max);
        t.examine(ebs)
            .mapToInt(EnumBitSet::bitLength, e -> e.is(max + 1))
            .mapToInt(EnumBitSet::size, e -> e.is(1));
    }


    // We may turn this into a public method eventually.  In this
    // case, we will have to add checks for duplicates.
    @SafeVarargs
    @SuppressWarnings("varargs")
    private static <E extends Enum<E>> EnumBitSet<E> of(E c0, E... cs) {
        BitSetEnumBitSet<E> result = new BitSetEnumBitSet<>(
            c0.getDeclaringClass());
        result.add(c0);
        result.addAll(Arrays.asList(cs));
        return EnumBitSet.unmodifiableSet(result);
    }

    @Test
    public void testOf(Testing t) {
        EnumBitSet<Two> ebs = new BitSetEnumBitSet<>(Two.class);
        ebs.add(Two.C0);
        t.examine(ebs)
            .is(of(Two.C0));
        t.<EnumBitSet<?>>examine(ebs)
            .isnt(of(Three.C0));
    }
}
