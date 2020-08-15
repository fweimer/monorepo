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

public final class TestChecksEmpty {
    @Test
    public void test(Testing t) {
        t.examine(List.of())
            .check(Checks.empty());
        t.examine(Collections.emptyList())
            .check(Checks.empty());
        t.examine(Set.of())
            .check(Checks.empty());
        t.examine(Collections.emptySet())
            .check(Checks.empty());
    }

    @Test
    public void testNonEmpty(Testing t) {
        checkNonEmpty(t, "nonEmpty1", List.of(1));
        checkNonEmpty(t, "nonEmpty2", Set.of(1));
        checkNonEmpty(t, "nonEmpty3", List.of(1));
        checkNonEmpty(t, "nonEmpty4", Set.of(1));
    }

    public void nonEmpty1(Testing t) {
        t.examine(List.of(1))
            .check(Checks.empty());
    }

    public void nonEmpty2(Testing t) {
        t.examine(Set.of(1))
            .check(Checks.empty());
    }

    public void nonEmpty3(Testing t) {
        t.examine(Collections.singletonList(1))
            .check(Checks.empty());
    }

    public void nonEmpty4(Testing t) {
        t.examine(Collections.singleton(1))
            .check(Checks.empty());
    }

    private void checkNonEmpty(Testing t, String methodName,
        Collection<Integer> nonEmpty) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("empty"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), e1 -> e1.is(nonEmpty))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
    }

    @Test
    public void testExaminedNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "examinedNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("empty (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
    }

    public void examinedNull(Testing t) {
        t.examine((Collection<Object>) null)
            .check(Checks.empty());
    }
}
