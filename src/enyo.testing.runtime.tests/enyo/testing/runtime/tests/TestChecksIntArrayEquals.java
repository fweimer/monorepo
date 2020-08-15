package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.Arrays;
import java.util.List;

public final class TestChecksIntArrayEquals {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.intArrayEquals((int[]) null),
            NullPointerException.class);
    }

    @Test
    public void testEmptyEmpty(Testing t) {
        t.examine(new int[0])
            .check(Checks.intArrayEquals(new int[0]));
    }

    @Test
    public void test00(Testing t) {
        t.examine(new int[] { 0 })
            .check(Checks.intArrayEquals(new int[] { 0 }));
    }

    @Test
    public void test11(Testing t) {
        t.examine(new int[] { 1 })
            .check(Checks.intArrayEquals(new int[] { 1 }));
    }

    @Test
    public void testExaminedNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "examinedNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "intArrayEquals (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
    }

    public void examinedNull(Testing t) {
        t.examine((int[]) null)
            .check(Checks.intArrayEquals());
    }

    @Test
    public void testZeroEmpty(Testing t) {
        checkOneFailure(t, "zeroEmpty", new int[1], new Integer[0]);
    }

    public void zeroEmpty(Testing t) {
        t.examine(new int[1])
            .check(Checks.intArrayEquals());
    }

    @Test
    public void testEmptyZero(Testing t) {
        checkOneFailure(t, "emptyZero", new int[0], new Integer[] { 0 });
    }

    public void emptyZero(Testing t) {
        t.examine(new int[0])
            .check(Checks.intArrayEquals(0));
    }

    @Test
    public void testZeroOne(Testing t) {
        checkOneFailure(t, "zeroOne", new int[1], new Integer[] { 1 });
    }

    public void zeroOne(Testing t) {
        t.examine(new int[1])
            .check(Checks.intArrayEquals(1));
    }

    @Test
    public void testZeroZeroOne(Testing t) {
        checkOneFailure(t, "zeroZeroOne", new int[1], new Integer[] { 0, 1 });
    }

    public void zeroZeroOne(Testing t) {
        t.examine(new int[1])
            .check(Checks.intArrayEquals(0, 1));
    }

    private void checkOneFailure(Testing t, String methodName,
        int[] examinedArray, Integer[] otherArray) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("intArrayEquals"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> (int[]) call.context.get(0).value(),
                    e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(examinedArray.length))
                    .test(array -> Arrays.equals(examinedArray, array)))
                .map(call -> call.otherObjects,
                    e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(otherArray.length))
                    .test(array -> Arrays.equals(otherArray, array))));
    }
}
