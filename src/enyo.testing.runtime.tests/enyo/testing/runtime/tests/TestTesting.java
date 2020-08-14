package enyo.testing.runtime.tests;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.List;

public final class TestTesting {
    @Test
    public void testFullBarrier(Testing t) {
        List<ResultsRecorder.Call> history;

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
            t.examine(history.get(0))
                .isExactClass(ResultsRecorder.FailureCall.class);
            t.examine(history.get(1))
                .isExactClass(ResultsRecorder.CompletedCall.class);
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
        t.expect(() -> t.fullBarrier(), IllegalArgumentException.class,
                 e -> t.assertFail("unreachable"));

        // This should not be reached.
        throw new UnsupportedOperationException("unexpected");
    }

    @Test
    public void testUnsupported(Testing t) {
        List<ResultsRecorder.Call> history;

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedSuccess1");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .isExactClass(ResultsRecorder.UnsupportedCall.class);
        t.examine(((ResultsRecorder.UnsupportedCall) history.get(0)).context)
            .is("unsupported context");

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedSuccess2");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .isExactClass(ResultsRecorder.UnsupportedCall.class);
        t.examine(((ResultsRecorder.UnsupportedCall) history.get(0)).context)
            .is("unsupported context 1");
        t.examine(history.get(1))
            .isExactClass(ResultsRecorder.UnsupportedCall.class);
        t.examine(((ResultsRecorder.UnsupportedCall) history.get(1)).context)
            .is("unsupported context 2");

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedFailure1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .isExactClass(ResultsRecorder.UnsupportedCall.class);
        t.examine(((ResultsRecorder.UnsupportedCall) history.get(0)).context)
            .is("unsupported context");
        t.examine(history.get(1))
            .isExactClass(ResultsRecorder.FailureCall.class);

        history = Helpers.testHistory(
            t, TestTesting.class, "unsupportedFailure2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .isExactClass(ResultsRecorder.FailureCall.class);
        t.examine(history.get(1))
            .isExactClass(ResultsRecorder.UnsupportedCall.class);
        t.examine(((ResultsRecorder.UnsupportedCall) history.get(1)).context)
            .is("unsupported context");
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
}
