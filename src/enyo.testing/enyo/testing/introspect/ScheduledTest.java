package enyo.testing.introspect;

import java.io.Serializable;
import java.util.Objects;

/**
 * An individual test that has been scheduled for execution.
 *
 * The {@linkplain #id() test ID} is used to identify this test during
 * the test run, without complicated hash table lookups.
 *
 * <p>Scheduled test objects are immutable.
 */
public final class ScheduledTest implements Serializable {
    /**
     * Creates a new scheduled test.
     * @param id the test ID
     * @param locator information on how to load the test
     * @param resources projected resource use of the test
     * @param exitDuringTest if the test harness may terminate the
     *   virtual machine while this test is running
     * @throws NullPointerException if {@code locator} or {@code resources}
     *   is null
     */
    public ScheduledTest(int id, TestMethodLocator locator,
        TestResources resources, boolean exitDuringTest) {
        this.id = id;
        this.locator = Objects.requireNonNull(locator, "locator");
        this.resources = Objects.requireNonNull(resources, "resources");
        this.exitDuringTest = exitDuringTest;
    }

    /**
     * Returns an integer uniquely identifying this test among other tests
     * executed at the same time.
     * @return the test ID, as passed to the constructor
     */
    public int id() {
        return id;
    }

    /**
     * Returns information on how to invoke this test.
     * @return the test method locator
     */
    public TestMethodLocator locator() {
        return locator;
    }

    /**
     * Returns the projected resource usage of this test.
     * @return the test resources
     */
    public TestResources resources() {
        return resources;
    }

    /**
     * Returns true if an {@link enyo.testing.ExitDuringTest} annotation
     * with a true value applies to this test.
     * @return whether the test harness may terminate the virtual machine
     *   while this test is running
     */
    public boolean exitDuringTest() {
        return exitDuringTest;
    }

    @Override
    public String toString() {
        return String.format("%s [%d] %s", locator, id, resources);
    }

    /**
     * Internal test number.
     * @see #id()
     */
    private final int id;

    /**
     * How to find the test method.
     * @see #locator()
     */
    private final TestMethodLocator locator;

    /**
     * Associated resources.
     * @see #resources()
     */
    private final TestResources resources;

    /**
     * Presence of the {@link enyo.testing.ExitDuringTest} annotation.
     * @see #exitDuringTest()
     */
    private final boolean exitDuringTest;

    private static final long serialVersionUID = 0x24941f066eba81e5L;
}
