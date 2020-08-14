package enyo.testing.runtime.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;

import java.util.Arrays;
import java.util.List;

public final class TestObjectExaminer {
    @Test
    public void testContext(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        history = Helpers.testHistory(t, TestObjectExaminer.class, "context1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", null)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 1 }));

        history = Helpers.testHistory(t, TestObjectExaminer.class, "context2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", null)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 1 }));
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.context).is(List.of(new Examined("context2", null)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 2 }));

        history = Helpers.testHistory(t, TestObjectExaminer.class, "context3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context)
            .is(List.of(new Examined("context1", "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string2" }));
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.context)
            .is(List.of(new Examined("context1", "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[]  {"string3" }));
    }

    // No @Test due to expect test failure.
    public void context1(Testing t) {
        t.examine(null)
            .context("context1")
            .nullableIs(1);
    }

    // No @Test due to expect test failure.
    public void context2(Testing t) {
        t.examine(null)
            .context("context1")
            .nullableIs(1)
            .context("context2")
            .nullableIs(2);
    }

    // No @Test due to expect test failure.
    public void context3(Testing t) {
        var e = t.examine("string1")
            .context("context1")
            .is("string2");
        t.expect(() -> e.context(null), NullPointerException.class);
        e.is("string3");
    }

    @Test
    public void testIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "is1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string2" }));

        // Test reporting of examined null objects.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "is2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "is3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "is4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));
    }

    // No @Test due to expect test failure.
    public void is1(Testing t) {
        t.examine("string1")
            .is("string1")
            .is(new String("string1"))
            .is("string2")
            .barrier()
            .is("string3");
    }

    // No @Test due to expect test failure.
    public void is2(Testing t) {
        t.examine(null)
            .is("string1");
    }

    // No @Test due to expect test failure.
    public void is3(Testing t) {
        t.examine("string1")
            .is(null);
    }

    // No @Test due to expect test failure.
    public void is4(Testing t) {
        t.examine(null)
            .is(null);
    }

    @Test
    public void testIsnt(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isnt1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of examined null objects.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isnt2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isnt3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isnt4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));
    }

    // No @Test due to expect test failure.
    public void isnt1(Testing t) {
        t.examine("string1")
            .isnt("string2")
            .isnt("string1")
            .barrier()
            .isnt("string1");
    }

    // No @Test due to expect test failure.
    public void isnt2(Testing t) {
        t.examine(null)
            .isnt("string1");
    }

    // No @Test due to expect test failure.
    public void isnt3(Testing t) {
        t.examine("string1")
            .isnt(null);
    }

    // No @Test due to expect test failure.
    public void isnt4(Testing t) {
        t.examine(null)
            .isnt(null);
    }
}
