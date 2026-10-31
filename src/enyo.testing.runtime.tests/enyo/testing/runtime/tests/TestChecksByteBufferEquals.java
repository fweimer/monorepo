package enyo.testing.runtime.tests;

import enyo.testing.Checks;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;

import enyo.util.ByteString;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public final class TestChecksByteBufferEquals {
    @Test
    public void testNull(Testing t) {
        t.expect(() -> Checks.byteBufferEquals((byte[]) null),
            NullPointerException.class);
    }

    @Test
    public void testEmptyEmpty(Testing t) {
        t.examine(ByteBuffer.allocate(0))
            .check(Checks.byteBufferEquals(new byte[0]));
	var buf = ByteBuffer.allocateDirect(3);
	buf.position(2);
	buf.limit(2);
        t.examine(buf).check(Checks.byteBufferEquals(new byte[0]));
    }

    @Test
    public void test00(Testing t) {
        t.examine(ByteBuffer.wrap(new byte[] { 0 }))
            .check(Checks.byteBufferEquals(new byte[] { 0 }));
    }

    @Test
    public void test11(Testing t) {
        t.examine(ByteBuffer.wrap(new byte[] { 1 }))
            .check(Checks.byteBufferEquals(new byte[] { 1 }));
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
                        "byteBufferEquals (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0])
                .map(byte[].class, e1 -> e1.map(e2 -> e2.length).is(0)));
    }

    public void examinedNull(Testing t) {
        t.examine((ByteBuffer) null)
            .check(Checks.byteBufferEquals());
    }

    @Test
    public void testZeroEmpty(Testing t) {
        checkOneFailure(t, "zeroEmpty", new byte[1], new byte[0]);
    }

    public void zeroEmpty(Testing t) {
        t.examine(ByteBuffer.wrap(new byte[1]))
            .check(Checks.byteBufferEquals());
    }

    @Test
    public void testEmptyZero(Testing t) {
        checkOneFailure(t, "emptyZero", new byte[0], new byte[] { 0 });
    }

    public void emptyZero(Testing t) {
        t.examine(ByteBuffer.allocate(1).position(1))
            .check(Checks.byteBufferEquals((byte) 0));
    }

    @Test
    public void testZeroOne(Testing t) {
        checkOneFailure(t, "zeroOne", new byte[1], new byte[] { 1 });
    }

    public void zeroOne(Testing t) {
        t.examine(ByteBuffer.wrap(new byte[1]))
            .check(Checks.byteBufferEquals((byte) 1));
    }

    @Test
    public void testZeroZeroOne(Testing t) {
        checkOneFailure(t, "zeroZeroOne", new byte[1], new byte[] { 0, 1 });
    }

    public void zeroZeroOne(Testing t) {
        t.examine(ByteBuffer.wrap(new byte[1]))
            .check(Checks.byteBufferEquals((byte) 0, (byte) 1));
    }

    private void checkOneFailure(Testing t, String methodName,
        byte[] examinedBuffer, byte[] otherBuffer) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), methodName);
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("byteBufferEquals"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> (ByteBuffer) call.context.get(0).value(),
                    e1 -> e1.map(ByteBuffer::remaining,
                        e2 -> e2.is(examinedBuffer.length))
			.map(ByteString::of,
			    e3 -> e3.is(ByteString.of(examinedBuffer))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0])
                .map(byte[].class, e1 -> e1
                    .map(array -> array.length,
                        e2 -> e2.is(otherBuffer.length))
                    .test(array -> Arrays.equals(otherBuffer, array))));
    }
}
