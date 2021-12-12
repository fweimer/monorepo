package enyo.testing.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class TestOrder {
    @Test
    public static void testNothing(Testing t) {
        var c = new Order<String>();
        c.check();
        c.checkComparable(String.class);
        t.examine(c.values()).is(List.of());
        t.examine(c.groups()).is(List.of());
    }

    @Test
    public static void testIntegers(Testing t) {
        var c = new Order<Integer>();
        for (int i = -200; i <= 200; ++i)
            c.add(i);
        c.check();
        c.checkComparable(Integer.class);
        t.examine(c.values()).is(
            IntStream.rangeClosed(-200, 200)
            .boxed()
            .collect(Collectors.toList()));
        t.examine(c.groups()).is(
            IntStream.rangeClosed(-200, 200)
            .mapToObj(i -> List.of(i))
            .collect(Collectors.toList()));
    }

    @Test
    public static void testDoubles(Testing t) {
        var c = new Order<Double>();
        for (int i = -200; i <= 200; ++i)
            c.group()
                .add((double) i)
                .add(Integer.valueOf(i).doubleValue())
                .close();
        c.check();
        c.checkComparable(Double.class);
        t.examine(c.values()).is(
            IntStream.rangeClosed(-200, 200)
            .mapToObj(i -> (double) i)
            .flatMap(d -> Stream.of(d, d))
            .collect(Collectors.toList()));
        t.examine(c.groups()).is(
            IntStream.rangeClosed(-200, 200)
            .mapToObj(i -> List.of((double) i, (double) i))
            .collect(Collectors.toList()));
    }

    @Test
    public void testMapDoubles(Testing t) {
        var c = new Order<Integer>();
        for (int i = -200; i <= 200; ++i)
            c.group()
                .add(i)
                .add(Integer.valueOf(i))
                .close();
        Order<Double> c1 = c.map(Integer::doubleValue);
        c1.check();
        c1.checkComparable(Double.class);
        t.examine(c.values().stream().map(Integer::doubleValue).collect(
                Collectors.toList()))
            .is(c1.values().stream().collect(Collectors.toList()));
    }

    @Test
    public static void testGroupsAugment(Testing t) {
        var c = new Order<Integer>();
        c.add(0);
        c.group()
            .add(100)
            .add(110)
            .close();
        c.group()
            .add(200)
            .add(220)
            .add(230)
            .close();
        t.examine(c.values())
            .is(List.of(0, 100, 110, 200, 220, 230));
        t.examine(c.groups())
            .is(List.of(
                    List.of(0),
                    List.of(100, 110),
                    List.of(200, 220, 230)));

        c.augment(i -> i + 1);
        t.examine(c.values())
            .is(List.of(0, 100, 110, 200, 220, 230,
                        1, 101, 111, 201, 221, 231));
        t.examine(c.groups())
            .is(List.of(
                    List.of(0, 1),
                    List.of(100, 110, 101, 111),
                    List.of(200, 220, 230, 201, 221, 231)));
    }

    @Test
    public void testEqualsNull(Testing t) {
        class EqualsNull {
            public boolean equals(Object o) {
                return true;
            }

            public int hashCode() {
                return 0;
            }
        }

        var c = new Order<EqualsNull>();
        var item = new EqualsNull();
        c.group()
            .add(item)
            .add(new EqualsNull())
            .close();
        t.expect(() -> c.check(), Order.ObjectFailure.class, e -> {
                t.examine(e.failed()).is(Order.Failed.EQUALS_NULL);
                t.examine(e.object()).isSame(item);
            });
    }

    @Test
    public void testEqualsFalse(Testing t) {
        class EqualsFalse {
            public boolean equals(Object o) {
                return false;
            }

            public int hashCode() {
                return 0;
            }
        }

        var c = new Order<EqualsFalse>();
        var item = new EqualsFalse();
        c.add(item);
        c.add(new EqualsFalse());
        t.expect(() -> c.check(), Order.PairFailure.class, e -> {
                t.examine(e.failed()).is(Order.FailedPair.EQUALS);
                t.examine(e.left()).isSame(item);
                t.examine(e.right()).isSame(item);
            });
    }

    @Test
    public void testHashCode(Testing t) {
        class BrokenHashCode {
            int hashCode;

            public BrokenHashCode(int hashCode) {
                this.hashCode = hashCode;
            }

            public boolean equals(Object o) {
                return o != null;
            }

            public int hashCode() {
                return hashCode;
            }
        }

        var c = new Order<BrokenHashCode>();
        var item1 = new BrokenHashCode(1);
        var item2 = new BrokenHashCode(2);
        c.group()
            .add(item1)
            .add(item2)
            .close();
        t.expect(() -> c.check(), Order.PairFailure.class, e -> {
                t.examine(e.failed()).is(Order.FailedPair.HASH_CODE);
                t.examine(e.left()).isSame(item1);
                t.examine(e.right()).isSame(item2);
            });
    }

    @Test
    public void testGroupDifference(Testing t) {
        var c = new Order<Integer>();
        Integer item1 = 1;
        Integer item2 = 2;
        c.add(0);
        c.group()
            .add(item1)
            .add(item2)
            .close();
        c.add(3);
        c.add(4);
        Consumer<Order.PairFailure> verify = e -> {
            t.examine(e.failed()).is(Order.FailedPair.EQUALS);
            t.examine(e.left()).isSame(item1);
            t.examine(e.right()).isSame(item2);
        };
        t.expect(() -> c.check(), Order.PairFailure.class, verify);
        t.expect(() -> c.checkComparable(Integer.class),
            Order.PairFailure.class, verify);
    }

    @Test
    public void testComparableUnassignable(Testing t) {
        var c = new Order<String>();
        var item = "";
        c.add(item);
        t.expect(() -> c.checkComparable(Integer.class),
            Order.ObjectFailure.class, e -> {
                t.examine(e.failed()).is(Order.Failed.COMPARABLE);
                t.examine(e.object()).isSame(item);
            });
    }

    @Test
    public void testComparisonOrder(Testing t) {
        var c = new Order<Integer>();
        Integer item1 = 1;
        Integer item2 = 2;
        c.add(item2);
        c.add(item1);
        t.expect(() -> c.checkComparable(Integer.class),
            Order.PairFailure.class, e -> {
                t.examine(e.failed()).is(Order.FailedPair.COMPARE_TO);
                t.examine(e.expectedComparison()).is(-1);
                t.examine(e.left()).isSame(item2);
                t.examine(e.right()).isSame(item1);
            });
    }

    @Test
    public void testComparisonEquals(Testing t) {
        var c = new Order<BrokenCompareTo>();
        c.add(new BrokenCompareTo(1, 1));
        var item1 = new BrokenCompareTo(2, 2);
        var item2 = new BrokenCompareTo(2, 3);
        c.group()
            .add(item1)
            .add(item2)
            .close();
        c.add(new BrokenCompareTo(4, 4));
        c.check();              // Equality implementation is not buggy.
        t.expect(() -> c.checkComparable(BrokenCompareTo.class),
            Order.PairFailure.class, e -> {
                t.examine(e.failed()).is(Order.FailedPair.COMPARE_TO);
                t.examine(e.expectedComparison()).is(0);
                t.examine(e.left()).isSame(item1);
                t.examine(e.right()).isSame(item2);
            });
    }

    @Test
    public void testComparisonEquals2(Testing t) {
        var c = new Order<BrokenCompareTo>();
        var item1 = new BrokenCompareTo(1, 1);
        var item2 = new BrokenCompareTo(4, 1);
        c.add(item1);
        c.group()
            .add(new BrokenCompareTo(2, 2))
            .add(new BrokenCompareTo(2, 2))
            .close();
        c.add(item2);
        c.check();              // Equality implementation is not buggy.
        t.expect(() -> c.checkComparable(BrokenCompareTo.class),
            Order.PairFailure.class, e -> {
                t.examine(e.failed()).is(Order.FailedPair.COMPARE_TO);
                t.examine(e.expectedComparison()).is(-1);
                t.examine(e.left()).isSame(item1);
                t.examine(e.right()).isSame(item2);
            });
    }

    private static final class BrokenCompareTo
        implements Comparable<BrokenCompareTo> {
        final int forEquals;
        final int forCompareTo;

        BrokenCompareTo(int forEquals, int forCompareTo) {
            this.forEquals = forEquals;
            this.forCompareTo = forCompareTo;
        }

        public boolean equals(Object other) {
            return other != null && other.getClass() == getClass()
                && forEquals == ((BrokenCompareTo) other).forEquals;
        }

        public int hashCode() {
            return forEquals;
        }

        public int compareTo(BrokenCompareTo other) {
            return Integer.compare(forCompareTo, other.forCompareTo);
        }
    }
}
