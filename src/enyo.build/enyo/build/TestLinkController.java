package enyo.build;

import enyo.testing.introspect.ScheduledTest;
import enyo.util.Result;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

/**
 * Interfacing with the test-link directory on the build daemon side.
 */
final class TestLinkController implements Closeable {
    /**
     * Sets up the {@code test-link} directory, so that tests can be
     * run in a separate VM via {@link RunTests}.
     */
    TestLinkController(DirectoryLayout dirs, List<String> modules,
                       SourceFileChanges.Waiter changeWaiter)
        throws IOException {
        this.dirs = dirs;

        FindTests findTests = new FindTests(dirs, modules);
        tests = findTests.tests.stream()
            .map(UnscheduledTest::scheduled)
            .collect(Collectors.toUnmodifiableList());
        errors = findTests.errors;
        testFiltersActive = findTests.testFiltersActive;

        parameters = new TestLink.Parameters(modules, tests.size());

        // Randomize (to benefit intra-test-class scheduling), and
        // then source by source file modification.
        ArrayList<UnscheduledTest> scratchList =
            new ArrayList<>(findTests.tests);
        Collections.shuffle(scratchList);
        Collections.sort(scratchList,
            UnscheduledTest.comparingSourceFileChange());

        boolean good = false;
        testLink = TestLink.forController(dirs);
        try {
            testLink.writeParameters(parameters);
            testLink.writeTests(QueueKind.MODIFICATION,
                scratchList.stream()
                .map(UnscheduledTest::scheduled));

            Collections.shuffle(scratchList);
            Collections.sort(scratchList,
                UnscheduledTest.comparingCost());
            testLink.writeTests(QueueKind.COST,
                scratchList.stream()
                .map(UnscheduledTest::scheduled));

            Collections.shuffle(scratchList);
            testLink.writeTests(QueueKind.RANDOM,
                scratchList.stream()
                .map(UnscheduledTest::scheduled));

            // The test-link directory has been set up.  TEST_STOP
            // remains acquired, until source changes are observed
            // below.
            testLink.lockArray().unlock(LockArray.Lock.MAIN);

            this.changeWaiter = changeWaiter;
            // Cancelling the changeWaiter terminates the waiting operation.
            ForkJoinPool.commonPool().submit(() -> {
                    changeWaiter.await();
                    testLink.sharedArray().set(
                        SharedArray.Slot.STOP_REQUESTED, 1);
                    testLink.lockArray().unlock(LockArray.Lock.TEST_STOP);
                    return null;
                });

            good = true;
        } finally {
            if (!good)
                testLink.close();
        }
    }

    @Override
    public void close() throws IOException {
        testLink.close();
    }

    /**
     * Returns the parameters computed as part of setting up the test
     * link.
     */
    TestLink.Parameters parameters() {
        return parameters;
    }

    /**
     * All scheduled tests.
     */
    List<ScheduledTest> tests() {
        return tests;
    }

    /**
     * All error messages.
     */
    List<String> errors() {
        return errors;
    }

    /**
     * Returns whether test filters are active.
     * @return true if any {@link enyo.testing.OnlyTests} annotations have
     *   been encountered, false otherwise
     */
    boolean testFiltersActive() {
        return testFiltersActive;
    }

    private final DirectoryLayout dirs;
    private final TestLink.Parameters parameters;
    private final TestLink testLink;
    private final List<ScheduledTest> tests;
    private final List<String> errors;
    private final boolean testFiltersActive;
    private final SourceFileChanges.Waiter changeWaiter;
}
