package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class TestChecksContains {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.contains(null), NullPointerException.class);
    }

    @Test
    public void test(Testing t) {
        t.examine(List.of(1))
            .check(Checks.contains(1));
        t.examine(Set.of(1, 2))
            .check(Checks.contains(1));
    }

    @Test
    public void testFailure(Testing t) {
        testFailure(t, "failure1", List.of(), 1);
        testFailure(t, "failure2", Set.of(), 2);
        testFailure(t, "failure3", List.of(30), 3);
        testFailure(t, "failure4", Set.of(40), 4);
    }

    private void testFailure(Testing t, String methodName,
        Collection<?> examined, Object checked) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("contains"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), e1 -> e1.is(examined))
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals(checked))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: " + methodName)));
    }


    public void failure1(Testing t) {
        t.examine(List.of()).check(Checks.contains(1));
        t.failedCheck("mark: failure1");
    }

    public void failure2(Testing t) {
        t.examine(Set.of()).check(Checks.contains(2));
        t.failedCheck("mark: failure2");
    }

    public void failure3(Testing t) {
        t.examine(List.of(30)).check(Checks.contains(3));
        t.failedCheck("mark: failure3");
    }

    public void failure4(Testing t) {
        t.examine(Set.of(40)).check(Checks.contains(4));
        t.failedCheck("mark: failure4");
    }

    @Test
    public void testExaminedNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "examinedNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("contains (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects,
                    e1 -> e1.check(Checks.arrayEquals(100))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: examinedNull")));
    }

    public void examinedNull(Testing t) {
        t.examine((Collection<Integer>) null).check(Checks.contains(100));
        t.failedCheck("mark: examinedNull");
    }
}
