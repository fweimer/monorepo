package enyo.build;

import java.util.Objects;

/**
 * An individual test that has been scheduled for execution.
 */
final class ScheduledTest {
    ScheduledTest(int id, TestMethodLocator locator) {
        this.id = id;
        this.locator = Objects.requireNonNull(locator);
    }

    /**
     * An integer uniquely identifying this test among other tests
     * executed at the same time.
     */
    int id() {
        return id;
    }

    /**
     * Information on how to invoke this test.
     */
    TestMethodLocator locator() {
        return locator;
    }

    private final int id;
    private final TestMethodLocator locator;
}
