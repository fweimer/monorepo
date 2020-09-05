package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Counter;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.Comparator;
import java.util.List;

public final class TestChecksMonotonic {
    @Test
    public static void testEmpty(Testing t) {
        t.examine(List.<String>of())
            .check(Checks.monotonic())
            .check(Checks.monotonic(
                    (a, b) -> t.assertFail("unreachable")))
            .check(Checks.strictlyMonotonic())
            .check(Checks.strictlyMonotonic(
                    (a, b) -> t.assertFail("unreachable")));
    }

    @Test
    public static void testSingleton(Testing t) {
        t.examine(List.of(""))
            .check(Checks.monotonic())
            .check(Checks.monotonic(
                    (a, b) -> t.assertFail("unreachable")))
            .check(Checks.strictlyMonotonic())
            .check(Checks.strictlyMonotonic(
                    (a, b) -> t.assertFail("unreachable")));
    }

    @Test
    public static void testTwo(Testing t) {
        t.examine(List.of(1, 2))
            .check(Checks.monotonic())
            .check(Checks.strictlyMonotonic());

        Counter counter = t.counter();
        Comparator<Integer> compare = (a, b) -> {
            counter.increment();
            t.examine(a).is(1);
            t.examine(b).is(2);
            return -1;
        };

        t.examine(List.of(1, 2))
            .check(Checks.monotonic(compare));
        counter.checkTarget();

        counter.reset();
        t.examine(List.of(1, 2))
            .check(Checks.strictlyMonotonic(compare));
        counter.checkTarget();
    }

