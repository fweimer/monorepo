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
    public static boolean invokeTest(ModuleLayer testLayer, Testing t) {
        Class<?> testClass;
        TestMethodLocator test = t.test;
        Constructor<?> constructor;
        try {
            ClassLoader loader = test.moduleName()
                .map(testLayer::findLoader)
                .orElse(InvokeTest.class.getClassLoader());
            testClass = loader.loadClass(test.className());
        } catch (ClassNotFoundException e) {
            t.recordFailure(ExceptionPhase.CLASS_LOOKUP, null, preferCause(e));
            return false;
        }
        Method testMethod;
        try {
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
        } catch (NoSuchMethodException e) {
            t.recordFailure(ExceptionPhase.METHOD_LOOKUP, null, preferCause(e));
            return false;
        }

        Object obj;
        if (constructor != null)
            try {
                obj = constructor.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                // Neither should happen because the test framework
                // should ensure that there is a public no-argument
                // constructor.
                t.recordFailure(ExceptionPhase.SETUP, null, e);
                return false;
            } catch (InvocationTargetException e) {
                t.recordFailure(ExceptionPhase.SETUP, null, preferCause(e));
                return false;
            }
        else
            obj = null;

        try {
            if (test.hasTestingArgument())
                testMethod.invoke(obj, t);
            else
                testMethod.invoke(obj);
            return !t.failed;
        } catch (IllegalAccessException e) {
            t.recordFailure(ExceptionPhase.METHOD_LOOKUP, "invoke", e);
            return false;
        } catch (InvocationTargetException e) {
            t.recordFailure(ExceptionPhase.EXECUTION, null, preferCause(e));
            return false;
        }
    }

    private static Throwable preferCause(Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null)
            return e;
        else
            return cause;
    }

    // Used for pruning stack traces in tests invoked from invokeTest().
    // Define these variables here, so that we can validate that the
    // referenced method does in fact exist up front, and not
    // implicitly when tests start running (or even later, during
    // failure).
    static String INVOKE_TEST_METHOD = "invokeTest";
    static MethodType INVOKE_TEST_METHOD_TYPE = MethodType.methodType(
        Boolean.TYPE, ModuleLayer.class, Testing.class);
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
