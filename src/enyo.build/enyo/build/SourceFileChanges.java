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

final class SourceFileChanges {
    /**
     * Callback invoked by
     * {@link SourceFileChanges#loop(DirectoryLayout,Callback)}.
     */
    interface Callback {
        /**
         * @return true if the looop should continue, false if it should exit
         */
        public boolean call() throws IOException, InterruptedException;
    }

    /**
     * Calls {@code callback} repeatedly to perform builds,
     * waiting for source file changes between runs.
     */
    static void loop(DirectoryLayout dirs, Callback callback)
        throws IOException, InterruptedException {
        while (true) {
            Map<Path, FileIdentity> currentSources = sourceFileIdentities(dirs);
            if (!callback.call())
                break;
            waitForSourceChange(dirs, currentSources);
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
                        .filter(path -> {
                                String fileName = path.getFileName().toString();
                                return fileName.endsWith(".java")
                                    && !fileName.startsWith(".#");
                            })
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
     */
    static void waitForSourceChange(DirectoryLayout dirs,
        Map<Path, FileIdentity> oldSources)
        throws IOException, InterruptedException {
        while (true) {
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
                        continue;
                    else
                        throw e;
                }

                // This check must come after registering the
                // watchers, otherwise changes can be missed.
                Map<Path, FileIdentity> currentSources =
                    sourceFileIdentities(dirs);
                if (!currentSources.equals(oldSources))
                    break;
                watcher.take();
            } finally {
                watcher.close();
            }
        }
    }

    private SourceFileChanges() { } // No instances.
}
