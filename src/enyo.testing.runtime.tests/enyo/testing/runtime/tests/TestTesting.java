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
}
