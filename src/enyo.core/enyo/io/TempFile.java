package enyo.io;

import enyo.io.function.IORunnable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * Temporary files which are deleted on close (or garbage collection).
 */
public final class TempFile implements AutoCloseable {
    /**
     * Wraps a path object as a temporary file.
     *
     * The path must exist.  It is deleted when the temporary file
     * object is closed or garbage-collected.
     *
     * @param path the path of an existing file
     * @return a new temporary file object
     * @throws NoSuchFileException if the {@code path} does not exist
     * @throws IOException if another I/O error occurs
     * @throws NullPointerException if path is null
     */
    public static TempFile of(Path path) throws IOException {
        if (!Files.exists(path))
            throw new NoSuchFileException(path.toString());
        return of0(path);
    }

    /**
     * Creates a new temporary file in a specific directory.
     *
     * The temporary file is automatically deleted when the temporary
     * file object is closed or garbage-collected.
     *
     * @param dir the directory in which the temporary file is created
     * @param prefix string at the beginning of the file name
     *   of the new temporary file
     * @param suffix string at the end of the file name
     *   of the new temporary file
     * @param attrs the file attributes to use when creating the file
     * @return a new temporary file object
     * @throws IOException if an I/O occurs
     * @throws NullPointerException if path is null
     * @see Files#createTempFile(Path,String,String,FileAttribute...)
     */
    public static TempFile create(Path dir, String prefix, String suffix,
        FileAttribute<?>... attrs) throws IOException {
        return of0(Files.createTempFile(dir, prefix, suffix, attrs));
    }

    /**
     * Creates a new temporary file in the system-default location.
     *
     * The temporary file is automatically deleted when the temporary
     * file object is closed or garbage-collected.
     *
     * @param prefix string at the beginning of the file name
     *   of the new temporary file
     * @param suffix string at the end of the file name
     *   of the new temporary file
     * @param attrs the file attributes to use when creating the file
     * @return a new temporary file object
     * @throws IOException if an I/O occurs
     * @throws NullPointerException if path is null
     * @see Files#createTempFile(Path,String,String,FileAttribute...)
     */
    public static TempFile create(String prefix, String suffix,
        FileAttribute<?>... attrs) throws IOException {
        return of0(Files.createTempFile(prefix, suffix, attrs));
    }

    /**
     * Deletes the temporary file.
     *
     * The path is not deleted again in subsequent calls even if it
     * has been re-created.  (Note that it is generally unsafe to
     * re-create temporary files in the same location.)
     *
     * @throws IOException if an I/O error occurs during deletion
     */
    @Override
    public void close() throws IOException {
        try {
            cleanable.clean();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Returns the path to the temporary file.
     *
     * @return a path object
     */
    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    private final Path path;
    private final Cleaner.Cleanable cleanable;
    private static final Cleaner cleaner = Cleaner.create();

    private TempFile(Path path) {
        this.path = path;
        // Note: This does not work with a security manager
        // because the cleaner thread has no permissions.
        this.cleanable = cleaner.register(
            this, IORunnable.of(() -> Files.deleteIfExists(path)));
    }

    private static TempFile of0(Path path) {
        try {
            return new TempFile(path);
        } catch (Throwable e) {
            // Delete the file (typically if we could not allocate the
            // cleaner object or the wrapper object).
            try {
                Files.delete(path);
            } catch (Throwable e1){
                e.addSuppressed(e1);
            }
            throw e;
        }
    }
}
