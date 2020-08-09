package enyo.lang.tests;

import enyo.lang.ThrowableErasure;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestThrowableErasure {
    @Test
    public static void testEmpty(Testing t) {
        Throwable exception = new Exception();
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).isNull();
        t.examine(erasure.cause()).isNull();
        t.examine(erasure.suppressed()).is(List.of());
        t.examine(erasure.toString()).is("java.lang.Exception");
        check(t, exception, erasure);
    }

    @Test
    public static void testStackTrace(Testing t) {
        StackTraceElement[] stackTrace =
            Thread.currentThread().getStackTrace();
        for (boolean fromEnd : List.of(false, true))
            IntStream.rangeClosed(0, stackTrace.length)
                .forEach(size -> {
                        StackTraceElement[] subTrace;
                        if (fromEnd)
                            subTrace = Arrays.copyOfRange(
                                stackTrace, size, stackTrace.length);
                        else
                            subTrace = Arrays.copyOf(stackTrace, size);
                        Throwable exception = new Exception();
                        exception.setStackTrace(subTrace);
                        ThrowableErasure erasure =
                            new ThrowableErasure(exception);
                        t.examine(erasure.stackTrace()).is(List.of(subTrace));
                        check(t, exception, erasure);

                        {
                            Throwable exceptionFullTrace =
                                new Exception(exception);
                            check(t, exceptionFullTrace,
                                new ThrowableErasure(exceptionFullTrace));
                        }
                        {
                            Throwable exceptionFullTrace =
                                new Exception(exception);
                            exception.initCause(exceptionFullTrace);
                            check(t, exception,
                                new ThrowableErasure(exception));
                        }
                    });
    }

    @Test
    public static void testMessage1(Testing t) {
        Throwable exception = new Exception("example message");
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).is("example message");
        t.examine(erasure.cause()).isNull();
        t.examine(erasure.suppressed()).is(List.of());
        t.examine(erasure.toString())
            .is("java.lang.Exception: example message");
        check(t, exception, erasure);
    }

    @Test
    public static void testMessage2(Testing t) {
        Throwable exception = new Exception("");
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).is("");
        t.examine(erasure.cause()).isNull();
        t.examine(erasure.suppressed()).is(List.of());
        t.examine(erasure.toString()).is("java.lang.Exception: ");
        check(t, exception, erasure);
    }

    @Test
    public static void testCause1(Testing t) {
        Throwable exception = new Exception(new RuntimeException());
        ThrowableErasure erasure = new ThrowableErasure(exception);

        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).is("java.lang.RuntimeException");

        t.examine(erasure.cause().className()).is("java.lang.RuntimeException");
        t.examine(erasure.cause().message()).isNull();

        check(t, exception, erasure);
    }

    @Test
    public static void testCause2(Testing t) {
        Throwable exception = new Exception(null, new RuntimeException());
        ThrowableErasure erasure = new ThrowableErasure(exception);

        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).isNull();

        t.examine(erasure.cause().className()).is("java.lang.RuntimeException");
        t.examine(erasure.cause().message()).isNull();

        check(t, exception, erasure);
    }

    @Test
    public static void testCause3(Testing t) {
        Throwable exception = new Exception("1", new RuntimeException("2"));
        ThrowableErasure erasure = new ThrowableErasure(exception);

        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).is("1");

        t.examine(erasure.cause().className()).is("java.lang.RuntimeException");
        t.examine(erasure.cause().message()).is("2");

        check(t, exception, erasure);
    }

    @Test
    public static void testCauseRecursive1(Testing t) {
        Throwable exception = new Exception() {
                @Override
                public Throwable getCause() {
                    return this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);

        t.examine(erasure.className()).is(exception.getClass().getName());
        t.examine(erasure.cause()).isSame(erasure);
        t.examine(erasure.message()).isNull();

        check(t, exception, erasure);
    }

    @Test
    public static void testCauseRecursive2(Testing t) {
        Throwable exception1 = new Exception();
        Throwable exception2 = new Exception(exception1);
        exception1.initCause(exception2);
        ThrowableErasure erasure = new ThrowableErasure(exception1);

        t.examine(erasure)
            .isNotSame(erasure.cause())
            .isSame(erasure.cause().cause());

        check(t, exception1, erasure);
        check(t, exception2, erasure.cause());
    }


    @Test
    public static void testSuppressed1(Testing t) {
        Throwable exception = new Exception();
        exception.addSuppressed(new RuntimeException());
        ThrowableErasure erasure = new ThrowableErasure(exception);

        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).isNull();

        t.examine(erasure.suppressed().stream()
            .map(ThrowableErasure::className)
            .collect(Collectors.toList()))
            .is(List.of("java.lang.RuntimeException"));

        check(t, exception, erasure);
    }

    @Test
    public static void testSuppressed2(Testing t) {
        Throwable exception = new Exception();
        exception.addSuppressed(new RuntimeException());
        exception.addSuppressed(new IllegalArgumentException());
        ThrowableErasure erasure = new ThrowableErasure(exception);

        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).isNull();

        t.examine(erasure.suppressed().stream()
            .map(ThrowableErasure::className)
            .collect(Collectors.toList()))
            .is(List.of("java.lang.RuntimeException",
                "java.lang.IllegalArgumentException"));

        check(t, exception, erasure);
    }

    @Test
    public static void testSuppressed3(Testing t) {
        Throwable exception1 = new Exception();
        Throwable exception2 = new RuntimeException();
        exception1.addSuppressed(exception2);
        exception1.addSuppressed(exception2);
        ThrowableErasure erasure = new ThrowableErasure(exception1);

        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.message()).isNull();

        t.examine(erasure.suppressed().stream()
            .map(ThrowableErasure::className)
            .collect(Collectors.toList()))
            .is(List.of("java.lang.RuntimeException",
                    "java.lang.RuntimeException"));
        t.examine(erasure.suppressed().get(1)).is(erasure.suppressed().get(0));

        check(t, exception1, erasure);
    }

    @Test
    public static void testSuppressedRecursive1(Testing t) {
        Throwable exception1 = new Exception();
        Throwable exception2 = new RuntimeException();
        exception1.addSuppressed(exception2);
        exception2.addSuppressed(exception1);

        ThrowableErasure erasure = new ThrowableErasure(exception1);
        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.suppressed().stream()
            .map(ThrowableErasure::className)
            .collect(Collectors.toList()))
            .is(List.of("java.lang.RuntimeException"));
        t.examine(erasure.suppressed().get(0).suppressed().get(0))
            .isSame(erasure);

        check(t, exception1, erasure);
        check(t, exception2, erasure.suppressed().get(0));
    }

    @Test
    public static void testSuppressedRecursive2(Testing t) {
        Throwable exception1 = new Exception();
        Throwable exception2 = new Exception();
        Throwable exception3 = new Exception();
        exception1.addSuppressed(exception2);
        exception2.addSuppressed(exception3);
        exception3.addSuppressed(exception1);

        ThrowableErasure erasure = new ThrowableErasure(exception1);
        t.examine(erasure.className()).is("java.lang.Exception");
        t.examine(erasure.suppressed().stream()
            .map(ThrowableErasure::className)
            .collect(Collectors.toList()))
            .is(List.of("java.lang.Exception"));
        t.examine(erasure.suppressed().get(0).suppressed().get(0)
            .suppressed().get(0))
            .isSame(erasure);

        check(t, exception1, erasure);
        check(t, exception2, erasure.suppressed().get(0));
        check(t, exception3, erasure.suppressed().get(0).suppressed().get(0));
    }

    @Test
    public static void testCauseSuppressedRecursive(Testing t) {
        Throwable exception1 = new Exception();
        Throwable exception2 = new RuntimeException(exception1);
        exception1.addSuppressed(exception2);
        t.examine(exception2.getCause()).isSame(exception1);
        t.examine(exception1.getSuppressed()[0]).isSame(exception2);

        ThrowableErasure erasure = new ThrowableErasure(exception1);
        t.examine(erasure).isSame(erasure.suppressed().get(0).cause());

        check(t, exception1, erasure);
        check(t, exception2, erasure.suppressed().get(0));
    }

    @Test
    public static void testToString(Testing t) {
        var exception = new RuntimeException() {
                @Override
                public String getMessage() {
                    return "getMessage";
                }

                @Override
                public String toString() {
                    return "toString";
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.message()).is("getMessage");
        t.examine(erasure.toString()).is("toString");
        check(t, exception, erasure);

        {
            var e1 = new Exception(exception);
            check(t, e1, new ThrowableErasure(e1));
        }
        {
            var e1 = new Exception();
            e1.addSuppressed(exception);
            check(t, e1, new ThrowableErasure(e1));
        }
    }

    @Test
    public static void testStackTraceError1(Testing t) {
        var exception = new RuntimeException() {
                @Override
                public StackTraceElement[] getStackTrace() {
                    throw this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.stackTrace()).is(List.of());
        t.examine(print(wr -> erasure.printStackTrace(wr)))
            .is(exception.getClass().getName() + '\n');
    }

    @Test
    public static void testStackTraceError2(Testing t) {
        var exception = new RuntimeException("message", new Exception()) {
                @Override
                public StackTraceElement[] getStackTrace() {
                    throw this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.stackTrace()).is(List.of());
        t.examine(print(wr -> erasure.printStackTrace(wr)))
            .is(exception.getClass().getName()
                + ": message\nCaused by: "
                + print(wr -> exception.getCause().printStackTrace(wr)));
    }

    @Test
    public static void testMessageError(Testing t) {
        var exception = new RuntimeException() {
                @Override
                public String getMessage() {
                    throw this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.message()).isNull();
        t.assertTrue(erasure.messageError());
        t.examine(erasure.cause()).isNull();
        t.assertFalse(erasure.causeError());
        t.examine(erasure.toString()).is(exception.getClass().getName());
    }


    @Test
    public static void testCauseError(Testing t) {
        Exception exception = new RuntimeException() {
                @Override
                public Throwable getCause() {
                    throw this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.message()).isNull();
        t.assertFalse(erasure.messageError());
        t.examine(erasure.cause()).isNull();
        t.assertTrue(erasure.causeError());
        t.examine(erasure.toString()).is(exception.getClass().getName());
    }

    @Test
    public static void testToStringError1(Testing t) {
        var exception = new RuntimeException() {
                @Override
                public String toString() {
                    throw this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.message()).isNull();
        t.assertFalse(erasure.messageError());
        t.examine(erasure.cause()).isNull();
        t.assertFalse(erasure.causeError());
        t.examine(erasure.toString()).is(exception.getClass().getName());
    }

    @Test
    public static void testToStringError2(Testing t) {
        var exception = new RuntimeException("message") {
                @Override
                public String toString() {
                    throw this;
                }
            };
        ThrowableErasure erasure = new ThrowableErasure(exception);
        t.examine(erasure.message()).is("message");
        t.assertFalse(erasure.messageError());
        t.examine(erasure.cause()).isNull();
        t.assertFalse(erasure.causeError());
        t.examine(erasure.toString())
            .is(exception.getClass().getName() + ": message");
    }

    private static void check(Testing t, Throwable expected,
        ThrowableErasure actual) {
        check0(t, new HashSet<>(), expected, actual);

        ThrowableErasure actual1;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ObjectOutputStream obj = new ObjectOutputStream(out)) {
                obj.writeObject(actual);
            }
            ByteArrayInputStream in =
                new ByteArrayInputStream(out.toByteArray());
            try (ObjectInputStream obj = new ObjectInputStream(in)) {
                actual1 = (ThrowableErasure) obj.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        check0(t, new HashSet<>(), expected, actual1);
    }

    private static void check0(Testing t, HashSet<ThrowableErasure> seen,
        Throwable expected, ThrowableErasure actual) {
        if (expected == null)
            t.examine(actual).isNull();
        if (actual == null)
            t.examine(expected).isNull();
        if (expected == null || actual == null)
            return;

        if (!seen.add(actual))
            return;

        t.examine(actual.className()).is(expected.getClass().getName());
        t.examine(actual.stackTrace()).is(List.of(expected.getStackTrace()));
        t.examine(actual.message()).nullableIs(expected.getMessage());
        t.assertFalse(actual.messageError());

        check0(t, seen, expected.getCause(), actual.cause());
        t.assertFalse(actual.causeError());
        Throwable[] suppressed = expected.getSuppressed();
        t.examine(actual.suppressed().size()).is(suppressed.length);
        for (int i = 0; i < suppressed.length; ++i)
            check0(t, seen, suppressed[i], actual.suppressed().get(i));

        t.examine(actual.toString()).is(expected.toString());

        // It is easier to rewrite the actual stack traces, not the
        // test expectations.
        t.examine(rewriteStackTrace(print(wr -> actual.printStackTrace(wr))))
            .is(print(wr -> expected.printStackTrace(wr)));
    }

    private static String print(Consumer<PrintWriter> printer) {
        StringWriter swr = new StringWriter();
        PrintWriter pwr = new PrintWriter(swr);
        printer.accept(pwr);
        pwr.close();
        return swr.toString();
    }

    // OpenJDK versions before 14 use "[CIRCULAR REFERENCE: ...]"
    // without prefixes for all circular references.  Later versions
    // have "Caused by: " and "Suppressed: " prefixes.
    private static final boolean INDENTED_CIRCULAR_REFERENCE;
    static {
        String stackTrace = print(wr ->
            new Exception() {
                @Override
                public Throwable getCause() {
                    return this;
                }
            }.printStackTrace(wr));
        INDENTED_CIRCULAR_REFERENCE =
            stackTrace.indexOf("\n\t[CIRCULAR REFERENCE:") < 0;
    }

    private static String rewriteStackTrace(String stackTrace) {
        if (INDENTED_CIRCULAR_REFERENCE)
            // No rewriting necessary for newer OpenJDK version.
            return stackTrace;
        else
            return stackTrace.replaceAll(
                "\n\t*(?:Caused by|Suppressed): \\[CIRCULAR REFERENCE: ",
                "\n\t[CIRCULAR REFERENCE:");
    }
}
