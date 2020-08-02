package enyo.build;

import enyo.testing.introspect.ScheduledTest;

import java.util.Comparator;
import java.util.Objects;

/**
 * Test case with associated scheduling information.
 */
final class UnscheduledTest {
    public UnscheduledTest(ScheduledTest scheduled,
        long sourceFileChange) {
        this.scheduled = Objects.requireNonNull(scheduled);
        this.sourceFileChange = sourceFileChange;
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

    private final ScheduledTest scheduled;
    private final long sourceFileChange;

    private static final Comparator<UnscheduledTest> SOURCE_FILE_CHANGE =
        Comparator.comparing(test -> -test.sourceFileChange());
}
