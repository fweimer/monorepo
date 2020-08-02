package enyo.build;

import java.util.Objects;

/**
 * An individual test that has been scheduled for execution.
 */
final class ScheduledTest {
    ScheduledTest(int id, TestMethodLocator locator, TestResources resources) {
        this.id = id;
        this.locator = Objects.requireNonNull(locator, "locator");
        this.resources = Objects.requireNonNull(resources, "resources");
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

    /**
     * Projected resource usage of this test.
     */
    TestResources resources() {
        return resources;
    }

    private final int id;
    private final TestMethodLocator locator;
    private final TestResources resources;
}
