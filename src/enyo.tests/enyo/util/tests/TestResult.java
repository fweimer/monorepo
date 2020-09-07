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
            var counter = t.counter();
            // FIXME: Should really be the same object.
            t.examine(r.flatMap(value -> {
                        t.examine(value).is(1);
                        counter.increment();
                        return r2;
                    }))
                .is(r2);
            counter.checkTarget();
            Result<Long, String> r3 = Result.ofError("3");
            counter.reset();
            // FIXME: Should really be the same object.
            t.examine(r.flatMap(value -> {
                        t.examine(value).is(1);
                        counter.increment();
                        return r3;
                    }))
                .is(r3);
            counter.checkTarget();
        }
        {
            var counter = t.counter();
            r.ifPresent(value -> {
                    t.examine(value).is(1);
                    counter.increment();
                });
            counter.checkTarget();
        }
        {
            var counter = t.counter();
            r.ifPresentOrElse(value -> {
                    t.examine(value).is(1);
                    counter.increment();
                }, error -> t.assertFail("Unreachable"));
            counter.checkTarget();
        }
        t.assertFalse(r.isError());
        t.assertTrue(r.isPresent());
        {
            var counter = t.counter();
            t.examine(r.map(
                    i -> {
                        counter.increment();
                        return i + 1;
                    }))
                .is(Result.of(2));
            counter.checkTarget();
        }
        {
            var counter = t.counter();
            t.examine(r.map(
                    i -> {
                        counter.increment();
                        return i + 1;
                    },
                    e -> t.assertFail("Unreachable")))
                .is(Result.of(2));
            counter.checkTarget();
        }
        t.<Result<?, ?>>examine(r.mapError(e -> t.assertFail("Unreachable")))
            .isSame(r);
        t.examine(r.value()).is(Optional.of(1));
        t.examine(r.orElse(2)).is(1);
        t.examine(r.orElseGet(() -> t.assertFail("Unreachable"))).is(1);
        t.examine(r.orElseThrow()).is(1);
        t.examine(r.stream().collect(Collectors.toList())).is(List.of(1));
        t.examine(r.toString()).is("1");
        t.examine(r.equalsOf(1)).is(true);
        t.examine(r.equalsOf(1L)).is(false);
        t.examine(r.equalsOfError(1)).is(false);

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
            var counter = t.counter();
            r.ifPresentOrElse(value -> t.assertFail("Unreachable"),
                error -> {
                    t.examine(error).is(1);
                    counter.increment();
                });
            counter.checkTarget();
        }
        t.assertTrue(r.isError());
        t.assertFalse(r.isPresent());
        t.<Result<?, ?>>examine(r.map(value -> t.assertFail("Unreachable")))
            .is(r);
        {
            var counter = t.counter();
            t.examine(r.map(
                    value -> t.assertFail("Unreachable"),
                    i -> {
                        counter.increment();
                        return i + 1;
                    }))
                .is(Result.ofError(2));
            counter.checkTarget();
        }
        {
            var counter = t.counter();
            t.examine(r.mapError(
                    i -> {
                        counter.increment();
                        return i + 1;
                    }))
                .is(Result.ofError(2));
            counter.checkTarget();
        }
        t.examine(r.value()).is(Optional.empty());
        t.examine(r.orElse("2")).is("2");
        {
            var counter = t.counter();
            t.examine(r.orElseGet(() -> {
                        counter.increment();
                        return "2";
                    }))
                .is("2");
            counter.checkTarget();
        }
        t.expect(() -> r.orElseThrow(),
            ResultException.class, e -> t.examine(e.getMessage()).is("1"));
        t.examine(r.stream().collect(Collectors.toList())).is(List.of());
        t.examine(r.toString()).is("1");
        t.examine(r.equalsOf(1)).is(false);
        t.examine(r.equalsOfError(1)).is(true);
        t.examine(r.equalsOfError(1L)).is(false);

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

    @Test
    public static void testOfEnumOrdinalNull(Testing t) {
        t.expect(() -> Result.ofEnumOrdinal(null, 0),
            NullPointerException.class);
        t.expect(() -> Result.ofEnumOrdinal(null, 1),
            NullPointerException.class);
        t.expect(() -> Result.ofEnumOrdinal(null, -1),
            NullPointerException.class);
    }

    enum EmptyEnum { }

    @Test
    public static void testOfEnumOrdinalEmpty(Testing t) {
        IntStream.rangeClosed(-1, 1).forEach(i ->
            t.examine(i)
            .mapToObj(i1 -> Result.ofEnumOrdinal(EmptyEnum.class, i1))
            .is(Result.ofError(i)));
    }

    enum OneEnum { E1 }

    @Test
    public static void testOfEnumOrdinalOne(Testing t) {
        t.examine(Result.ofEnumOrdinal(OneEnum.class, 0))
            .is(Result.of(OneEnum.E1));
        List.of(-1, 1).forEach(i ->
            t.examine(i)
            .map(i1 -> Result.ofEnumOrdinal(OneEnum.class, i1))
            .is(Result.ofError(i)));
    }

    enum TwoEnum { E1, E2 }

    @Test
    public static void testOfEnumOrdinalTwo(Testing t) {
        t.examine(Result.ofEnumOrdinal(TwoEnum.class, 0))
            .is(Result.of(TwoEnum.E1));
        t.examine(Result.ofEnumOrdinal(TwoEnum.class, 1))
            .is(Result.of(TwoEnum.E2));
        List.of(-2, -1, 2, 3).forEach(i ->
            t.examine(i)
            .map(i1 -> Result.ofEnumOrdinal(TwoEnum.class, i1))
            .is(Result.ofError(i)));
    }
}
