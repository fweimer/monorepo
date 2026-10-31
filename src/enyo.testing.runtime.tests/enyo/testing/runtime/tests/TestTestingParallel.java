package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Counter;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.ThrowingCallable;
import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.TestResources;
import enyo.testing.runtime.Examined;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class TestTestingParallel {
    @Test
    public static void test1(Testing t) {
        Counter counter = t.counter(1);
        t.parallel(List.of(() -> counter.increment()));
        counter.checkTarget();
    }

    @Test
    @Resources(threads=2)
    public static void test2(Testing t) {
        Counter counter = t.counter(2);
        t.parallel(List.of(
            () -> counter.increment(),
            () -> counter.increment()));
        counter.checkTarget();
    }

    @Test
    @Resources(threads=3)
    public static void test3(Testing t) {
        Counter counter = t.counter(3);
        t.examine(t.parallelValues(IntStream.range(0, 3)
            .mapToObj(i -> (ThrowingCallable<Integer>) () -> {
            counter.increment();
            // The thread index is deteriministic.
            return i;
        }).toList()))
            .is(List.of(0, 1, 2));
        counter.checkTarget();
    }

    @Test
    @Resources(threads=10)
    public static void testExceptions(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, TestTestingParallel.class, "exceptions");
        t.examine(Helpers.isSuccess(t, history)).is(false);

        // The failure history can contain a varying number of
        // exception failures, depending on how quickly the tests
        // proceeded.
        t.examine(history.size())
            .check(Checks.greaterThanOrEquals(2));
        for (var h : history.subList(0, history.size() - 1))
            t.examine(h)
                .map(Recorder.Exception.class)
                .map(e -> e.exception)
                .map(Throwable::getMessage)
                .test(s -> s.startsWith("thread "));
        t.examine(history.get(history.size() - 1))
            .map(Recorder.Completed.class);
    }

    // Not marked with @Test due to expected test failure.
    @Resources(threads=10)
    public static void exceptions(Testing t) {
        AtomicInteger counter = new AtomicInteger();
        t.examine(t.parallelValues(IntStream.range(0, 10)
            .mapToObj(i -> (ThrowingCallable<Integer>) () -> {
            counter.incrementAndGet();
            throw new Throwable("thread " + i);
        }).toList()))
            .is(Arrays.asList(new Integer[10]));
        t.examine(counter.get()).is(10);
    }

    @Test
    @Resources(threads=10)
    public static void testExceptionsInterrupt(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, TestTestingParallel.class, "exceptionsInterrupt");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        // There is exactly one failure in the history, from the
        // exception.  (InterruptedException from the post-failure
        // interrupts should not be recorded.)
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Exception.class)
            .map(e -> e.exception)
            .map(Throwable::getMessage)
            .test(s -> s.startsWith("exit"));

        t.examine(history.get(history.size() - 1))
            .map(Recorder.Completed.class);
    }

    // Not marked with @Test due to expected test failure.
    @Resources(threads=10)
    public static void exceptionsInterrupt(Testing t) {
        t.examine(t.parallelValues(IntStream.range(0, 10)
            .mapToObj(i -> (ThrowingCallable<String>) () -> {
            if (i == 0)
                throw new Throwable("exit");
            Thread.sleep(Long.MAX_VALUE);
            return "unreachable";
        }).toList()))
            .is(Arrays.asList(new String[10]));
    }

    @Test
    public static void testUnderAllocation(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, new ScheduledTest(0,
                Helpers.locator(TestTestingParallel.class, "underAllocation"),
                new TestResources(1, TestResources.DEFAULT.memory()),
                false));
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context,
                    e1-> e1.is(List.of(new Examined("threads", 2))))
                .map(call -> call.check, e1 -> e1.is("lessThanOrEquals"))
                .map(call -> List.of(call.otherObjects),
                    e1 -> e1.is(List.of(1))));
    }

    // Not marked with @Test due to expected test failure.
    public static void underAllocation(Testing t) {
        Counter counter = t.counter(2);
        t.parallel(List.of(() -> counter.increment(),
            () -> counter.increment()));
        counter.checkTarget();
    }
}
