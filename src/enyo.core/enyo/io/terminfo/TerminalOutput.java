package enyo.io.terminfo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Access to a terminal connect to the current process.
 *
 * If no actual terminal can be obtained, {@link System#err}
 * is used as a fallback.  In this case, the terminal returned by
 * {@link #terminal()} has no capabilities.
 */
public final class TerminalOutput extends OutputStream
    implements TerminalTarget {
    /**
     * Creates a new terminal object.  The implementation attempts to
     * use {@code /dev/tty} to obtain the terminal.  If that fails,
     * all writes will be targeted at {@link System#err} instead (and
     * the current variable of the variable at each write is used).
     * If a terminal is present, the terminal description is obtained
     * via the {@code TERM} environment variable and the system
     * terminfo database.
     * @see TermFile#current()
     */
    public TerminalOutput() {
        OutputStream tty;
        try {
            tty = new FileOutputStream("/dev/tty");
        } catch (FileNotFoundException e) {
            tty = null;
        }
        out = tty;
        if (tty != null)
            terminal = TermFile.current();
        else
            terminal = TermFile.of();
    }

    /**
     * Returns the description of the terminal associated with this
     * object.
     * @return a terminal description
     */
    public TermFile terminal() {
        return terminal;
    }

    @Override
    public void write(int b) {
        if (out != null)
            try {
                out.write(b);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        else
            System.err.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (out != null)
            out.write(b);
        else
            System.err.write(b);
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        if (out != null)
            out.write(b, offset, length);
        else
            System.err.write(b, offset, length);
    }

    @Override
    public void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (out != null)
            out.close();
    }

    private final OutputStream out;
    private final TermFile terminal;
}
