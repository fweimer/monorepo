package enyo.testing.runtime.tests;

import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;

import java.util.List;

public final class TestTesting {
    @Test
    public void testFullBarrier(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, TestTesting.class, "fullBarrier1");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);

        // A test barrier does not record an exception if terminated.
        // Also test integration with Testing.expect().
        for (String method : List.of(
                "fullBarrier2", "fullBarrierExpect1", "fullBarrierExpect2")) {
            history = Helpers.testHistory(t, TestTesting.class, method);
            t.examine(Helpers.isSuccess(t, history)).is(false);
            t.examine(history.size()).is(2);
            t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
            t.examine(history.get(1)).isExactClass(Recorder.Completed.class);
        }
    }

    @Test
    public void fullBarrier1(Testing t) {
        t.fullBarrier();
    }

    // No @Test because of expected failure.
    public void fullBarrier2(Testing t) {
        // Produce an error.
        t.examine(true).is(false);

        // This should throw.
        t.fullBarrier();

        // This should not be reached.
        throw new UnsupportedOperationException("unexpected");
    }

    // No @Test because of expected failure.
    public void fullBarrierExpect1(Testing t) {
        // Produce an error.
        t.examine(true).is(false);

        // This should throw, but not record an exception match failure.
        t.expect(() -> t.fullBarrier(), IllegalArgumentException.class);

        // This should not be reached.
        throw new UnsupportedOperationException("unexpected");
    }

    // No @Test because of expected failure.
    public void fullBarrierExpect2(Testing t) {
        // Produce an error.
        t.examine(true).is(false);

        // This should throw, but not record an exception match failure.
        t.expect(() -> t.fullBarrier(), IllegalArgumentException.class)
	    .test(_ -> t.assertFail("unreachable"));

        // This should not be reached.
        throw new UnsupportedOperationException("unexpected");
    }

    @Test
    public void testExpect(Testing t) {
	@SuppressWarnings("serial")
	class E extends Exception { }
	var e = new E();
	t.expect(() -> { throw e; }, E.class).isSame(e);

	@SuppressWarnings("serial")
	class E1 extends E { }
	var e1 = new E1();
	t.expect(() -> { throw e1; }, E.class).isSame(e1);
    }

    @Test
    public void testUnsupported(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedSuccess1");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .isExactClass(Recorder.Unsupported.class);
        t.examine(((Recorder.Unsupported) history.get(0)).context)
            .is(List.of(new Examined("unsupported context", null)));

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedSuccess2");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Unsupported.class);
        t.examine(((Recorder.Unsupported) history.get(0)).context)
            .is(List.of(new Examined("unsupported context 1", null)));
        t.examine(history.get(1)).isExactClass(Recorder.Unsupported.class);
        t.examine(((Recorder.Unsupported) history.get(1)).context)
            .is(List.of(new Examined("unsupported context 2", null)));

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedFailure1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Unsupported.class);
        t.examine(((Recorder.Unsupported) history.get(0)).context)
            .is(List.of(new Examined("unsupported context", null)));
        t.examine(history.get(1)).isExactClass(Recorder.Failure.class);

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedFailure2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        t.examine(history.get(1)).isExactClass(Recorder.Unsupported.class);
        t.examine(((Recorder.Unsupported) history.get(1)).context)
            .is(List.of(new Examined("unsupported context", null)));
    }

    // Not marked as @Test due to spurious unsupported message.
    public void unsupportedSuccess1(Testing t) {
        t.unsupported("unsupported context");
    }

    // Not marked as @Test due to spurious unsupported message.
    public void unsupportedSuccess2(Testing t) {
        t.unsupported("unsupported context 1");
        t.unsupported("unsupported context 2");
    }

    // Not marked as @Test due to expected test failure.
    public void unsupportedFailure1(Testing t) {
        t.unsupported("unsupported context");

        // Produce an error.
        t.examine(true).is(false);
    }

    // Not marked as @Test due to expected test failure.
    public void unsupportedFailure2(Testing t) {
        // Produce an error.
        t.examine(true).is(false);

        t.unsupported("unsupported context");
    }

    @Test
    public void testAssertFail(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, TestTesting.class, "assertFail1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)) .isExactClass(Recorder.Exception.class);
        t.examine(((Recorder.Exception) history.get(0))
            .exception)
            .isExactClass(TestFailure.class);
        t.examine(history.get(1)).isExactClass(Recorder.Completed.class);

        // Test that catching the assertion failure exception still
        // results in test failure.
        history = Helpers.testHistory(t, TestTesting.class, "assertFail2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(1);
        t.examine(history.get(0)).isExactClass(Recorder.Completed.class);
    }

    // Not marked as @Test due to expected test failure.
    public void assertFail1(Testing t) {
        t.assertFail("expected");
    }

    // Not marked as @Test due to expected test failure.
    public void assertFail2(Testing t) {
        try {
            t.assertFail("expected");
        } catch (Throwable e) {
            // Do not report to caller.
        }
    }

    @Test
    public void testAssertTrue(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, TestTesting.class, "assertTrue1");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);

        history = Helpers.testHistory(t, TestTesting.class, "assertTrue2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.examine(((Recorder.Exception) history.get(0)).exception)
            .isExactClass(TestFailure.class);
        t.examine(history.get(1)).isExactClass(Recorder.Completed.class);
    }

    @Test
    public void assertTrue1(Testing t) {
        t.assertTrue(true);
    }

    // Not marked as @Test due to expected test failure.
    public void assertTrue2(Testing t) {
        t.assertTrue(false);
    }

    @Test
    public void testAssertFalse(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, TestTesting.class, "assertFalse1");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);

        history = Helpers.testHistory(t, TestTesting.class, "assertFalse2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.examine(((Recorder.Exception) history.get(0)).exception)
            .isExactClass(TestFailure.class);
        t.examine(history.get(1)).isExactClass(Recorder.Completed.class);
    }

    @Test
    public void assertFalse1(Testing t) {
        t.assertFalse(false);
    }

    // Not marked as @Test due to expected test failure.
    public void assertFalse2(Testing t) {
        t.assertFalse(true);
    }

    @Test
    public static void testFailedCheck(Testing t) {
        List<Recorder.Call> history  = Helpers.testHistory(
            t, TestTesting.class, "failedCheck");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(8);
        t.examine(history.get(0))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is("check1"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is("check2"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("object1")));
        t.examine(history.get(2))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is("check3"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.isNull()));
        t.examine(history.get(3))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is("check4"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(2))
                .map(call -> call.otherObjects[0], e1 -> e1.isNull())
                .map(call -> call.otherObjects[1], e1 -> e1.isNull()));
        t.examine(history.get(4))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is(
                        "non-null objects array in Testing.failedCheck"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("check5")));
        t.examine(history.get(5))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is("check5"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(6))
            .map(Recorder.Failure.class,
                e -> e
                .map(call -> call.context, e1-> e1.is(List.of()))
                .map(call -> call.check, e1 -> e1.is(
                        "(non-null check argument in Testing.failedCheck)"))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("object2")));
    }

    // Not marked as @Test due to expected test failure.
    public static void failedCheck(Testing t) {
        t.failedCheck("check1");
        t.failedCheck("check2", "object1");
        t.failedCheck("check3", (Object) null);
        t.failedCheck("check4", null, null);
        t.failedCheck("check5", (Object[]) null);
        t.failedCheck(null, "object2");
    }
}
