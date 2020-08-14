package enyo.testing.runtime.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;

import java.util.Arrays;
import java.util.List;

public final class TestCharExaminer {
    @Test
    public void testContext(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        history = Helpers.testHistory(t, TestCharExaminer.class, "context1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", '0')));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { '1' }));

        history = Helpers.testHistory(t, TestCharExaminer.class, "context3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", '0')));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { '2' }));
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.context).is(List.of(new Examined("context1", '0')));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { '3' }));
    }

    // No @Test due to expect test failure.
    public void context1(Testing t) {
        t.examine('0')
            .context("context1")
            .is('1');
    }

    // No @Test due to expect test failure.
    public void context3(Testing t) {
        var e = t.examine('0')
            .context("context1")
            .is('2');
        t.expect(() -> e.context(null), NullPointerException.class);
        e.is('3');
    }

    @Test
    public void testIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestCharExaminer.class, "is1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("==");
        t.examine(failure.context).is(List.of(new Examined(null, '1')));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { '2' }));
    }

    // No @Test due to expect test failure.
    public void is1(Testing t) {
        t.examine('1')
            .is('1')
            .is('2')
            .barrier()
            .is('3');
    }

    @Test
    public void testIsnt(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestCharExaminer.class, "isnt1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!=");
        t.examine(failure.context).is(List.of(new Examined(null, '1')));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { '1' }));
    }

    // No @Test due to expect test failure.
    public void isnt1(Testing t) {
        t.examine('1')
            .isnt('2')
            .isnt('1')
            .barrier()
            .isnt('1');
    }
}
