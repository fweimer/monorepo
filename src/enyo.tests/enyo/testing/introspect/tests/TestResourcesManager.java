package enyo.testing.introspect.tests;

import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.introspect.TestResources;
import enyo.testing.introspect.ResourcesManager;

public final class TestResourcesManager {
    @Test
    public static void testDefault(Testing t) throws InterruptedException {
        ResourcesManager rm = new ResourcesManager();
        Flag flag = new Flag(t);
        rm.withResources(TestResources.DEFAULT, () -> {
                flag.setOnce();
            });
        flag.assertSet();
    }

    @Test
    public static void testLarge(Testing t) throws InterruptedException {
        ResourcesManager rm = new ResourcesManager();
        TestResources tr = rm.available();
        Flag flag = new Flag(t);
        rm.withResources(tr, () -> {
                flag.setOnce();
            });
        flag.assertSet();
    }

    @Test
    public static void testImpossiblyLarge(Testing t)
        throws InterruptedException {
        ResourcesManager rm = new ResourcesManager();
        TestResources tr = new TestResources(
            rm.available().threads() + 1,
            rm.available().memory() + 1);
        Flag flag = new Flag(t);
        rm.withResources(tr, () -> {
                flag.setOnce();
            });
        flag.assertSet();
    }

    private static final class Flag {
        Flag(Testing t) {
            this.t = t;
        }

        private final Testing t;
        private boolean flag;

        void setOnce() {
            t.assertFalse(flag);
            flag = true;
        }

        void clear() {
            flag = false;
        }

        void assertSet() {
            t.assertTrue(flag);
        }
    }
}
