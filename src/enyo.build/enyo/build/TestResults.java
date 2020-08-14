package enyo.build;

import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.runtime.ExceptionPhase;
import enyo.testing.runtime.Results;

import java.util.List;

class TestResults implements Results {
    @Override
    public void unsupported(TestMethodLocator test, String context) {
        synchronized (System.err) {
            System.err.format("%s: warning: unsupported test: %s%n",
                test, context);
        }
    }

    @Override
    public void failure(TestMethodLocator test, String context, String check,
        List<? extends Object> values,
        List<StackWalker.StackFrame> stackTrace) {
        synchronized (System.err) {
            if (context == null)
                System.err.format("%s: error: failed test: %s%n",
                    test, check);
            else
                System.err.format("%s: error: failed test: %s: %s%n",
                    test, context, check);
            stackTrace.stream()
                .forEach(frame -> System.err.format("\t%s%n", frame));
        }
    }

    @Override
    public void exception(TestMethodLocator test, ExceptionPhase phase,
        String context, Throwable exception,
        Class<? extends Throwable> expected) {
        synchronized (System.err) {
            if (expected == null) {
                if (context == null)
                    System.err.format("%s: error: failed test%n", test);
                else
                    System.err.format("%s: error: failed test: %s%n",
                        test, context);
            } else {
                if (context == null)
                    System.err.format(
                        "%s: error: failed test expecting exception %s%n",
                        test, expected.getName());
                else
                    System.err.format(
                        "%s: error: failed test expecting exception %s: %s%n",
                        test, expected.getName(), context);
            }
            exception.printStackTrace(System.err);
        }
    }

    @Override
    public void completed(TestMethodLocator test, boolean success) {
    }
}
