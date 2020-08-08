package enyo.build;

import enyo.io.function.IOConsumer;
import enyo.io.FileIdentity;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

final class SourceFileChanges {
    /**
     * Result of the {@linkplain Callback callback function}.
     */
    enum Result {
        /**
         * The loop should continue, after waiting for changes to
         * *.java files.
         */
        CONTINUE,

        /**
         * The loop should exit.
         */
        EXIT,

        /**
         * A compilation failure occurred.  This means that rebuilds
         * should be attempted even if lock files go away, in case the
         * compilation failure was triggered by a lock file.
         */
        CONTINUE_COMPILE_FAILURE,
    }

    /**
     * Blocking until source files change.
     */
    interface Waiter {
        /**
         * Blocks the calling thread until source file changes have been
         * detected.  Lock file changes are ignored.
         *
         * @throws IOException the source directory tree could not be accessed
         * @throws InterruptedException the waiting operation was interrupted
         */
        public void await() throws IOException, InterruptedException;
    }

    /**
     * Callback invoked by
     * {@link SourceFileChanges#loop(DirectoryLayout,Callback)}.
     */
    interface Callback {
        /**
         * @param changeWaiter this can optionally be called to wait
         *   for actual source code changes to happen
         */
        public Result call(Waiter changeWaiter)
            throws IOException, InterruptedException;
    }

    /**
     * Calls {@code callback} repeatedly to perform builds,
     * waiting for source file changes between runs.
     */
    static void loop(DirectoryLayout dirs, Callback callback)
        throws IOException, InterruptedException {
        while (true) {
            Map<Path, FileIdentity> currentSources = sourceFileIdentities(dirs);
            Waiter waiter = new ChangeBlocker(
                dirs, currentSources, SourceFileChanges::filterOutLockFiles);
            switch (callback.call(waiter)) {
            case CONTINUE:
                // Remove the lock files from consideration.
                waiter.await();
                break;
            case EXIT:
                return;
            case CONTINUE_COMPILE_FAILURE:
                // Also retry on lock file changes.
                waitForSourceChange(dirs, currentSources, Function.identity());
                break;
            }
        }
    }

    /**
     * Returns file identity information for all source files.
     */
    static Map<Path, FileIdentity> sourceFileIdentities(DirectoryLayout dirs)
        throws IOException {
        while (true)
            try {
                try {
                    return Files.walk(dirs.sources())
                        .filter(path ->
                            path.getFileName().toString().endsWith(".java"))
                        .collect(FileIdentity.mapToFileIdentity());
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            } catch (NoSuchFileException e) {
                if (Files.isDirectory(dirs.sources()))
                    continue;
                else
                    throw e;
            }
    }

    /**
     * Removes the lock files from the map.
     */
    static Map<Path, FileIdentity> filterOutLockFiles(
        Map<Path, FileIdentity> map) {
        return map.entrySet().stream()
            .filter(e ->
                !e.getKey().getFileName().toString().startsWith(".#"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Register all source subdirectories with the watcher.
     */
    static void registerSourceDirectories(DirectoryLayout dirs,
        WatchService watcher) throws IOException {
        try {
            Files.walk(dirs.sources())
                .filter(path -> Files.isDirectory(
                        path, LinkOption.NOFOLLOW_LINKS))
                .forEach(IOConsumer.of(path -> path.register(watcher,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY)));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Blocks until source changes are detected.
     * @param filter old and new source file identities are filtered
     *   with this function before the comparison
     */
    static void waitForSourceChange(DirectoryLayout dirs,
        Map<Path, FileIdentity> oldSources,
        Function<Map<Path, FileIdentity>, Map<Path, FileIdentity>> filter)
        throws IOException, InterruptedException {
        try {
            ForkJoinPool.managedBlock(
                new ChangeBlocker(dirs, oldSources, filter));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Helper class to make waitForSourceChange ForkJoinPool-friendly.
     */
    private static final class ChangeBlocker
        implements Waiter, ForkJoinPool.ManagedBlocker {
        private final DirectoryLayout dirs;
        private final Map<Path, FileIdentity> oldSources;
        private final Function<Map<Path, FileIdentity>,
            Map<Path, FileIdentity>> filter;

        // Multiple threads can use the same Waiter, but they will
        // wait separately (which is somewhat inefficient).  No
        // synchronization is necessary because this field will only
        // transition from false to true, and additional wait
        // operations (using WatchService etc.) are not a problem.
        private boolean released;

        @Override
        public void await() throws IOException, InterruptedException {
            try {
                ForkJoinPool.managedBlock(this);
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        private ChangeBlocker(DirectoryLayout dirs,
            Map<Path, FileIdentity> oldSources,
            Function<Map<Path, FileIdentity>, Map<Path, FileIdentity>> filter) {
            this.dirs = dirs;
            this.oldSources = filter.apply(oldSources);
            this.filter = filter;
        }

        @Override
        public boolean isReleasable() {
            try {
                if (released)
                    return true;
                Map<Path, FileIdentity> currentSources =
                    filter.apply(sourceFileIdentities(dirs));
                if (currentSources.equals(oldSources))
                    return false;
                released = true;
                return true;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public boolean block() throws InterruptedException {
            try {
                WatchService watcher =
                    dirs.sources().getFileSystem().newWatchService();
                try {
                    try {
                        registerSourceDirectories(dirs, watcher);
                    } catch (NoSuchFileException e) {
                        // Make this more robust in the face of unexpected
                        // iteration results.  As long as the top-level
                        // directory exists, a traversal will eventually
                        // succeed.
                        if (Files.isDirectory(dirs.sources()))
                            // To be called again by managedBlock().
                            return false;
                        else
                            throw e;
                    }

                    // This check must come after registering the
                    // watchers, otherwise changes can be missed.
                    if (isReleasable())
                        return true;
                    watcher.take();
                } finally {
                    watcher.close();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            // To be called again by managedBlock().
            return false;
        }
    }

    private SourceFileChanges() { } // No instances.
}
