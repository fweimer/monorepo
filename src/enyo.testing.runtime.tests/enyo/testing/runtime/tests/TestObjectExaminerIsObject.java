package enyo.testing.runtime.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for the Examer::is* variants which erase the type of the
 * other object.
 */
public final class TestObjectExaminerIsObject {
    private static final Class<TestObjectExaminerIsObject> CLASS =
        TestObjectExaminerIsObject.class;

    @Test
    public static void testWithErasure(Testing t) {
        // These would not compile without erasure.
        t.examine(List.of("1"))
            .isObject(List.<Object>of("1"));
        t.examine(List.of("1"))
            .nullableIsObject(List.<Object>of("1"));
        t.examine(List.of("1"))
            .isntObject(List.<Object>of("2"));
        Integer one = 1;
        Number oneNumber = one;
        t.examine(one)
            .isSameObject(oneNumber);
        t.examine(Arrays.asList("1"))
            .isNotSameObject(List.<Object>of("1"));
    }

    @Test
    public void testIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, CLASS, "is1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string2" }));

        // Test reporting of examined null objects.
        history = Helpers.testHistory(t, CLASS, "is2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(t, CLASS, "is3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(t, CLASS, "is4");
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
            .isObject("string1")
            .isObject(new String("string1"))
            .isObject("string2")
            .barrier()
            .isObject("string3");
    }

    // No @Test due to expect test failure.
    public void is2(Testing t) {
        t.examine(null)
            .isObject("string1");
    }

    // No @Test due to expect test failure.
    public void is3(Testing t) {
        t.examine("string1")
            .isObject(null);
    }

    // No @Test due to expect test failure.
    public void is4(Testing t) {
        t.examine(null)
            .isObject(null);
    }

    @Test
    public void testIsnt(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, CLASS, "isnt1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of examined null objects.
        history = Helpers.testHistory(t, CLASS, "isnt2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(t, CLASS, "isnt3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not equals (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(t, CLASS, "isnt4");
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
            .isntObject("string2")
            .isntObject("string1")
            .barrier()
            .isntObject("string1");
    }

    // No @Test due to expect test failure.
    public void isnt2(Testing t) {
        t.examine(null)
            .isntObject("string1");
    }

    // No @Test due to expect test failure.
    public void isnt3(Testing t) {
        t.examine("string1")
            .isntObject(null);
    }

    // No @Test due to expect test failure.
    public void isnt4(Testing t) {
        t.examine(null)
            .isntObject(null);
    }

    @Test
    public void testNullabeIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, CLASS, "nullableIs1");
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
            t, CLASS, "nullableIs2");
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
            t, CLASS, "nullableIs3");
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
            t, CLASS, "nullableIs4");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);
    }

    // No @Test due to expect test failure.
    public void nullableIs1(Testing t) {
        t.examine("string1")
            .nullableIsObject("string1")
            .nullableIsObject(new String("string1"))
            .nullableIsObject("string2")
            .barrier()
            .nullableIsObject("string3");
    }

    // No @Test due to expect test failure.
    public void nullableIs2(Testing t) {
        t.examine(null)
            .nullableIsObject("string1");
    }

    // No @Test due to expect test failure.
    public void nullableIs3(Testing t) {
        t.examine("string1")
            .nullableIsObject(null);
    }

    @Test
    public void nullableIs4(Testing t) {
        t.examine(null)
            .nullableIsObject(null);
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
        history = Helpers.testHistory(t, CLASS, "isSame1");
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
        history = Helpers.testHistory(t, CLASS, "isSame2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("== (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(t, CLASS, "isSame3");
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
        history = Helpers.testHistory(t, CLASS, "isSame4");
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
            .isSameObject(AlwaysEquals.INSTANCE1)
            .isSameObject(AlwaysEquals.INSTANCE2)
            .barrier()
            .isSameObject(new AlwaysEquals());
    }

    // No @Test due to expect test failure.
    public void isSame2(Testing t) {
        t.examine(null)
            .isSameObject(AlwaysEquals.INSTANCE2);
    }

    // No @Test due to expect test failure.
    public void isSame3(Testing t) {
        t.examine(AlwaysEquals.INSTANCE1)
            .isSameObject(null);
    }

    // No @Test due to expect test failure.
    public void isSame4(Testing t) {
        t.examine(null)
            .isSameObject(null);
    }

    @Test
    public void testIsNotSame(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, CLASS, "isNotSame1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!=");
        t.examine(failure.context)
            .is(List.of(new Examined(null, AlwaysEquals.INSTANCE1)));
        t.assertTrue(failure.context.get(0).value() == AlwaysEquals.INSTANCE1);
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { AlwaysEquals.INSTANCE1 }));
        t.assertTrue(failure.otherObjects[0] == AlwaysEquals.INSTANCE1);

        // Test reporting of examined null objects.
        history = Helpers.testHistory(
            t, CLASS, "isNotSame2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!= (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { AlwaysEquals.INSTANCE2 }));
        t.assertTrue(failure.otherObjects[0] == AlwaysEquals.INSTANCE2);

        // Test reporting of comparison against null.
        history = Helpers.testHistory(
            t, CLASS, "isNotSame3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!= (not null)");
        t.examine(failure.context)
            .is(List.of(new Examined(null, AlwaysEquals.INSTANCE1)));
        t.assertTrue(failure.context.get(0).value() == AlwaysEquals.INSTANCE1);
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(t, CLASS, "isNotSame4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!= (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));
    }

    // No @Test due to expect test failure.
    public void isNotSame1(Testing t) {
        t.examine(AlwaysEquals.INSTANCE1)
            .isNotSameObject(AlwaysEquals.INSTANCE2)
            .isNotSameObject(AlwaysEquals.INSTANCE1)
            .barrier()
            .isNotSameObject(AlwaysEquals.INSTANCE1);
    }

    // No @Test due to expect test failure.
    public void isNotSame2(Testing t) {
        t.examine(null)
            .isNotSameObject(AlwaysEquals.INSTANCE2);
    }

    // No @Test due to expect test failure.
    public void isNotSame3(Testing t) {
        t.examine(AlwaysEquals.INSTANCE1)
            .isNotSameObject(null);
    }

    // No @Test due to expect test failure.
    public void isNotSame4(Testing t) {
        t.examine(null)
            .isNotSameObject(null);
    }
}
