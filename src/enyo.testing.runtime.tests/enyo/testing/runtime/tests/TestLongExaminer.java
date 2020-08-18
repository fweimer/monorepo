package enyo.testing.runtime.tests;

import enyo.testing.Check;
import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;
import enyo.testing.runtime.ExceptionPhase;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

public final class TestLongExaminer {
    @Test
    public void testContext(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        history = Helpers.testHistory(t, TestLongExaminer.class, "context1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", 0L)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 1L }));

        history = Helpers.testHistory(t, TestLongExaminer.class, "context3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.context).is(List.of(new Examined("context1", 0L)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 2L }));
        failure = (Recorder.Failure) history.get(1);
        t.examine(failure.context).is(List.of(new Examined("context1", 0L)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 3L }));
    }

    // No @Test due to expect test failure.
    public void context1(Testing t) {
        t.examine(0L)
            .context("context1")
            .is(1);
    }

    // No @Test due to expect test failure.
    public void context3(Testing t) {
        var e = t.examine(0L)
            .context("context1")
            .is(2);
        t.expect(() -> e.context(null), NullPointerException.class);
        e.is(3);
    }

    @Test
    public void testMap(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, getClass(), "map1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("map (not null)"),
            ctx -> ctx.is(List.of(new Examined(null, 17L))),
            obj -> obj.check(Checks.arrayEquals(new Object[] { null })));

        history = Helpers.testHistory(t, getClass(), "map2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            ctx -> ctx.is(List.of(new Examined("context1", 17L))),
            phs -> phs.is(ExceptionPhase.EXAMINER),
            exc -> exc.isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Check that TestAlreadyFailed from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, getClass(), "map3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("equals"),
            ctx -> ctx.is(List.of(new Examined(null, true))),
            obj -> obj.check(Checks.arrayEquals(new Object[] { false })));

        // Check that TestFailure from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, getClass(), "map4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            ctx -> ctx.is(List.of()),
            phs -> phs.is(ExceptionPhase.EXECUTION),
            exc -> exc.isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Working barrier.
        history = Helpers.testHistory(t, getClass(), "map5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("=="),
            ctx -> ctx.is(List.of(new Examined("context1", 17L))),
            obj -> obj.check(Checks.arrayEquals(new Object[] { 18L })));
        Recorder.examineFailure(t.examine(history.get(1)),
            chk -> chk.is("mark: map5"),
            ctx -> ctx.is(List.of()),
            obj -> obj.check(Checks.arrayEquals()));
    }

    @Test
    public void map0(Testing t) {
        t.examine(17L)
            .map(i -> i + 1)
            .is(18);
    }

    public void map1(Testing t) {
        t.examine(17L)
            .map(null)
            .is(18);
    }

    public void map2(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(i -> { throw new UnsupportedOperationException("expected"); })
            .is(18);
    }

    public void map3(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return 0; // Not reached.
                })
            .is(18);
    }

    public void map4(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(s -> {
                    t.assertFail("expected");
                    return 0; // Not reached.
                })
            .is(18);
    }

    public void map5(Testing t) {
        t.examine(17L)
            .context("context1")
            .is(18)
            .barrier()
            .map(s -> t.assertFail("unreachable"))
            .is(19);
        t.failedCheck("mark: map5");
    }

    @Test
    public void testMapConsumer(Testing t) {
        List<Recorder.Call> history;

        // Null function.
        history = Helpers.testHistory(t, getClass(), "mapConsumer1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("map (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects -> objects
            .test(array -> array.length == 2)
            .test(array -> array[0] == null)
            .test(array -> array[1] instanceof Consumer));

        // Null consumer.
        history = Helpers.testHistory(t, getClass(), "mapConsumer2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("map (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects -> objects
            .test(array -> array.length == 2)
            .test(array -> array[0] instanceof LongUnaryOperator)
            .test(array -> array[1] == null));

        // Null function and null consumer.
        history = Helpers.testHistory(t, getClass(), "mapConsumer3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("map (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects ->  objects.check(Checks.arrayEquals(null, null)));

        // Exception in function.
        history = Helpers.testHistory(
            t, getClass(), "mapConsumer4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.is(List.of(new Examined("context1", 17L))),
            phase -> phase.is(ExceptionPhase.EXAMINER),
            exception -> exception
            .isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined("context1", 17L))),
            objects -> objects.check(Checks.arrayEquals(18L)));

        // Exception in consumer.
        history = Helpers.testHistory(t, getClass(), "mapConsumer5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.is(
                List.of(
                    new Examined("context1", 17L),
                    new Examined(null, 18L))),
            phase -> phase.is(ExceptionPhase.EXAMINER),
            exception -> exception
            .isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined("context1", 17L))),
            objects -> objects.check(Checks.arrayEquals(19L)));

        // Ignored barrier exception in function.
        history = Helpers.testHistory(t, getClass(), "mapConsumer6");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("equals"),
            context -> context.is(List.of(new Examined(null, true))),
            objects -> objects.check(Checks.arrayEquals(false)));

        // Ignored test failure exception in function.
        history = Helpers.testHistory(
            t, getClass(), "mapConsumer7");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.check(Checks.empty()),
            phase -> phase.is(ExceptionPhase.EXECUTION),
            exception -> exception
            .isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Ignored barrier exception in consumer.
        history = Helpers.testHistory(
            t, getClass(), "mapConsumer8");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("equals"),
            context -> context.is(List.of(new Examined(null, true))),
            objects -> objects.check(Checks.arrayEquals(false)));

        // Ignored test failure exception in consumer.
        history = Helpers.testHistory(
            t, getClass(), "mapConsumer9");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.check(Checks.empty()),
            phase -> phase.is(ExceptionPhase.EXECUTION),
            exception -> exception
            .isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Working barrier.
        history = Helpers.testHistory(
            t, getClass(), "mapConsumer10");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined("context1", 17L))),
            objects -> objects.check(Checks.arrayEquals(18L)));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("mark: mapConsumer10"),
            context -> context.check(Checks.empty()),
            objects -> objects.check(Checks.arrayEquals()));
    }

    @Test
    public void mapConsumer0(Testing t) {
        t.examine(17L)
            .map(i -> i + 1, e -> e.is(18))
            .is(17L);
    }

    public void mapConsumer1(Testing t) {
        t.examine(17L)
            .map(null, e -> e.is(18))
            .is(17L);
    }

    public void mapConsumer2(Testing t) {
        t.examine(17L)
            .map(i -> i + 1, null)
            .is(17L);
    }

    public void mapConsumer3(Testing t) {
        t.examine(17L)
            .map(null, null)
            .is(17L);
    }

    public void mapConsumer4(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(s -> { throw new UnsupportedOperationException("expected"); },
                e -> t.assertFail("unreachable"))
            .is(18);
    }

    public void mapConsumer5(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(i -> i + 1, e -> {
                    e.is(18);
                    throw new UnsupportedOperationException("expected");
                })
            .is(19);
    }

    public void mapConsumer6(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return 0;
                },
                e -> t.assertFail("unreachable"))
            .is(18);
    }

    public void mapConsumer7(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(s -> {
                    t.assertFail("expected");
                    return 0;
                },
                e -> t.assertFail("unreachable"))
            .is(18);
    }

    public void mapConsumer8(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(i -> i + 1,
                e -> {
                    e.is(18);
                    t.examine(true).is(false);
                    t.fullBarrier();
                })
            .is(19);
    }

    public void mapConsumer9(Testing t) {
        t.examine(17L)
            .context("context1")
            .map(i -> i + 1, e -> t.assertFail("expected"))
            .is(18);
    }

    public void mapConsumer10(Testing t) {
        t.examine(17L)
            .context("context1")
            .is(18)
            .barrier()
            .map(i -> i + 1, e -> t.assertFail("unreachable"))
            .is(19);
        t.failedCheck("mark: mapConsumer10");
    }

    @Test
    public void testMapToObj(Testing t) {
        List<Recorder.Call> history;

        history = Helpers.testHistory(t, getClass(), "mapToObj1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("mapToObj (not null)"),
            ctx -> ctx.is(List.of(new Examined(null, 17L))),
            obj -> obj.check(Checks.arrayEquals(new Object[] { null })));

        history = Helpers.testHistory(t, getClass(), "mapToObj2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            ctx -> ctx.is(List.of(new Examined("context1", 17L))),
            phs -> phs.is(ExceptionPhase.EXAMINER),
            exc -> exc.isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Check that TestAlreadyFailed from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, getClass(), "mapToObj3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("equals"),
            ctx -> ctx.is(List.of(new Examined(null, true))),
            obj -> obj.check(Checks.arrayEquals(new Object[] { false })));

        // Check that TestFailure from the mapping function is
        // not recorded separately.
        history = Helpers.testHistory(t, getClass(), "mapToObj4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            ctx -> ctx.is(List.of()),
            phs -> phs.is(ExceptionPhase.EXECUTION),
            exc -> exc.isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Working barrier.
        history = Helpers.testHistory(t, getClass(), "mapToObj5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("=="),
            ctx -> ctx.is(List.of(new Examined("context1", 17L))),
            obj -> obj.check(Checks.arrayEquals(new Object[] {18L })));
        Recorder.examineFailure(t.examine(history.get(1)),
            chk -> chk.is("mark: mapToObj5"),
            ctx -> ctx.is(List.of()),
            obj -> obj.check(Checks.arrayEquals()));
    }

    @Test
    public void mapToObj0(Testing t) {
        t.examine(17L)
            .mapToObj(Long::toString)
            .is("17");
    }

    public void mapToObj1(Testing t) {
        t.examine(17L)
            .mapToObj((LongFunction<String>) null)
            .is("17");
    }

    public void mapToObj2(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(
                i -> { throw new UnsupportedOperationException("expected"); })
            .is(17L);
    }

    public void mapToObj3(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return "null"; // Not reached.
                })
            .is("18"); // Protected by barrier.
    }

    public void mapToObj4(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(s -> {
                    t.assertFail("expected");
                    return "null"; // Not reached.
                })
            .is("18"); // Protected by barrier.
    }

    public void mapToObj5(Testing t) {
        t.examine(17L)
            .context("context1")
            .is(18)
            .barrier()
            .<String>mapToObj(s -> t.assertFail("unreachable"))
            .is("19");
        t.failedCheck("mark: mapToObj5");
    }

    @Test
    public void testMapToObjConsumer(Testing t) {
        List<Recorder.Call> history;

        // Null function.
        history = Helpers.testHistory(t, getClass(), "mapToObjConsumer1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("mapToObj (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects -> objects
            .test(array -> array.length == 2)
            .test(array -> array[0] == null)
            .test(array -> array[1] instanceof Consumer));

        // Null consumer.
        history = Helpers.testHistory(t, getClass(), "mapToObjConsumer2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("mapToObj (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects -> objects
            .test(array -> array.length == 2)
            .test(array -> array[0] instanceof LongFunction)
            .test(array -> array[1] == null));

        // Null function and null consumer.
        history = Helpers.testHistory(t, getClass(), "mapToObjConsumer3");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("mapToObj (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects ->  objects.check(Checks.arrayEquals(null, null)));

        // Exception in function.
        history = Helpers.testHistory(
            t, getClass(), "mapToObjConsumer4");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.is(List.of(new Examined("context1", 17L))),
            phase -> phase.is(ExceptionPhase.EXAMINER),
            exception -> exception
            .isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined("context1", 17L))),
            objects -> objects.check(Checks.arrayEquals(18L)));

        // Exception in consumer.
        history = Helpers.testHistory(t, getClass(), "mapToObjConsumer5");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.is(
                List.of(
                    new Examined("context1", 17L),
                    new Examined(null, "17"))),
            phase -> phase.is(ExceptionPhase.EXAMINER),
            exception -> exception
            .isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined("context1", 17L))),
            objects -> objects.check(Checks.arrayEquals(19L)));

        // Ignored barrier exception in function.
        history = Helpers.testHistory(t, getClass(), "mapToObjConsumer6");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("equals"),
            context -> context.is(List.of(new Examined(null, true))),
            objects -> objects.check(Checks.arrayEquals(false)));

        // Ignored test failure exception in function.
        history = Helpers.testHistory(
            t, getClass(), "mapToObjConsumer7");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.check(Checks.empty()),
            phase -> phase.is(ExceptionPhase.EXECUTION),
            exception -> exception
            .isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Ignored barrier exception in consumer.
        history = Helpers.testHistory(
            t, getClass(), "mapToObjConsumer8");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("equals"),
            context -> context.is(List.of(new Examined(null, true))),
            objects -> objects.check(Checks.arrayEquals(false)));

        // Ignored test failure exception in consumer.
        history = Helpers.testHistory(
            t, getClass(), "mapToObjConsumer9");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.check(Checks.empty()),
            phase -> phase.is(ExceptionPhase.EXECUTION),
            exception -> exception
            .isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));

        // Working barrier.
        history = Helpers.testHistory(
            t, getClass(), "mapToObjConsumer10");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined("context1", 17L))),
            objects -> objects.check(Checks.arrayEquals(18L)));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("mark: mapToObjConsumer10"),
            context -> context.check(Checks.empty()),
            objects -> objects.check(Checks.arrayEquals()));
    }

    @Test
    public void mapToObjConsumer0(Testing t) {
        t.examine(17L)
            .mapToObj(Long::toString, e -> e.is("17"))
            .is(17L);
    }

    public void mapToObjConsumer1(Testing t) {
        t.examine(17L)
            .mapToObj(null, e -> e.is(18))
            .is(17L);
    }

    public void mapToObjConsumer2(Testing t) {
        t.examine(17L)
            .mapToObj(Long::toString, null)
            .is(17L);
    }

    public void mapToObjConsumer3(Testing t) {
        t.examine(17L)
            .mapToObj(null, null)
            .is(17L);
    }

    public void mapToObjConsumer4(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(
                s -> { throw new UnsupportedOperationException("expected"); },
                e -> t.assertFail("unreachable"))
            .is(18);
    }

    public void mapToObjConsumer5(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(Long::toString, e -> {
                    e.is("17");
                    throw new UnsupportedOperationException("expected");
                })
            .is(19);
    }

    public void mapToObjConsumer6(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(s -> {
                    t.examine(true).is(false);
                    t.fullBarrier();
                    return null;
                },
                e -> t.assertFail("unreachable"))
            .is(18);
    }

    public void mapToObjConsumer7(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(s -> {
                    t.assertFail("expected");
                    return null;
                },
                e -> t.assertFail("unreachable"))
            .is(18);
    }

    public void mapToObjConsumer8(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(Long::toString,
                e -> {
                    e.is("17");
                    t.examine(true).is(false);
                    t.fullBarrier();
                })
            .is(19);
    }

    public void mapToObjConsumer9(Testing t) {
        t.examine(17L)
            .context("context1")
            .mapToObj(Long::toString, e -> t.assertFail("expected"))
            .is(18);
    }

    public void mapToObjConsumer10(Testing t) {
        t.examine(17L)
            .context("context1")
            .is(18L)
            .barrier()
            .mapToObj(Long::toString, e -> t.assertFail("unreachable"))
            .is(19L);
        t.failedCheck("mark: mapToObjConsumer10");
    }

    @Test
    public void testInspect(Testing t) {
        List<Recorder.Call> history;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, getClass(), "inspect1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("=="),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects -> objects.check(Checks.arrayEquals(18L)));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("mark: inspect1"),
            context -> context.check(Checks.empty()),
            objects -> objects.check(Checks.arrayEquals()));

        history = Helpers.testHistory(t, getClass(), "inspect2");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineException(t.examine(history.get(0)),
            context -> context.is(List.of(new Examined(null, 17L))),
            phase -> phase.is(ExceptionPhase.EXAMINER),
            exception -> exception
            .isExactClass(UnsupportedOperationException.class)
            .map(Throwable::getMessage)
            .is("expected"));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("mark: inspect2"),
            context -> context.check(Checks.empty()),
            objects -> objects.check(Checks.arrayEquals()));

        history = Helpers.testHistory(t, getClass(), "inspect3");
        Recorder.examineFailure(t.examine(history.get(0)),
            check -> check.is("inspect (not null)"),
            context -> context.is(List.of(new Examined(null, 17L))),
            objects -> objects.check(Checks.arrayEquals((Object) null)));
        Recorder.examineFailure(t.examine(history.get(1)),
            check -> check.is("mark: inspect3"),
            context -> context.check(Checks.empty()),
            objects -> objects.check(Checks.arrayEquals()));
    }

    @Test
    public void inspect0(Testing t) {
        var counter = t.counter();
        t.examine(17L)
            .map(i -> i + 1)
            .inspect(s -> {
                    t.examine(s).is(18);
                    counter.increment();
                })
            .is(18);
        counter.checkTarget();
    }

    public void inspect1(Testing t) {
        var counter = t.counter();
        t.examine(17L)
            .is(18)
            .barrier()
            .inspect(s -> counter.increment())
            .is(19);
        t.examine(counter.value()).is(0);
        t.failedCheck("mark: inspect1");
    }

    public void inspect2(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        t.examine(17L)
            .is(17L)
            .inspect(s -> {
                    c1.increment();
                    throw new UnsupportedOperationException("expected");
                })
            .inspect(s -> c2.increment()); // Should still be called.
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: inspect2");
    }

    public void inspect3(Testing t) {
        var c1 = t.counter();
        t.examine(17L)
            .is(17L)
            .inspect(null)
            .inspect(s -> c1.increment()); // Should still be called.
        c1.checkTarget();
        t.failedCheck("mark: inspect3");
    }

    @Test
    public void testCheckPass(Testing t) {
        var c1 = t.counter();
        t.examine(1)
            .check(i -> {
                    t.examine(i).is(1);
                    c1.increment();
                    return null;
                });
        c1.checkTarget();
    }

    @Test
    public void testCheckFail(Testing t) {
        // Checks failure reporting and barrier effectiveness.
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkFail");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("my check"),
            ctx -> ctx.is(List.of(new Examined(null, 1))),
            obj -> obj.check(Checks.arrayEquals(1, 2, 3)));
        Recorder.examineFailure(t.examine(history.get(1)),
            chk -> chk.is("mark: checkFail"),
            ctx -> ctx.is(List.of()),
            obj -> obj.check(Checks.arrayEquals()));
    }

    public void checkFail(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        t.examine(1)
            .check(i -> {
                    t.examine(i).is(1);
                    c1.increment();
                    return new Check.Failure("my check", 1, 2, 3);
                })
            .check(i -> {
                    t.examine(i).is(1);
                    c2.increment();
                    return null;
                })
            .barrier()
            .check(i -> t.assertFail("unreachable"));
        c1.checkTarget();
        c2.checkTarget();
        t.failedCheck("mark: checkFail");
    }

    @Test
    public void testCheckNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("check (not null)"),
            ctx -> ctx.is(List.of(new Examined(null, 17L))),
            obj -> obj.check(Checks.arrayEquals()));
        Recorder.examineFailure(t.examine(history.get(1)),
            chk -> chk.is("mark: checkNull"),
            ctx -> ctx.is(List.of()),
            obj -> obj.check(Checks.arrayEquals()));
    }

    public void checkNull(Testing t) {
        t.examine(17L)
            .check(null);
        t.failedCheck("mark: checkNull");
    }

    @Test
    public void testCheckAssertFail(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkAssertFail");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineException(t.examine(history.get(0)),
            ctx -> ctx.is(List.of()),
            phs -> phs.is(ExceptionPhase.EXECUTION),
            exc -> exc.isExactClass(TestFailure.class)
            .map(Throwable::getMessage)
            .is("expected"));
    }

    public void checkAssertFail(Testing t) {
        t.examine(17L)
            .context("context1")
            .check(i -> t.assertFail("expected"));
        t.failedCheck("not reached");
    }

    @Test
    public void testCheckFullBarrier(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkFullBarrier");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        Recorder.examineFailure(t.examine(history.get(0)),
            chk -> chk.is("=="),
            ctx -> ctx.is(List.of(new Examined("context1", 17L))),
            obj -> obj.check(Checks.arrayEquals(18L)));
    }

    public void checkFullBarrier(Testing t) {
        t.examine(17L)
            .context("context1")
            .is(18L)
            .check(i -> {
                    t.fullBarrier();
                    return null; // Not reached.
                });
        t.failedCheck("not reached");
    }

    @Test
    public void testIs(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestLongExaminer.class, "is1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("==");
        t.examine(failure.context).is(List.of(new Examined(null, 1L)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 2L }));
    }

    // No @Test due to expect test failure.
    public void is1(Testing t) {
        t.examine(1L)
            .is(1)
            .is(2)
            .barrier()
            .is(3);
    }

    @Test
    public void testIsnt(Testing t) {
        List<Recorder.Call> history;
        Recorder.Failure failure;

        // Test that the barrier is effective.
        history = Helpers.testHistory(t, TestLongExaminer.class, "isnt1");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        failure = (Recorder.Failure) history.get(0);
        t.examine(failure.check).is("!=");
        t.examine(failure.context).is(List.of(new Examined(null, 1L)));
        t.assertTrue(Arrays.equals(failure.otherObjects, new Object[] { 1L }));
    }

    // No @Test due to expect test failure.
    public void isnt1(Testing t) {
        t.examine(1L)
            .isnt(2)
            .isnt(1)
            .barrier()
            .isnt(1);
    }
}
