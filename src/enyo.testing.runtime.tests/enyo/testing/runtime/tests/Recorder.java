package enyo.testing.runtime.tests;

import enyo.testing.Examiner;
import enyo.testing.Testing;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.runtime.Examined;
import enyo.testing.runtime.ExceptionPhase;
import enyo.testing.runtime.Results;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class Recorder implements Results {
    static abstract class Call {
        final TestMethodLocator test;

        Call(TestMethodLocator test) {
            this.test = test;
        }

        void check(Testing t) {
            t.examine(test).isNotNull();
        }
    }

    static abstract class ContextCall extends Call {
        final List<Examined> context;

        ContextCall(TestMethodLocator test, String context) {
            super(test);
            this.context = List.of(new Examined(context, null));
        }

        ContextCall(TestMethodLocator test, List<Examined> context) {
            super(test);
            this.context = context;
        }
    }

    static final class Unsupported extends ContextCall {
        Unsupported(TestMethodLocator test, String context) {
            super(test, context);
        }
    }

    static final class Failure extends Call {
        final String check;
        final List<Examined> context;
        final Object[] otherObjects;
        final List<StackWalker.StackFrame> stackTrace;


        Failure(TestMethodLocator test, List<Examined> context, String check,
            Object[] otherObjects, List<StackWalker.StackFrame> stackTrace) {
            super(test);
            this.context = context;
            this.check = check;
            this.otherObjects = otherObjects;
            this.stackTrace = stackTrace;
        }

        void check(Testing t) {
            super.check(t);
            t.examine(context).isNotNull();
            t.examine(check).isNotNull();
            t.examine(otherObjects).isNotNull();
            t.examine(stackTrace).isNotNull();
        }
    }

    static Examiner<Call> examineFailure(
        Examiner<Call> e,
        Consumer<Examiner<String>> check,
        Consumer<Examiner<List<Examined>>> context,
        Consumer<Examiner<Object[]>> otherObjects) {
        return e.map(Failure.class, e1 -> e1
            .map(call -> call.check, check)
            .map(call -> call.context, context)
            .map(call -> call.otherObjects, otherObjects));
    }

    static final class Exception extends ContextCall {
        final ExceptionPhase phase;
        final Throwable exception;
        final Class<? extends Throwable> expected;


        Exception(TestMethodLocator test, List<Examined> context,
            ExceptionPhase phase, Throwable exception,
            Class<? extends Throwable> expected) {
            super(test, context);
            this.phase = phase;
            this.exception = exception;
            this.expected = expected;
        }

        @Override
        public String toString() {
            if (exception == null)
                return "missing exception: " + expected.getName();
            else
                return exception.getClass().getName()
                       + ": " + exception.getMessage();
        }

        void check(Testing t) {
            super.check(t);
            t.examine(phase).isNotNull();
            t.examine(exception).isNotNull();
        }
    }

    static Examiner<Call> examineException(
        Examiner<Call> e,
        Consumer<Examiner<List<Examined>>> context,
        Consumer<Examiner<ExceptionPhase>> phase,
        Consumer<Examiner<Throwable>> exception) {
        return e.map(Exception.class, e1 -> e1
            .map(call -> call.context, context)
            .map(call -> call.phase, phase)
            .map(call -> call.exception, exception)
            .map(call -> call.expected, e2 -> e2.isNull()));
    }

    static final class Completed extends Call {
        final boolean success;

        Completed(TestMethodLocator test, boolean success) {
            super(test);
            this.success = success;
        }
    }

    @Override
    public synchronized void unsupported(TestMethodLocator test,
        String context) {
        history.add(new Unsupported(test, context));
    }


    @Override
    public synchronized void failure(TestMethodLocator test,
        List<Examined> context, String check, Object[] otherObjects,
        List<StackWalker.StackFrame> stackTrace) {
        history.add(new Failure(test, context, check, otherObjects,
                stackTrace));
    }

    @Override
    public synchronized void exception(TestMethodLocator test,
        List<Examined> context, ExceptionPhase phase, Throwable exception,
        Class<? extends Throwable> expected) {
        history.add(new Exception(test, context, phase, exception, expected));
    }

    @Override
    public synchronized void completed(TestMethodLocator test,
        boolean success) {
        history.add(new Completed(test, success));
    }

    private final ArrayList<Call> history = new ArrayList<>();

    synchronized List<Call> history() {
        return List.copyOf(history);
    }

    synchronized void check(Testing t) {
        for(Call call : history)
            call.check(t);
        t.<List<Call>>examine(history).isnt(List.of());
        t.examine(history.get(history.size() -1))
            .isExactClass(Completed.class);
    }
}
