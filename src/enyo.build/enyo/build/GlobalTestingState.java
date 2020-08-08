package enyo.build;

import enyo.testing.introspect.ResourcesManager;
import enyo.testing.introspect.ScheduledTestSet;

import java.io.IOException;

/**
 * This class collects global state related to test execution.
 */
final class GlobalTestingState {
    /**
     * Sets up the testing state.  This is suppoed to happen once per
     * test run.
     * @see TestLinkConsumer(TestLink)
     */
    GlobalTestingState(TestLink testLink) throws IOException {
        this.directoryLayout = testLink.directoryLayout();
        this.parameters = testLink.readParameters();
        this.testLayer = RunTests.testLayer(
            directoryLayout, parameters.testModules());
        this.startedTests = new ScheduledTestSet(parameters.testCount());
        this.resources = new ResourcesManager();
        this.sharedArray = testLink.sharedArray();
    }

    ModuleLayer testLayer() {
        return testLayer;
    }

    /**
     * The set of tests that have begun executing.
     */
    ScheduledTestSet startedTests() {
        return startedTests;
    }

    /**
     * Returns the global resources manger.
     */
    ResourcesManager resources() {
        return resources;
    }

    /**
     * Checks whether testing should stop.
     *
     * @return false if testing should proceed, true if
     *   termination is requested
     */
    boolean stopRequested() {
        return sharedArray.get(SharedArray.Slot.STOP_REQUESTED) != 0;
    }

    /**
     * Called to signal the completion of one test.
     */
    void oneTestCompleted() {
        // FIXME: Not yet implemented.
    }

    private final DirectoryLayout directoryLayout;
    private final TestLink.Parameters parameters;
    private final ModuleLayer testLayer;
    private final ScheduledTestSet startedTests;
    private final ResourcesManager resources;
    private final SharedArray sharedArray;
}
