package enyo.build;

import enyo.testing.introspect.ScheduledTestSet;

/**
 * This class collects global state related to test execution.
 */
final class GlobalTestingState {
    /**
     * Sets up the testing state.  This is suppoed to happen once per
     * test run.
     * @see TestLinkConsumer(TestLink)
     */
    GlobalTestingState(DirectoryLayout dirs, TestLink.Parameters parameters) {
        this.directoryLayout = dirs;
        this.parameters = parameters;
        this.testLayer = RunTests.testLayer(dirs, parameters.testModules());
        this.startedTests = new ScheduledTestSet(parameters.testCount());
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

    private final DirectoryLayout directoryLayout;
    private final TestLink.Parameters parameters;
    private final ModuleLayer testLayer;
    private final ScheduledTestSet startedTests;
}
