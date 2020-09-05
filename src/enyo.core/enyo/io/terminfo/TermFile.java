package enyo.io.terminfo;

import enyo.io.ExtendedCharsets;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data from a terminal description file containing compiled terminfo data.
 *
 * Objects of this class are immutable.
 */
public final class TermFile {
    /**
     * Creates a new terminal description based on its compiled
     * representation.
     *
     * @param data a compiled terminal description
     * @return the parsed terminal description
     * @throws TermFileException if the terminal description is malformed
     * @throws NullPointerException if {@code data} is null
     */
    public static TermFile of(byte[] data) {
        data = data.clone();
        ByteBuffer b = ByteBuffer.wrap(data)
            .order(ByteOrder.LITTLE_ENDIAN);
        if (b.remaining() < 12)
            throw new TermFileException("Size too small: " + data.length);

        int magic = fetchHeaderShort(b, "magic");
        if (magic != 0432 && magic != EXTENDED_MAGIC)
            throw new TermFileException(
                "Invalid magic number: " + magic);

        int nameSize = fetchHeaderShort(b, "names section size");
        int booleanSize = fetchHeaderShort(b, "boolean section size");
        int numbersCount = fetchHeaderShort(b, "numbers section count");
        int offsetsCount = fetchHeaderShort(b, "strings section count");
        int stringTableSize = fetchHeaderShort(b, "string table size");

        ByteBuffer namesBuffer;
        long booleans;
        int[] numbers;
        int[] stringTable;
        ByteBuffer stringsBuffer;
        try {
            namesBuffer = sliceBuffer(b, nameSize);
            booleans = readBooleans(b, booleanSize);
            numbers = readNumbers(b, numbersCount, magic);
            stringTable = readStringTable(b, offsetsCount);
            stringsBuffer = sliceBuffer(b, stringTableSize);
        } catch (BufferUnderflowException e) {
            throw new TermFileException("Data too short: " + data.length, e);
        }

        // Extended capabilities come here, but are presently ignored.

        List<String> names = parseNames(namesBuffer);
        byte[][] strings = parseStrings(stringTable, stringsBuffer);

        return new TermFile(names, booleans, numbers, strings);
    }

    /**
     * Return a terminal object with no names and no capabilities.
     *
     * @return a terminal object
     */
    public static TermFile of() {
        return EMPTY;
    }

    /**
     * Returns the description of the current terminal.  The name of
     * the terminal is determined based on the {@code TERM}
     * environment variable, and its description is loaded from the
     * system terminfo database.
     *
     * @return a terminal description, possibly without capabilities
     *  if the terminal or its description cannot be identified
     */
    public static TermFile current() {
        String term = System.getenv("TERM");
        if (term != null)
            try  {
                return load(term);
            } catch (IllegalArgumentException | UnknownTerminalException e) {
                return of();
            }
        else
            return of();
    }

    /**
     * Load a terminal description for a named terminal.
     * Both the user and system terminal databases are used to locate
     * the terminal.
     * @param name the name of the terminal
     * @return a terminal description
     * @throws IllegalArgumentException if {@code name} is not a
     *  syntactically valid terminal name
     * @throws UnknownTerminalException if the terminal {@code name}
     *   cannot be found
     * @throws TermFileException if the terminal database contains a
     *   malformed terminal description
     * @throws NullPointerException if {@code name} is null
     */
    public static TermFile load(String name) {
        if (name.isEmpty())
            throw new IllegalArgumentException("Empty terminal name");
        if (name.length() < 2
            || name.indexOf('/') >= 0
            || name.indexOf('\\') >= 0
            || name.startsWith("."))
            throw new IllegalArgumentException(
                "Invalid terminal name: " + name);

        String subdirectory = name.substring(0, 1);
        for (Path base : terminfoDirectories()) {
            TermFile termFile = tryOpen(
                base.resolve(subdirectory).resolve(name));
                if (termFile != null)
                    return termFile;
        }
        throw new UnknownTerminalException(name);
    }

    /**
     * Returns a list of directories which contain terminfo databases.
     * The directories in the list need not exist.
     *
     * @return an unmodifiable list
     */
    public static List<Path> terminfoDirectories() {
        List<Path> result = terminfoDirectories;
        if (result != null)
            return result;

        Path p1 = Path.of("/etc/terminfo");
        Path p2 = Path.of("/lib/terminfo");
        Path p3 = Path.of("/usr/share/terminfo");

        String homeDirString = System.getProperty("user.home");
        if (homeDirString != null)
            result = List.of(
                Path.of(homeDirString, ".terminfo"), p1, p2, p3);
        else
            result = List.of(p1, p2, p3);
        terminfoDirectories = result;
        return result;
    }

