package enyo.testing.introspect.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.TestResources;

public final class TestTestResources {
    @Test
    public static void test(Testing t) {
        TestResources tr = new TestResources(1, 2);
        t.assertEquals(1, tr.threads());
        t.assertEquals(2, tr.memory());
        t.assertEquals("threads=1 memory=2", tr.toString());
    }

    @Test
    public static void testIllegalArguments(Testing t) {
        t.expect(() -> new TestResources(0, 2),
            IllegalArgumentException.class);
        t.expect(() -> new TestResources(-1, 2),
            IllegalArgumentException.class);
        t.expect(() -> new TestResources(1, 0),
            IllegalArgumentException.class);
        t.expect(() -> new TestResources(1, -1),
            IllegalArgumentException.class);
    }
}
