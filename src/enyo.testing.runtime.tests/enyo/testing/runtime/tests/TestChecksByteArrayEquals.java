package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.Arrays;
import java.util.List;

public final class TestChecksByteArrayEquals {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.byteArrayEquals((byte[]) null),
            NullPointerException.class);
    }

    @Test
    public void testEmptyEmpty(Testing t) {
        t.examine(new byte[0])
            .check(Checks.byteArrayEquals(new byte[0]));
    }

    @Test
    public void test00(Testing t) {
        t.examine(new byte[] { 0 })
            .check(Checks.byteArrayEquals(new byte[] { 0 }));
    }

    @Test
    public void test11(Testing t) {
        t.examine(new byte[] { 1 })
            .check(Checks.byteArrayEquals(new byte[] { 1 }));
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
                        "byteArrayEquals (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0])
                .map(byte[].class, e1 -> e1.map(e2 -> e2.length).is(0)));
    }

    public void examinedNull(Testing t) {
        t.examine((byte[]) null)
            .check(Checks.byteArrayEquals());
    }

    @Test
    public void testZeroEmpty(Testing t) {
        checkOneFailure(t, "zeroEmpty", new byte[1], new byte[0]);
    }

    public void zeroEmpty(Testing t) {
        t.examine(new byte[1])
            .check(Checks.byteArrayEquals());
    }

    @Test
    public void testEmptyZero(Testing t) {
        checkOneFailure(t, "emptyZero", new byte[0], new byte[] { 0 });
    }

    public void emptyZero(Testing t) {
        t.examine(new byte[0])
            .check(Checks.byteArrayEquals((byte) 0));
    }

    @Test
    public void testZeroOne(Testing t) {
        checkOneFailure(t, "zeroOne", new byte[1], new byte[] { 1 });
    }

    public void zeroOne(Testing t) {
        t.examine(new byte[1])
            .check(Checks.byteArrayEquals((byte) 1));
    }

    @Test
    public void testZeroZeroOne(Testing t) {
        checkOneFailure(t, "zeroZeroOne", new byte[1], new byte[] { 0, 1 });
    }

    public void zeroZeroOne(Testing t) {
        t.examine(new byte[1])
            .check(Checks.byteArrayEquals((byte) 0, (byte) 1));
    }

    private void checkOneFailure(Testing t, String methodName,
        byte[] examinedArray, byte[] otherArray) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("byteArrayEquals"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> (byte[]) call.context.get(0).value(),
                    e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(examinedArray.length))
                    .test(array -> Arrays.equals(examinedArray, array)))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0])
                .map(byte[].class, e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(otherArray.length))
                    .test(array -> Arrays.equals(otherArray, array))));
    }
}
