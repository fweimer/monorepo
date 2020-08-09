package enyo.util.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.Result;
import enyo.util.ResultException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestResult {
    @Test
    public static void testPresent(Testing t) {
        Result<Integer, String> r = Result.of(1);
        t.examine(r.compute(
                value -> value + 1.0,
                error -> t.assertFail("Unreachable")))
            .is(2.0);
        t.examine(r.error()).is(Optional.empty());
        {
            Result<Long, String> r2 = Result.of(2L);
            Flag flag = new Flag(t);
            // FIXME: Should really be the same object.
            t.examine(r.flatMap(value -> {
                        t.examine(value).is(1);
                        flag.setOnce();
                        return r2;
                    }))
                .is(r2);
            flag.assertSet();
            Result<Long, String> r3 = Result.ofError("3");
            flag.clear();
            // FIXME: Should really be the same object.
            t.examine(r.flatMap(value -> {
                        t.examine(value).is(1);
                        flag.setOnce();
                        return r3;
                    }))
                .is(r3);
            flag.assertSet();
        }
        {
            Flag flag = new Flag(t);
            r.ifPresent(value -> {
                    t.examine(value).is(1);
                    flag.setOnce();
                });
            flag.assertSet();
        }
        {
            Flag flag = new Flag(t);
            r.ifPresentOrElse(value -> {
                    t.examine(value).is(1);
                    flag.setOnce();
                }, error -> t.assertFail("Unreachable"));
            flag.assertSet();
        }
        t.assertFalse(r.isError());
        t.assertTrue(r.isPresent());
        {
            Flag flag = new Flag(t);
            t.examine(r.map(
                    i -> {
                        flag.setOnce();
                        return i + 1;
                    }))
                .is(Result.of(2));
            flag.assertSet();
        }
        {
            Flag flag = new Flag(t);
            t.examine(r.map(
                    i -> {
                        flag.setOnce();
                        return i + 1;
                    },
                    e -> t.assertFail("Unreachable")))
                .is(Result.of(2));
            flag.assertSet();
        }
        t.<Result<?, ?>>examine(r.mapError(e -> t.assertFail("Unreachable")))
            .isSame(r);
        t.examine(r.value()).is(Optional.of(1));
        t.examine(r.orElse(2)).is(1);
        t.examine(r.orElseGet(() -> t.assertFail("Unreachable"))).is(1);
        t.examine(r.orElseThrow()).is(1);
        t.examine(r.stream().collect(Collectors.toList())).is(List.of(1));
        t.examine(r.toString()).is("Result.of(1)");

        // The type of the error does not matter.
        Result<Integer, Void> r1 = Result.of(1);
    }

    @Test
    public static void testError(Testing t) {
        Result<String, Integer> r = Result.ofError(1);
        t.examine(r.compute(
                value -> t.assertFail("Unreachable"),
                error -> error + 1.0))
            .is(2.0);
        t.examine(r.error()).is(Optional.of(1));
        t.<Result<?, ?>>examine(r.flatMap(value -> t.assertFail("Unreachable")))
            .is(r);
        r.ifPresent(value -> t.assertFail("Unreachable"));
        {
            Flag flag = new Flag(t);
            r.ifPresentOrElse(value -> t.assertFail("Unreachable"),
                error -> {
                    t.examine(error).is(1);
                    flag.setOnce();
                });
            flag.assertSet();
        }
        t.assertTrue(r.isError());
        t.assertFalse(r.isPresent());
        t.<Result<?, ?>>examine(r.map(value -> t.assertFail("Unreachable")))
            .is(r);
        {
            Flag flag = new Flag(t);
            t.examine(r.map(
                    value -> t.assertFail("Unreachable"),
                    i -> {
                        flag.setOnce();
                        return i + 1;
                    }))
                .is(Result.ofError(2));
            flag.assertSet();
        }
        {
            Flag flag = new Flag(t);
            t.examine(r.mapError(
                    i -> {
                        flag.setOnce();
                        return i + 1;
                    }))
                .is(Result.ofError(2));
            flag.assertSet();
        }
        t.examine(r.value()).is(Optional.empty());
        t.examine(r.orElse("2")).is("2");
        {
            Flag flag = new Flag(t);
            t.examine(r.orElseGet(() -> {
                        flag.setOnce();
                        return "2";
                    }))
                .is("2");
            flag.assertSet();
        }
        t.expect(() -> r.orElseThrow(),
            ResultException.class, e -> t.examine(e.getMessage()).is("1"));
        t.examine(r.stream().collect(Collectors.toList())).is(List.of());
        t.examine(r.toString()).is("Result.ofError(1)");

        // The type of the value does not matter.
        Result<Void, Integer> r1 = Result.ofError(1);
    }

    @Test
    public static void testOrder() {
        Order<Result<Integer, Integer>> order = new Order<>();
        order.add(Result.of(1));
        order.add(Result.of(2));
        order.group()
            .add(Result.of(3))
            .add(Result.of(3))
            .close();
        order.add(Result.ofError(1));
        order.add(Result.ofError(2));
        order.group()
            .add(Result.ofError(3))
            .add(Result.ofError(3))
            .close();
        order.check();
    }

    @Test
    public static void testErrorsFirst() {
        Order<Result<Integer, String>> order = new Order<>();
        order.add(Result.ofError("a"));
        order.add(Result.ofError("b"));
        order.group()
            .add(Result.ofError("c"))
            .add(Result.ofError("c"))
            .close();
        order.add(Result.of(1));
        order.add(Result.of(2));
        order.group()
            .add(Result.of(3))
            .add(Result.of(3))
            .close();
        order.check(Result.errorsFirst());
        order.check(Result.errorsFirst(Comparator.naturalOrder(),
                Comparator.naturalOrder()));
    }

    @Test
    public static void testErrorsLast() {
        Order<Result<Integer, String>> order = new Order<>();
        order.add(Result.of(1));
        order.add(Result.of(2));
        order.group()
            .add(Result.of(3))
            .add(Result.of(3))
            .close();
        order.add(Result.ofError("a"));
        order.add(Result.ofError("b"));
        order.group()
            .add(Result.ofError("c"))
            .add(Result.ofError("c"))
            .close();
        order.check(Result.valuesFirst());
        order.check(Result.valuesFirst(Comparator.naturalOrder(),
                Comparator.naturalOrder()));
    }

    // Alternative implementation of enyo.io.function.IOFunction.
    interface IOResultFunction<T, U>
        extends Function<T, Result<U, IOException>> {
        U applyIO(T value) throws IOException;

        @Override
        default Result<U, IOException> apply(T value) {
            try {
                return Result.of(applyIO(value));
            } catch (IOException e) {
                return Result.ofError(e);
            } catch (UncheckedIOException e) {
                return Result.ofError(e.getCause());
            }
        }

        static <T, U> IOResultFunction<T, U> of(IOResultFunction<T, U> mapper) {
            return mapper;
        }
    }

    @Test
    public void testIOResultFunction(Testing t) {
        IOException e1 = new IOException("E1");
        IOException e2 = new IOException("E2");
        UncheckedIOException e3 = new UncheckedIOException(e2);
        List<Result<Integer, IOException>> expected = List.of(
            Result.of(0),
            Result.ofError(e1),
            Result.ofError(e2),
            Result.ofError(e2),
            Result.of(4),
            Result.of(5));
        List<Result<Integer, IOException>> actual = IntStream.range(10, 16)
            .boxed()
            .map(IOResultFunction.of(i -> {
                        if (i == 11)
                            throw e1;
                        if (i == 12)
                            throw e2;
                        if (i == 13)
                            throw e3;
                        return i - 10;
                    }))
            .collect(Collectors.toList());
        t.examine(actual).is(expected);
    }


    private static final class Flag {
        Flag(Testing t) {
            this.t = t;
        }

        private final Testing t;
        private boolean flag;

        void setOnce() {
            t.assertFalse(flag);
            flag = true;
        }

        void clear() {
            flag = false;
        }

        void assertSet() {
            t.assertTrue(flag);
        }
    }
}
