package enyo.testing.runtime;

import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.TestAlreadyFailed;

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
     * @param testLayer the module layer that is used for resolving
     *   module names in the {@linkplain TestMethodLocator test method locator}
     * @param t the instance of {@link enyo.testing.Testing} to use for
     *   executing the test; also determines the
     *   {@linkplain TestMethodLocator test method locator}
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
            t.recordFailure(ExceptionPhase.CLASS_LOOKUP, preferCause(e));
            t.recordCompleted();
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
            t.recordFailure(ExceptionPhase.METHOD_LOOKUP, preferCause(e));
            t.recordCompleted();
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
                t.recordFailure(ExceptionPhase.SETUP, e);
                t.recordCompleted();
                return false;
            } catch (InvocationTargetException e) {
                t.recordFailure(ExceptionPhase.SETUP, preferCause(e));
                t.recordCompleted();
                return false;
            }
        else
            obj = null;

        try {
            if (test.hasTestingArgument())
                testMethod.invoke(obj, t);
            else
                testMethod.invoke(obj);
            t.recordCompleted();
            return !t.failed;
        } catch (IllegalAccessException e) {
            t.recordFailure(ExceptionPhase.METHOD_LOOKUP, e);
            t.recordCompleted();
            return false;
        } catch (InvocationTargetException e) {
            Throwable e1 = preferCause(e);
            // If the test has already failed and the exception was
            // caused by a test barrier, do not record it.
            if (!(e1.getClass() == TestAlreadyFailed.class && t.failed))
                t.recordFailure(ExceptionPhase.EXECUTION, e1);
            t.recordCompleted();
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
