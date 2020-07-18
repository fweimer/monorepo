package enyo.build;

import java.nio.file.Path;

/**
 * Computes paths in the build tree.
 */
final class DirectoryLayout {
    DirectoryLayout(Path root) {
        this.root = root;
        this.sources = root.resolve("src");
        this.build = root.resolve("build");
    }

    Path root() {
        return root;
    }

    Path sources() {
        return sources;
    }

    Path build() {
        return build;
    }

    Path buildGeneration(int generation) {
        return build().resolve("g" + generation);
    }

    Path docGeneration(int generation) {
        return build().resolve("d" + generation);
    }

    Path bootstrapBuild() {
        return build().resolve("bootstrap");
    }

    Path moduleSources(String moduleName) {
        return sources.resolve(moduleName);
    }

    private final Path root;
    private final Path sources;
    private final Path build;
}
