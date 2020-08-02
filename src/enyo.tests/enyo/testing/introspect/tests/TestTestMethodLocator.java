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
        t.assertEquals(Optional.of("enyo.tests"), tml.moduleName());
        t.assertEquals(TestTestMethodLocator.class.getName(), tml.className());
        t.assertEquals("testWithModuleName", tml.methodName());
        t.assertTrue(tml.hasTestingArgument());
        t.assertEquals("enyo.tests/enyo.testing.introspect.tests"
            + ".TestTestMethodLocator::testWithModuleName"
            + "(enyo.testing.Testing)", tml.toString());
    }

    @Test
    public static void testWithoutModuleName(Testing t) {
        TestMethodLocator tml = new TestMethodLocator(
            Optional.empty(),
            TestTestMethodLocator.class.getName(),
            "testOrder",
            false);
        t.assertEquals(Optional.empty(), tml.moduleName());
        t.assertEquals(TestTestMethodLocator.class.getName(), tml.className());
        t.assertEquals("testOrder", tml.methodName());
        t.assertFalse(tml.hasTestingArgument());
        t.assertEquals("enyo.testing.introspect.tests"
            + ".TestTestMethodLocator::testOrder()", tml.toString());
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
