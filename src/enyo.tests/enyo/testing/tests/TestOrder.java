package enyo.testing.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TestOrder {
    @Test
    public void testNothing() {
        var c = new Order<String>();
        c.check();
        c.checkComparable(String.class);
    }

    @Test
    public void testIntegers() {
        var c = new Order<Integer>();
        for (int i = -200; i <= 200; ++i)
            c.add(i);
        c.check();
        c.checkComparable(Integer.class);
    }

    @Test
    public void testDoubles() {
        var c = new Order<Double>();
        for (int i = -200; i <= 200; ++i)
            c.group()
                .add((double) i)
                .add(Integer.valueOf(i).doubleValue())
                .close();
        c.check();
        c.checkComparable(Double.class);
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
        t.assertEquals(
            c.values().stream().map(Integer::doubleValue).collect(
                Collectors.toList()),
            c1.values().stream().collect(Collectors.toList()));
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
                t.assertEquals(Order.Failed.EQUALS_NULL, e.failed());
                t.assertSame(item, e.object());
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
                t.assertEquals(Order.FailedPair.EQUALS, e.failed());
                t.assertSame(item, e.left());
                t.assertSame(item, e.right());
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
                t.assertEquals(Order.FailedPair.HASH_CODE, e.failed());
                t.assertSame(item1, e.left());
                t.assertSame(item2, e.right());
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
            t.assertEquals(Order.FailedPair.EQUALS, e.failed());
            t.assertSame(item1, e.left());
            t.assertSame(item2, e.right());
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
                t.assertEquals(Order.Failed.COMPARABLE, e.failed());
                t.assertSame(item, e.object());
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
                t.assertEquals(Order.FailedPair.COMPARE_TO, e.failed());
                t.assertEquals(-1, e.expectedComparison());
                t.assertSame(item2, e.left());
                t.assertSame(item1, e.right());
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
                t.assertEquals(Order.FailedPair.COMPARE_TO, e.failed());
                t.assertEquals(0, e.expectedComparison());
                t.assertSame(item1, e.left());
                t.assertSame(item2, e.right());
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
                t.assertEquals(Order.FailedPair.COMPARE_TO, e.failed());
                t.assertEquals(-1, e.expectedComparison());
                t.assertSame(item1, e.left());
                t.assertSame(item2, e.right());
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
