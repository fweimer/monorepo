package enyo.build;

import enyo.testing.introspect.ScheduledTest;

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
     * Returns a reader for the test queue.
     * The caller should close the queue.
     */
    QueueReader testQueue() throws IOException {
        return new QueueReader(testLink.openTestsList("main"),
            state.startedTests());
    }

    // Mutable state should be added to GlobalTestingState, not here.
    private final TestLink testLink;
    private final TestLink.Parameters parameters;
    private final GlobalTestingState state;
}
