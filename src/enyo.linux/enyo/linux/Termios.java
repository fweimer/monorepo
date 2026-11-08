package enyo.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;
import java.util.List;

/**
 * Terminal interface.
 */
public final class Termios {
    private Termios () {} // No instances.

    /**
     * Returns the layout of {@code struct termios2}.
     * On most architectures, Linux supports two structs, and this
     * function returns the newer variant, except on POWER, where
     * {@code struct termios} is used (which is equivalent to the newer
     * {@code struct termios2}).
     * @return a struct layout
     */
    public static StructLayout structTermios() {
        return Struct.TERMIOS;
    }

    /**
     * Returns true if the descriptor is a terminal.
     * @param fd the raw file descriptor
     * @return true if a terminal, false if not
     * @see Descriptor#isatty()
     */
    public static boolean isatty(int fd) {
        try (var arena = Arena.ofConfined()) {
            int tcgets = IoctlConstants.TCGETS2;
            if (tcgets == 0)
                tcgets = IoctlConstants.TCGETS;
            return Ioctl.ioctl(fd, IoctlConstants.TCGETS,
                arena.allocate(Termios.structTermios())) == 0;
        }
    }

    /**
     * Unlocks the service end of this pseudo-terminal.
     * @param fd the raw file descriptor
     * @see Descriptor#unlockpt()
     */
    public static void unlockpt(int fd) {
        try (var arena = Arena.ofConfined()) {
            var num = arena.allocate(ValueLayout.JAVA_INT);
            long result = Ioctl.ioctl(fd, IoctlConstants.TIOCSPTLCK, num);
            if (result != 0)
                throw Ioctl.newException(
                    fd, IoctlConstants.TIOCSPTLCK, num, result);
        }
    }

    /**
     * Returns the path to the service side of a pseudo-terminal multiplexer.
     * @param fd the raw file descriptor
     * @return a path under {@code /dev/pts/}.
     * @throws SyscallException if the descriptor does not refer to a
     *   pseudo-terminal multiplexer
     * @see Descriptor#ptsname()
     */
    public static Path ptsname(int fd) {
        try (var arena = Arena.ofConfined()) {
            var num = arena.allocate(ValueLayout.JAVA_INT);
            long result = Ioctl.ioctl(fd, IoctlConstants.TIOCGPTN, num);
            if (result != 0)
                throw Ioctl.newException(
                    fd, IoctlConstants.TIOCGPTN, num, result);
            return Path.of("/dev/pts/" + num.get(ValueLayout.JAVA_INT, 0));
        }
    }

    /**
     * Returns an open file descriptor for the service end of a pseudo-terminal.
     * @param fd the raw file descriptor of the terminal multiplexer
     * @param flags the file descriptor flags to be passed to the kernel;
     *   {@code O_CLOEXEC} is implied
     * @return an open file descriptor
     * @throws SyscallException if the descriptor does not refer to a
     *   pseudo-terminal multiplexer
     * @see Descriptor#ptsname()
     */
    public static int openpty(int fd, int flags) {
        flags |= Fcntl.O_CLOEXEC;
        long result = Ioctl.ioctlUnchecked(
            fd, IoctlConstants.TIOCGPTPEER, flags);
        if (result < 0)
            throw Ioctl.newException(
                fd, IoctlConstants.TIOCGPTPEER, flags, result);
        return (int) result;
    }

    /**
     * The terminal window size.
     * @param row the number of terminal rows
     * @param col the number of terminal columns
     * @param xpixel the number of pixels in the horizontal direction
     * @param ypixel the number of pixels in the vertical direction
     * @see Descriptor#tcgetwinsize()
     * @see Descriptor#tcsetwinsize(Winsize)
     * @see Termios#tcgetwinsize(int)
     * @see Termios#tcsetwinsize(int,Winsize)
     */
    public record Winsize(int row, int col, int xpixel, int ypixel) {
        /**
         * Creates a new terminal window size record.
         * @throws IllegalArgumentException if any dimension does not
         *   fall within 0 to 65535 (inclusive)
         */
        public Winsize {
            if ((row | col | xpixel | ypixel) < 0
                || (row | col | xpixel | ypixel) >= 65536)
                throw new IllegalArgumentException(String.format(
                        "Invalid window size (%d, %d, %d, %d)",
                        row, col, xpixel, ypixel));
        }
    }

    /**
     * Returns the terminal size for a given descriptor.
     * @param fd the raw file descriptor
     * @return the window size
     * @throws SyscallException if the descriptor does not refer to a terminal
     * @see Descriptor#tcgetwinsize()
     */
    public static Winsize tcgetwinsize(int fd) {
        try (var arena = Arena.ofConfined()) {
            var buf = arena.allocate(ValueLayout.JAVA_SHORT, 4);
            long result = Ioctl.ioctl(fd, IoctlConstants.TIOCGWINSZ, buf);
            if (result != 0)
                throw Ioctl.newException(
                    fd, IoctlConstants.TIOCGWINSZ, buf, result);
            return new Winsize(buf.get(ValueLayout.JAVA_SHORT, 0) & 0xffff,
                buf.get(ValueLayout.JAVA_SHORT, 2) & 0xffff,
                buf.get(ValueLayout.JAVA_SHORT, 4) & 0xffff,
                buf.get(ValueLayout.JAVA_SHORT, 6) & 0xffff);
        }
    }

    /**
     * Set the terminal size for a given descriptor.
     * @param fd the raw file descriptor
     * @param winsize the new terminal size
     * @throws SyscallException if the descriptor does not refer to a terminal
     * @see Descriptor#tcsetwinsize(Winsize)
     */
    public static void tcsetwinsize(int fd, Winsize winsize) {
        try (var arena = Arena.ofConfined()) {
            var buf = arena.allocate(ValueLayout.JAVA_SHORT, 4);
            buf.set(ValueLayout.JAVA_SHORT, 0, (short) winsize.row());
            buf.set(ValueLayout.JAVA_SHORT, 2, (short) winsize.col());
            buf.set(ValueLayout.JAVA_SHORT, 4, (short) winsize.xpixel());
            buf.set(ValueLayout.JAVA_SHORT, 6, (short) winsize.ypixel());

            long result = Ioctl.ioctl(fd, IoctlConstants.TIOCSWINSZ, buf);
            if (result != 0)
                throw Ioctl.newException(
                    fd, IoctlConstants.TIOCSWINSZ, buf, result);
        }
    }

    private final static class Struct {
        static final StructLayout TERMIOS;
        static {
            var tcflag_t = ValueLayout.JAVA_INT; // Actually unsigned int.
            var cc_t = ValueLayout.JAVA_BYTE;    // Actually unsigned char.
            var speed_t = ValueLayout.JAVA_INT; // Actually unsigned int.

            TERMIOS = MemoryLayout.structLayout(
                tcflag_t.withName("c_iflag"),
                tcflag_t.withName("c_oflag"),
                tcflag_t.withName("c_cflag"),
                tcflag_t.withName("c_lflag"),
                cc_t.withName("c_line"),
                MemoryLayout.sequenceLayout(IoctlConstants.NCCS, cc_t)
                .withName("c_cc"),
                speed_t.withName("c_ispeed)"),
                speed_t.withName("c_ospeed"));
        }
    }
}
