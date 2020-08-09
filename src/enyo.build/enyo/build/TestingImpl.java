package enyo.build;

import enyo.testing.Examiner;
import enyo.testing.Testing;
import enyo.testing.TestFailure;
import enyo.testing.TestAlreadyFailed;
import enyo.testing.ThrowingRunnable;
import enyo.testing.introspect.TestMethodLocator;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

final class TestingImpl implements Testing {
    private final TestMethodLocator test;
    volatile boolean failed;

    TestingImpl(TestMethodLocator test) {
        this.test = Objects.requireNonNull(test);
    }

    @Override
    public void fullBarrier() {
        if (failed)
            throw new TestAlreadyFailed();
    }

    @Override
    public <T> Examiner<T> examine(T value) {
        return new Examiner<>() {
            private String context;

            @Override
            public Examiner<T> context(String context) {
                context = Objects.requireNonNull(context);
                return this;
            }

            @Override
            public Examiner<T> is(T other) {
                if (value == null)
                    recordFailure(context, "not null", value);
                if (other == null)
                    recordFailure(context, "not null", other);
                if (!Objects.equals(value, other))
                    recordFailure(context, "equals", value, other);
                return this;
            }

            @Override
            public Examiner<T> isnt(T other) {
                if (value == null)
                    recordFailure(context, "not null", value);
                if (other == null)
                    recordFailure(context, "not null", other);
                if (Objects.equals(value, other))
                    recordFailure(context, "not equals", value, other);
                return this;
            }

            @Override
            public Examiner<T> isSame(T other) {
                if (value == null)
                    recordFailure(context, "not null", value);
                if (other == null)
                    recordFailure(context, "not null", other);
                if (value != null && value != other)
                    recordFailure(context, "==", value, other);
                return this;
            }

            @Override
            public Examiner<T> isExactClass(Class<? extends T> cls) {
                if (value.getClass() != cls)
                    recordFailure(context, "exact class", value, cls);
                return this;
            }

            @Override
            public Examiner<T> isNotSame(T other) {
                if (value == null)
                    recordFailure(context, "not null", value);
                if (other == null)
                    recordFailure(context, "not null", other);
                if (value != null && value == other)
                    recordFailure(context, "!=", value, other);
                return this;
            }
        };
    }

    @Override
    public Examiner.OfInt examine(int value) {
        return new Examiner.OfInt() {
            private String context;

            @Override
            public Examiner.OfInt context(String context) {
                context = Objects.requireNonNull(context);
                return this;
            }

            @Override
            public Examiner.OfInt is(int other) {
                if (value != other)
                    recordFailure(context, "==", value, other);
                return this;
            }

            @Override
            public Examiner.OfInt isnt(int other) {
                if (value == other)
                    recordFailure(context, "!=", value, other);
                return this;
            }
        };
    }

    @Override
    public Examiner.OfLong examine(long value) {
        return new Examiner.OfLong() {
            private String context;

            @Override
            public Examiner.OfLong context(String context) {
                context = Objects.requireNonNull(context);
                return this;
            }

            @Override
            public Examiner.OfLong is(long other) {
                if (value != other)
                    recordFailure(context, "==", value, other);
                return this;
            }

            @Override
            public Examiner.OfLong isnt(long other) {
                if (value == other)
                    recordFailure(context, "!=", value, other);
                return this;
            }
        };
    }

    @Override
    public Examiner.OfDouble examine(double value) {
        return new Examiner.OfDouble() {
            private String context;

            @Override
            public Examiner.OfDouble context(String context) {
                context = Objects.requireNonNull(context);
                return this;
            }

            @Override
            public Examiner.OfDouble is(double other) {
                if (value != other)
                    recordFailure(context, "==", value, other);
                return this;
            }

            @Override
            public Examiner.OfDouble isnt(double other) {
                if (value == other)
                    recordFailure(context, "!=", value, other);
                return this;
            }
        };
    }

    @Override
    public Examiner.OfChar examine(char value) {
        return new Examiner.OfChar() {
            private String context;

            @Override
            public Examiner.OfChar context(String context) {
                context = Objects.requireNonNull(context);
                return this;
            }

            @Override
            public Examiner.OfChar is(char other) {
                if (value != other)
                    recordFailure(context, "==", value, other);
                return this;
            }

            @Override
            public Examiner.OfChar isnt(char other) {
                if (value == other)
                    recordFailure(context, "!=", value, other);
                return this;
            }
        };
    }

    @Override
    public void unsupported(String message) {
        System.err.format("%s: warning: unsupported test: %s%n", test, message);
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
    public void assertNull(Object obj) {
        if (obj != null) {
            failed = true;
            throw new TestFailure("Not null");
        }
        fullBarrier();
    }

    @Override
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
                } catch (Throwable e1) {
                    if (e1.getCause() == null)
                        e1.initCause(t);
                    throw e1;
                }
            else {
                failed = true;
                throw new TestFailure(
                    "Expected exception " + exception.getName() + label, t);
            }
            return;
        }
        failed = true;
        throw new TestFailure("Missing expected exception "
            + exception.getName() + label);
    }

    private void recordFailure(String context, String check, Object value) {
        failed = true;
        synchronized (System.err) {
            if (context == null)
                System.err.format("%s: error: failed test: %s%n",
                    test, check);
            else
                System.err.format("%s: error: failed test: %s: %s%n",
                    test, context, check);
            StackWalker walker = StackWalker.getInstance();
            walker.forEach(frame ->
                System.err.format("\t%s%n", frame));
        }
    }

    private void recordFailure(String context, String check, Object left,
        Object right) {
        // Object data is not recorded yet, so the two implementations
        // are the same.
        recordFailure(context, check, left);
    }
}
