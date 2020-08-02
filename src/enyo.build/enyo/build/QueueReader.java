package enyo.build;

import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.ScheduledTestSet;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reader for a serialized list of test sequences.
 *
 * The methods of this class are thread-safe.
 *
 * @see ExecutionFilter
 * @see TestSequence
 */
final class QueueReader implements Closeable {
    /**
     * Creates a new queue reader.  The reader returns test sequences
     * stored in the specified file, but only if the filter indicates
     * that the test has not yet been returned for execution.
     *
     * @param file the path of the file that contains the test sequences
     * @param filter the execution filter that ensures that tests only
     *   run once
     * @throws IOException if the file cannot be opened
     * @throws NullPointerException if any argument is null
     */
    public QueueReader(DataInputStream in, ScheduledTestSet startedTests) {
        this.in = Objects.requireNonNull(in, "in");
        this.startedTests = Objects.requireNonNull(
            startedTests, "startedTests");
    }

    /**
     * Closes the underlying file.
     *
     * @throws IOException if an I/O error occurred
     */
    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            in.close();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reads the next object from the queue.  If the sequence contains
     * another element, it is returned, wrapped in an optional value.
     *
     * @return the next queue element, if available
     * @throws IOException if a read error occurred
     */
    public Optional<ScheduledTest> read() throws IOException {
        while (true) {
            Optional<ScheduledTest> obj;
            lock.lock();
            try {
                if (eof)
                    return Optional.empty();
                obj = TestLink.readOneTest(in);
                if (!obj.isPresent()) {
                    // End of stream.
                    eof = true;
                    return obj;
                }
            } finally {
                lock.unlock();
            }
            if (startedTests.add(obj.orElseThrow()))
                return obj;
            // Test was already run.  Get another test.
        }
    }

    private final DataInputStream in;
    private final ScheduledTestSet startedTests;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean eof;
}
