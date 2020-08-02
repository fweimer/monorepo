package enyo.build;

import enyo.testing.introspect.ScheduledTest;

import java.util.Comparator;
import java.util.Objects;

/**
 * Test case with associated scheduling information.
 */
final class UnscheduledTest {
    public UnscheduledTest(ScheduledTest scheduled,
        long sourceFileChange, double cost) {
        this.scheduled = Objects.requireNonNull(scheduled);
        this.sourceFileChange = sourceFileChange;
        this.cost = cost;
    }

    public ScheduledTest scheduled() {
        return scheduled;
    }

    /**
     * An indicator for the last source file change.
     */
    public long sourceFileChange() {
        return sourceFileChange;
    }

    /**
     * Return an estimate for the cost of running this test.
     */
    public double cost() {
        return cost;
    }

    @Override
    public String toString() {
        return scheduled.toString() + " sourceFileChange=" + sourceFileChange;
    }

    /**
     * Returns a comparator sorting most recently modified source
     * files first.
     */
    public static Comparator<UnscheduledTest> comparingSourceFileChange() {
        return SOURCE_FILE_CHANGE;
    }

    /**
     * Returns a comparator sorting by descending cost.
     */
    public static Comparator<UnscheduledTest> comparingCost() {
        return COST;
    }

    private final ScheduledTest scheduled;
    private final long sourceFileChange;
    private final double cost;

    private static final Comparator<UnscheduledTest> SOURCE_FILE_CHANGE =
        Comparator.comparing(test -> -test.sourceFileChange());
    private static final Comparator<UnscheduledTest> COST =
        Comparator.comparing(test -> -test.cost());
}
