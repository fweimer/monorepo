package enyo.testing.introspect.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.TestMethodLocator;

import java.util.Optional;

public final class TestTestMethodLocator {
    @Test
    public static void testWithModuleName(Testing t) {
        TestMethodLocator tml = new TestMethodLocator(
            Optional.of("enyo.tests"),
            TestTestMethodLocator.class.getName(),
            "testWithModuleName",
            true);
        t.examine(tml.moduleName()).is(Optional.of("enyo.tests"));
        t.examine(tml.className()).is(TestTestMethodLocator.class.getName());
        t.examine(tml.methodName()).is("testWithModuleName");
        t.assertTrue(tml.hasTestingArgument());
        t.examine(tml.toString())
            .is("enyo.tests/enyo.testing.introspect.tests"
                + ".TestTestMethodLocator::testWithModuleName"
                + "(enyo.testing.Testing)");
    }

    @Test
    public static void testWithoutModuleName(Testing t) {
        TestMethodLocator tml = new TestMethodLocator(
            Optional.empty(),
            TestTestMethodLocator.class.getName(),
            "testOrder",
            false);
        t.examine(tml.moduleName()).is(Optional.empty());
        t.examine(tml.className()).is(TestTestMethodLocator.class.getName());
        t.examine(tml.methodName()).is("testOrder");
        t.assertFalse(tml.hasTestingArgument());
        t.examine(tml.toString())
            .is("enyo.testing.introspect.tests"
                + ".TestTestMethodLocator::testOrder()");
    }

    @Test
    public static void testOrder() {
        Order<TestMethodLocator> order = new Order<>();
        order.add(new TestMethodLocator(
                Optional.empty(),
                "enyo.testing.introspect.tests.TestTestMethodLocator",
                "testWithModuleName",
                false));
        order.add(new TestMethodLocator(
                Optional.empty(),
                "enyo.testing.introspect.tests.TestTestMethodLocator",
                "testWithModuleName",
                true));
        order.add(new TestMethodLocator(
                Optional.of("enyo.test"),
                "enyo.testing.introspect.tests.TestTestMethodLocato",
                "testWithoutModuleName",
                false));
        order.add(new TestMethodLocator(
                Optional.of("enyo.tests"),
                "enyo.testing.introspect.tests.TestTestMethodLocato",
                "testWithoutModuleName",
                false));
        order.add(new TestMethodLocator(
                Optional.of("enyo.tests"),
                "enyo.testing.introspect.tests.TestTestMethodLocator",
                "testWithModuleName",
                false));
        order.add(new TestMethodLocator(
                Optional.of("enyo.tests"),
                "enyo.testing.introspect.tests.TestTestMethodLocator",
                "testWithModuleName",
                true));
        order.add(new TestMethodLocator(
                Optional.of("enyo.tests"),
                "enyo.testing.introspect.tests.TestTestMethodLocator",
                "testWithoutModuleName",
                false));
        order.checkComparable(TestMethodLocator.class);
    }
}
