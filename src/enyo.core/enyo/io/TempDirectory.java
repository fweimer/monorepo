package enyo.io;

import enyo.io.function.IORunnable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A temporary directory that with automated removal.
 */
public final class TempDirectory implements AutoCloseable {
    /**
     * Deletes this temporary directory tree.
     * @throws IOException if an exception occurs during deletion
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
     * Returns a string representation of this temporary directory's path.
     * @return the string corresponding to the directory path
     */
    @Override
    public String toString() {
        return path.toString();
    }

    /**
     * Returns the path of the temporary directory itself.
     * @return the top-level directory of this temporary directory tree
     */
    public Path path() {
        return path;
    }

    /**
     * Resolves a path relative to this temporary directory.
     *
     * @param name the file name to resolve against this directory
     * @return the resolved path, equal to {@code path().resolve(name)}.
     * @see #path()
     */
    public Path resolve(String name) {
        return path.resolve(name);
    }

    /**
     * Creates a new temporary directory with the specified name prefix.
     * The directory is created in the
     * {@linkplain systemTemporaryDirectory() system temporary directory}.
     * An automatically-generated suffix is added to the supplied prefix
     * to produce a unique directory name.
     * <p>
     * The caller should eventually invoke the {@link #close()} method
     * to delete the temporary directory.
     * @param prefix the prefix of the directory name to create
     * @return a new temporary directory with a unique name
     * @throws IOException if the system temporary directory cannot be
     *   determined, or another I/O error occurs
     */
    public static TempDirectory withPrefix(String prefix)
        throws IOException {
        Path sysTempDir = systemTemporaryDirectory();
        Path newTempDir = Files.createTempDirectory(sysTempDir, prefix);
        try {
            return of0(newTempDir);
        } catch (Throwable t) {
            try {
                Files.deleteIfExists(newTempDir);
            } catch (Exception e) {
                t.addSuppressed(e);
            }
            throw t;
        }
    }

    /**
     * Returns a path to the system temporary directory.
     * @return a path to a temporary directory
     * @throws IOException if the system temporary directory cannot be
     *   determined
     */
    public static final Path systemTemporaryDirectory() throws IOException {
        FileSystem fs = FileSystems.getDefault();
        String property = System.getProperty("java.io.tmpdir");
        if (property != null) {
            Path path = fs.getPath(property);
            if (isValid(path))
                return path;
        }
        Path path = fs.getPath("/tmp");
        if (isValid(path))
            return path;
        throw new IOException("Cannot locate system temporary directory");
    }

    /**
     * Returns true if {@code path} is a writable directory.
     */
    private static final boolean isValid(Path path) {
        return Files.isDirectory(path) && Files.isWritable(path);
    }

    private static final void deleteTree(Path path) throws IOException {
        List<Path> queue = new ArrayList<>();
        queue.add(path);

        while (true) {
            Path current = queue.get(queue.size() - 1);

            // Materialize the directory contents, so that we do not
            // change the order while iterating over it.
            List<Path> contents =
                Files.list(current).collect(Collectors.toList());

            if (contents.isEmpty()) {
                // If the directory is empty, we can delete it from
                // the disk and the queue.
                Files.delete(current);
                queue.remove(queue.size() - 1);
                if (queue.isEmpty())
                    break;
            } else {
                for (Path p : contents) {
                    if (Files.isDirectory(p))
                        queue.add(p);
                    else
                        Files.delete(p);
                }
                // If there are no subdirectories, this will leave the
                // current path at the head of the queue, so that it
                // is deleted on the next iteration.
            }
        }
    }

    private final Path path;
    private final Cleaner.Cleanable cleanable;

    private TempDirectory(Path path) {
        this.path = path;
        // Note: This does not work with a security manager
        // because the cleaner thread has no permissions.
        this.cleanable = TempFile.cleaner.register(
            this, IORunnable.of(() -> deleteTree(path)));
    }

    private static TempDirectory of0(Path path) {
        try {
            return new TempDirectory(path);
        } catch (Throwable e) {
            // Delete the directory (typically if we could not
            // allocate the cleaner object or the wrapper object).
            try {
                Files.delete(path);
            } catch (Throwable e1){
                e.addSuppressed(e1);
            }
            throw e;
        }
    }
}
