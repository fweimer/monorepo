package enyo.build;

import java.nio.file.Path;

/**
 * Computes paths in the build tree.
 */
final class DirectoryLayout {
    DirectoryLayout(Path root, int generation) {
        this.root = root;
        this.generation = generation;
        this.sources = root.resolve("src");
        this.build = root.resolve("build");
    }

    DirectoryLayout nextGeneration() {
        return new DirectoryLayout(root, generation + 1);
    }

    Path root() {
        return root;
    }

    int generation() {
        return generation;
    }

    Path sources() {
        return sources;
    }

    Path build() {
        return build;
    }

    /**
     * The path of the directory for daemon/test VM communication.
     * @return the {@code test-link} subdirectory of the build directory
     */
    Path testLink() {
        return build().resolve("test-link");
    }

    Path buildGeneration() {
        return build().resolve("g" + generation);
    }

    Path headerGeneration() {
        return build().resolve("h" + generation);
    }

    /**
     * Returns the per-generation directory for native shared objects.
     * @return a path to a directory under {@link #build()}
     */
    Path libraryGeneration() {
        return build().resolve("l" + generation);
    }

    Path docGeneration() {
        return build().resolve("d" + generation);
    }

    Path bootstrapBuild() {
        return build().resolve("bootstrap");
    }

    Path moduleSources(String moduleName) {
        return sources.resolve(moduleName);
    }

    private final Path root;
    private final int generation;
    private final Path sources;
    private final Path build;
}
