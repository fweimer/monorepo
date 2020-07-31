package enyo.build;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Lock file for the {@code test-link} subdirectory.
 *
 * This is only used for error checking; execution is expected to be
 * serialized externally.
 */
final class TestLinkLock {
    /**
     * Acquires an exclusive lock on the
     * {@link DirectoryLayout#testLink() test-link} subdirectory.
     * @throws IOException if the directory has not been set up, or if it
     *   is already locked
     */
    TestLinkLock(DirectoryLayout dirs) throws IOException {
        path = dirs.testLink().resolve("lock");
        channel = FileChannel.open(path, StandardOpenOption.CREATE,
            StandardOpenOption.WRITE);
        try {
            lock = channel.tryLock(0, 0, false);
            if (lock == null)
                throw new IOException("Cannot lock " + dirs.testLink());
        } catch (Throwable e) {
            try {
                channel.close();
            } catch (Throwable e1) {
                e.addSuppressed(e1);
            }
            throw e;
        }
    }

    /**
     * Returns the path of the lock file.
     */
    Path path() {
        return path;
    }

    /**
     * Releases the lock on the directory.
     */
    void release() throws IOException {
        try {
            lock.release();
        } finally {
            channel.close();
        }
    }

    private final Path path;
    private final FileChannel channel;
    private final FileLock lock;
}
