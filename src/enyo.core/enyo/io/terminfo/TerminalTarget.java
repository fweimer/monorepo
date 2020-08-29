package enyo.io.terminfo;

import java.io.InterruptedIOException;
import java.io.UncheckedIOException;

/**
 * A target for terminal write operations.
 *
 * Some terminals need waiting operations for correct operation,
 * which is why this interface provides a {@link #wait(int)} method.
 */
public interface TerminalTarget {
    /**
     * Writes a byte to the terminal.
     * Writing may be delayed until {@link #flush()} is called.
     * @param b the byte to write; only the lower 8 bits are used
     * @throws UncheckedIOException an output error occurred
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b);

    /**
     * Flushes pending data to the terminal.
     * @throws java.io.UncheckedIOException an output error occurred
    * @see java.io.OutputStream#flush()
     */
    public void flush();

    /**
     * Waits for the specified time.
     * It is expected that the caller has already invoked {@link #flush()}.
     * The default implementation calls {@link Thread#sleep(long)}.
     * @param millis the waiting time, in milliseconds
     * @throws UncheckedIOException if the thread was interrupted
     */
    public default void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new UncheckedIOException(new InterruptedIOException());
        }
    }
}
