package enyo.testing.runtime.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.TestFailure;
import enyo.testing.runtime.Examined;
import enyo.testing.runtime.ExceptionPhase;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

        history = Helpers.testHistory(t, TestObjectExaminer.class, "context4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(5);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(
            List.of(new Examined("context1", "string1")));
        t.examine(failure.check).is("equals");
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string2" }));
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.context)
            .is(List.of(new Examined("context1", "string1"),
                    new Examined(null, "STRING1")));
        t.examine(failure.check).is("equals");
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string1" }));
        failure = (Recorder.Failure) history.get(2);
        t.examine(failure.context)
            .is(List.of(new Examined("context1", "string1"),
                    new Examined("context2", "STRING1")));
        t.examine(failure.check).is("equals");
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string2" }));
        failure = (Recorder.Failure) history.get(3);
        t.examine(failure.context)
            // Back to the original context.
            .is(List.of(new Examined("context1", "string1")));
        t.examine(failure.check).is("equals");
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { "string3" }));
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

    // No @Test due to expect test failure.
    public void context4(Testing t) {
        var e = t.examine("string1")
            .context("context1")
            .is("string2");
        e.map(String::toUpperCase)
            .is("string1")
            .context("context2")
            .is("string2");
        e.is("string3");
    }

    @Test
    public void testMap(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        history = Helpers.testHistory(t, TestObjectExaminer.class, "map1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { null }));

        history = Helpers.testHistory(t, TestObjectExaminer.class, "map2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        Recorder.Exception exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context)
            .is(List.of(new Examined("context1", "string1")));
        t.examine(exception.phase).is(ExceptionPhase.EXAMINER);
        t.examine(exception.exception)
            .isExactClass(UnsupportedOperationException.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Check that TestAlreadyFailed from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "map3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, true)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { false }));

        // Check that TestFailure from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "map4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context).is(List.of());
        t.examine(exception.phase).is(ExceptionPhase.EXECUTION);
        t.examine(exception.exception)
            .isExactClass(TestFailure.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Working barrier.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "map5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(
            List.of(new Examined("context1", "string1")));
        t.examine(failure.otherObjects.length).is(1);
        t.examine(failure.otherObjects[0]).is("string2");
        t.examine(history.get(1)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.check).is("mark: map5");
    }

    @Test
    public void map0(Testing t) {
        t.examine("string1")
            .map(List::of)
            .is(List.of("string1"));
    }

    public void map1(Testing t) {
        t.examine("string1")
            .map(null)
            .is(false);
    }

    public void map2(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(s -> { throw new UnsupportedOperationException("expected"); })
            .is(false);
    }

    public void map3(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return null; // Not reached.
                })
            .is(false);
    }

    public void map4(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(s -> {
                    t.assertFail("expected");
                    return null; // Not reached.
                })
            .is(false);
    }

    public void map5(Testing t) {
        t.examine("string1")
            .context("context1")
            .is("string2")
            .barrier()
            .map(s -> t.assertFail("unreachable"))
            .is("string3");
        t.failedCheck("mark: map5");
    }

    @Test
    public void testMapConsumer(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;
        Recorder.Exception exception;

        // Null consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.examine(failure.otherObjects.length).is(2);
        t.examine(failure.otherObjects[0]).isNull();
        t.assertTrue(failure.otherObjects[1] instanceof Consumer);

        // Null function.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.examine(failure.otherObjects.length).is(2);
        t.assertTrue(failure.otherObjects[0] instanceof Function);
        t.examine(failure.otherObjects[1]).isNull();

        // Null function and null consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[2]));

        // Exception in function.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context)
            .is(List.of(new Examined("context1", "string1")));
        t.examine(exception.phase).is(ExceptionPhase.EXAMINER);
        t.examine(exception.exception)
            .isExactClass(UnsupportedOperationException.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Exception in consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context)
            .is(List.of(new Examined("context1", "string1"),
                    new Examined(null, List.of("string1"))));
        t.examine(exception.phase).is(ExceptionPhase.EXAMINER);
        t.examine(exception.exception)
            .isExactClass(UnsupportedOperationException.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Ignored barrier exception in function.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer6");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, true)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { false }));

        // Ignored test failure exception in function.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer7");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context).is(List.of());
        t.examine(exception.phase).is(ExceptionPhase.EXECUTION);
        t.examine(exception.exception)
            .isExactClass(TestFailure.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Ignored barrier exception in consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer8");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, true)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { false }));

        // Ignored test failure exception in consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer9");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context).is(List.of());
        t.examine(exception.phase).is(ExceptionPhase.EXECUTION);
        t.examine(exception.exception)
            .isExactClass(TestFailure.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Working barrier.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapConsumer10");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(
            List.of(new Examined("context1", "string1")));
        t.examine(failure.otherObjects.length).is(1);
        t.examine(failure.otherObjects[0]).is("string2");
        t.examine(history.get(1)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.check).is("mark: mapConsumer10");
    }

    @Test
    public void mapConsumer0(Testing t) {
        t.examine("string1")
            .map(List::of, e -> e.is(List.of("string1")))
            .is("string1");
    }

    public void mapConsumer1(Testing t) {
        t.examine("string1")
            .map((Function<String, List<String>>) null,
                e -> e.is(List.of("string1")))
            .is("string1");
    }

    public void mapConsumer2(Testing t) {
        t.examine("string1")
            .map(List::of, null)
            .is("string1");
    }

    public void mapConsumer3(Testing t) {
        t.examine("string1")
            .map((Function<String, List<String>>) null, null)
            .is("string1");
    }

    public void mapConsumer4(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(s -> { throw new UnsupportedOperationException("expected"); },
                e -> t.assertFail("unreachable"))
            .is("string1");
    }

    public void mapConsumer5(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(List::of,
                e -> { throw new UnsupportedOperationException("expected"); })
            .is("string1");
    }

    public void mapConsumer6(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return null;
                },
                e -> t.assertFail("unreachable"))
            .is("string1");
    }

    public void mapConsumer7(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(s -> {
                    t.assertFail("expected");
                    return null;
                },
                e -> t.assertFail("unreachable"))
            .is("string1");
    }

    public void mapConsumer8(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(List::of,
                e -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                })
            .is("string1");
    }

    public void mapConsumer9(Testing t) {
        t.examine("string1")
            .context("context1")
            .map(List::of, e -> t.assertFail("expected"))
            .is("string1");
    }

    public void mapConsumer10(Testing t) {
        t.examine("string1")
            .context("context1")
            .is("string2")
            .barrier()
            .map(List::of, e -> t.assertFail("unreachable"))
            .is("string3");
        t.failedCheck("mark: mapConsumer10");
    }

    @Test
    public void testMapClass(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;
        Recorder.Exception exception;

        // Null class.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.examine(failure.otherObjects.length).is(2);
        t.examine(failure.otherObjects[0]).isNull();
        t.assertTrue(failure.otherObjects[1] instanceof Consumer);

        // Null consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { String.class, null }));

        // Null class and null consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("map (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[2]));

        // Exception in function.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context)
            .is(List.of(new Examined("context1", "string1")));
        t.examine(exception.phase).is(ExceptionPhase.EXAMINER);
        t.examine(exception.exception)
            .isExactClass(ClassCastException.class);
        t.examine(exception.exception.getMessage())
            .is("Cannot cast java.lang.String to java.lang.Integer");

        // Exception in consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context)
            .is(List.of(new Examined("context1", "string1"),
                    new Examined("context2", "string1")));
        t.examine(exception.phase).is(ExceptionPhase.EXAMINER);
        t.examine(exception.exception)
            .isExactClass(UnsupportedOperationException.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Ignored barrier exception in consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass8");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(List.of(new Examined(null, true)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { false }));

        // Ignored test failure exception in consumer.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass9");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        exception = (Recorder.Exception) history.get(0);
        t.examine(exception.context).is(List.of());
        t.examine(exception.phase).is(ExceptionPhase.EXECUTION);
        t.examine(exception.exception)
            .isExactClass(TestFailure.class);
        t.examine(exception.exception.getMessage()).is("expected");

        // Working barrier.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "mapClass10");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("equals");
        t.examine(failure.context).is(
            List.of(new Examined("context1", "string1")));
        t.examine(failure.otherObjects.length).is(1);
        t.examine(failure.otherObjects[0]).is("string2");
        t.examine(history.get(1)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.check).is("mark: mapClass10");
    }

    @Test
    public void mapClass0(Testing t) {
        t.<Object>examine("string1")
            .map(String.class, e -> e.map(String::length).is(7))
            .is("string1");
    }

    public void mapClass1(Testing t) {
        t.<Object>examine("string1")
            .map((Class<String>) null, e -> e.is("string1"))
            .is("string1");
    }

    public void mapClass2(Testing t) {
        t.<Object>examine("string1")
            .map(String.class, null)
            .is("string1");
    }

    public void mapClass3(Testing t) {
        t.<Object>examine("string1")
            .map((Class<String>) null, null)
            .is("string1");
    }

    public void mapClass4(Testing t) {
        t.<Object>examine("string1")
            .context("context1")
            .map(Integer.class, e -> t.assertFail("unreachable"))
            .is("string1");
    }

    public void mapClass5(Testing t) {
        t.<Object>examine("string1")
            .context("context1")
            .map(String.class,
                e -> {
                    e.context("context2");
                    throw new UnsupportedOperationException("expected");
                })
            .is("string1");
    }

    public void mapClass8(Testing t) {
        t.<Object>examine("string1")
            .context("context1")
            .map(String.class,
                e -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                })
            .is("string1");
    }

    public void mapClass9(Testing t) {
        t.<Object>examine("string1")
            .context("context1")
            .map(String.class, e -> t.assertFail("expected"))
            .is("string1");
    }

    public void mapClass10(Testing t) {
        t.<Object>examine("string1")
            .context("context1")
            .is("string2")
            .barrier()
            .map(String.class, e -> t.assertFail("unreachable"))
            .is("string3");
        t.failedCheck("mark: mapClass10");
    }

    @Test
    public void testInspect(Testing t) {
        List<Recorder.Call> history;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "inspect1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("equals"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("string2")));

        history = Helpers.testHistory(t, TestObjectExaminer.class, "inspect2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Exception.class, e -> e
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.exception, (e1 -> e1
                    .isExactClass(UnsupportedOperationException.class)
                    .map(Throwable::getMessage).is("expected"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: inspect2")));
    }

    @Test
    public void inspect0(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .map(String::toUpperCase)
            .inspect(s -> {
                    t.examine(s).is("STRING1");
                    counter.increment();
                })
            .is("STRING1");
        counter.checkTarget();
    }

    // No @Test due to expected test failure.
    public void inspect1(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .is("string2")
            .barrier()
            .inspect(s -> counter.increment())
            .is("string3");
        t.examine(counter.value()).is(0);
    }

    // No @Test due to expected test failure.
    public void inspect2(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        t.examine("string1")
            .is("string1")
            .inspect(s -> {
                    c1.increment();
                    throw new UnsupportedOperationException("expected");
                })
            .inspect(s -> c2.increment()); // Should still be called.
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: inspect2");
    }

    @Test
    public void testInspectClass(Testing t) {
        List<Recorder.Call> history;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "inspectClass1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("equals"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("string2")));

        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "inspectClass2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Exception.class, e -> e
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.exception, (e1 -> e1
                    .isExactClass(UnsupportedOperationException.class)
                    .map(Throwable::getMessage).is("expected"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: inspectClass2")));
    }

    @Test
    public void inspectClass0(Testing t) {
        var counter = t.counter();
        t.<Object>examine("string1")
            .inspect(String.class, s -> {
                    t.examine(s)
                        .map(String::toUpperCase)
                        .is("STRING1");
                    counter.increment();
                })
            .is("string1");
        counter.checkTarget();
    }

    // No @Test due to expected test failure.
    public void inspectClass1(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .is("string2")
            .barrier()
            .inspect(String.class, s -> counter.increment())
            .is("string3");
        t.examine(counter.value()).is(0);
    }

    // No @Test due to expected test failure.
    public void inspectClass2(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        t.examine("string1")
            .is("string1")
            .inspect(String.class, s -> {
                    c1.increment();
                    throw new UnsupportedOperationException("expected");
                })
            .inspect(String.class,
                s -> c2.increment()); // Should still be called.
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: inspectClass2");
    }

    @Test
    public void testTestPredicate(Testing t) {
        List<Recorder.Call> history;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicate1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("equals"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("string2")));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: testPredicate1")));

        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicate2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Exception.class, e -> e
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.exception, (e1 -> e1
                    .isExactClass(UnsupportedOperationException.class)
                    .map(Throwable::getMessage).is("expected"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: testPredicate2")));

        // Check that test failures are reported.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicate3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("test predicate"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("test predicate"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined("call 2", "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: testPredicate3")));

        // Null predicate.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicate4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "test predicate (not null)"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: testPredicate4")));
    }

    @Test
    public void testPredicate0(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .map(String::toUpperCase)
            .test(s -> {
                    counter.increment();
                    return s.equals("STRING1");
                })
            .is("STRING1");
        counter.checkTarget();
    }

    // No @Test due to expected test failure.
    public void testPredicate1(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .is("string2")
            .barrier()
            .test(s -> {
                    counter.increment();
                    return true;
                })
            .is("string3");
        t.examine(counter.value()).is(0);
        t.failedCheck("mark: testPredicate1");
    }

    // No @Test due to expected test failure.
    public void testPredicate2(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .is("string1")
            .test(s -> {
                    c1.increment();
                    t.examine(s).isSame(string1);
                    throw new UnsupportedOperationException("expected");
                })
            .test(s -> {
                    c2.increment(); // Should still be called.
                    return s == string1;
                });
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: testPredicate2");
    }

    // No @Test due to expected test failure.
    public void testPredicate3(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .test(s -> {
                    c1.increment();
                    t.examine(s).isSame(string1);
                    return false;
                })
            .context("call 2")
            .test(s -> {
                    c2.increment();
                    t.examine(s).isSame(string1);
                    return false;
                });
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: testPredicate3");
    }

    // No @Test due to expected test failure.
    public void testPredicate4(Testing t) {
        t.examine("string1")
            .test((Predicate<Object>) null);
        t.failedCheck("mark: testPredicate4");
    }

    @Test
    public void testTestNotPredicate(Testing t) {
        List<Recorder.Call> history;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testNotPredicate1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("equals"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("string2")));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testNotPredicate1")));

        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testNotPredicate2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Exception.class, e -> e
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.exception, (e1 -> e1
                    .isExactClass(UnsupportedOperationException.class)
                    .map(Throwable::getMessage).is("expected"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testNotPredicate2")));

        // Check that test failures are reported.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testNotPredicate3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("testNot predicate"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("testNot predicate"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined("call 2", "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testNotPredicate3")));

        // Null predicate.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testNotPredicate4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "testNot predicate (not null)"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testNotPredicate4")));
    }

    @Test
    public void testNotPredicate0(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .map(String::toUpperCase)
            .testNot(s -> {
                    counter.increment();
                    return !s.equals("STRING1");
                })
            .is("STRING1");
        counter.checkTarget();
    }

    // No @Test due to expected test failure.
    public void testNotPredicate1(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .is("string2")
            .barrier()
            .testNot(s -> {
                    counter.increment();
                    return false;
                })
            .is("string3");
        t.examine(counter.value()).is(0);
        t.failedCheck("mark: testNotPredicate1");
    }

    // No @Test due to expected test failure.
    public void testNotPredicate2(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .is("string1")
            .testNot(s -> {
                    c1.increment();
                    t.examine(s).isSame(string1);
                    throw new UnsupportedOperationException("expected");
                })
            .testNot(s -> {
                    c2.increment(); // Should still be called.
                    return s != string1;
                });
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: testNotPredicate2");
    }

    // No @Test due to expected test failure.
    public void testNotPredicate3(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .testNot(s -> {
                    c1.increment();
                    t.examine(s).isSame(string1);
                    return true;
                })
            .context("call 2")
            .testNot(s -> {
                    c2.increment();
                    t.examine(s).isSame(string1);
                    return true;
                });
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: testNotPredicate3");
    }

    // No @Test due to expected test failure.
    public void testNotPredicate4(Testing t) {
        t.examine("string1")
            .testNot((Predicate<Object>) null);
        t.failedCheck("mark: testNotPredicate4");
    }

    @Test
    public void testTestPredicateDescription(Testing t) {
        List<Recorder.Call> history;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicateDescription1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("equals"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("string2")));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testPredicateDescription1")));

        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicateDescription2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Exception.class, e -> e
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.exception, (e1 -> e1
                    .isExactClass(UnsupportedOperationException.class)
                    .map(Throwable::getMessage).is("expected"))));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testPredicateDescription2")));

        // Check that test failures are reported.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicateDescription3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("my check 1"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("my check 2"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined("call 2", "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testPredicateDescription3")));

        // Null check message.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicateDescription4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "test predicate description (not null)"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testPredicateDescription4")));

        // Null predicate.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicateDescription5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "test predicate (not null)"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("my check")));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testPredicateDescription5")));

        // Null description and predicate.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "testPredicateDescription6");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);
        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "test predicate description (not null)"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "test predicate (not null)"))
                .map(call -> call.context, e1 -> e1.is(
                        List.of(new Examined(null, "string1"))))
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is(
                        "mark: testPredicateDescription6")));
    }

    @Test
    public void testPredicateDescription0(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .map(String::toUpperCase)
            .test("my check", s -> {
                    counter.increment();
                    return s.equals("STRING1");
                })
            .is("STRING1");
        counter.checkTarget();
    }

    // No @Test due to expected test failure.
    public void testPredicateDescription1(Testing t) {
        var counter = t.counter();
        t.examine("string1")
            .is("string2")
            .barrier()
            .test("my check", s -> {
                    counter.increment();
                    return true;
                })
            .is("string3");
        t.examine(counter.value()).is(0);
        t.failedCheck("mark: testPredicateDescription1");
    }

    // No @Test due to expected test failure.
    public void testPredicateDescription2(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .is("string1")
            .test("my check 1", s -> {
                    t.examine(s).isSame(string1);
                    c1.increment();
                    throw new UnsupportedOperationException("expected");
                })
            .test("my check 2", s -> {
                    c2.increment(); // Should still be called.
                    return s == string1;
                });
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: testPredicateDescription2");
    }

    // No @Test due to expected test failure.
    public void testPredicateDescription3(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .test("my check 1", s -> {
                    c1.increment();
                    t.examine(s).isSame(string1);
                    return false;
                })
            .context("call 2")
            .test("my check 2", s -> {
                    c2.increment();
                    t.examine(s).isSame(string1);
                    return false;
                });
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: testPredicateDescription3");
    }

    // No @Test due to expected test failure.
    public void testPredicateDescription4(Testing t) {
        var counter = t.counter();
        String string1 = "string1";
        t.examine(string1)
            .test(null, s -> {
                    counter.increment();
                    return s == string1;
                });
        counter.checkTarget();
        t.failedCheck("mark: testPredicateDescription4");
    }

    // No @Test due to expected test failure.
    public void testPredicateDescription5(Testing t) {
        String string1 = "string1";
        t.examine(string1)
            .test("my check", null);
        t.failedCheck("mark: testPredicateDescription5");
    }

    // No @Test due to expected test failure.
    public void testPredicateDescription6(Testing t) {
        t.examine("string1")
            .test(null, (Predicate<Object>) null);
        t.failedCheck("mark: testPredicateDescription6");
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

    @Test
    public void testIsNotSame(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isNotSame1");
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
            t, TestObjectExaminer.class, "isNotSame2");
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
            t, TestObjectExaminer.class, "isNotSame3");
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
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isNotSame4");
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
            .isNotSame(AlwaysEquals.INSTANCE2)
            .isNotSame(AlwaysEquals.INSTANCE1)
            .barrier()
            .isNotSame(AlwaysEquals.INSTANCE1);
    }

    // No @Test due to expect test failure.
    public void isNotSame2(Testing t) {
        t.examine(null)
            .isNotSame(AlwaysEquals.INSTANCE2);
    }

    // No @Test due to expect test failure.
    public void isNotSame3(Testing t) {
        t.examine(AlwaysEquals.INSTANCE1)
            .isNotSame(null);
    }

    // No @Test due to expect test failure.
    public void isNotSame4(Testing t) {
        t.examine(null)
            .isNotSame(null);
    }

    @Test
    public void testIsNull(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isNull1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("null");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.examine(failure.otherObjects.length).is(0);

        // Test reporting of examined null objects.
        history = Helpers.testHistory(t, TestObjectExaminer.class, "isNull2");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);
    }

    // No @Test due to expect test failure.
    public void isNull1(Testing t) {
        t.examine("string1")
            .isNull()
            .barrier()
            .isNull();
    }

    @Test
    public void isNull2(Testing t) {
        t.examine(null).isNull();
    }

    @Test
    public void testIsNotNull(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isNotNull1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("not null");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.examine(failure.otherObjects.length).is(0);

        // Test reporting of examined null objects.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isNotNull2");
        t.examine(Helpers.isSuccess(t, history)).is(true);
        t.examine(history.size()).is(1);
    }

    // No @Test due to expect test failure.
    public void isNotNull1(Testing t) {
        t.examine(null)
            .isNotNull()
            .barrier()
            .isNotNull();
    }

    @Test
    public void isNotNull2(Testing t) {
        t.examine("string1").isNotNull();
    }

    @Test
    public void testIsExactClass(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isExactClass1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("exact class");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { Integer.class }));

        // Test reporting of examined null objects.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isExactClass2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("exact class (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { Integer.class }));

        // Test reporting of comparison against null.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isExactClass3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("exact class (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, "string1")));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test reporting of double-null comparison.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isExactClass4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("exact class (not null)");
        t.examine(failure.context).is(List.of(new Examined(null, null)));
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { null }));

        // Test comparison against superclass.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isExactClass5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("exact class");
        t.examine(failure.context.size()).is(1);
        t.<Class<?>>examine(failure.context.get(0).value().getClass())
            .isSame(RuntimeException.class);
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { Exception.class }));

        // Test comparison against subclass.
        history = Helpers.testHistory(
            t, TestObjectExaminer.class, "isExactClass6");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("exact class");
        t.examine(failure.context.size()).is(1);
        t.<Class<?>>examine(failure.context.get(0).value().getClass())
            .isSame(Exception.class);
        t.assertTrue(Arrays.equals(
                failure.otherObjects, new Object[] { RuntimeException.class }));
    }

    // No @Test due to expect test failure.
    public void isExactClass1(Testing t) {
        t.<Object>examine("string1")
            .isExactClass(String.class)
            .isExactClass(Integer.class)
            .barrier()
            .isExactClass(Long.class);
    }

    // No @Test due to expect test failure.
    public void isExactClass2(Testing t) {
        t.examine(null)
            .isExactClass(Integer.class);
    }

    // No @Test due to expect test failure.
    public void isExactClass3(Testing t) {
        t.examine("string1")
            .isExactClass(null);
    }

    // No @Test due to expect test failure.
    public void isExactClass4(Testing t) {
        t.examine(null)
            .isExactClass(null);
    }

    public void isExactClass5(Testing t) {
        t.<Object>examine(new RuntimeException())
            .isExactClass(Exception.class);
    }

    public void isExactClass6(Testing t) {
        t.examine(new Exception())
            .isExactClass(RuntimeException.class);
    }
}
