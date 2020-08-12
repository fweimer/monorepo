package enyo.testing.runtime;

import enyo.testing.introspect.TestMethodLocator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * External interface for the test run-time facilities.
 */
public final class InvokeTest {
    /**
     * Run a single test.
     * @return true if the test succeed, false if it could not be located
     *   or if it failed
     */
    public static boolean invokeTest(ModuleLayer testLayer,
        TestMethodLocator test) {
        Class<?> testClass;
        Method testMethod;
        Constructor<?> constructor;
        try {
            ClassLoader loader = test.moduleName()
                .map(testLayer::findLoader)
                .orElse(InvokeTest.class.getClassLoader());
            testClass = loader.loadClass(test.className());
            if (test.hasTestingArgument())
                testMethod = testClass.getMethod(test.methodName(),
                    // NB: Tests reference the interface, not the
                    // implementing class.
                    enyo.testing.Testing.class);
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
                Testing t = new Testing(test);
                testMethod.invoke(obj, t);
                return !t.failed;
            } else {
                testMethod.invoke(obj);
                return true;
            }
        } catch (InstantiationException | InvocationTargetException
            | IllegalAccessException e) {
            synchronized (System.err) {
                System.err.format("FAIL: %s%n", test);
                Throwable e1;
                if (e instanceof InvocationTargetException)
                    e1 = e.getCause();
                else
                    e1 = e;
                e1.printStackTrace(System.err);
            }
            return false;
        }
    }

    // Used for pruning stack traces in tests invoked from invokeTest().
    // Define these variables here, so that we can validate that the
    // referenced method does in fact exist up front, and not
    // implicitly when tests start running (or even later, during
    // failure).
    static String INVOKE_TEST_METHOD = "invokeTest";
    static MethodType INVOKE_TEST_METHOD_TYPE = MethodType.methodType(
        Boolean.TYPE, ModuleLayer.class, TestMethodLocator.class);
    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle ignored = lookup.findStatic(
                InvokeTest.class, INVOKE_TEST_METHOD, INVOKE_TEST_METHOD_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalError(e);
        }
    }

    private InvokeTest() { } // No instances.
}
