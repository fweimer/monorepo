package enyo.sound.alsa.tests;

import enyo.linux.alsa.PCM;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

public final class TestPCMAccess {
    @Test
    public static void testOrdinal(Testing t) {
        testOrdinal(t, "MMAP_INTERLEAVED", 0);
        testOrdinal(t, "MMAP_NONINTERLEAVED", 1);
        testOrdinal(t, "MMAP_COMPLEX", 2);
        testOrdinal(t, "RW_INTERLEAVED", 3);
        testOrdinal(t, "RW_NONINTERLEAVED", 4);
    }

    private static void testOrdinal(Testing t, String name, int value) {
        t.examine(name)
            .map(PCM.Access::valueOf)
            .mapToInt(PCM.Access::ordinal)
            .is(value);
    }
}
