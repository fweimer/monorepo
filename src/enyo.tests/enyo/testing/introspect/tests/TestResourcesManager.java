package enyo.testing.introspect.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.TestResources;
import enyo.testing.introspect.ResourcesManager;

public final class TestResourcesManager {
    @Test
    public static void testDefault(Testing t) throws InterruptedException {
        ResourcesManager rm = new ResourcesManager();
        var counter = t.counter();
        rm.withResources(TestResources.DEFAULT, () -> {
                counter.increment();
            });
        counter.checkTarget();
    }

    @Test
    public static void testLarge(Testing t) throws InterruptedException {
        ResourcesManager rm = new ResourcesManager();
        TestResources tr = rm.available();
        var counter = t.counter();
        rm.withResources(tr, () -> {
                counter.increment();
            });
        counter.checkTarget();
    }

    @Test
    public static void testImpossiblyLarge(Testing t)
        throws InterruptedException {
        ResourcesManager rm = new ResourcesManager();
        TestResources tr = new TestResources(
            rm.available().threads() + 1,
            rm.available().memory() + 1);
        var counter = t.counter();
        rm.withResources(tr, () -> {
                counter.increment();
            });
        counter.checkTarget();
    }
}
