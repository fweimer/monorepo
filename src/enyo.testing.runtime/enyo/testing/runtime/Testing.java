package enyo.testing.runtime;

import enyo.testing.Examiner;
import enyo.testing.TestFailure;
import enyo.testing.TestAlreadyFailed;
import enyo.testing.ThrowingRunnable;
import enyo.testing.introspect.TestMethodLocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Default implementation of the {@link enyo.testing.Testing} interface.
 * Tests are expected to be run by
 * {@link InvokeTest#invokeTest(ModuleLayer,Testing)}.
 */
public final class Testing implements enyo.testing.Testing {
    final TestMethodLocator test;
    private final Results results;
    volatile boolean failed;

    /**
     * Creates a new testing object.
     * @param test the test to execute
     * @param results where to report test results
     * @see InvokeTest#invokeTest(ModuleLayer,Testing)
     */
    public Testing(TestMethodLocator test, Results results) {
        this.test = Objects.requireNonNull(test, "tests");
        this.results = Objects.requireNonNull(results, "results");
    }

    @Override
    public void fullBarrier() {
        if (failed)
            throw new TestAlreadyFailed();
    }

    @Override
    public <T> Examiner<T> examine(T value) {
        return new ObjectExaminer<>(this, value);
    }

    @Override
    public Examiner.OfInt examine(int value) {
        return new IntExaminer(this, value);
    }

    @Override
    public Examiner.OfLong examine(long value) {
        return new LongExaminer(this, value);
    }

    @Override
    public Examiner.OfDouble examine(double value) {
        return new DoubleExaminer(this, value);
    }

    @Override
    public Examiner.OfChar examine(char value) {
        return new CharExaminer(this, value);
    }

    @Override
    public void unsupported(String message) {
        results.unsupported(test, message);
    }

    @Override
    public <T> T assertFail(String message) {
        failed = true;
        if (message != null)
            throw new TestFailure(message);
        else
            throw new TestFailure("assertFail(null) called");
    }

    @Override
    public void assertTrue(boolean flag) {
        if (!flag) {
            failed = true;
            throw new TestFailure("Not true");
        }
        fullBarrier();
    }

    @Override
    public void assertTrue(String message, boolean flag) {
        if (!flag) {
            failed = true;
            throw new TestFailure("Not true: " + message);
        }
        fullBarrier();
    }

    @Override
    @Deprecated
    public void assertNull(Object obj) {
        if (obj != null) {
            failed = true;
            throw new TestFailure("Not null");
        }
        fullBarrier();
    }

    @Override
    @Deprecated
    public void assertNull(String message, Object obj) {
        if (obj != null) {
            failed = true;
            throw new TestFailure("Not null: "  + message);
        }
        fullBarrier();
    }

    @Override
    public void assertFalse(boolean flag) {
        if (flag) {
            failed = true;
            throw new TestFailure("Not false");
        }
        fullBarrier();
    }

    @Override
    public void assertFalse(String message, boolean flag) {
        if (flag) {
            failed = true;
            throw new TestFailure("Not false: " + message);
        }
        fullBarrier();
    }

    @Override
    @Deprecated
    public void assertEquals(Object left, Object right) {
        if (!Objects.equals(left, right)) {
            failed = true;
            throw new TestFailure("Not equals");
        }
        fullBarrier();
    }

    @Override
    @Deprecated
    public void assertEquals(String message, Object left, Object right) {
        if (!Objects.equals(left, right)) {
            failed = true;
            throw new TestFailure("Not equals: " + message);
        }
        fullBarrier();
    }

    @Override
    @Deprecated
    public void assertNotEquals(Object left, Object right) {
        if (Objects.equals(left, right)) {
            failed = true;
            throw new TestFailure("Equals");
        }
        fullBarrier();
    }

    @Override
    public void assertArrayEquals(Object[] left, Object[] right) {
        if (left == null && right == null) {
            failed = true;
            throw new TestFailure("left and right are null");
        }
        if (left == null) {
            failed = true;
            throw new TestFailure("left is null");
        }
        if (right == null) {
            failed = true;
            throw new TestFailure("right is null");
        }
        if (left.length != right.length) {
            failed = true;
            throw new TestFailure("Array length differs: "
                + left.length + " != " + right.length);
        }
        if (!Arrays.equals(left, right)) {
            failed = true;
            throw new TestFailure("Arrays are not equal");
        }
        fullBarrier();
    }

    @Override
    public void assertByteArrayEquals(byte[] left, byte[] right) {
        if (left == null && right == null) {
            failed = true;
            throw new TestFailure("left and right are null");
        }
        if (left == null) {
            failed = true;
            throw new TestFailure("left is null");
        }
        if (right == null) {
            failed = true;
            throw new TestFailure("right is null");
        }
        if (left.length != right.length) {
            failed = true;
            throw new TestFailure("Array length differs: "
                + left.length + " != " + right.length);
        }
        if (!Arrays.equals(left, right)) {
            failed = true;
            throw new TestFailure("Arrays are not equal");
        }
        fullBarrier();
    }


    @Override
    @Deprecated
    public void assertSame(Object left, Object right) {
        if (left == null && right == null) {
            failed = true;
            throw new TestFailure("left and right are null");
        }
        if (left == null) {
            failed = true;
            throw new TestFailure("left is null");
        }
        if (right == null) {
            failed = true;
            throw new TestFailure("right is null");
        }
        if (left != right) {
            failed = true;
            throw new TestFailure("Objects are not the same");
        }
        fullBarrier();
    }

    @Override
    @Deprecated
    public void assertNotSame(Object left, Object right) {
        if (left == null && right == null) {
            failed = true;
            throw new TestFailure("left and right are null");
        }
        if (left == null) {
            failed = true;
            throw new TestFailure("left is null");
        }
        if (right == null) {
            failed = true;
            throw new TestFailure("right is null");
        }
        if (left == right) {
            failed = true;
            throw new TestFailure("Objects are the same");
        }
        fullBarrier();
    }

    @Override
    public <E extends Throwable> void expect(
        ThrowingRunnable source, Class<E> exception,
        Consumer<? super E> consumer) {
        expect0("", source, exception, consumer);
    }

    @Override
    public <E extends Throwable> void expect(
        ThrowingRunnable source, Class<E> exception) {
        expect(source, exception, e -> { });
    }

    @Override
    public <E extends Throwable> void expect(String label,
        ThrowingRunnable source, Class<E> exception,
        Consumer<? super E> consumer) {
        expect0(" for: " + label, source, exception, consumer);
    }

    @Override
    public <E extends Throwable> void expect(String label,
        ThrowingRunnable source, Class<E> exception) {
        expect(label, source, exception, e -> { });
    }

    private <T, E extends Throwable> void expect0(
        String label, ThrowingRunnable source,
        Class<E> exception, Consumer<? super E> consumer) {
        Objects.requireNonNull(exception, "exception");
        Objects.requireNonNull(consumer, "consumer");
        try {
            source.run();
        } catch (Throwable t) {
            if (exception.isAssignableFrom(t.getClass()))
                try {
                    consumer.accept(exception.cast(t));
                } catch (Throwable e) {
                    e.addSuppressed(t);
                    throw e;
                }
            else
                recordFailure(ExceptionPhase.EXCEPTION_TEST,
                    label, t, exception);
            return;
        }
        recordFailure(label, "missing exception", exception);
    }

    void recordFailure(String context, String check, Object value) {
        recordFailure(context, check, List.of(value), captureStackTrace());
    }

    void recordFailure(String context, String check, Object left,
        Object right) {
        recordFailure(context, check, List.of(left, right),
            captureStackTrace());
    }

    void recordFailure(String context, String check,
        List<? extends Object> values,
        List<StackWalker.StackFrame> stackTrace) {
        failed = true;
        results.failure(test, context, check, values, stackTrace);
    }

    // Returns a list of interesting stack frames, ignoring classes in
    // this package and below the RunTests.runTest() method.
    private static List<StackWalker.StackFrame> captureStackTrace() {
        Backtrace backtrace = new Backtrace();
        StackWalker walker = StackWalker.getInstance(
            StackWalker.Option.RETAIN_CLASS_REFERENCE);
        walker.forEach(backtrace);
        return List.copyOf(backtrace.frames);
    }

    // For use with StackWalker.forEach().
    private static class Backtrace implements Consumer<StackWalker.StackFrame> {
        @Override
        public void accept(StackWalker.StackFrame frame) {
            switch (state) {
            case 0:
                // Start capturing frames until we leave this package.
                if (frame.getDeclaringClass().getPackage() == PACKAGE)
                    return;
                state = 1;
                frames.add(frame);
                break;
            case 1:
                // Capture stack frames until we encounter test
                // invocation method.
                if (frame.getDeclaringClass() == InvokeTest.class
                    && InvokeTest.INVOKE_TEST_METHOD.equals(
                        frame.getMethodName())
                    && InvokeTest.INVOKE_TEST_METHOD_TYPE.equals(
                        frame.getMethodType()))
                    state = 2;
                else
                    frames.add(frame);
                break;
            case 2:
                // Ignore remaining frames.  There should not be a lot
                // of them, so using forEach() does not hurt much.
                break;
            default:
                throw new InternalError("Invalid state: " +  state);
            }
        }

        private final ArrayList<StackWalker.StackFrame> frames =
            new ArrayList<>();
        private int state;

        private static Package PACKAGE = Backtrace.class.getPackage();
    }

    void recordFailure(ExceptionPhase phase, String context,
        Throwable exception) {
        recordFailure(phase, context, exception, null);
    }

    void recordFailure(ExceptionPhase phase, String context,
        Throwable exception, Class<? extends Throwable> expected) {
        failed = true;
        results.failure(test, phase, context, exception, expected);
    }

    void recordCompleted() {
        results.completed(test, !failed);
    }
}
