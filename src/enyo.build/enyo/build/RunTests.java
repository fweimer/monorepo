package enyo.build;

import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;
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

/**
 * The Enyo testing tool.
 */
public final class RunTests {
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
        modules.stream()
            .filter(IOPredicate.of(
                    mod -> FindTests.isTestModule(dirs, generation, mod)))
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
                        System.out.println(test);
                        runTest(testLayer, test);
                    }
                });
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

    static void runTest(ModuleLayer testLayer, TestMethodLocator test) {
        Class<?> testClass;
        Method testMethod;
        Constructor<?> constructor;
        try {
            testClass = testLayer.findLoader(test.module())
                .loadClass(test.className());
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
            return;
        }
        try {
            Object obj;
            if (constructor != null)
                obj = constructor.newInstance();
            else
                obj = null;

            if (test.hasTestingArgument()) {
                Testing t = new TestingImpl(test);
                testMethod.invoke(obj, t);
            } else {
                testMethod.invoke(obj);
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
        }
    }
    private RunTests() { } // No instances.
}

