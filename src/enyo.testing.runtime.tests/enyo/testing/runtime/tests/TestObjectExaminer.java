package enyo.testing.runtime.tests;

import enyo.testing.Order;
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

    @Test
    public void testNullabeIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "nullableIs1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string2" }));

        // Test reporting of examined null objects.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "nullableIs2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "nullableIs3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "nullableIs4");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);
    }

    // No @Test due to expect test failure.
    public void nullableIs1(Testing t) {
        t.examine("string1")
            .nullableIs("string1")
            .nullableIs(new String("string1"))
            .nullableIs("string2")
            .barrier()
            .nullableIs("string3");
    }

    // No @Test due to expect test failure.
    public void nullableIs2(Testing t) {
        t.examine(null)
            .nullableIs("string1");
    }

    // No @Test due to expect test failure.
    public void nullableIs3(Testing t) {
        t.examine("string1")
            .nullableIs(null);
    }

    @Test
    public void nullableIs4(Testing t) {
        t.examine(null)
            .nullableIs(null);
    }

    final static class AlwaysEquals {
        @Override
        public boolean equals(Object o) {
            return o != null && o.getClass() == AlwaysEquals.class;
        }

        @Override
        public int hashCode() {
            return 1009;
        }

        final static AlwaysEquals INSTANCE1 = new AlwaysEquals();
        final static AlwaysEquals INSTANCE2 = new AlwaysEquals();
    }

    @Test
    public void testAlwaysEquals() {
        Order<AlwaysEquals> order = new Order<>();
        order.group()
            .add(AlwaysEquals.INSTANCE1)
            .add(AlwaysEquals.INSTANCE1)
            .add(AlwaysEquals.INSTANCE2)
            .add(AlwaysEquals.INSTANCE2)
            .add(new AlwaysEquals())
            .add(new AlwaysEquals())
            .close();
        order.check();
    }

    @Test
    public void testIsSame(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isSame1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("==");
        t.examine(failure.context)
            .is(List.of(new Examined(null, AlwaysEquals.INSTANCE1)));
        t.assertTrue(failure.context.get(0).value() == AlwaysEquals.INSTANCE1);
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { AlwaysEquals.INSTANCE2 }));
        t.assertTrue(failure.otherObjects[0] == AlwaysEquals.INSTANCE2);

        // Test reporting of examined null objects.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isSame2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("== (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isSame3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("== (not null)");
        t.examine(failure.context)
            .is(List.of(new Examined(null, AlwaysEquals.INSTANCE1)));
        t.assertTrue(failure.context.get(0).value() == AlwaysEquals.INSTANCE1);
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isSame4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("== (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));
    }

    // No @Test due to expect test failure.
    public void isSame1(Testing t) {
        t.examine(AlwaysEquals.INSTANCE1)
            .isSame(AlwaysEquals.INSTANCE1)
            .isSame(AlwaysEquals.INSTANCE2)
            .barrier()
            .isSame(new AlwaysEquals());
    }

    // No @Test due to expect test failure.
    public void isSame2(Testing t) {
        t.examine(null)
            .isSame(AlwaysEquals.INSTANCE2);
    }

    // No @Test due to expect test failure.
    public void isSame3(Testing t) {
        t.examine(AlwaysEquals.INSTANCE1)
            .isSame(null);
    }

    // No @Test due to expect test failure.
    public void isSame4(Testing t) {
        t.examine(null)
            .isSame(null);
    }
}
