package enyo.build;

import java.util.List;

/**
 * Types of queues for scheduling tests.
 *
 * @see QueueReader
 * @see QueueProcessor
 */
enum QueueKind {
    // Order of the enum values matters: Later queues may receive
    // fewer processing threads due to rounding differences.

    /**
     * A queue ordered by modification time of the test source file.
     */
    MODIFICATION,

    /**
     * A queue with randomized order.
     */
    RANDOM;

    /**
     * Returns the queue file name, without the extension.
     * @return the base name of a file name
     */
    String queueName() {
        return toString().toLowerCase();
    }

    /**
     * Returns a list of proposed queues, one for each thread.
     * @param threads the number of available threads for parallel
     *   test execution
     * @return a queue kind for each thread
     * @throws IllegalArgumentException if threads is not positive
     */
    static List<QueueKind> proposedQueues(int threads) {
        if (threads <= 0)
            throw new IllegalArgumentException("threads: " + threads);

        int modificationCount;
        switch (threads) {
        case 1:
            modificationCount = 1;
            break;
        case 2:
        case 3:
        case 4:
            modificationCount = 2;
            break;
        case 5:
            modificationCount = 3;
            break;
        case 6:
            modificationCount = 4;
            break;
        default:
            modificationCount = threads / 2 + 2;
        }

        // The remaining threads execute tests randomly.
        int randomCount = threads - modificationCount;

        QueueKind[] result = new QueueKind[threads];
        int i = 0;
        for (int j = 0; j < modificationCount; ++j)
            result[i++] = MODIFICATION;
        for (int j = 0; j < randomCount; ++j)
            result[i++] = RANDOM;

        return List.of(result);
    }
}
