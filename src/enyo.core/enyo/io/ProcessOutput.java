package enyo.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Capture standard output and standard error from a process.
 *
 * Subprocesses can interleave output on then standard output and
 * standard error streams.  Writes to pipes on these streams are not
 * necessarily ordered, and this can cause unintended interleaving.
 * Instances of this class relay data from both streams to a single
 * {@linkplain BlockingQueue FIFO queue}.
 *
 * <p>Interleaving in the middle of lines can still occur.
 * If this happens, {@link Data#isCompleteLine()} is false,
 * and {@linkplain Data#data() the bytes that constitute the line}
 * do not contain a new line character {@code '\n'}.  It is still
 * possible that the data item that completes the line is immediately
 * available from the queue, following an incomplete item.  The
 * queue consumer can try to detect this situation and merge the
 * lines.
 *
 * <p>Queue consumers need to count the number of data items received
 * for which {@link Data#isLastForStream()} returns true.  After two
 * such data items, no more data will be submitted to the queue.
 *
 * @see Process#getInputStream()
 * @see Process#getErrorStream()
 */
public final class ProcessOutput implements AutoCloseable {
    /**
     * Creates a new process output capturer.
     *
     * Capturing output begins immediately in the background.
     *
     * @param process the process whose standard output and standard error
     *   are captured
     * @param queue the queue to which data is submitted
     * @throws NullPointerException if any of the arguments is null
     */
    public ProcessOutput(Process process, BlockingQueue<? super Data> queue) {
        this(process.getInputStream(), process.getErrorStream(), queue);
    }

    /**
     * Creates a new standard output/standard errror capturer.
     *
     * Standard output and standard error are passed as separate streams.
     *
     * @param output the standard output stream of the process
     * @param error the standard error stream of the process
     * @param queue the queue to which data is submitted
     * @throws NullPointerException if any of the arguments is null
     * @see Process#getInputStream()
     * @see Process#getErrorStream()
     */
    public ProcessOutput(InputStream output, InputStream error,
        BlockingQueue<? super Data> queue) {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(error, "error");
        this.queue = Objects.requireNonNull(queue, "queue");
        outputThread = new CaptureThread(Source.OUTPUT, output);
        errorThread = new CaptureThread(Source.ERROR, error);
        outputThread.start();
        errorThread.start();
    }

    /**
     * Deallocates the internal resources.
     * Calling {@code close()} interrupts the threads that
     * feed data to the queue.
     */
    @Override
    public void close() {
        outputThread.interrupt();
        errorThread.interrupt();
        try {
            try {
                outputThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            try {
                errorThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        queue.clear();
    }

    /**
     * Type of the data source.
     */
    public enum Source {
        /**
         * Standard ouptut from the subprocess.
         * @see Process#getInputStream()
         */
        OUTPUT,
        /**
         * Standard error from the subprocess.
         * @see Process#getErrorStream()
         */
        ERROR,
    }

    /**
     * Data provided by a subprocess.
     *
     * {@link ProcessOutput} instances put such data items into the
     * specified {@linkplain BlockingQueue FIFO queue}.
     */
    public sealed interface Data permits DataImpl {
        /**
         * Returns the source stream of this data item.
         * @return indicates whether this data came from standard output
         *   or standard error
         */
        Source source();

        /**
         * Returns the receive time of this data item.
         *
         * This time is not intended for ordering purposes.
         * Millisecond resolution may be insufficient, and wall clock
         * time is not strictly monotonic.
         *
         * @return the time when the data was received, according to
         *   {@link System#currentTimeMillis()}
         */
        long timeMillis();

        /**
         * An exception that occurred while receiving data.
         * After an exception, no more data will arrive from the same
         * {@linkplain #source() source}.
         * @return the exception if encountered, or null
         */
        Throwable exception();

        /**
         * Returns received data.
         * @return null if no data is associated with this item (either
         *   an {@linkplain #exception() exception}, or
         *   {@linkplain #isLastForStream() end of stream}), or
         *   a non-empty array with the data types
         */
        byte[] data();

        /**
         * Returns true if this is the last data item for this
         * {@linkplain #source() source}.
         * @return true if no more data items for this source will follow
         */
        boolean isLastForStream();

        /**
         * Returns true if this captured data entry ends with a complete line.
         *
         * {@link ProcessOutput} creates data items in such a way that
         * if an item is incomplete (this method returns false), it
         * does not contain any newline characters at all.
         *
         * @return true if this entry ends with a completed line, false
         *   if the last line is partial, or if {@link #data()} is null
         */
        boolean isCompleteLine();
    }

    private record DataImpl(Source source, long timeMillis, Object obj)
        implements Data {
        @Override
        public byte[] data() {
            return obj instanceof byte[] b ? b : null;
        }

        @Override
        public Throwable exception() {
            return obj instanceof Throwable e ? e : null;
        }

        @Override
        public boolean isLastForStream() {
            return data() == null;
        }

        @Override
        public boolean isCompleteLine() {
            var data = data();
            return data != null && data.length > 0
                && data[data.length - 1] == '\n';
        }
    }

    private final class CaptureThread extends Thread {
        private final Source source;
        private final InputStream in;

        private CaptureThread(Source source, InputStream in) {
            this.source = source;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                run0();
            } catch (InterruptedException e) {
                // ProcessOutput has interrupted this thread.
            } catch (Throwable e) {
                // Signal error.
                try {
                    queue.put(new DataImpl(source, System.nanoTime(), e));
                } catch (InterruptedException e1) {
                    // Ignore the exception.  Only the owning ProcessOutput
                    // object is supposed to interrupted this thread, and it
                    // will not need a marker object in this case.
                }
            }
        }

        private void run0() throws IOException, InterruptedException {
            // The buffer size is fairly large here to avoid
            // unnecessary output/error interleaving.
            final byte[] buffer = new byte[16384];
            while (true) {
                // Read the data and record the receiving time as
                // soon as possible.
                int ret = in.read(buffer);
                long timeMillis = System.currentTimeMillis();

                if (ret <= 0) {
                    // Signal EOF.
                    queue.put(new DataImpl(source, timeMillis, null));
                    break;
                }

                // Here ret > 0.
                if (buffer[ret - 1] == '\n')
                    // Complete line(s), can send directly.
                    queue.put(new DataImpl(source, timeMillis,
                            Arrays.copyOf(buffer, ret)));
                else {
                    // Partial line.  Need to split at the last '\n'.
                    int lastLF = ret - 1;
                    while (lastLF >= 0)
                        if (buffer[lastLF] == '\n')
                            break;
                        else
                            --lastLF;
                    if (lastLF < 0)
                        // No line separator found.  Can send directly.
                        queue.put(new DataImpl(source, timeMillis,
                                Arrays.copyOf(buffer, ret)));
                    else {
                        // Need to split into complete line(s) and
                        // partial line.
                        queue.put(new DataImpl(source, timeMillis,
                                Arrays.copyOf(buffer, lastLF + 1)));
                        queue.put(new DataImpl(source, timeMillis,
                                Arrays.copyOfRange(buffer,
                                    lastLF + 1, ret)));
                    }
                }
            }
        }
    }

    private final CaptureThread outputThread;
    private final CaptureThread errorThread;
    private final BlockingQueue<? super Data> queue;
}
