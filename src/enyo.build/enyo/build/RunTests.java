package enyo.build;

import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;
import enyo.lang.RelaunchVM;
import enyo.testing.Testing;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Enyo testing tool.
 */
public final class RunTests {
    /**
     * Entry point for manually running tests in one class.
     *
     * @param args a single command line argument, containing the test
     *   class name, optionally with a module prefix
     * @throws Exception if something goes wrong while locating the test
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            String argv0 = RunTests.class.getName();
            System.err.format("usage: %s MODULE/TEST-CLASS::METHOD()%n", argv0);
            System.err.format("       %s TEST-CLASS::METHOD()%n", argv0);
            System.exit(1);
        }

        String specifier = args[0];
        int slash = specifier.indexOf('/');
        int colons = specifier.indexOf("::", slash + 1);
        int descriptorStart = specifier.indexOf('(', colons + 1);
        if (colons < 0 || descriptorStart < 0) {
            System.err.format("error: missing \"::\" in specifier: %s%n",
                specifier);
            System.exit(1);
        }

        Optional<String> moduleName;
        if (slash >= 0)
            moduleName = Optional.of(specifier.substring(0, slash));
        else
            moduleName = Optional.empty();
        String className = specifier.substring(slash + 1, colons);
        String methodName = specifier.substring(colons + 2, descriptorStart);
        boolean hasTestingArgument;
        switch (specifier.substring(descriptorStart)) {
        case "()":
            hasTestingArgument = false;
            break;
        case "(Testing)":
        case "(enyo.testing.Testing)":
            hasTestingArgument = true;
            break;
        default:
            System.err.format("error: invalid argument list in specifier: %s%n",
                specifier);
            System.exit(1);
            return; // Unreachable.
        }
        TestMethodLocator test = new TestMethodLocator(
            moduleName, className, methodName, hasTestingArgument);
        runOneTest(test);
    }

    /**
     * Entry point for running tests within the same virtual machine.
     * @param root the directory containing sources and build outputs
     * @param modules the names of the modules discovered;
     *   only {@linkplain enyo.testing.TestModule test modules} will be used
     * @param generation the generation number; used to determine the build
     *   subdirectory
     * @param testLayer the module layer containing the test modules
     *   and this {@link RunTests} class
     * @throws IOException an I/O error occurred
     */
    public static void runTests(Path root, List<String> modules,
        int generation, ModuleLayer testLayer) throws IOException {
        DirectoryLayout dirs = new DirectoryLayout(root);

        adjustSystemProperties(dirs, generation, () -> {
                System.err.println("info: testing started");

                long nanoStart = System.nanoTime();

                AtomicLong errors = new AtomicLong();
                modules.stream()
                    .filter(IOPredicate.of(
                            mod -> FindTests.isTestModule(
                                dirs, generation, mod)))
                    .flatMap(IOFunction.of(
                            mod -> FindTests.tests(dirs, generation, mod)))
                    .filter(method -> method.hasTestAnnotation)
                    .forEach(method -> {
                            if (method.error != null)
                                System.err.format("%s %s %s: %s%n",
                                    method.classDetails.module,
                                    method.classDetails.clazz.className(),
                                    method.method.name(),
                                    method.error);
                            else {
                                TestMethodLocator test = method.locator();
                                if (!runTest(testLayer, test))
                                    errors.incrementAndGet();
                            }
                        });
                {
                    long errors0 = errors.get();
                    if (errors0 == 0) {
                        long nanoEnd = System.nanoTime();
                        System.err.format(
                            "info: testing completed successfully (%.3f s)%n",
                            (nanoEnd - nanoStart) * 1e-9);
                    } else if (errors0 == 1)
                        System.err.println(
                            "error: testing completed with 1 error");
                    else
                        System.err.format(
                            "info: testing completed with %d errors%n",
                            errors0);
                }
            });
    }

    /**
     * Used by adjustSystemProperties below.
     */
    private interface Callback {
        void run();
    }

    /**
     * Some tests depend on system properties to recognize the
     * execution environment due to the module loader changes.
     */
    static void adjustSystemProperties(DirectoryLayout dirs, int generation,
        Callback callback) {
        String modulePath = RelaunchVM.class.getName() + ".modulePath";
        String oldModulePathValue = System.getProperty(modulePath);
        System.setProperty(
            modulePath, dirs.buildGeneration(generation).toString());
        try {
            callback.run();
        } finally {
            if (oldModulePathValue == null)
                System.getProperties().remove(modulePath);
            else
                System.setProperty(modulePath, oldModulePathValue);
        }
    }

    private static void runOneTest(TestMethodLocator test) {
        if (runTest(ModuleLayer.boot(), test))
            System.exit(0);
        else
            System.exit(1);
    }

    static ModuleLayer testLayer(DirectoryLayout dirs, int generation,
        Collection<String> testModules) {
        ModuleLayer bootLayer = ModuleLayer.boot();
        Configuration conf = bootLayer.configuration().resolve(
            ModuleFinder.of(dirs.buildGeneration(generation)),
            ModuleFinder.of(), testModules);
        return bootLayer.defineModulesWithOneLoader(
            conf, ClassLoader.getSystemClassLoader());
    }

    static Class<?> findBuiltClass(ModuleLayer testLayer, Class<?> clazz)
        throws ClassNotFoundException {
        return testLayer.findLoader(clazz.getModule().getName())
            .loadClass(clazz.getName());
    }

    /**
     * Run a single test.
     * @return true if the test succeed, false if it could not be located
     *   or if it failed
     */
    static boolean runTest(ModuleLayer testLayer, TestMethodLocator test) {
        Class<?> testClass;
        Method testMethod;
        Constructor<?> constructor;
        try {
            ClassLoader loader = test.moduleName()
                .map(testLayer::findLoader)
                .orElse(RunTests.class.getClassLoader());
            testClass = loader.loadClass(test.className());
            if (test.hasTestingArgument())
                testMethod = testClass.getMethod(test.methodName(),
                    Testing.class);
            else
                testMethod = testClass.getMethod(test.methodName());
            if ((testMethod.getModifiers() & Modifier.STATIC) == 0)
                constructor = testClass.getConstructor();
            else
                constructor = null;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.err.format("%s: cannot find test: %s%n",
                test, e.getMessage());
            return false;
        }
        try {
            Object obj;
            if (constructor != null)
                obj = constructor.newInstance();
            else
                obj = null;

            if (test.hasTestingArgument()) {
                TestingImpl t = new TestingImpl(test);
                testMethod.invoke(obj, t);
                return !t.failed;
            } else {
                testMethod.invoke(obj);
                return true;
            }
        } catch (InstantiationException | InvocationTargetException
            | IllegalAccessException e) {
            System.err.format("FAIL: %s%n", test);
            Throwable e1;
            if (e instanceof InvocationTargetException)
                e1 = e.getCause();
            else
                e1 = e;
            e1.printStackTrace(System.err);
            return false;
        }
    }
    private RunTests() { } // No instances.
}

