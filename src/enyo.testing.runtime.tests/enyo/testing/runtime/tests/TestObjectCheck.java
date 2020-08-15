package enyo.testing.runtime.tests;

import enyo.testing.Check;
import enyo.testing.Examiner;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.runtime.Examined;

import java.util.List;

public final class TestObjectCheck {
    @Test
    public void testCheckSuccess(Testing t) {
        var counter = t.counter();
        var object = new Object() { };
        t.examine(object)
            .check(obj -> {
                    counter.increment();
                    t.examine(obj).isSame(object);
                    return null;
                });
        counter.checkTarget();
    }

    @Test
    public void testCheckFailure(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkFailure");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(5);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("check 1"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNotNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        Object object = ((Recorder.Failure) history.get(0)).context.get(0)
            .value();
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("check 2"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.isSame(object))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("object 1")));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("check 3"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.isSame(object))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], Examiner::isNull));
        t.examine(history.get(3))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: checkFailure")));
    }

    // No @Test due to expected test failure.
    public void checkFailure(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        var c3 = t.counter();
        var object = new Object() { };
        t.examine(object)
            .check(obj -> {
                    c1.increment();
                    t.examine(obj).isSame(object);
                    return new Check.Failure("check 1");
                })
            .check(obj -> {
                    c2.increment();
                    t.examine(obj).isSame(object);
                    return new Check.Failure("check 2", "object 1");
                })
            .check(obj -> {
                    c3.increment();
                    t.examine(obj).isSame(object);
                    return new Check.Failure("check 3", (Object) null);
                })
            .isSame(object);
        c1.checkTarget();
        c2.checkTarget();
        c3.checkTarget();
        t.failedCheck("mark: checkFailure");
    }

    @Test
    public void testCheckBarrier(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkBarrier");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("check 1"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNotNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: checkBarrier")));
    }

    // No @Test due to expected test failure.
    public void checkBarrier(Testing t) {
        var c1 = t.counter();
        var object = new Object() { };
        t.examine(object)
            .check(obj -> {
                    c1.increment();
                    t.examine(obj).isSame(object);
                    return new Check.Failure("check 1");
                })
            .barrier()
            .check(obj -> t.assertFail("unreachable"));
        t.failedCheck("mark: checkBarrier");
    }

    @Test
    public void testCheckNull(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkNull");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(3);

        t.examine(history.get(0))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("check (not null)"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNotNull)
                .map(call -> call.otherObjects.length, e1 -> e1.is(0)));
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: checkNull")));
    }

    // No @Test due to expected test failure.
    public void checkNull(Testing t) {
        var c1 = t.counter();
        var object = new Object() { };
        t.examine(object)
            .check((Check<Object>) null);
        t.failedCheck("mark: checkNull");
    }

    @Test
    public void testCheckThrow(Testing t) {
        List<Recorder.Call> history = Helpers.testHistory(
            t, getClass(), "checkThrow");
        t.examine(Helpers.isSuccess(t, history)).is(false);
        t.examine(history.size()).is(4);

        t.examine(history.get(0))
            .map(Recorder.Exception.class, e -> e
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(), Examiner::isNotNull)
                .map(call -> call.exception, e1 -> e1.isExactClass(
                        UnsupportedOperationException.class))
                .map(call -> call.exception.getMessage(), e1 -> e1.is(
                        "expected")));
        Object object = ((Recorder.Exception) history.get(0)).context.get(0)
            .value();
        t.examine(history.get(1))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("check 3"))
                .map(call -> call.context.get(0).context(), Examiner::isNull)
                .map(call -> call.context.get(0).value(),
                    e1 -> e1.isSame(object))
                .map(call -> call.otherObjects.length, e1 -> e1.is(1))
                .map(call -> call.otherObjects[0], e1 -> e1.is("object 1")));
        t.examine(history.get(2))
            .map(Recorder.Failure.class, e -> e
                .map(call -> call.check, e1 -> e1.is("mark: checkThrow")));
    }

    // No @Test due to expected test failure.
    public void checkThrow(Testing t) {
        var c1 = t.counter();
        var c2 = t.counter();
        var c3 = t.counter();
        var object = new Object() { };
        t.examine(object)
            .check(obj -> {
                    c1.increment();
                    t.examine(obj).isSame(object);
                    throw new UnsupportedOperationException("expected");
                })
            .check(obj -> {
                    c2.increment();
                    t.examine(obj).isSame(object);
                    return null;
                })
            .check(obj -> {
                    c3.increment();
                    t.examine(obj).isSame(object);
                    return new Check.Failure("check 3", "object 1");
                })
            .isSame(object);
        c1.checkTarget();
        c2.checkTarget();
        c3.checkTarget();
        t.failedCheck("mark: checkThrow");
    }
}
