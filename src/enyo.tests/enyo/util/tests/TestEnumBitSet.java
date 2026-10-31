package enyo.util.tests;

import enyo.util.BitSetEnumBitSet;
import enyo.util.EnumBitSet;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class TestEnumBitSet {
    final static class Comparing<E extends Enum<E>> implements EnumBitSet<E> {
        Comparing(Testing t, EnumBitSet<E> left, EnumBitSet<E> right) {
            this.t = t;
            this.left = left;
            this.right = right;
            check();
        }

        @Override
        public Class<E> type() {
            check();
            return left.type();
        }

        @Override
        public int size() {
            check();
            return left.size();
        }

        @Override
        public boolean isEmpty() {
            check();
            return left.isEmpty();
        }

        @Override
        public int bitLength() {
            check();
            return left.bitLength();
        }

        @Override
        public boolean contains(E e) {
            return check(set -> set.contains(e));
        }

        @Override
        public boolean contains(int value) {
            return check(set -> set.contains(value));
        }

        @Override
        public Set<Object> toSet() {
            return check(set -> set.toSet());
        }

        @Override
        public BitSet toBitSet() {
            return check(set -> set.toBitSet());
        }

        @Override
        public Stream<Object> stream() {
            check();
            List<Object> leftList =
                left.stream().collect(Collectors.toList());
            List<Object> rightList =
                right.stream().collect(Collectors.toList());
            Set<Object> leftSet = Set.copyOf(leftList);
            Set<Object> rightSet = Set.copyOf(rightList);

            // Check that there were no duplicates in the stream.
            t.examine(leftSet)
                .mapToInt(Set::size)
                .is(leftList.size());

            // Check that the streams produced the same elements.
            t.examine(leftSet)
                .is(rightSet);

            return leftList.stream();
        }

        @Override
        public boolean add(E e) {
            check();
            return check(set -> set.add(e));
        }

        @Override
        public boolean add(int value) {
            return check(set -> set.add(value));
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return check(set -> set.addAll(c));
        }

        @Override
        public boolean remove(E e) {
            return check(set -> set.remove(e));
        }

        @Override
        public boolean remove(int value) {
            return check(set -> set.remove(value));
        }

        @Override
        public void clear() {
            check(set -> {
                    set.clear();
                    return (Void) null;
                });
        }

        @Override
        public boolean equals(Object o) {
            return check(set -> set.equals(o));
        }

        @Override
        public int hashCode() {
            return check(EnumBitSet::hashCode);
        }

        private final Testing t;
        private final EnumBitSet<E> left;
        private final EnumBitSet<E> right;

        private void check() {
            t.examine(left)
                .map(EnumBitSet::type)
                .is(right.type());
            t.examine(left)
                .map(EnumBitSet::bitLength)
                .is(right.bitLength());
            t.examine(left)
                .map(EnumBitSet::size)
                .is(right.size());
            t.examine(left)
                .map(EnumBitSet::isEmpty)
                .is(right.isEmpty());
            t.examine(left)
                .map(EnumBitSet::toSet)
                .is(right.toSet());
            t.examine(left)
                .map(EnumBitSet::toBitSet)
                .is(right.toBitSet());

            int bitLength = left.bitLength();
            for (int i = 0; i < bitLength; ++i)
                t.examine(i)
                    .mapToObj(i0 -> left.contains(i0))
                    .is(right.contains(i));

            E[] values = left.type().getEnumConstants();
            for (E c : values)
                t.examine(c)
                    .map(c0 -> left.contains(c0))
                    .is(right.contains(c));
        }

        private <R> R check(Function<EnumBitSet<E>, R> op) {
            R result;
            check();
            try {
                result = op.apply(left);
            } catch (RuntimeException e) {
                t.expect(() -> op.apply(right),
                    e.getClass())
                    .map(RuntimeException::getMessage)
                    .nullableIs(e.getMessage());
                t.fullBarrier();
                check();
                throw e;
            }
            t.examine(result)
                .nullableIs(op.apply(right));
            check();
            return result;
        }
    }

    interface Factory {
        <E extends Enum<E>> EnumBitSet<E> create(Class<E> type);
    }

    static <E extends Enum<E>> EnumBitSet<E> mkBitSet(Class<E> type) {
        return new BitSetEnumBitSet<>(type);
    }

    final static int BUFFER_SIZE = 2;

    static <E extends Enum<E>> EnumBitSet<E> mkIntBuffer(Class<E> type) {
        return EnumBitSet.wrap(type, IntBuffer.allocate(BUFFER_SIZE));
    }

    enum E0 { }
    enum E40 {
        C00, C01, C02, C03, C04, C05, C06, C07, C08, C09,
        C10, C11, C12, C13, C14, C15, C16, C17, C18, C19,
        C20, C21, C22, C23, C24, C25, C26, C27, C28, C29,
        C30, C31, C32, C33, C34, C35, C36, C37, C38, C39,
    }

    private static void test(Testing t, Factory mkLeft, Factory mkRight) {
        {
            var left = mkLeft.create(E0.class);
            var right = mkRight.create(E0.class);
            var cmp = new Comparing<>(t, left, right);
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.add(i0))
                        .is(true);
                    t.examine(cmp)
                        .mapToInt(EnumBitSet::size, e -> e.is(i + 1))
                        .map(EnumBitSet::toSet,
                            e -> e.is(IntStream.rangeClosed(0, i)
                                .boxed()
                                .collect(Collectors.toSet())));
                });
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.add(i0))
                        .is(false);
                    t.examine(cmp)
                        .mapToInt(EnumBitSet::size, e -> e.is(32 * BUFFER_SIZE));
                });
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.remove(i0))
                        .is(true);
                    t.examine(cmp)
                        .mapToInt(EnumBitSet::size, e -> e.is(
                                32 * BUFFER_SIZE - 1 - i));
                });
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.remove(i0))
                        .is(false);
                    t.examine(cmp)
                        .map(EnumBitSet::isEmpty)
                        .is(true);
                });

            t.examine(cmp.add(0)).is(true);
            t.examine(cmp.contains(0)).is(true);
            cmp.clear();
            t.examine(cmp.contains(0)).is(false);
            cmp.clear();
        }
        {
            var left = mkLeft.create(E40.class);
            var right = mkRight.create(E40.class);
            var cmp = new Comparing<>(t, left, right);
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.add(i0))
                        .is(true);
                });
            t.examine(cmp)
                .map(EnumBitSet::toSet,
                    e -> e.is(Stream.concat(
                            Arrays.stream(E40.values()),
                            IntStream.range(40, 32 * BUFFER_SIZE).boxed())
                        .collect(Collectors.toSet())));
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.add(i0))
                        .is(false);
                    t.examine(cmp)
                        .mapToInt(EnumBitSet::size, e -> e.is(32 * BUFFER_SIZE));
                });
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.remove(i0))
                        .is(true);
                    t.examine(cmp)
                        .mapToInt(EnumBitSet::size, e -> e.is(
                                32 * BUFFER_SIZE - 1 - i));
                });
            IntStream.range(0, 32 * BUFFER_SIZE).forEach(i -> {
                    t.examine(i)
                        .mapToObj(i0 -> cmp.remove(i0))
                        .is(false);
                    t.examine(cmp)
                        .map(EnumBitSet::isEmpty)
                        .is(true);
                });

            t.examine(cmp.add(0)).is(true);
            t.examine(cmp.contains(0)).is(true);
            cmp.clear();
            t.examine(cmp.contains(0)).is(false);
            cmp.clear();
        }
        {
            var left0 = mkLeft.create(E40.class);
            var right0 = mkRight.create(E40.class);
            var cmp0 = new Comparing<>(t, left0, right0);
            cmp0.addAll(Arrays.asList(E40.values()));

            var cmp = new Comparing<>(t,
                EnumBitSet.unmodifiableSet(left0),
                EnumBitSet.unmodifiableSet(right0));
            t.expect(() -> cmp.addAll(List.of()),
                UnsupportedOperationException.class);
            t.expect(() -> cmp.addAll(List.of(E40.C00)),
                UnsupportedOperationException.class);
            t.expect(cmp::clear, UnsupportedOperationException.class);
            t.expect(() -> cmp.add(E40.C00),
                UnsupportedOperationException.class);
            t.expect(() -> cmp.remove(E40.C00),
                UnsupportedOperationException.class);
            t.expect(() -> cmp.add(0),
                UnsupportedOperationException.class);
            t.expect(() -> cmp.remove(0),
                UnsupportedOperationException.class);
            t.examine(cmp)
                .is(cmp0);
            cmp0.remove(0);
            cmp0.remove(39);
            t.examine(cmp)
                .is(cmp0);
        }
    }

    @Test
    public void testBitSetBitSet(Testing t) {
        test(t, TestEnumBitSet::mkBitSet, TestEnumBitSet::mkBitSet);
    }

    @Test
    public void testBitSetIntBuffer(Testing t) {
        test(t, TestEnumBitSet::mkBitSet, TestEnumBitSet::mkIntBuffer);
    }

    @Test
    public void testOrder() {
        Order<EnumBitSet<?>> order = new Order<>();
        order.group()
            .add(new BitSetEnumBitSet<>(E0.class))
            .add(new BitSetEnumBitSet<>(E0.class))
            .add(EnumBitSet.wrap(E0.class, IntBuffer.allocate(0)))
            .add(EnumBitSet.wrap(E0.class, IntBuffer.allocate(1)))
            .add(EnumBitSet.wrap(E0.class, IntBuffer.allocate(2)))
            .add(EnumBitSet.wrap(E0.class, IntBuffer.allocate(3)))
            .close();
        order.group()
            .add(new BitSetEnumBitSet<>(E40.class))
            .add(new BitSetEnumBitSet<>(E40.class))
            .add(EnumBitSet.wrap(E40.class, IntBuffer.allocate(0)))
            .add(EnumBitSet.wrap(E40.class, IntBuffer.allocate(1)))
            .add(EnumBitSet.wrap(E40.class, IntBuffer.allocate(2)))
            .add(EnumBitSet.wrap(E40.class, IntBuffer.allocate(3)))
            .close();

        for (int i : List.of(0, 1, 31)) {
            var group = order.group();
            for (var ebs : List.of(
                    new BitSetEnumBitSet<>(E0.class),
                    new BitSetEnumBitSet<>(E0.class),
                    EnumBitSet.wrap(E0.class, IntBuffer.allocate(1)),
                    EnumBitSet.wrap(E0.class, IntBuffer.allocate(2)),
                    EnumBitSet.wrap(E0.class, IntBuffer.allocate(3)))) {
                ebs.add(i);
                group = group.add(ebs);
            }
            group.close();

            group = order.group();
            for (var ebs : List.of(
                    new BitSetEnumBitSet<>(E40.class),
                    new BitSetEnumBitSet<>(E40.class),
                    EnumBitSet.wrap(E40.class, IntBuffer.allocate(1)),
                    EnumBitSet.wrap(E40.class, IntBuffer.allocate(2)),
                    EnumBitSet.wrap(E40.class, IntBuffer.allocate(3)))) {
                ebs.add(i);
                group = group.add(ebs);
            }
            group.close();
        }

        for (int i : List.of(32, 63) ){
            var group = order.group();
            for (var ebs : List.of(
                    new BitSetEnumBitSet<>(E0.class),
                    EnumBitSet.wrap(E0.class, IntBuffer.allocate(2)),
                    EnumBitSet.wrap(E0.class, IntBuffer.allocate(3)))) {
                ebs.add(i - 32);
                ebs.add(i);
                group = group.add(ebs);
            }
            group.close();

            group = order.group();
            for (var ebs : List.of(
                    new BitSetEnumBitSet<>(E40.class),
                    EnumBitSet.wrap(E40.class, IntBuffer.allocate(2)),
                    EnumBitSet.wrap(E40.class, IntBuffer.allocate(3)))) {
                ebs.add(i - 32);
                ebs.add(i);
                group = group.add(ebs);
            }
            group.close();
        }

        order.check();
    }
}
