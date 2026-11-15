package enyo.sound.pcm.tests;

import enyo.sound.pcm.PCMFormat;
import enyo.sound.pcm.FrameFormat;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.ByteOrder;

public final class TestPCMFormat {
    public static void testIllegal(Testing t) {
        t.expect(() -> new PCMFormat(null, 1), NullPointerException.class);
        t.expect(() -> new PCMFormat(
                new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN), 0),
            IllegalArgumentException.class);
        t.expect(() -> new PCMFormat(
                new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN), -1),
            IllegalArgumentException.class);
    }
}
