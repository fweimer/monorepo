package enyo.build;

import enyo.io.function.IOPredicate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;
import java.lang.reflect.Method;

/**
 * The Enyo build tool.
 */
public final class Main {
    /**
     * Invoked from the command line to perform a build.  See the class
     * documentation for options.
     * @param args the arguments to pass; see above
     * @throws IOException an I/O error occurred
     */
    public static void main(String[] args) throws IOException {
        DirectoryLayout dirs = new DirectoryLayout(Path.of(""));
        List<String> modules = listModules(dirs);
        build(dirs, modules, 0);
        document(dirs, modules, 0);
        invokeRunTests(dirs, modules, 0);
    }

    static List<String> listModules(DirectoryLayout dirs) throws IOException {
        return Files.list(dirs.sources())
            .filter(Files::isDirectory)
            .filter(IOPredicate.of(path -> Files.walk(path)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .anyMatch(pathString -> pathString.endsWith(".java"))))
            .map(Path::getFileName)
            .map(Path::toString)
            .sorted()
            .collect(Collectors.toList());
    }

    static void build(DirectoryLayout dirs, List<String> modules,
        int generation) throws IOException {
        Path targetPath = dirs.buildGeneration(generation);
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        String modulesString = modules.stream()
            .collect(Collectors.joining(","));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int status = compiler.run(InputStream.nullInputStream(),
            System.out, System.err,
            new String[] {
                "-Xlint:all",
                "-Xdiags:verbose",
                "-d", targetPath.toString(),
                "--module-source-path", "src",
                "--module", modulesString,
            });
        if (status != 0)
            System.exit(status);
    }

    static void document(DirectoryLayout dirs, List<String> modules,
        int generation) throws IOException {
        Path targetPath = dirs.docGeneration(generation);
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        String modulesString = modules.stream()
            .filter(IOPredicate.of(
                    mod -> !FindTests.isTestModule(dirs, generation, mod)))
            .collect(Collectors.joining(","));

        DocumentationTool doc = ToolProvider.getSystemDocumentationTool();
        int status = doc.run(InputStream.nullInputStream(),
            System.out, System.err,
            new String[] {
                "-quiet",
                "-d", targetPath.toString(),
                "-linkoffline", PUBLIC_API, "etc/javadoc",
                "--module-source-path", "src",
                "--module", modulesString,
            });
    }

    static void invokeRunTests(DirectoryLayout dirs, List<String> modules,
        int generation) {
        try {
            ModuleLayer testLayer =
                RunTests.testLayer(dirs, generation, modules);
            Class<?> runTestsClass = RunTests.
                findBuiltClass(testLayer, RunTests.class);
            Method runTests = runTestsClass.getMethod("runTests",
                Path.class, List.class, Integer.TYPE, ModuleLayer.class);
            runTests.invoke(null, dirs.root(), modules, generation, testLayer);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

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

    private static final String PUBLIC_API =
        "https://docs.oracle.com/en/java/javase/14/docs/api/";

    private Main() { } // No instances.
}
