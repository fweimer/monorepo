package enyo.build;

import enyo.io.function.IOConsumer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Low-level I/O for the {@code test-link} directory.
 */
final class TestLink {
    /**
     * Obtains the {@code test-link} directory from the directory layout.
     */
    TestLink(DirectoryLayout dirs) {
        this.dirs = Objects.requireNonNull(dirs);
    }

    /**
     * Make sure that the {@code test-link} directory exists and lock it.
     */
    TestLinkLock prepareAndLock() throws IOException {
        Path testLink = dirs.testLink();
        if (!Files.isDirectory(testLink))
            Files.createDirectory(testLink);

        TestLinkLock lock = lock();
        boolean ok = false;
        try {
            cleanupTestLink(lock);
            ok = true;
        } finally {
            if (!ok)
                lock.release();
        }
        return lock;
    }

    /**
     * Locks the {@code test-link} directory.
     */
    TestLinkLock lock() throws IOException {
        return new TestLinkLock(dirs);
    }

    private final DirectoryLayout dirs;

    /**
     * Remove stale files in the test-link subdirectory.
     */
    private void cleanupTestLink(TestLinkLock lock) throws IOException {
        Files.list(dirs.testLink())
            // Do not delete the lock file.
            .filter(p -> !p.equals(lock.path()))
            .forEach(IOConsumer.of(Files::delete));
    }
}
