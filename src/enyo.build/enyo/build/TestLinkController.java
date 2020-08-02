package enyo.build;

import enyo.testing.introspect.ScheduledTest;
import enyo.util.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfacing with the test-link directory on the build daemon side.
 */
final class TestLinkController {
    /**
     * Sets up the {@code test-link} directory, so that tests can be
     * run in a separate VM via {@link RunTests}.
     */
    TestLinkController(DirectoryLayout dirs, List<String> modules)
        throws IOException {
        this.dirs = dirs;

        ArrayList<UnscheduledTest> scratchList;
        {
            Result.Collected<ArrayList<UnscheduledTest>, ArrayList<String>>
                collected = FindTests.collectedTests(dirs, modules);
            scratchList = collected.values();
            tests = scratchList.stream()
                .map(UnscheduledTest::scheduled)
                .collect(Collectors.toUnmodifiableList());
            errors = List.copyOf(collected.errors());
        }
        parameters = new TestLink.Parameters(modules, tests.size());

        // Randomize (to benefit intra-test-class scheduling), and
        // then source by source file modification.
        Collections.shuffle(scratchList);
        Collections.sort(scratchList,
            UnscheduledTest.comparingSourceFileChange());

        TestLink testLink = new TestLink(dirs);
        TestLinkLock lock = testLink.prepareAndLock();
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

        } finally {
            lock.release();
        }
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

    private final DirectoryLayout dirs;
    private final TestLink.Parameters parameters;
    private final List<ScheduledTest> tests;
    private final List<String> errors;
}
