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
    public static void main(String[] args) throws Exception {
        Path targetPath = Path.of("build/bootstrap");
        bootstrapBuild(targetPath);
        invokeBuild(targetPath);
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
     * Names of the modules which are needed to bootstrap the build
     * module.
     */
    private static final List<String> BOOTSTRAP_MODULES = List.of(
        BUILD_MODULE,
        // The following are dependencies of the enyo.build.
        "enyo.core",
        "enyo.testing");

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
     * @param targetPath the directory where the built modules are written
     * @throws IOException if an I/O error occurs
     */
    private static void bootstrapBuild(Path targetPath) throws IOException {
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        String modulesString = BOOTSTRAP_MODULES.stream()
            .collect(Collectors.joining(","));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int status = compiler.run(InputStream.nullInputStream(),
            System.out, System.err,
            new String[] {
                "-g",
                "-d", targetPath.toString(),
                "--module-source-path", "src",
                "--module", modulesString,
            });
        if (status != 0)
            System.exit(status);
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

    private static void invokeBuild(Path path)
        throws ClassNotFoundException, NoSuchMethodException,
               IllegalAccessException, InvocationTargetException {
        Class<?> mainClass = bootstrapModuleLayer(path)
            .findLoader(BUILD_MODULE)
            .loadClass(BUILD_CLASS);
        mainClass.getMethod("main", String[].class)
            .invoke(null, (Object) new String[0]);
    }
}
