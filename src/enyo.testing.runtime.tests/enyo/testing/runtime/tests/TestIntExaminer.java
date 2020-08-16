package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;
import enyo.testing.runtime.ExceptionPhase;

import java.util.Arrays;
import java.util.List;

public final class TestIntExaminer {
    @Test
    public void testContext(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        history = Helpers.testHistory(t, TestIntExaminer.class, "context1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", 0)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 1 }));

        history = Helpers.testHistory(t, TestIntExaminer.class, "context3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", 0)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 2 }));
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.context).is(List.of(new Examined("context1", 0)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 3 }));
    }

    // No @Test due to expect test failure.
    public void context1(Testing t) {
        t.examine(0)
            .context("context1")
            .is(1);
    }

    // No @Test due to expect test failure.
    public void context3(Testing t) {
        var e = t.examine(0)
            .context("context1")
            .is(2);
        t.expect(() -> e.context(null), NullPointerException.class);
        e.is(3);
    }

    @Test
    public void testMap(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, getClass(), "map1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Failure.class)
            .map(call -> call.check, e -> e.is("map (not null)"))
            .map(call -> call.context,
                e -> e.is(List.of(new Examined(null, 17))))
            .map(call -> call.otherObjects,
                e-> e.check(Checks.arrayEquals(new Object[] { null })));

        history = Helpers.testHistory(t, getClass(), "map2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Exception.class)
            .map(call -> call.context,
                e -> e.is(List.of(new Examined("context1", 17))))
            .map(call -> call.phase,
                e -> e.is(ExceptionPhase.EXAMINER))
            .map(call -> call.exception,
                e -> e
                .isExactClass(UnsupportedOperationException.class)
                .map(Throwable::getMessage)
                .is("expected"));

        // Check that TestAlreadyFailed from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, getClass(), "map3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Failure.class)
            .map(call -> call.check, e -> e.is("equals"))
            .map(call -> call.context,
                e -> e.is(List.of(new Examined(null, true))))
            .map(call -> call.otherObjects,
                e -> e.check(Checks.arrayEquals(false)));

        // Check that TestFailure from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, getClass(), "map4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Exception.class)
            .map(call -> call.context, e -> e.is(List.of()))
            .map(call -> call.phase, e -> e.is(ExceptionPhase.EXECUTION))
            .map(call -> call.exception,
                e -> e
                .isExactClass(TestFailure.class)
                .map(Throwable::getMessage)
                .is("expected"));

        // Working barrier.
        history = Helpers.testHistory(t, getClass(), "map5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class)
            .map(call -> call.check, e -> e.is("=="))
            .map(call -> call.context,
                e -> e.is(List.of(new Examined("context1", 17))))
            .map(call -> call.otherObjects,
                e -> e.check(Checks.arrayEquals(18)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class)
            .map(call -> call.check)
            .is("mark: map5");
    }

    @Test
    public void map0(Testing t) {
        t.examine(17)
            .map(i -> i + 1)
            .is(18);
    }

    public void map1(Testing t) {
        t.examine(17)
            .map(null)
            .is(18);
    }

    public void map2(Testing t) {
        t.examine(17)
            .context("context1")
            .map(i -> { throw new UnsupportedOperationException("expected"); })
            .is(18);
    }

    public void map3(Testing t) {
        t.examine(17)
            .context("context1")
            .map(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return 0; // Not reached.
                })
            .is(18);
    }

    public void map4(Testing t) {
        t.examine(17)
            .context("context1")
            .map(s -> {
                    t.assertFail("expected");
                    return 0; // Not reached.
                })
            .is(18);
    }

    public void map5(Testing t) {
        t.examine(17)
            .context("context1")
            .is(18)
            .barrier()
            .map(s -> t.assertFail("unreachable"))
            .is(19);
        t.failedCheck("mark: map5");
    }

    @Test
    public void testIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestIntExaminer.class, "is1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("==");
        t.examine(failure.context).is(List.of(new Examined(null, 1)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 2 }));
    }

    // No @Test due to expect test failure.
    public void is1(Testing t) {
        t.examine(1)
            .is(1)
            .is(2)
            .barrier()
            .is(3);
    }

    @Test
    public void testIsnt(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestIntExaminer.class, "isnt1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!=");
        t.examine(failure.context).is(List.of(new Examined(null, 1)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 1 }));
    }

    // No @Test due to expect test failure.
    public void isnt1(Testing t) {
        t.examine(1)
            .isnt(2)
            .isnt(1)
            .barrier()
            .isnt(1);
    }
}
