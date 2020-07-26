package enyo.build;

import enyo.testing.Testing;
import enyo.testing.TestFailure;
import enyo.testing.TestAlreadyFailed;
import enyo.testing.ThrowingSupplier;
import enyo.testing.ThrowingAction;

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
    public void assertEquals(Object left, Object right) {
        if (!Objects.equals(left, right)) {
            failed = true;
            throw new TestFailure("Not equals");
        }
        fullBarrier();
    }

    @Override
    public void assertEquals(String message, Object left, Object right) {
        if (!Objects.equals(left, right)) {
            failed = true;
            throw new TestFailure("Not equals: " + message);
        }
        fullBarrier();
    }

    @Override
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
    public <T, E extends Throwable> void expect(
        ThrowingSupplier<T> source,
        Class<E> exception, Consumer<? super E> consumer) {
        expect0("", source, exception, consumer);
    }

    @Override
    public <T, E extends Throwable> void expect(
        ThrowingSupplier<T> source, Class<E> exception) {
        expect(source, exception, e -> { });
    }

    @Override
    public <T, E extends Throwable> void expect(
        String label, ThrowingSupplier<T> source,
        Class<E> exception, Consumer<? super E> consumer) {
        expect0(" for: " + label, source, exception, consumer);
    }

    @Override
    public <T, E extends Throwable> void expect(
        String label, ThrowingSupplier<T> source, Class<E> exception) {
        expect(label, source, exception, e -> { });
    }

    @Override
    public <E extends Throwable> void expect(
        ThrowingAction source, Class<E> exception,
        Consumer<? super E> consumer) {
        expect0("", source, exception, consumer);
    }

    @Override
    public <E extends Throwable> void expect(
        ThrowingAction source, Class<E> exception) {
        expect(source, exception, e -> { });
    }

    @Override
    public <E extends Throwable> void expect(String label,
        ThrowingAction source, Class<E> exception,
        Consumer<? super E> consumer) {
        expect0(" for: " + label, source, exception, consumer);
    }

    @Override
    public <E extends Throwable> void expect(String label,
        ThrowingAction source, Class<E> exception) {
        expect(label, source, exception, e -> { });
    }

    private <T, E extends Throwable> void expect0(
        String label, ThrowingSupplier<T> source,
        Class<E> exception, Consumer<? super E> consumer) {
        Objects.requireNonNull(exception, "exception");
        Objects.requireNonNull(consumer, "consumer");
        try {
            source.get();
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

    private <E extends Throwable> void expect0(
        String label, ThrowingAction source,
        Class<E> exception, Consumer<? super E> consumer) {
        expect0(label, () -> {
                source.perform();
                return null;
            }, exception, consumer);
    }
}