    /**
     * Returns true if the file descriptor refers to a terminal.
     *
     * @param fd the file descriptor to check
     * @return true if {@code fd} denotes a terminal, false if not
     *   or if this cannot be determined.
     * @throws NullPointerException of {@code fd} is null
     */
    public static boolean isTerminal(FileDescriptor fd) {
        Objects.requireNonNull(fd);
        int fileno;
        if (fd == FileDescriptor.in)
            fileno = 0;
        else if (fd == FileDescriptor.out)
            fileno = 1;
        else if (fd == FileDescriptor.err)
            fileno = 2;
        else
            // We cannot otherwise obtain the file descriptor to look
            // for.
            return false;

        // Determine the file identity of the descriptor.
        Object fileKey;
        {
            Path procfd = Path.of("/proc/self/fd/" + fileno);
            try {
                fileKey = Files.readAttributes(
                    procfd, BasicFileAttributes.class).fileKey();
            } catch (IOException e) {
                return false;
            }
            if (fileKey == null)
                return false;
        }

        // Try to find the same file among the terminals.
        try (DirectoryStream<Path> dir =
            Files.newDirectoryStream(Path.of("/dev/pts"))) {
            for (Path path : dir) {
                Object candidate = Files.readAttributes(
                    path, BasicFileAttributes.class).fileKey();
                if (fileKey.equals(candidate))
                    return true;
            }
        } catch (IOException e) {
            // Ignore, try further.
        }

        // Try again, this time for the entirety of /dev.
        try (DirectoryStream<Path> dir =
            Files.newDirectoryStream(Path.of("/dev"))) {
            for (Path path : dir)
                if (path.getFileName().startsWith("tty")) {
                    Object candidate = Files.readAttributes(
                        path, BasicFileAttributes.class).fileKey();
                    if (fileKey.equals(candidate))
                        return true;
                }
            // Nothing found.
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the names associated with this terminal.
     *
     * @return a list of names, possibly empty
     */
    public List<String> names() {
        return names;
    }

    /**
     * Returns the value of the specified boolean capability.
     * Missing capabilities are reported as false.
     *
     * @param cap the capability to return
     * @return the capability flag
     */
    public boolean get(BooleanCapability cap) {
        return (booleans & (1L << cap.ordinal())) != 0;
    }

    /**
     * Returns the numeric value of the specified capability.
     *
     * @param cap the capability to return
     * @return the non-negative capability value, or -1 if the
     *   capability is missing
     */
    public int get(NumericCapability cap) {
        int index = cap.ordinal();
        if (index < numbers.length)
            return numbers[index];
        else
            return -1;
    }

    /**
     * Returns the bytes of the specified string capability.
     *
     * @param cap the capability to return
     * @return the byte array with the capability string,
     *  excluding the terminating null byte, or {@code null} if the
     *  capability is not present
     */
    public byte[] get(StringCapability cap) {
        byte[] result = get0(cap.index());
        if (result != null)
            return result.clone();
        else
            return null;
    }

    /**
     * Returns the bytes of the specified key string capability.
     *
     * @param cap the capability to return
     * @return the byte array with the capability string,
     *  excluding the terminating null byte, or {@code null} if the
     *  capability is not present
     */
    public byte[] get(KeyCapability cap) {
        byte[] result = get0(cap.index());
        if (result != null)
            return result.clone();
        else
            return null;
    }

    private final List<String> names;
    private final long booleans;
    private final int[] numbers;
    private final byte[][] strings;

    private static final int EXTENDED_MAGIC = 542;
    private static final TermFile EMPTY =
        new TermFile(List.of(), 0, new int[0], new byte[0][]);
    private static final long serialVersionUID = 0x1b8e1e818202afcbL;

    // Initialized by terminfoDirectories.
    private static List<Path> terminfoDirectories;

    private TermFile(List<String> names, long booleans, int[] numbers,
        byte[][] strings) {
        this.names = names;
        this.booleans = booleans;
        this.numbers = numbers;
        this.strings = strings;
    }

    private static ByteBuffer sliceBuffer(ByteBuffer b, int size) {
        ByteBuffer result = b.slice().limit(size);
        b.position(b.position() + size);
        return result;
    }

    private static int fetchHeaderShort(ByteBuffer b, String context) {
        short value = b.getShort();
        if (value < 0)
            throw new TermFileException(
                "Invalid " + context + ": " + Short.toUnsignedInt(value));
        return value;
    }

    private static long readBooleans(ByteBuffer b, int count) {
        long result = 0;
        for (int i = 0; i < count; ++i) {
            byte flag = b.get();
            if (flag < 0 || flag > 1)
                throw new TermFileException(
                    "Invalid boolean capability " + i + ": "
                    + Byte.toUnsignedInt(flag));
            if (i < 64 && flag != 0)
                result |= 1L << i;
        }
        if ((b.position() & 1) != 0)
            // Align to two-byte boundary.
            b.get();
        return result;
    }

    private static int[] readNumbers(ByteBuffer b, int count,
        int magic) {
        int[] result = new int[Math.min(count, NumericCapability.COUNT)];
        for (int i = 0; i < count; ++i) {
            int value;
            if (magic == EXTENDED_MAGIC) {
                value = b.getInt();
            } else {
                value = b.getShort();
            }
            if (value < -2)
                throw new TermFileException(
                    "Invalid numeric capability " + i + ": " + value);
            if (value == -2)
                // ncurses calls these numbers "canceled".  It seems
                // that this an implementation detail of its terminfo
                // compiler which leaks into the produced binary.
                // Treat them as regular absent numbers.
                value = -1;
            if (i < result.length)
                result[i] = value;
        }
        return result;
    }

    private static int[] readStringTable(ByteBuffer b, int count) {
        int[] result = new int[Math.min(count, StringCapability.COUNT)];
        for (int i = 0; i < count; ++i) {
            int value = b.getShort();
            if (value < -2)
                throw new TermFileException(
                    "Invalid string table entry " + i + ": " + value);
            if (value == -2)
                // ncurses calls these strings "canceled".  It seems
                // that this an implementation detail of its terminfo
                // compiler which leaks into the produced binary.
                // Treat them as regular absent strings.
                value = -1;
            if (i < result.length)
                result[i] = value;
        }
        return result;
    }

    private static List<String> parseNames(ByteBuffer b) {
        CharsetDecoder decoder = ExtendedCharsets.UTF_8S.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        ArrayList<String> result = new ArrayList<String>();
        int start = b.position();
        while (true) {
            int current = b.position();
            byte ch;
            if (b.hasRemaining())
                ch = b.get();
            else
                ch = 0;
            if (ch == '|' || ch == 0) {
                int length = current - start;
                if (length > 0) {
                    b.position(start);
                    b.limit(current);
                    CharBuffer name;
                    try {
                        name = decoder.decode(b);
                    } catch (CharacterCodingException e) {
                        // With the UTF-8S charset in use, this should
                        // not happen.
                        throw new TermFileException(
                            "Name decoding failure", e);
                    }
                    result.add(String.valueOf(name));
                    start = current + 1;
                }
                if (ch == 0)
                    break;
                // Skip over the name and its separator.
                b.clear();
                b.position(current + 1);
            }
        }
        return List.copyOf(result);
    }

    private static byte[][] parseStrings(
        int[] stringTable, ByteBuffer buffer) {
        byte[][] result = new byte[stringTable.length][];
        for (int i = 0; i < result.length; ++i)
            if (stringTable[i] >= 0)
                result[i] = readStringAt(buffer, stringTable[i]);
        return result;
    }

    private static byte[] readStringAt(ByteBuffer buffer, int start) {
        int index = start;
        while (true) {
            byte b;
            try {
                b = buffer.get(index);
            } catch (IndexOutOfBoundsException e) {
                throw new TermFileException(
                    "Missing null termination in string at " + start, e);
            }
            if (b == 0)
                break;
            ++index;
        }
        byte[] result = new byte[index - start];
        for (int i = 0; i < result.length; ++i)
            result[i] = buffer.get(start + i);
        return result;
    }

    private static TermFile tryOpen(Path path) {
        if (!Files.isReadable(path))
            return null;
        byte[] data;
        try (var in = Files.newInputStream(path)) {
            data = in.readAllBytes();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new TermFileException("Reading " + path, e);
        }
        return TermFile.of(data);
    }

    private byte[] get0(int index) {
        if (index < strings.length)
            return strings[index];
        else
            return null;
    }
}
