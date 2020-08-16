package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestChecksStartsWith {
    @Test
    public static void testNull(Testing t) {
        t.expect(() -> Checks.startsWith(null), NullPointerException.class);
    }

    @Test
    public static void testPass(Testing t) {
        t.examine("").check(Checks.startsWith(""));
        t.examine("1").check(Checks.startsWith(""));
        t.examine("1").check(Checks.startsWith("1"));
        t.examine("12").check(Checks.startsWith("1"));
        t.examine("12").check(Checks.startsWith("12"));
    }

    @Test
    public void testFailure(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "failure");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("startsWith"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.is(""))
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals("1"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("startsWith"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), e1 -> e1.is("1"))
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals("12"))));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: failure")));
    }

    // No @Test due to expected test failure.
    public static void failure(Testing t) {
        t.examine("").check(Checks.startsWith("1"));
        t.examine("1").check(Checks.startsWith("12"));
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
                .map(call -> call.check, e1 -> e1.is("startsWith (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals("3"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: examinedNull")));
    }
    public static void examinedNull(Testing t) {
        t.examine((String) null).check(Checks.startsWith("3"));
        t.failedCheck("mark: examinedNull");
    }
}
