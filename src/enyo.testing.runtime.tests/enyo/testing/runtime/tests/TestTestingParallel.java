package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Counter;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.TestResources;
import enyo.testing.runtime.Examined;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class TestTestingParallel {
    @Test
    public static void test1(Testing t) {
        Counter counter = t.counter(1);
        t.parallel(1, () -> counter.increment());
        counter.checkTarget();
    }

    @Test
    @Resources(threads=2)
    public static void test2(Testing t) {
        Counter counter = t.counter(2);
        t.parallel(2, () -> counter.increment());
        counter.checkTarget();
    }

    @Test
    @Resources(threads=3)
    public static void test3(Testing t) {
        Counter counter = t.counter(3);
        t.parallel(3, () -> counter.increment());
        counter.checkTarget();
    }

    @Test
    @Resources(threads=3)
    public static void testExceptions(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, TestTestingParallel.class, "exceptions");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);
        t.examine(history.subList(0, 3).stream()
            .map(Object::getClass)
            .collect(Collectors.toSet()))
            .isObject(Set.of(Recorder.Exception.class));
        t.examine(history.subList(0, 3).stream()
            .map(res -> (Recorder.Exception) res)
            .map(res -> res.exception.getMessage())
            .collect(Collectors.toSet()))
            .is(Set.of("thread 1", "thread 2", "thread 3"));
    }

    // Not marked with @Test due to expected test failure.
    @Resources(threads=3)
    public static void exceptions(Testing t) {
        AtomicInteger counter = new AtomicInteger();
        t.parallel(3, () -> {
                int assigned = counter.incrementAndGet();
                throw new Throwable("thread " + assigned);
            });
        t.examine(counter.get()).is(3);
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
        t.parallel(2, () -> counter.increment());
        counter.checkTarget();
    }
}
