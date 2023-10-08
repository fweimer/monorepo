package enyo.util.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.Either;
import enyo.util.EitherException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestEither {
    @Test
    public static void testLeft(Testing t) {
        Either<Integer, String> r = Either.ofLeft(1);
        t.examine(r.compute(
                value -> value + 1.0,
                error -> t.assertFail("Unreachable")))
            .is(2.0);
        t.examine(r.toRight()).is(Optional.empty());
        {
            Either<Long, String> r2 = Either.ofLeft(2L);
            var counter = t.counter();
            t.examine(r.flatMapLeft(value -> {
                        t.examine(value).is(1);
                        counter.increment();
                        return r2;
                    }))
                .isSame(r2);
            counter.checkTarget();
            Either<Long, String> r3 = Either.ofRight("3");
            counter.reset();
            t.examine(r.flatMapLeft(value -> {
                        t.examine(value).is(1);
                        counter.increment();
                        return r3;
                    }))
                .isSame(r3);
            counter.checkTarget();
        }
        t.<Either<?, ?>>examine(r.flatMapRight(
                value -> t.assertFail("Unreachable")))
            .isSame(r);
        t.assertFalse(r.isRight());
        t.assertTrue(r.isLeft());
        {
            var counter = t.counter();
            t.examine(r.mapLeft(
                    i -> {
                        counter.increment();
                        return i + 1;
                    }))
                .is(Either.ofLeft(2));
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
                .is(Either.ofLeft(2));
            counter.checkTarget();
        }
        t.<Either<?, ?>>examine(r.mapRight(e -> t.assertFail("Unreachable")))
            .isSame(r);
        t.examine(r.toLeft()).is(Optional.of(1));
        t.examine(r.leftOrElse(2)).is(1);
        t.examine(r.leftOrElseGet(() -> t.assertFail("Unreachable"))).is(1);
        t.examine(r.leftOrElseThrow()).is(1);
        t.examine(r.leftStream().collect(Collectors.toList())).is(List.of(1));
        t.examine(r.toString()).is("1");
        t.examine(r.equalsOfLeft(1)).is(true);
        t.examine(r.equalsOfLeft(1L)).is(false);
        t.examine(r.equalsOfRight(1)).is(false);

        // The type of the error does not matter.
        Either<Integer, Void> r1 = Either.ofLeft(1);
    }

    @Test
    public static void testRight(Testing t) {
        Either<String, Integer> r = Either.ofRight(1);
        t.examine(r.compute(
                value -> t.assertFail("Unreachable"),
                error -> error + 1.0))
            .is(2.0);
        t.examine(r.toRight()).is(Optional.of(1));
        {
            Either<String, Long> r2 = Either.ofRight(2L);
            var counter = t.counter();
            t.examine(r.flatMapRight(value -> {
                        t.examine(value).is(1);
                        counter.increment();
                        return r2;
                    }))
                .isSame(r2);
            counter.checkTarget();
            Either<String, Long> r3 = Either.ofLeft("3");
            counter.reset();
            t.examine(r.flatMapRight(value -> {
                        t.examine(value).is(1);
                        counter.increment();
                        return r3;
                    }))
                .isSame(r3);
            counter.checkTarget();
        }
        t.<Either<?, ?>>examine(r.flatMapLeft(
                value -> t.assertFail("Unreachable")))
            .isSame(r);
        t.assertTrue(r.isRight());
        t.assertFalse(r.isLeft());
        t.<Either<?, ?>>examine(r.mapLeft(value -> t.assertFail("Unreachable")))
            .is(r);
        {
            var counter = t.counter();
            t.examine(r.map(
                    value -> t.assertFail("Unreachable"),
                    i -> {
                        counter.increment();
                        return i + 1;
                    }))
                .is(Either.ofRight(2));
            counter.checkTarget();
        }
        {
            var counter = t.counter();
            t.examine(r.mapRight(
                    i -> {
                        counter.increment();
                        return i + 1;
                    }))
                .is(Either.ofRight(2));
            counter.checkTarget();
        }
        t.examine(r.toLeft()).is(Optional.empty());
        t.examine(r.leftOrElse("2")).is("2");
        {
            var counter = t.counter();
            t.examine(r.leftOrElseGet(() -> {
                        counter.increment();
                        return "2";
                    }))
                .is("2");
            counter.checkTarget();
        }
        t.expect(() -> r.leftOrElseThrow(),
            EitherException.class, e -> t.examine(e.getMessage()).is("1"));
        t.examine(r.leftStream().collect(Collectors.toList())).is(List.of());
        t.examine(r.toString()).is("1");
        t.examine(r.equalsOfLeft(1)).is(false);
        t.examine(r.equalsOfRight(1)).is(true);
        t.examine(r.equalsOfRight(1L)).is(false);

        // The type of the value does not matter.
        Either<Void, Integer> r1 = Either.ofRight(1);
    }

    @Test
    public static void testOrder() {
        Order<Either<Integer, Integer>> order = new Order<>();
        order.add(Either.ofLeft(1));
        order.add(Either.ofLeft(2));
        order.group()
            .add(Either.ofLeft(3))
            .add(Either.ofLeft(3))
            .close();
        order.add(Either.ofRight(1));
        order.add(Either.ofRight(2));
        order.group()
            .add(Either.ofRight(3))
            .add(Either.ofRight(3))
            .close();
        order.check();
    }

    @Test
    public static void testRightFirst() {
        Order<Either<Integer, String>> order = new Order<>();
        order.add(Either.ofRight("a"));
        order.add(Either.ofRight("b"));
        order.group()
            .add(Either.ofRight("c"))
            .add(Either.ofRight("c"))
            .close();
        order.add(Either.ofLeft(1));
        order.add(Either.ofLeft(2));
        order.group()
            .add(Either.ofLeft(3))
            .add(Either.ofLeft(3))
            .close();
        order.check(Either.rightFirst());
        order.check(Either.rightFirst(Comparator.naturalOrder(),
                Comparator.naturalOrder()));
    }

    @Test
    public static void testLeftFirst() {
        Order<Either<Integer, String>> order = new Order<>();
        order.add(Either.ofLeft(1));
        order.add(Either.ofLeft(2));
        order.group()
            .add(Either.ofLeft(3))
            .add(Either.ofLeft(3))
            .close();
        order.add(Either.ofRight("a"));
        order.add(Either.ofRight("b"));
        order.group()
            .add(Either.ofRight("c"))
            .add(Either.ofRight("c"))
            .close();
        order.check(Either.leftFirst());
        order.check(Either.leftFirst(Comparator.naturalOrder(),
                Comparator.naturalOrder()));
    }

    // Alternative implementation of enyo.io.function.IOFunction.
    interface IOEitherFunction<T, U>
        extends Function<T, Either<U, IOException>> {
        U applyIO(T value) throws IOException;

        @Override
        default Either<U, IOException> apply(T value) {
            try {
                return Either.ofLeft(applyIO(value));
            } catch (IOException e) {
                return Either.ofRight(e);
            } catch (UncheckedIOException e) {
                return Either.ofRight(e.getCause());
            }
        }

        static <T, U> IOEitherFunction<T, U> of(IOEitherFunction<T, U> mapper) {
            return mapper;
        }
    }

    @Test
    public void testIOEitherFunction(Testing t) {
        IOException e1 = new IOException("E1");
        IOException e2 = new IOException("E2");
        UncheckedIOException e3 = new UncheckedIOException(e2);
        List<Either<Integer, IOException>> expected = List.of(
            Either.ofLeft(0),
            Either.ofRight(e1),
            Either.ofRight(e2),
            Either.ofRight(e2),
            Either.ofLeft(4),
            Either.ofLeft(5));
        List<Either<Integer, IOException>> actual = IntStream.range(10, 16)
            .boxed()
            .map(IOEitherFunction.of(i -> {
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
        t.expect(() -> Either.ofEnumOrdinal(null, 0),
            NullPointerException.class);
        t.expect(() -> Either.ofEnumOrdinal(null, 1),
            NullPointerException.class);
        t.expect(() -> Either.ofEnumOrdinal(null, -1),
            NullPointerException.class);
    }

    enum EmptyEnum { }

    @Test
    public static void testOfEnumOrdinalEmpty(Testing t) {
        IntStream.rangeClosed(-1, 1).forEach(i ->
            t.examine(i)
            .mapToObj(i1 -> Either.ofEnumOrdinal(EmptyEnum.class, i1))
            .is(Either.ofRight(i)));
    }

    enum OneEnum { E1 }

    @Test
    public static void testOfEnumOrdinalOne(Testing t) {
        t.examine(Either.ofEnumOrdinal(OneEnum.class, 0))
            .is(Either.ofLeft(OneEnum.E1));
        List.of(-1, 1).forEach(i ->
            t.examine(i)
            .map(i1 -> Either.ofEnumOrdinal(OneEnum.class, i1))
            .is(Either.ofRight(i)));
    }

    enum TwoEnum { E1, E2 }

    @Test
    public static void testOfEnumOrdinalTwo(Testing t) {
        t.examine(Either.ofEnumOrdinal(TwoEnum.class, 0))
            .is(Either.ofLeft(TwoEnum.E1));
        t.examine(Either.ofEnumOrdinal(TwoEnum.class, 1))
            .is(Either.ofLeft(TwoEnum.E2));
        List.of(-2, -1, 2, 3).forEach(i ->
            t.examine(i)
            .map(i1 -> Either.ofEnumOrdinal(TwoEnum.class, i1))
            .is(Either.ofRight(i)));
    }

    private static void snippet(Either<?, ?> e) {
        switch (e) {
        case Either.Left(var value) -> System.out.format("left: %s%n", value);
        case Either.Right(var value) -> System.out.format("right: %s%n", value);
        }
    }
}
