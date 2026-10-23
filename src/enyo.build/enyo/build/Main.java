package enyo.build;

import enyo.io.function.IOConsumer;
import enyo.io.function.IOPredicate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DocumentationTool;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * The Enyo build tool.
 *
 * <h2>Native library support</h2>
 *
 * Source code for native libraries resides in the module source tree
 * under <code>src/<i>module</i>/</code>.  Sources files must have the
 * extensions {@code .cpp}, {@code .cc}, or {@code .c}.  These source
 * files are compiled using the system C++ compiler (including {@code .c}
 * files).  All compiled native sources for one module are linked into
 * one shared object, written to
 * <code>build/l<i>0</i>/libjni-<i>module</i>.so</code>.  Here,
 * <code><i>0</i></code> is the internal build generation number, and
 * <code><i>module</i></code> is the module name.  For convenience,
 * the last successful native build also has a symbolic link,
 * <code>build/latest-native</code>, which links to the approriate
 * <code>build/l<i>0/</i></code> directory.
 *
 * <p>At run time, if a class is located in the package that has the
 * same name as its module, it may load the native module components
 * like this:
 * <pre>
 * static {
 *     System.loadLibrary("jni-" + Control.class.getPackage().getName());
 * }
 * </pre>
 *
 * Note the lack of the {@code lib} prefix and the {@code .so} suffix.
 * This load operation assumes that when the application is installed,
 * its native component is installed in the default library search
 * path (usually {@code /usr/lib/jvm/}).
 *
 * <p>During testing, the appropriate <code>build/l<i>0/</i></code>
 * directory is placed on the library search path, in the {@code
 * java.library.path} system property.
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
        return run(true, args);
    }

    private static boolean run(boolean forBootstrap, String[] args)
        throws IOException, InterruptedException {
        Options options = Options.of(args);

        DirectoryLayout rootGeneration = new DirectoryLayout(Path.of(""), 0);
        clearGenerations(rootGeneration);

        RebootstrapCheck rebootstrapCheck =
            new RebootstrapCheck(rootGeneration);

        AtomicReference<DirectoryLayout> generations =
            new AtomicReference<>(rootGeneration);

        if (options.once()) {
            SourceFileChanges.loop(rootGeneration, changeWaiter -> {
                    DirectoryLayout dirs = generations.getAndUpdate(
                        DirectoryLayout::nextGeneration);
                    List<String> modules = listModules(dirs);

                    if (build(dirs, modules)) {
                        if (!rebootstrapCheck.unchanged(dirs))
                            System.err.println(
                                "warning: New bootstrap required");
                        BuildNative.build(dirs, changeWaiter.sources());
                        document(dirs, modules);
                        if (options.testSameVm())
                            invokeRunTests(dirs, modules);
                        else
                            RunTests.testInSeparateVM(
                                options, dirs, modules, changeWaiter);
                    }
                    return SourceFileChanges.Result.EXIT;
                });
            return false;
        }

        var buildLog = generations.get().build().resolve(
            "latest-build-diagnostics.txt");
        try (var redirectStreams = new TeeSystemStreams(buildLog)) {
            SourceFileChanges.loop(rootGeneration, changeWaiter -> {
                    redirectStreams.rewind();

                    DirectoryLayout dirs = generations.getAndUpdate(
                        DirectoryLayout::nextGeneration);
                    List<String> modules = listModules(dirs);

                    if (build(dirs, modules)) {
                        if (!rebootstrapCheck.unchanged(dirs)) {
                            if (forBootstrap) {
                                System.err.println(
                                    "info: performing re-bootstrap");
                                clearGenerations(dirs);
                                return SourceFileChanges.Result.EXIT;
                            } else
                                System.err.println(
                                    "warning: New bootstrap required");
                        }
                        if (!BuildNative.build(dirs, changeWaiter.sources()))
                            return SourceFileChanges.Result
                                .CONTINUE_COMPILE_FAILURE;
                        document(dirs, modules);
                        if (options.testSameVm())
                            invokeRunTests(dirs, modules);
                        else
                            RunTests.testInSeparateVM(
                                options, dirs, modules, changeWaiter);
                        return SourceFileChanges.Result.CONTINUE;
                    } else
                        return SourceFileChanges.Result
                            .CONTINUE_COMPILE_FAILURE;
                });
        }

        return true;
    }

    static List<String> listModules(DirectoryLayout dirs) throws IOException {
        try (Stream<Path> stream = Files.list(dirs.sources())) {
            return stream.filter(Files::isDirectory)
            .filter(IOPredicate.of(path -> {
                        try (Stream<Path> stream1 = Files.walk(path)) {
                            return stream1.map(Path::getFileName)
                                .map(Path::toString)
                                .anyMatch(pathString ->
                                    pathString.endsWith(".java"));
                        }
                    }))
            .map(Path::getFileName)
            .map(Path::toString)
            .sorted()
            .collect(Collectors.toList());
        }
    }

    /**
     * Remove the build/g* and build/d* files built by previous
     * invocations.
     */
    static void clearGenerations(DirectoryLayout dirs) throws IOException {
        try (Stream<Path> stream = Files.list(dirs.build())) {
            stream.filter(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.startsWith("g")
                        || fileName.startsWith("d")
                        || fileName.startsWith("h")
                        || fileName.startsWith("l")) {
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
    }

    /**
     * Compiles the source tree.
     * @return true if the compilation was successful
     */
    static boolean build(DirectoryLayout dirs, List<String> modules)
        throws IOException {
        long nanoStart = System.nanoTime();

        Path targetPath = dirs.buildGeneration();
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
                "-h", dirs.headerGeneration().toString(),
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

    static void document(DirectoryLayout dirs, List<String> modules)
        throws IOException {
        Path targetPath = dirs.docGeneration();
        removeTree(targetPath);
        Files.createDirectories(targetPath);

        // Do not preserve the last successful documentation build
        // because it may document the wrong thing.
        Path latestSymlink = dirs.build().resolve("latest-doc");
        Files.deleteIfExists(latestSymlink);

        String modulesString = modules.stream()
            .filter(IOPredicate.of(mod -> !FindTests.isTestModule(dirs, mod)))
            .collect(Collectors.joining(","));

        DocumentationTool doc = ToolProvider.getSystemDocumentationTool();
        var result = ToolResults.run(doc, null,
            List.of(
                "-quiet",
                "-d", targetPath.toString(),
                "-linkoffline", PUBLIC_API, "etc/javadoc",
                "--module-source-path", "src",
                "--module", modulesString),
            List.of());
        if (!result.additionalOutput().isEmpty()) {
            System.out.println(
                "warning: additional output from documentation tool:");
            System.out.println(result.additionalOutput().trim());
            System.out.print("info: (end of documentation tool output)");
        }
        if (!result.diagnostics().isEmpty()) {
            System.out.format(
                "warning: documentation tool diagnostics follow:%n");
            for (var d : result.diagnostics())
                System.out.println(d);
        }
        if (result.consolidatedSuccess()) {
            System.err.format(
                "info: documentation completed successfully (%.3f s)%n",
                result.durationSeconds());
            Files.createSymbolicLink(latestSymlink,
                dirs.build().relativize(targetPath));
        }
        else
            System.err.format(
                "info: documentation completed with notes (%.3f s)%n",
                result.durationSeconds());
    }

    static void invokeRunTests(DirectoryLayout dirs, List<String> modules) {
        try {
            // Load the RunTests class using the layer that is also
            // used for testing.  Our RunTests class uses enyo.testing
            // and other packages from the bootstrap path, so it is
            // not compatible with testing requirements.  Be careful
            // not to pass any types not in the platform modules here.
            ModuleLayer testLayer = RunTests.testLayer(dirs, modules);
            Class<?> runTestsClass = RunTests.
                findBuiltClass(testLayer, RunTests.class);
            Method runTests = runTestsClass.getMethod("runTests",
                Path.class, List.class, Integer.TYPE, ModuleLayer.class);
            runTests.invoke(null, dirs.root(), modules, dirs.generation(),
                testLayer);
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
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path path :
                     // Delete more deeply nested files first.
                     stream.sorted(Comparator.reverseOrder())
                     .collect(Collectors.toList()))
                Files.delete(path);;
        }
    }

    private static final String PUBLIC_API =
        "https://docs.oracle.com/en/java/javase/14/docs/api/";

    private Main() { } // No instances.
}
