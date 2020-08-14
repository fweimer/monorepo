package enyo.testing.runtime.tests;

import enyo.testing.Testing;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.runtime.ExceptionPhase;
import enyo.testing.runtime.Results;

import java.util.ArrayList;
import java.util.List;

final class ResultsRecorder implements Results {
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
        final String context;

        ContextCall(TestMethodLocator test, String context) {
            super(test);
            this.context = context;
        }
    }

    static final class FailureCall extends ContextCall {
        final String check;
        final List<? extends Object> values;
        final List<StackWalker.StackFrame> stackTrace;


        FailureCall(TestMethodLocator test, String context, String check,
            List<? extends Object> values,
            List<StackWalker.StackFrame> stackTrace) {
            super(test, context);
            this.check = check;
            this.values = values;
            this.stackTrace = stackTrace;
        }

        void check(Testing t) {
            super.check(t);
            t.examine(check).isNotNull();
            t.examine(values).isNotNull();
            t.examine(stackTrace).isNotNull();
        }
    }

    static final class ExceptionFailureCall extends ContextCall {
        final ExceptionPhase phase;
        final Throwable exception;
        final Class<? extends Throwable> expected;


        ExceptionFailureCall(TestMethodLocator test, ExceptionPhase phase,
            String context, Throwable exception,
            Class<? extends Throwable> expected) {
            super(test, context);
            this.phase = phase;
            this.exception = exception;
            this.expected = expected;
        }

        void check(Testing t) {
            super.check(t);
            t.examine(phase).isNotNull();
            t.examine(exception).isNotNull();
        }
    }

    static final class CompletedCall extends Call {
        final boolean success;

        CompletedCall(TestMethodLocator test, boolean success) {
            super(test);
            this.success = success;
        }
    }

    @Override
    public synchronized void failure(TestMethodLocator test, String context,
        String check, List<? extends Object> values,
        List<StackWalker.StackFrame> stackTrace) {
        history.add(new FailureCall(test, context, check, values, stackTrace));
    }

    @Override
    public synchronized void failure(TestMethodLocator test,
        ExceptionPhase phase, String context, Throwable exception,
        Class<? extends Throwable> expected) {
        history.add(new ExceptionFailureCall(
                test, phase, context, exception, expected));
    }

    @Override
    public synchronized void completed(TestMethodLocator test,
        boolean success) {
        history.add(new CompletedCall(test, success));
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
            .isExactClass(CompletedCall.class);
    }
}
