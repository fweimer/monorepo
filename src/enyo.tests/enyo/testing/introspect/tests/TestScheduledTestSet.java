package enyo.testing.introspect.tests;

import enyo.testing.Cost;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.ScheduledTestSet;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.introspect.TestResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestScheduledTestSet {
    @Test
    public static void testAdd(Testing t) {
        ScheduledTestSet set = new ScheduledTestSet(4);
        t.assertTrue(set.add(test(0)));
        t.assertTrue(set.add(test(1)));
        t.assertFalse(set.add(test(0)));
        t.assertTrue(set.add(test(2)));
        t.assertTrue(set.add(test(3)));
        t.assertFalse(set.add(test(3)));
    }

    @Test
    public static void testContains(Testing t) {
        ScheduledTestSet set = new ScheduledTestSet(4);
        Supplier<List<Integer>> makeList = () ->
            IntStream.range(0, 4)
            .filter(i -> set.contains(test(i)))
            .boxed()
            .collect(Collectors.toList());
        t.assertEquals(List.of(), makeList.get());
        t.assertTrue(set.add(test(1)));
        t.assertEquals(List.of(1), makeList.get());
        t.assertTrue(set.add(test(3)));
        t.assertEquals(List.of(1, 3), makeList.get());
        t.assertTrue(set.add(test(2)));
        t.assertEquals(List.of(1, 2, 3), makeList.get());
        t.assertFalse(set.add(test(2)));
        t.assertEquals(List.of(1, 2, 3), makeList.get());
    }

    @Test
    public static void testStream(Testing t) {
        ScheduledTestSet set = new ScheduledTestSet(4);
        Supplier<List<Integer>> makeList = () ->
            set.stream().boxed().collect(Collectors.toList());
        t.assertEquals(List.of(), makeList.get());
        t.assertTrue(set.add(test(1)));
        t.assertEquals(List.of(1), makeList.get());
        t.assertTrue(set.add(test(3)));
        t.assertEquals(List.of(1, 3), makeList.get());
        t.assertTrue(set.add(test(2)));
        t.assertEquals(List.of(1, 2, 3), makeList.get());
        t.assertFalse(set.add(test(2)));
        t.assertEquals(List.of(1, 2, 3), makeList.get());
    }

    @Test
    public static void testToString(Testing t) {
        BiConsumer<String, String> check = (present, expected) -> {
            ScheduledTestSet set = new ScheduledTestSet(10);
            present.chars()
            .forEach(ch -> t.assertTrue(set.add(test(ch - '0'))));
            t.assertEquals(expected, set.toString());
        };

        check.accept("", "{}");
        check.accept("0", "{0}");
        check.accept("1", "{1}");
        check.accept("9", "{9}");
        check.accept("01", "{0-1}");
        check.accept("021", "{0-2}");
        check.accept("02", "{0,2}");
        check.accept("213", "{1-3}");
        check.accept("2138", "{1-3,8}");
        check.accept("92138", "{1-3,8-9}");
        check.accept("792138", "{1-3,7-9}");
        check.accept("7921538", "{1-3,5,7-9}");
        check.accept("79215380", "{0-3,5,7-9}");
        check.accept("792153806", "{0-3,5-9}");
        check.accept("7921538064", "{0-9}");
    }

    @Cost(0.5)
    @Resources(threads=6, memory=200)
    @Test
    public static void testParallel(Testing t) throws InterruptedException {
        // This test tries to ensure that concurrent adds succeed
        // exactly once.

        int count = 1000 * 1000;
        int threadCount = 6;
        ScheduledTestSet set = new ScheduledTestSet(count);

        List<ScheduledTest> tests = IntStream.range(0, count)
            .mapToObj(i -> test(i))
            .collect(Collectors.toList());

        ArrayList<ScheduledTest> testsShuffled = new ArrayList<>(tests);
        Collections.shuffle(testsShuffled);

        ArrayList<ScheduledTest> testsReversed = new ArrayList<>(tests);
        Collections.reverse(testsReversed);

        CountDownLatch latch = new CountDownLatch(threadCount);

        class Runner extends Thread {
            private final List<ScheduledTest> list;
            private volatile Throwable exception;
            private boolean[] local = new boolean[count];

            Runner(List<ScheduledTest> list) {
                this.list = list;
            }

            @Override
            public void run() {
                try {
                    latch.countDown();
                    latch.await();
                    for (ScheduledTest t : list)
                        local[t.id()] = set.add(t);
                } catch (Throwable e) {
                    exception = e;
                }
            }

            IntStream stream() {
                return IntStream.range(0, local.length)
                    .filter(i -> local[i]);
            }
        }

        List<Runner> threads = List.of(
            new Runner(tests),
            new Runner(tests),
            new Runner(tests),
            new Runner(tests),
            new Runner(testsShuffled),
            new Runner(testsReversed));
        t.assertEquals(threadCount, threads.size());
        for (Runner r : threads)
            r.start();
        for (Runner r : threads)
            r.join();
        for (Runner r : threads)
            t.assertNull(r.exception);

        // Checks that the stream has the expected length and no
        // duplicates.
        Consumer<Supplier<IntStream>> checkStream = supplier -> {
            IntSummaryStatistics stats = supplier.get().summaryStatistics();
            t.assertEquals(0, stats.getMin());
            t.assertEquals(count - 1, stats.getMax());
            t.assertEquals((long) count, stats.getCount());
            t.assertEquals((long) count, supplier.get().distinct().count());
        };

        // The separate streams captured by the threads.
        checkStream.accept(() -> threads.stream()
            .flatMapToInt(Runner::stream));

        // And the contents of the set.
        checkStream.accept(() -> set.stream());
    }

    /**
     * Returns a fake scheduled test with the specified ID.
     */
    private static ScheduledTest test(int id) {
        return new ScheduledTest(id,
            new TestMethodLocator(Optional.empty(),
                "enyo.tests.TestClass",
                "m" + id, false),
            TestResources.DEFAULT);
    }
}
