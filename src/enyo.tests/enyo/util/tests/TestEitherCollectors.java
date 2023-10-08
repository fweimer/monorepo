package benyo.util.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.Either;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TestEitherCollectors {
    @Test
    public static void testToCollection(Testing t) {
        List<Either<Integer, String>> input = List.of(
            Either.ofLeft(1),
            Either.ofRight("one"),
            Either.ofLeft(2),
            Either.ofRight("two"),
            Either.ofLeft(2),
            Either.ofLeft(3));
        t.<Either.Collected<List<Integer>, List<String>>>examine(input.stream()
            .collect(Either.Collectors.toCollection(
                    ArrayList::new, ArrayList::new)))
            .is(new Either.Collected<>(List.of(1, 2, 2, 3),
                    List.of("one", "two")));
        t.<Either.Collected<Set<Integer>, List<String>>>examine(input.stream()
            .collect(Either.Collectors.toCollection(
                    HashSet::new, ArrayList::new)))
            .is(new Either.Collected<>(Set.of(1, 2, 3), List.of("one", "two")));

    }
}
