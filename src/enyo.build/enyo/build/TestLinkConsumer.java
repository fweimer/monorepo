package enyo.build;

import enyo.testing.introspect.ScheduledTest;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
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
    QueueReader testQueue(QueueKind kind) throws IOException {
        return new QueueReader(testLink.openTestsList(kind),
            state.startedTests());
    }

    Map<QueueKind, QueueReader> testQueues() throws IOException {
        EnumMap<QueueKind, QueueReader> result = new EnumMap<>(QueueKind.class);
        for (QueueKind kind : QueueKind.values())
            result.put(kind, testQueue(kind));
        return Map.copyOf(result);
    }

    // Mutable state should be added to GlobalTestingState, not here.
    private final TestLink testLink;
    private final TestLink.Parameters parameters;
    private final GlobalTestingState state;
}
