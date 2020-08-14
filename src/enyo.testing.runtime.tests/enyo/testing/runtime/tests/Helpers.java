package enyo.testing.runtime.tests;

import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.runtime.InvokeTest;
import enyo.testing.runtime.ExceptionPhase;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class Helpers {
    /**
     * Returns the test method locator for a public method.
     * This does not require that the method is annotated as a test.
     */
    static TestMethodLocator locator(Class<?> cls, String methodName) {
        Optional<String> moduleName = Optional.ofNullable(cls.getModule())
            .map(Module::getName);
        String className = cls.getName();
        boolean hasTestingArgument;
        try {
            cls.getMethod(methodName, Testing.class);
            hasTestingArgument = true;
        } catch (NoSuchMethodException e) {
            hasTestingArgument = false;
        }
        return new TestMethodLocator(moduleName, className, methodName,
            hasTestingArgument);
    }

    static List<Recorder.Call> testHistory(
        Testing t, Class<?> cls, String methodName) {
        return testHistory(t, locator(cls, methodName));
    }

    static List<Recorder.Call> testHistory(Testing t,
        TestMethodLocator locator) {
        Recorder results = new Recorder();
        var inner = new enyo.testing.runtime.Testing(locator, results);
        ModuleLayer layer =
            Optional.ofNullable(Helpers.class.getModule())
            .map(Module::getLayer)
            .orElse(ModuleLayer.boot());
        boolean success = InvokeTest.invokeTest(layer, inner);
        List<Recorder.Call> history = results.history();
        t.examine(isSuccess(t, history)).is(success);
        t.examine(history.stream()
            .filter(call -> !call.test.equals(locator))
            .findFirst())
            .is(Optional.empty());
        return history;
    }

    static boolean isSuccess(Testing t,
        List<? extends Recorder.Call> history) {
        Optional<? extends Recorder.Call> last = history.stream()
            .reduce((previousLast, current) -> current);
        t.examine(last).isnt(Optional.empty());
        t.fullBarrier();
        t.examine(last.get()).isExactClass(Recorder.Completed.class);
        t.fullBarrier();
        return ((Recorder.Completed) last.get()).success;
    }

    @Test
    public static void testSuccess(Testing t) {
        for (String method : List.of(
                "successStatic", "success",
                "successPlainStatic", "successPlain")) {
            List<Recorder.Call> history =
                testHistory(t, Helpers.class, method);
            t.examine(isSuccess(t, history)).is(true);
        }
    }

    @Test
    public static void testExamineFailure(Testing t) {
        List<Recorder.Call> history =
            testHistory(t, Helpers.class, "examineFailure");
        t.examine(isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Failure.class);
        t.fullBarrier();
        var call = (Recorder.Failure) history.get(0);
        t.examine(call.context).isNull();
        t.examine(call.check).is("equals");
        t.<List<?>>examine(call.values).is(List.of(true, false));
    }

    // Tests that t.assertFail() is recorded properly.
    @Test
    public static void testAssertFail(Testing t) {
        List<Recorder.Call> history =
            testHistory(t, Helpers.class, "assertFail");
        t.examine(isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.fullBarrier();
        var call = (Recorder.Exception) history.get(0);
        t.examine(call.phase).is(ExceptionPhase.EXECUTION);
        t.examine(call.context).isNull();
        t.examine(call.exception).isExactClass(TestFailure.class);
        t.examine(call.exception.getMessage()).is("expected");
        t.examine(call.expected).isNull();
    }

    // Tests that termination by an exception is recorded properly.
    @Test
    public static void testException(Testing t) {
        List<Recorder.Call> history =
            testHistory(t, Helpers.class, "exception");
        t.examine(isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.fullBarrier();
        var call = (Recorder.Exception) history.get(0);
        t.examine(call.phase).is(ExceptionPhase.EXECUTION);
        t.examine(call.context).isNull();
        t.examine(call.exception).isExactClass(
            UnsupportedOperationException.class);
        t.examine(call.exception.getMessage()).is("expected");
        t.examine(call.expected).isNull();
    }

    // Test that InvokeTest records a missing method.
    @Test
    public static void testNoSuchMethod(Testing t) {
        List<Recorder.Call> history =
            testHistory(t, Helpers.class, "missing"); // Does not exist.
        t.examine(isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.fullBarrier();
        var call = (Recorder.Exception) history.get(0);
        t.examine(call.phase).is(ExceptionPhase.METHOD_LOOKUP);
        t.examine(call.context).isNull();
        t.examine(call.exception).isExactClass(
            NoSuchMethodException.class);
        t.examine(call.exception.getMessage())
            .is(Helpers.class.getName() + ".missing()");
        t.examine(call.expected).isNull();
    }

    // Test that InvokeTest records constructor exceptions.
    @Test
    public static void testConstructorException(Testing t) {
        List<Recorder.Call> history =
            testHistory(t, BrokenConstructor.class, "test");
        t.examine(isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.fullBarrier();
        var call = (Recorder.Exception) history.get(0);
        t.examine(call.phase).is(ExceptionPhase.SETUP);
        t.examine(call.context).isNull();
        t.examine(call.exception).isExactClass(
            UnsupportedOperationException.class);
        t.examine(call.exception.getMessage()).is("expected from constructor");
        t.examine(call.expected).isNull();

        // Constructor failures do not matter to static tests.
        t.examine(isSuccess(t, testHistory(
                    t, BrokenConstructor.class, "staticTest")))
            .is(true);
    }

    // Test that InvokeTest records class loading failures.
    @Test
    public static void testPackagePrivateClass(Testing t) {
        Optional<String> moduleName =
            Optional.ofNullable(Helpers.class.getModule())
            .map(Module::getName);
        // The named class does not exist.
        String className = Helpers.class.getName() + "$Missing";
        TestMethodLocator locator =
            new TestMethodLocator(moduleName, className, "missing", false);

        List<Recorder.Call> history = testHistory(t, locator);
        t.examine(isSuccess(t, history)).is(false);
        t.examine(history.size()).is(2);
        t.examine(history.get(0)).isExactClass(Recorder.Exception.class);
        t.fullBarrier();
        var call = (Recorder.Exception) history.get(0);
        t.examine(call.phase).is(ExceptionPhase.CLASS_LOOKUP);
        t.examine(call.context).isNull();
        t.examine(call.exception).isExactClass(
            ClassNotFoundException.class);
        t.examine(call.exception.getMessage()).is(className);
        t.examine(call.expected).isNull();
    }

    // Used by testSuccess().
    @Test
    public static void successStatic(Testing t) {
    }

    // Used by testSuccess().
    @Test
    public void success(Testing t) {
    }

    // Used by testSuccess().
    @Test
    public static void successPlainStatic() {
    }

    // Used by testSuccess().
    @Test
    public void successPlain() {
    }

    // Used by testExamineFailure().  Cannot be annotated with @Test
    // due to expected failure.
    public static void examineFailure(Testing t) {
        t.examine(true).is(false);
    }

    // Used by testAssertFail().  Cannot be annotated with @Test
    // due to expected failure.
    public static void assertFail(Testing t) {
        t.assertFail("expected");
    }

    // Used by testAssertFail().  Cannot be annotated with @Test
    // due to expected failure.
    public static void exception(Testing t) {
        throw new UnsupportedOperationException("expected");
    }

    // Used by testSetupFailure() to check constructor failure reporting.
    public static class BrokenConstructor {
        public BrokenConstructor() {
            throw new UnsupportedOperationException(
                "expected from constructor");
        }

        // Used by testSetupFailure().  Cannot be marked with @Test
        // due to expected failure.
        public void test() {
        }

        // Constructor failures do not matter to static tests.
        @Test
        public static void staticTest() {
        }
    }
}
