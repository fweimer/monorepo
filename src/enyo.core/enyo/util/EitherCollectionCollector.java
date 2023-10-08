package enyo.util;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Helper class for implementing {@link Either} collectors.
 */
final class EitherCollectionCollector
    <L, R, CL extends Collection<L>, CR extends Collection<R>>
    implements Collector<Either<L, R>, Either.Collected<CL, CR>,
                             Either.Collected<CL, CR>> {
    private final Supplier<CL> leftFactory;
    private final Supplier<CR> rightFactory;

    private static final Set<Characteristics> CHARACTERISTICS =
        Set.of(Characteristics.IDENTITY_FINISH);

    EitherCollectionCollector(Supplier<CL> leftFactory,
        Supplier<CR> rightFactory) {
        this.leftFactory = leftFactory;
        this.rightFactory = rightFactory;
    }

    @Override
    public BiConsumer<Either.Collected<CL, CR>, Either<L, R>> accumulator() {
        return (mut, e) -> {
            switch (e) {
            case Either.Left(var v) -> mut.leftValues().add(v);
            case Either.Right(var v) -> mut.rightValues().add(v);
            };
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    @Override
    public BinaryOperator<Either.Collected<CL, CR>> combiner() {
        return (left, right) -> {
            left.leftValues().addAll(right.leftValues());
            left.rightValues().addAll(right.rightValues());
            return left;
        };
    }

    @Override
    public Function<Either.Collected<CL, CR>, Either.Collected<CL, CR>>
        finisher() {
        return Function.identity();
    }

    @Override
    public Supplier<Either.Collected<CL, CR>> supplier() {
        return () ->
            new Either.Collected<>(leftFactory.get(), rightFactory.get());
    }
}