    @Test
    public void testTwoNot1(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "twoNot1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("monotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of("2", "1")))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(0, "2", "1"))));
    }

    // Not @Test, expected to fail.
    public static void twoNot1(Testing t) {
        t.examine(List.of("2", "1"))
            .check(Checks.monotonic());
    }

    @Test
    public void testTwoNot2(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "twoNot2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("monotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of(1, 2)))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(0, 1, 2))));
    }

    // Not @Test, expected to fail.
    public static void twoNot2(Testing t) {
        Counter counter = t.counter();
        Comparator<Integer> compare = (a, b) -> {
            counter.increment();
            t.examine(a).is(1);
            t.examine(b).is(2);
            return 1;
        };
        t.examine(List.of(1, 2))
            .check(Checks.monotonic(compare));
        counter.checkTarget();
    }

    @Test
    public static void testTwoNotStrictly(Testing t) {
        t.examine(List.of("1", new String("1")))
            .check(Checks.monotonic());

        Counter counter = t.counter();
        Comparator<Integer> compare = (a, b) -> {
            counter.increment();
            t.examine(a).is(1);
            t.examine(b).is(2);
            return 0;
        };

        t.examine(List.of(1, 2))
            .check(Checks.monotonic(compare));
        counter.checkTarget();
    }

    @Test
    public void testTwoNotStrictly1(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "twoNotStrictly1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("strictlyMonotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of("1", "1")))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(0, "1", "1"))));
    }

    // Not @Test, expected to fail.
    public static void twoNotStrictly1(Testing t) {
        t.examine(List.of("1", new String("1")))
            .check(Checks.strictlyMonotonic());
    }

    @Test
    public void testTwoNotStrictly2(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "twoNotStrictly2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("strictlyMonotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of(1, 2)))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(0, 1, 2))));
    }

    // Not @Test, expected to fail.
    public static void twoNotStrictly2(Testing t) {
        Counter counter = t.counter();
        Comparator<Integer> compare = (a, b) -> {
            counter.increment();
            t.examine(a).is(1);
            t.examine(b).is(2);
            return 0;
        };
        t.examine(List.of(1, 2))
            .check(Checks.strictlyMonotonic(compare));
        counter.checkTarget();
    }

    @Test
    public static void testThree(Testing t) {
        t.examine(List.of(1, 2, 3))
            .check(Checks.monotonic())
            .check(Checks.strictlyMonotonic());

        Counter counter = t.counter(2);
        Comparator<Integer> compare = (a, b) -> {
            switch (counter.value()) {
            case 0:
                t.examine(a).is(1);
                t.examine(b).is(2);
                break;
            case 1:
                t.examine(a).is(2);
                t.examine(b).is(3);
                break;
            default:
                t.failedCheck("unexpected compare call");
            }
            counter.increment();
            return -1;
        };

        t.examine(List.of(1, 2, 3))
            .check(Checks.monotonic(compare));
        counter.checkTarget();

        counter.reset();
        t.examine(List.of(1, 2, 3))
            .check(Checks.strictlyMonotonic(compare));
        counter.checkTarget();
    }

    @Test
    public void testThreeNot1(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "threeNot1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("monotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of("1", "3", "2")))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(1, "3", "2"))));
    }

    // Not @Test, expected to fail.
    public static void threeNot1(Testing t) {
        t.examine(List.of("1", "3", "2"))
            .check(Checks.monotonic());
    }

    @Test
    public void testThreeNot2(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "threeNot2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("monotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of(1, 2, 3)))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(1, 2, 3))));
    }

    // Not @Test, expected to fail.
    public static void threeNot2(Testing t) {
        Counter counter = t.counter(2);
        Comparator<Integer> compare = (a, b) -> {
            switch (counter.value()) {
            case 0:
                t.examine(a).is(1);
                t.examine(b).is(2);
                counter.increment();
                return -1;
            case 1:
                t.examine(a).is(2);
                t.examine(b).is(3);
                counter.increment();
                return 1;
            default:
                t.failedCheck("unexpected compare call");
                return 1;
            }
        };
        t.examine(List.of(1, 2, 3))
            .check(Checks.monotonic(compare));
        counter.checkTarget();
    }

    @Test
    public static void testThreeNotStrictly(Testing t) {
        t.examine(List.of(1, 2, 2))
            .check(Checks.monotonic());

        Counter counter = t.counter(2);
        Comparator<Integer> compare = (a, b) -> {
            switch (counter.value()) {
            case 0:
                t.examine(a).is(1);
                t.examine(b).is(2);
                counter.increment();
                return -1;
            case 1:
                t.examine(a).is(2);
                t.examine(b).is(3);
                counter.increment();
                return 0;
            default:
                t.failedCheck("unexpected compare call");
                return 1;
            }
        };

        t.examine(List.of(1, 2, 3))
            .check(Checks.monotonic(compare));
        counter.checkTarget();
    }

    @Test
    public void testThreeNotStrictly1(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "threeNotStrictly1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("strictlyMonotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of("1", "2", "2")))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(1, "2", "2"))));
    }

    // Not @Test, expected to fail.
    public static void threeNotStrictly1(Testing t) {
        t.examine(List.of("1", "2", "2"))
            .check(Checks.strictlyMonotonic());
    }

    @Test
    public void testThreeNotStrictly2(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "threeNotStrictly2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("strictlyMonotonic"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(List.of(1, 2, 3)))
                .map(call -> call.otherObjects, e1 -> e1.check(
                        Checks.arrayEquals(1, 2, 3))));
    }

    // Not @Test, expected to fail.
    public static void threeNotStrictly2(Testing t) {
        Counter counter = t.counter(2);
        Comparator<Integer> compare = (a, b) -> {
            switch (counter.value()) {
            case 0:
                t.examine(a).is(1);
                t.examine(b).is(2);
                counter.increment();
                return -1;
            case 1:
                t.examine(a).is(2);
                t.examine(b).is(3);
                counter.increment();
                return 0;
            default:
                t.failedCheck("unexpected compare call");
                return 1;
            }
        };
        t.examine(List.of(1, 2, 3))
            .check(Checks.strictlyMonotonic(compare));
        counter.checkTarget();
    }

    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.monotonic(null), NullPointerException.class);
        t.expect(() -> Checks.strictlyMonotonic(null),
            NullPointerException.class);
    }
}
