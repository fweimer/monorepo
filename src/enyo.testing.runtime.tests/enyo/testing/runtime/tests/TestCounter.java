package enyo.testing.runtime.tests;

import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;
import enyo.testing.Counter;

import java.util.List;

public final class TestCounter {
    @Test
    public static void testDefault(Testing t) {
        Counter c = new Counter();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);
        t.expect(() -> c.checkTarget(),
            TestFailure.class)
	    .map(Exception::getMessage).is(
                "Counter has not reached target 1: 0");
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);
        c.increment();
        c.checkTarget();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
        t.expect(() -> c.increment(),
            TestFailure.class)
	    .map(Exception::getMessage).is("Counter reached target: 1");
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
        c.reset();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);
        t.expect(() -> c.checkTarget(),
            TestFailure.class)
	    .map(Exception::getMessage).is(
		"Counter has not reached target 1: 0");
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);
        c.increment();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
    }

    @Test
    public static void testCounter17(Testing t) {
        int target = 17;
        Counter c = new Counter(target);

        doLoop(t, c, target);

        t.expect(() -> c.increment(),
            TestFailure.class)
	    .map(Exception::getMessage).is(
		"Counter reached target: " + target);
        t.examine(c.target()).is(target);
        t.examine(c.value()).is(target);

        c.reset();
        c.increment();
        t.examine(c.target()).is(target);
        t.examine(c.value()).is(1);
        t.expect(() -> c.checkTarget(),
            TestFailure.class)
	    .map(Exception::getMessage).is(
                "Counter has not reached target " + target + ": 1");
        c.reset();
        doLoop(t, c, target);
    }

    private static void doLoop(Testing t, Counter c, int target) {
        for (int i = 0; i < target; ++i) {
            t.examine(c.target()).is(target);
            t.examine(c.value()).is(i);
            c.increment();
            t.examine(c.target()).is(target);
            t.examine(c.value()).is(i + 1);
        }
        c.checkTarget();
    }

    @Test
    public static void testNegative(Testing t) {
        t.expect(() -> new Counter(-1), IllegalArgumentException.class);
        // Does not result in a direct test failure, only an exception.
        t.expect(() -> t.counter(-1), IllegalArgumentException.class);
    }

    @Test
    public static void testDefaultTesting(Testing t) {
        List<Recorder.Call> history  = Helpers.testHistory(
            t, TestCounter.class, "defaultTesting");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(10);

        checkMark(t, history, 0, "before checkTarget");
        t.examine(history.get(1))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.check, e1 -> e1.is("counter at target"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(2))
                .map(call -> call.otherObjects[0], e1 -> e1.is(1))
                .map(call -> call.otherObjects[1], e1 -> e1.is(0)));
        checkMark(t, history, 2, "after checkTarget");

        checkMark(t, history, 3, "before increment");
        t.examine(history.get(4))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.check, e1 -> e1.is("counter not at target"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is(1)));
        checkMark(t, history, 5, "after increment");

        checkMark(t, history, 6, "before checkTarget 2");
        t.examine(history.get(7))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.check, e1 -> e1.is("counter at target"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(2))
                .map(call -> call.otherObjects[0], e1 -> e1.is(1))
                .map(call -> call.otherObjects[1], e1 -> e1.is(0)));
        checkMark(t, history, 8, "after checkTarget 2");
    }

    private static void checkMark(Testing t, List<Recorder.Call> history,
        int index, String mark) {
        t.examine(history.get(index))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1 -> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is("mark: " + mark)));
    }

    // No @Test due to expected test failure.
    public static void defaultTesting(Testing t) {
        Counter c = t.counter();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);

        t.failedCheck("mark: before checkTarget");
        c.checkTarget(); // Expected to fail.
        t.failedCheck("mark: after checkTarget");

        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);
        c.increment();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
        c.checkTarget();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);

        t.failedCheck("mark: before increment");
        c.increment(); // Expected to fail.
        t.failedCheck("mark: after increment");

        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
        c.reset();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);

        t.failedCheck("mark: before checkTarget 2");
        c.checkTarget(); // Expected to fail.
        t.failedCheck("mark: after checkTarget 2");

        t.examine(c.target()).is(1);
        t.examine(c.value()).is(0);
        c.increment();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
        c.checkTarget();
        t.examine(c.target()).is(1);
        t.examine(c.value()).is(1);
    }

    @Test
    public static void testTesting17(Testing t) {
        int target = 17;
        List<Recorder.Call> history  = Helpers.testHistory(
            t, TestCounter.class, "testing17");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(10);

        checkMark(t, history, 0, "before checkTarget");
        t.examine(history.get(1))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.check, e1 -> e1.is("counter at target"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(2))
                .map(call -> call.otherObjects[0], e1 -> e1.is(17))
                .map(call -> call.otherObjects[1], e1 -> e1.is(0)));
        checkMark(t, history, 2, "after checkTarget");

        checkMark(t, history, 3, "before increment");
        t.examine(history.get(4))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.check, e1 -> e1.is("counter not at target"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is(17)));
        checkMark(t, history, 5, "after increment");

        checkMark(t, history, 6, "before checkTarget 2");
        t.examine(history.get(7))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.check, e1 -> e1.is("counter at target"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(2))
                .map(call -> call.otherObjects[0], e1 -> e1.is(17))
                .map(call -> call.otherObjects[1], e1 -> e1.is(1)));
        checkMark(t, history, 8, "after checkTarget 2");
    }

    // No @Test due to expected test failure.
    public static void testing17(Testing t) {
        int target = 17;
        Counter c = t.counter(target);
        t.examine(c.target()).is(target);
        t.examine(c.value()).is(0);

        t.failedCheck("mark: before checkTarget");
        c.checkTarget(); // Expected to fail.
        t.failedCheck("mark: after checkTarget");

        doLoop(t, c, target);

        t.failedCheck("mark: before increment");
        c.increment(); // Expected to fail.
        t.failedCheck("mark: after increment");

        t.examine(c.target()).is(target);
        t.examine(c.value()).is(target);
        c.reset();
        t.examine(c.target()).is(target);
        t.examine(c.value()).is(0);

        c.increment();
        t.examine(c.target()).is(target);
        t.examine(c.value()).is(1);
        t.failedCheck("mark: before checkTarget 2");
        c.checkTarget(); // Expected to fail.
        t.failedCheck("mark: after checkTarget 2");

        c.reset();
        t.examine(c.target()).is(target);
        t.examine(c.value()).is(0);
        doLoop(t, c, target);
    }
}
