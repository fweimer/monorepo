package enyo.testing.introspect.tests;

import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.ScheduledTest;
import enyo.testing.introspect.TestMethodLocator;
import enyo.testing.introspect.TestResources;

import java.util.Optional;

public final class TestScheduledTest {
    @Test
    public static void test(Testing t) {
        TestMethodLocator tml = new TestMethodLocator(
            Optional.of("enyo.tests"),
            TestTestMethodLocator.class.getName(),
            "testWithModuleName",
            true);
        TestResources tr = new TestResources(1, 2);
        ScheduledTest st = new ScheduledTest(17, tml, tr, false);
        t.examine(st.id()).is(17);
        t.assertSame(tml, st.locator());
        t.assertSame(tr, st.resources());
        t.examine(st.toString())
            .is("enyo.tests/enyo.testing.introspect.tests"
                + ".TestTestMethodLocator::testWithModuleName"
                + "(enyo.testing.Testing) [17] threads=1 memory=2");
    }

    @Test
    public static void testNullPointerException(Testing t) {
        t.expect(() -> new ScheduledTest(17, null, TestResources.DEFAULT, false),
            NullPointerException.class);
        TestMethodLocator tml = new TestMethodLocator(
            Optional.of("enyo.tests"),
            TestTestMethodLocator.class.getName(),
            "testWithModuleName",
            true);
        t.expect(() -> new ScheduledTest(17, tml, null, false),
            NullPointerException.class);
    }
}
