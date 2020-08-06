package enyo.io;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * An array sharable between multiple processes, stored in the file
 * system.
 *
 * Shared arrays a denoted by a file system path, and their contents
 * is stored there.  Shared arrays are somewhat expensive to create
 * and maintain because they internally depend on a mapped buffer.
 *
 * <p>The on-disk encoding uses native byte order for the integers.
 */
public final class SharedIntArray {
    /**
     * Creates a new shared array or opens an existing shared array.
     *
     * The size argument specifies the number of array elements if the
     * shared array is created in the file system.  If the file
     * already exists, it must have a matching size.
     *
     * @param path the location of the shared array in the file system
     * @param size a positive number, the number of array elements
     * @param options the open options; include
     *   {@link StandardOpenOption#WRITE} for write access
     * @return a new shared array object
     * @throws IllegalArgumentException if {@code size} is invalid
     * @throws IOException if an error creating or opening the file occurred,
     *   or if the file already exists and has the wrong size
     * @throws NullPointerException if {@code path} is null
     * @see StandardOpenOption
     */
    public static SharedIntArray open(Path path, int size,
        OpenOption... options) throws IOException {
        if (size <= 0 || size > Integer.MAX_VALUE / 4)
            throw new IllegalArgumentException("size: " + size);
        return open0(path, size, options);
    }

    /**
     * Opens an existing shared array for reading or writing.
     *
     * @param path the location of the shared array in the file system
     * @param options the open opens; include
     *   {@link StandardOpenOption#WRITE} for write access
     * @throws IllegalArgumentException if
     *   {@link StandardOpenOption#CREATE} or
     *   {@link StandardOpenOption#CREATE_NEW} is specified in {@code options}
     * @return a new shared array object
     * @throws IOException if an error creating or opening the file occurred,
     *   or if the file already exists and has the wrong size
     * @throws NullPointerException if {@code path} is null
     * @see StandardOpenOption
     */
    public static SharedIntArray open(Path path, OpenOption... options)
        throws IOException {
        for (OpenOption option : options)
            if (option == StandardOpenOption.CREATE
                || option == StandardOpenOption.CREATE_NEW)
                throw new IllegalArgumentException(
                    "Invalid option for open(): " + option);
        return open0(path, -1, options);
    }

    /**
     * Returns the path to the backing file of this shared array.
     *
     * @return file system location of the shared array
     */
    public Path path() {
        return path;
    }

    /**
     * Returns a buffer containing the data of the shared array.
     *
     * The buffer is read-only unless the shared array was opened for
     * writing.
     *
     * @return a new integer buffer
     */
    public IntBuffer buffer() {
        return buffer.asIntBuffer();
    }

    /**
     * Returns the number of elements in the shared array.
     * @return the size with which the array was created, a positive number
     */
    public int size() {
        return buffer.capacity() / 4;
    }

    /**
     * Returns the integer at the given index.
     * The read uses {@link VarHandle#getVolatile(Object...)} semantics.
     * @param index the array index
     * @return the integer value at the index
     * @throws IndexOutOfBoundsException if index is negative
     *   or not less than size
     */
    public int get(int index) {
        return (int) VARHANDLE.getVolatile(buffer, 4 * index);
    }

    /**
     * Returns the integer at the given index.
     * The write uses {@link VarHandle#setVolatile(Object...)} semantics.
     * @param index the array index
     * @param value the new value
     * @throws IndexOutOfBoundsException if index is negative
     *   or not less than size
     * @throws UnsupportedOperationException if the shared array is read-only
     */
    public void set(int index, int value) {
        try {
            VARHANDLE.setVolatile(buffer, 4 * index, value);
        } catch (ReadOnlyBufferException e) {
            // UnsupportedOperationException for alignment with
            // unmodifiable collection classes.
            throw new UnsupportedOperationException(
                "Attempt to write to read-only array", e);
        }
    }

    private final Path path;
    private final ByteBuffer buffer;

    private static final VarHandle VARHANDLE =
        MethodHandles.byteBufferViewVarHandle(
            int[].class, ByteOrder.nativeOrder());

    private SharedIntArray(Path path, ByteBuffer buffer) {
        this.path = path;
        this.buffer = buffer;
    }

    private static SharedIntArray open0(Path path, int size,
        OpenOption[] options) throws IOException {
        FileChannel.MapMode mapMode = FileChannel.MapMode.READ_ONLY;
        boolean createOption = false;
        for (OpenOption option : options) {
            if (option == StandardOpenOption.WRITE)
                mapMode = FileChannel.MapMode.READ_WRITE;
            if (option == StandardOpenOption.CREATE
                || option == StandardOpenOption.CREATE_NEW)
                createOption = true;
        }
        // The create options are ignored unless WRITE is also specified.
        createOption = createOption
            && mapMode != FileChannel.MapMode.READ_ONLY;

        ByteBuffer buffer;
        try (var channel = FileChannel.open(path, options)) {
            long fileSize = channel.size();
            int byteCount;
            if (size >= 0) {
                // Explicit array size specified.  Check against file size.
                byteCount = size * 4;
                if (fileSize == 0 && createOption)
                    channel.truncate(byteCount);
                else if (fileSize != byteCount)
                    throw new IOException(String.format(
                            "%s: invalid file size %d, expected %d bytes"
                            + " (%d elements)",
                            path.toString(), fileSize, byteCount, size));
            } else {
                // Size not specified by caller.  Use file size.
                byteCount = (int) fileSize;
                if (byteCount < 0 || (byteCount % 4) != 0
                    || fileSize != byteCount)
                    throw new IOException(String.format(
                            "%s: invalid file size %d", path, fileSize));
            }
            buffer = channel.map(mapMode, 0, byteCount)
                .order(ByteOrder.nativeOrder());
        }
        return new SharedIntArray(path, buffer);
    }
}
