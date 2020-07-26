package enyo.build;

import enyo.io.function.IOConsumer;
import enyo.io.function.IOPredicate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.tools.DocumentationTool;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * The Enyo build tool.
 */
public final class Main {
    /**
     * Invoked from the command line to perform a build.  See the class
     * documentation for options.
     * @param args the arguments to pass; see above
     * @throws IOException an I/O error occurred
     * @throws InterruptedException the thread was interrupted
     */
    public static void main(String[] args)
        throws IOException, InterruptedException {
        run(false, args);
    }

    /**
     * Invoked by the bootstrapping to perform builds.
     * @return true if a rebootstrap has been requested, false if
     *   no further builds should be performed
     * @param args the arguments to pass; see above
     * @throws IOException an I/O error occurred
     * @throws InterruptedException the thread was interrupted
     */
    public static boolean bootstrap(String[] args)
        throws IOException, InterruptedException {
        run(true, args);
        return true;
    }

    private static void run(boolean forBootstrap, String[] args)
        throws IOException, InterruptedException {
        if (args.length != 0) {
            System.err.println("error: no arguments supported");
            System.exit(1);
        }

        DirectoryLayout dirs = new DirectoryLayout(Path.of(""));
        clearGenerations(dirs);

        RebootstrapCheck rebootstrapCheck = new RebootstrapCheck(dirs);

        AtomicInteger generations = new AtomicInteger();
        SourceFileChanges.loop(dirs, () -> {
                int generation = generations.getAndIncrement();
                List<String> modules = listModules(dirs);

                if (build(dirs, modules, generation)) {
                    if (!rebootstrapCheck.unchanged(dirs, generation)) {
                        if (forBootstrap) {
                            System.err.println("info: performing re-bootstrap");
                            clearGenerations(dirs);
                            return SourceFileChanges.Result.EXIT;
                        } else
                            System.err.println(
                                "warning: New bootstrap required");
                    }
                    document(dirs, modules, generation);
                    if (TEST_IN_SEPARATE_VM)
                        RunTests.testInSeparateVM(dirs, modules, generation);
                    else
                        invokeRunTests(dirs, modules, generation);
                    return SourceFileChanges.Result.CONTINUE;
                } else
                    return SourceFileChanges.Result.CONTINUE_COMPILE_FAILURE;
            });
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

    /**
     * Remove the build/g* and build/d* files built by previous
     * invocations.
     */
    static void clearGenerations(DirectoryLayout dirs) throws IOException {
        Files.list(dirs.build())
            .filter(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.startsWith("g")
                        || fileName.startsWith("d")) {
                        try {
                            Integer.parseInt(fileName.substring(1));
                            return true;
                        } catch (NumberFormatException e) {
                            // fall through
                        }
                    }
                    return false;
                })
            .forEach(IOConsumer.of(Main::removeTree));
    }

    /**
     * Compiles the source tree.
     * @return true if the compilation was successful
     */
    static boolean build(DirectoryLayout dirs, List<String> modules,
        int generation) throws IOException {
        long nanoStart = System.nanoTime();

        Path targetPath = dirs.buildGeneration(generation);
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        String modulesString = modules.stream()
            .collect(Collectors.joining(","));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int status = compiler.run(InputStream.nullInputStream(),
            System.out, System.err,
            new String[] {
                "-g",
                "-Xlint:all",
                "-Xdiags:verbose",
                "-d", targetPath.toString(),
                "--module-source-path", "src",
                "--module", modulesString,
            });
        if (status == 0) {
            Path latestSymlink = dirs.build().resolve("latest-modules");
            Files.deleteIfExists(latestSymlink);
            Files.createSymbolicLink(latestSymlink,
                dirs.build().relativize(targetPath));

            long nanoEnd = System.nanoTime();
            System.err.format(
                "info: build completed successfully (%.3f s)%n",
                (nanoEnd - nanoStart) * 1e-9);
            return true;
        } else
            return false;
    }

    static void document(DirectoryLayout dirs, List<String> modules,
        int generation) throws IOException {
        long nanoStart = System.nanoTime();

        Path targetPath = dirs.docGeneration(generation);
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        // Do not preserve the last successful documentation build
        // because it may document the wrong thing.
        Path latestSymlink = dirs.build().resolve("latest-doc");
        Files.deleteIfExists(latestSymlink);

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
        if (status == 0) {
            Files.createSymbolicLink(latestSymlink,
                dirs.build().relativize(targetPath));

            long nanoEnd = System.nanoTime();
            System.err.format(
                "info: documentation completed successfully (%.3f s)%n",
                (nanoEnd - nanoStart) * 1e-9);
        }
    }

    static void invokeRunTests(DirectoryLayout dirs, List<String> modules,
        int generation) {
        try {
            // Load the RunTests class using the layer that is also
            // used for testing.  Our RunTests class uses enyo.testing
            // and other packages from the bootstrap path, so it is
            // not compatible with testing requirements.  Be careful
            // not to pass any types not in the platform modules here.
            ModuleLayer testLayer =
                RunTests.testLayer(dirs, generation, modules);
            Class<?> runTestsClass = RunTests.
                findBuiltClass(testLayer, RunTests.class);
            Method runTests = runTestsClass.getMethod("runTests",
                Path.class, List.class, Integer.TYPE, ModuleLayer.class);
            runTests.invoke(null, dirs.root(), modules, generation, testLayer);
            return;
        } catch (InvocationTargetException e) {
            Throwable e1 = e;
            if (e1.getCause() != null)
                e1 = e.getCause();
            e1.printStackTrace(System.err);
        } catch (ClassNotFoundException | NoSuchMethodException
            | IllegalAccessException | RuntimeException e) {
            e.printStackTrace(System.err);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        System.err.println("error: unhandled exception during tests");
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

    private static final boolean TEST_IN_SEPARATE_VM = true;

    private Main() { } // No instances.
}
