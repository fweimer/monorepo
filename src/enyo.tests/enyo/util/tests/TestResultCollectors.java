package benyo.util.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TestResultCollectors {
    @Test
    public static void testToCollection(Testing t) {
        List<Result<Integer, String>> input = List.of(
            Result.of(1),
            Result.ofError("one"),
            Result.of(2),
            Result.ofError("two"),
            Result.of(2),
            Result.of(3));
        t.assertEquals(
            new Result.Collected<>(List.of(1, 2, 2, 3), List.of("one", "two")),
            input.stream()
            .collect(Result.Collectors.toCollection(
                    ArrayList::new, ArrayList::new)));
        t.assertEquals(
            new Result.Collected<>(Set.of(1, 2, 3), List.of("one", "two")),
            input.stream()
            .collect(Result.Collectors.toCollection(
                    HashSet::new, ArrayList::new)));
    }
}
