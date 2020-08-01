package enyo.build;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Interfacing with the test-link directory on the test VM side.
 */
final class TestLinkConsumer {
    /**
     * Reads the parameters file and sets up the global state.
     * @see RunTests#runInModularMode(String[])
     */
    TestLinkConsumer(TestLink testLink) throws IOException {
        this.testLink = testLink;
        this.parameters = testLink.readParameters();
        this.state = new GlobalTestingState(
            testLink.directoryLayout(), parameters);
    }

    /**
     * The testing parameters, as read by the constructor.
     */
    TestLink.Parameters parameters() {
        return parameters;
    }

    /**
     * The global testing state, as created by the constructor.
     */
    GlobalTestingState state() {
        return state;
    }

    /**
     * Invoke the callback for each test in the named list of tests.
     * The list is read from the {@code test-link} subdirectory.
     */
    void iterateTestsList(String listName, Consumer<ScheduledTest> tester)
        throws IOException {
        try (DataInputStream in = testLink.openTestsList(listName)) {
            while (true) {
                Optional<ScheduledTest> test = TestLink.readOneTest(in);
                if (!test.isPresent())
                    break;
                tester.accept(test.get());
            }
        }
    }

    // Mutable state should be added to GlobalTestingState, not here.
    private final TestLink testLink;
    private final TestLink.Parameters parameters;
    private final GlobalTestingState state;
}
