package enyo.build;

import enyo.crypto.FileDigest;
import enyo.crypto.DigestAlgorithm;
import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Detecting changes to boostrap classes.
 *
 * If the class files in the modules which are compiled as part of the
 * bootstrap change, a new bootstrap cycle needs to be started.
 */
final class RebootstrapCheck {
    /**
     * Computes digests of the bootstrap classes for later comparison.
     */
    RebootstrapCheck(DirectoryLayout dirs) throws IOException {
        bootstrapClasses = classFiles(dirs.bootstrapBuild())
            .collect(Collectors.toUnmodifiableMap(
                    path -> dirs.bootstrapBuild().relativize(path),
                    IOFunction.of(RebootstrapCheck::fileDigest)));
        bootstrapModules = bootstrapClasses.keySet().stream()
            .map(path -> path.getName(0))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Checks if no new bootstrap is needed.
     * @return true if there have not been any bootstrap class file changes,
     *   false if changes are detected
     */
    boolean unchanged(DirectoryLayout dirs) {
        Path root = dirs.buildGeneration();
        // Restrict the class list to classes in the bootstrap modules.
        List<Path> currentClasses;
        try {
            currentClasses = bootstrapModules.stream()
            .flatMap(IOFunction.of(module ->
                    classFiles(root.resolve(module))))
                .collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof NoSuchFileException)
                // A module used in the bootstrap went away completely.
                // This is a fundamental change that needs a rebootstrap.
                return false;
            else
                throw e;
        }
        // Only check that the classes contained in the bootstrap
        // build are unchanged.  The bootstrap build only contains a
        // full set of classes for enyo.build; classes in other
        // modules are only if they are needed by enyo.build and its
        // dependencies.  Since the set of needed classes can only
        // change as the result of a class from enyo.build or its
        // dependencies, that change triggers a rebuild, and a
        // bootstrap build with the new dependency.
        return currentClasses.stream()
            .allMatch(IOPredicate.of(path -> {
                        Path relpath = root.relativize(path);
                        FileDigest expected = bootstrapClasses.get(relpath);
                        if (expected == null)
                            // Not present in the bootstrap build.
                            // Changes to such classes cannot affect
                            // the bootstrap result unless there are
                            // other changes (see above).
                            return true;
                        else
                            // Classes part of the boostrap must match.
                            return fileDigest(path).equals(expected);
                    }));
    }

    private static Stream<Path> classFiles(Path root) throws IOException {
        return Files.walk(root)
            .filter(path -> path.getFileName().toString().endsWith(".class"));
    }

    private static FileDigest fileDigest(Path path) throws IOException {
        DigestAlgorithm algorithm = DigestAlgorithm.SHA256;
        MessageDigest md = algorithm.newDigest();
        long length = 0;
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[4096];
            while (true) {
                int ret = in.read(buffer);
                if (ret <= 0)
                    break;
                md.update(buffer, 0, ret);
                length += ret;
            }
        }
        return FileDigest.of(algorithm, length, md.digest());
    }

    private final Map<Path, FileDigest> bootstrapClasses;
    private final Set<Path> bootstrapModules;
}
