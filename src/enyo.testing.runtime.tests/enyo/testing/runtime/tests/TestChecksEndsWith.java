package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestChecksEndsWith {
    @Test
    public static void testNull(Testing t) {
        t.expect(() -> Checks.endsWith(null), NullPointerException.class);
    }

    @Test
    public static void testPass(Testing t) {
        t.examine("").check(Checks.endsWith(""));
        t.examine("1").check(Checks.endsWith(""));
        t.examine("1").check(Checks.endsWith("1"));
        t.examine("21").check(Checks.endsWith("1"));
        t.examine("21").check(Checks.endsWith("21"));
    }

    @Test
    public void testFailure(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "failure");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("endsWith"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(""))
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals("1"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("endsWith"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), e1 -> e1.is("1"))
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals("21"))));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: failure")));
    }

    // No @Test due to expected test failure.
    public static void failure(Testing t) {
        t.examine("").check(Checks.endsWith("1"));
        t.examine("1").check(Checks.endsWith("21"));
        t.failedCheck("mark: failure");
    }

    @Test
    public void testExaminedNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "examinedNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("endsWith (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals("3"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: examinedNull")));
    }
    public static void examinedNull(Testing t) {
        t.examine((String) null).check(Checks.endsWith("3"));
        t.failedCheck("mark: examinedNull");
    }
}
