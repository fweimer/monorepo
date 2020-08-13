package enyo.build;

import enyo.testing.introspect.ResourcesManager;
import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.ScheduledTestSet;
import enyo.testing.runtime.Testing;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class collects global state related to test execution.
 */
final class GlobalTestingState {
    /**
     * Sets up the testing state.  This is supposed to happen once per
     * test run.  Some changes to the virtual machine can be
     * destructive to it (such as exiting because the test controller
     * has issued a stop request).
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
        this.results = new TestResults();

        // Launch the daemon thread that waits for test termination
        // requests.
        new ExitMonitorThread(this, testLink.lockArray()).start();
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
     * Creates a new testing instance for a scheduled test.
     */
    Testing newTesting(ScheduledTest test) {
        return new Testing(test.locator(), results);
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
     * Adds another running test that acts as an exit inhibitor.
     * The caller must check that a {@linkplain #stopRequested() stop request}
     * has not arrived after this method has returned and before the
     * test actually starts. Another thread may be in the process
     * of acting on the stop request, so execution of an exit-inhibiting
     * test may not commence.
     *
     * <p>After the test completes, {@link #removeExitInhibitor()}
     * must be called.
     */
    void addExitInhibitor() {
        if (exitInhibitors.incrementAndGet() <= 0)
            throw new InternalError("Inhibitor count overflow");
    }

    /**
     * Undoes the effect of a previous {@link #addExitInhibitor()} call.
     * This may terminate the virtual machine if this test
     * was the last running exit-inhibiting test, and there is a
     * pending {@linkplain #stopRequested() stop request}.
     */
    void removeExitInhibitor() {
        int newCount = exitInhibitors.decrementAndGet();
        if (newCount < 0)
            throw new InternalError("Inhibitor count overflow");
        if (newCount == 0 && stopRequested())
            RunTests.exitDuringTest();
    }

    /**
     * Check for an exit requests that can be acted upon.
     * If there is a pending stop request eand there are exit inhibitors,
     * the stop request has to be ignored for time being.
     * @return whether there is a pending stop request (which had to be
     *   ignored due to exit inhibitors)
     * @see #stopRequested()
     * @see #addExitInhibitor()
     */
    boolean probeForExit() {
        if (stopRequested()) {
            if (exitInhibitors.get() == 0)
                RunTests.exitDuringTest();
            return true;
        } else
            return false;
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
    private final TestResults results;
    private final AtomicInteger exitInhibitors = new AtomicInteger();
}
