package enyo.io.terminfo.tests;

import enyo.io.terminfo.KeyCapability;
import enyo.io.terminfo.TermFile;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.util.Comparator;

public final class TestKeyCapability {
    @Test
    public static void testIndex(Testing t) {
        // Check that the capabilities are sorted in increasing order
        // of their indices.
        t.examine(KeyCapability.values())
            .check(Checks.strictlyMonotonicArray(Comparator.comparing(
                        KeyCapability::index)));
    }

    @Test
    public static void testKey(Testing t) {
        // Check that all capabilities start with KEY_.
        for (KeyCapability cap : KeyCapability.values())
            t.examine(cap)
                .map(KeyCapability::toString)
                .test(s -> s.startsWith("KEY_"));
    }

    @Test
    public static void testAgainstTic(Testing t)
        throws IOException, InterruptedException {
        // Use terminfo identifiers in the terminfo source file.
        TestStringCapability.testAgainstTic(t, KeyCapability.class,
            KeyCapability::terminfoName, TermFile::get, false);
    }

    @Test
    public static void testAgainstTicC(Testing t)
        throws IOException, InterruptedException {
        // Use C identifiers in the terminfo source file.
        TestStringCapability.testAgainstTic(t, KeyCapability.class,
            KeyCapability::terminfoName, TermFile::get, true);
    }
}
