import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public final class Bootstrap {
    public static void main(String[] args) throws Throwable {
        Path targetPath = Path.of("build/bootstrap");
        int status = bootstrapBuild(targetPath);
        if (status != 0)
            System.exit(status);
        while (true) {
            if (!invokeBuild(targetPath, args))
                break;
            tryToRebootstrap(targetPath);
        }
    }

    /**
     * Name of the module that contains the build tool.
     */
    private static final String BUILD_MODULE = "enyo.build";

    /**
     * Name of the main class in the build module.
     */
    private static final String BUILD_CLASS = BUILD_MODULE + ".Main";

    /**
     * Delete the entire directory tree, including the root.
     * @param root the root of the directory tree to delete
     * @throws IOException if an I/O error occurs
     */
    private static void removeTree(Path root) throws IOException {
        if (!Files.exists(root))
            return;
        for (Path path : Files.walk(root)
                 // Delete more deeply nested files first.
                 .sorted(Comparator.reverseOrder())
                 .collect(Collectors.toList()))
            Files.delete(path);;
    }

    /**
     * Invokes the Java compiler to build the boostrap modules.
     * All files under {@code targetPath} are deleted first.
     * @return the suggested exited status, or 0 for a successful completion
     * @param targetPath the directory where the built modules are written
     * @throws IOException if an I/O error occurs
     */
    private static int bootstrapBuild(Path targetPath) throws IOException {
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(InputStream.nullInputStream(),
            System.out, System.err,
            new String[] {
                "-g",
                "-d", targetPath.toString(),
                "--module-source-path", "src",
                "--module", BUILD_MODULE,
            });
    }

    private static void tryToRebootstrap(Path targetPath) throws IOException {
        // Build to a temporary path, so that we do not overwrite the existing
        // known-good build with something that is broken.
        Path tempBootstrapTarget = targetPath.resolveSibling(
            targetPath.getFileName() + "-1");
        removeTree(tempBootstrapTarget);
        int status = bootstrapBuild(tempBootstrapTarget);
        if (status != 0) {
            System.err.println(
                "error: re-bootstrap failed, using previous build");
            // The caller will re-use the existing (non-rebuilt)
            // bootstrap directory.  enyo.build.Main will see the
            // build failure, and wait for it to be fixed.
            return;
        }

        // Replace the old bootstrap directory with the new one.
        removeTree(targetPath);
        Files.move(tempBootstrapTarget, targetPath);
    }

    /**
     * Returns the module layer for the bootstrap modules.
     * @path path the directory containing the bootstrap modules
     * @return a module layer containing the bootstrap modules
     */
    private static ModuleLayer bootstrapModuleLayer(Path path) {
        ModuleLayer bootLayer = ModuleLayer.boot();
        Configuration conf = bootLayer.configuration().resolve(
            ModuleFinder.of(path), ModuleFinder.of(),
            List.of(BUILD_MODULE));
        return bootLayer.defineModulesWithOneLoader(
            conf, ClassLoader.getSystemClassLoader());
    }

    /**
     * Invoke enyo.build.Build.bootstrap(String[]).
     * @return true if a rebootstrap has been requested
     */
    private static boolean invokeBuild(Path path, String[] args)
        throws Throwable {
        Class<?> mainClass = bootstrapModuleLayer(path)
            .findLoader(BUILD_MODULE)
            .loadClass(BUILD_CLASS);
        try {
            return (boolean) mainClass.getMethod("bootstrap", String[].class)
                .invoke(null, (Object) args);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null)
                throw e.getCause();
            else
                throw e;
        }
    }
}
