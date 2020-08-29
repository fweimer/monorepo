package enyo.io.terminfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Translating {@code $<...>} directives while writing to a terminal
 * target.
 *
 * A terminal writer receives a byte stream, scans it for {@code $<}
 * directives, and invokes the {@link TerminalTarget#wait(int)} method
 * as necessary.
 */
public final class TerminalWriter {
    /**
     * Creates a new writer for the specified terminal target.
     * @param target the terminal target to which bytes are written
     * @param term the terminal description used to determine terminal
     *   capabilities
     */
    public TerminalWriter(TerminalTarget target, TermFile term) {
        xon = term.get(BooleanCapability.XON_XOFF);
        this.target = Objects.requireNonNull(target, "target");
    }

    /**
     * Sets the size of the current operation.  This is the scale
     * factor used when the directive contains a {@code *} flag.
     * The scale factor is initially 1.
     * @param value the new scale factor
     * @throws IllegalArgumentException if the argument is not positive
     * @see #scale()
     */
    public void scale(int value) {
        if (value < 1)
            throw new IllegalArgumentException();
        scale = value;
    }

    /**
     * Returns the current scale factor.
     * @return the scale factor, a positive integer
     * @see #scale(int)
     */
    public int scale() {
        return scale;
    }

    /**
     * Writes a byte to the terminal that is part of a string capability.
     * {@code $<...>} sequences are automatically interpreted, and if
     * necessary, {@link TerminalTarget#wait(int)} is called.
     * @param b the byte to process
     * @throws UncheckedIOException an output error occured,
     *   including a malformed directive
     */
    public void write(int b) {
        b = (byte) b;
        try {
            switch (state) {
            case 0:
                if (b == '$') {
                    state = 1;
                    waitValue = 0;
                    forceFlag = false;
                    scaleFlag = false;
                } else
                    target.write(b);
                break;
            case 1:
                if (b == '<')
                    state = 2;
                else {
                    target.write('$');
                    target.write(b);
                    state = 0;
                }
                break;
            case 2:
                switch (b) {
                case '>':
                    if (scaleFlag)
                        waitValue = Math.multiplyExact(scale, waitValue);
                    if (!xon || forceFlag)
                        target.wait(waitValue);
                    state = 0;
                    break;
                case '/':
                    forceFlag = true;
                    // Keep in state 2.
                    break;
                case '*':
                    scaleFlag = true;
                    // Keep in state 2.
                    break;
                default:
                    if (b < '0' || b > '9' || forceFlag || scaleFlag)
                        throw new UncheckedIOException(
                            new IOException(
                                "Malformed $<...> terminal directive"));
                    waitValue = Math.addExact(
                        Math.multiplyExact(waitValue, 10),
                        b - '0');
                    break;
                }
            }
        } catch (ArithmeticException e) {
            throw new UncheckedIOException(
                new IOException(
                    "Integer overflow in $<...> terminal directive", e));
        }
    }

    /**
     * Syncs this terminal writer.
     * This method should be called after a string capability has
     * been render to the writer using the {@link #write(int)} method.
     * It ensures that the capability is properly terminated even if
     * its description ends with a {@code $} character.
     * @throws IllegalStateException if this writer is in the middle
     *   of a {@code $<...>} diretive
     * @throws UncheckedIOException an output error occured
     */
    public void sync() {
        switch (state) {
        case 0:
            break;
        case 1:
            target.write('$');
            state = 0;
            break;
        default:
            throw new IllegalStateException(
                "Cannot sync in the middle of a $<...> directive");
        }
    }

    /**
     * Syncs and flushes terminal output.  Calls the {@link #sync()}
     * and {@link TerminalTarget#flush()} methods.
     * @throws IllegalStateException if this writer is in the middle
     *   of a {@code $<...>} diretive
     * @throws UncheckedIOException an output error occured
     */
    public void flush() {
        sync();
        target.flush();
    }

    private final boolean xon;
    private final TerminalTarget target;
    private int scale = 1;
    private byte state;
    private int waitValue;
    private boolean forceFlag;
    private boolean scaleFlag;
}
