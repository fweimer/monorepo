package enyo.util;

import java.util.Comparator;

/**
 * Helper class for implementing {@link Either} comparators.
 */
final class EitherNaturalOrder
    <L extends Comparable<? super L>, R extends Comparable<? super R>>
    implements Comparator<Either<L, R>> {
    @Override
    public int compare(Either<L, R> left, Either<L, R> right) {
        return switch (left) {
        case Either.Left(L a) ->
            switch (right) {
            case Either.Left(L b) -> a.compareTo(b);
            case Either.Right(R b) -> leftAndRight;
            };
        case Either.Right(var a) ->
            switch (right) {
            case Either.Left(var b) -> -leftAndRight;
            case Either.Right(var b) -> a.compareTo(b);
            };
        };
    }

    private final int leftAndRight;

    private EitherNaturalOrder(int leftAndRight) {
        this.leftAndRight = leftAndRight;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final EitherNaturalOrder LEFT_FIRST = new EitherNaturalOrder(-1);
    @SuppressWarnings({"unchecked", "rawtypes"})
    static final EitherNaturalOrder RIGHT_FIRST = new EitherNaturalOrder(1);

    static <L, R> Comparator<Either<L, R>> comparator(
        Comparator<? super L> leftComparator,
        Comparator<? super R> rightComparator,
        int leftAndRight) {
        return (left, right) ->
            switch (left) {
            case Either.Left(var a) ->
                switch (right) {
                case Either.Left(var b) -> leftComparator.compare(a, b);
                case Either.Right(var b) -> leftAndRight;
                };
            case Either.Right(var a) ->
                switch (right) {
                case Either.Left(var b) -> -leftAndRight;
                case Either.Right(var b) -> rightComparator.compare(a, b);
                };
            };
    }
}
