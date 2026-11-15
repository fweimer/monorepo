package enyo.sound.pcm.tests;

import enyo.sound.pcm.FrameFormat;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.ByteOrder;

public final class TestFrameFormat {
    @Test
    public static void testToString(Testing t) {
        t.examine(new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN)
            .toString()).is("FrameFormat(bitsPersample=16, channels=2,"
                + " order=LITTLE_ENDIAN)");
    }

    @Test
    public static void testFrameSizeInBytes(Testing t) {
        t.examine(new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN))
            .mapToInt(FrameFormat::frameSizeInBytes)
            .is(4);
        t.examine(new FrameFormat(24, 2, ByteOrder.LITTLE_ENDIAN))
            .mapToInt(FrameFormat::frameSizeInBytes)
            .is(6);
        t.examine(new FrameFormat(24, 1, ByteOrder.LITTLE_ENDIAN))
            .mapToInt(FrameFormat::frameSizeInBytes)
            .is(3);
        t.examine(new FrameFormat(1 << 16, 1 << 16, ByteOrder.LITTLE_ENDIAN))
            .mapToInt(FrameFormat::frameSizeInBytes)
            .is(1 << 29);
        t.examine(new FrameFormat(1 << 16, 1 << 16, ByteOrder.LITTLE_ENDIAN))
            .mapToInt(FrameFormat::frameSizeInBytes)
            .is(1 << 29);
        t.expect(() ->
            new FrameFormat(1 << 17, 1 << 17, ByteOrder.LITTLE_ENDIAN)
            .frameSizeInBytes(),
            IllegalStateException.class);
        t.expect(() ->
            new FrameFormat(18, 2, ByteOrder.LITTLE_ENDIAN)
            .frameSizeInBytes(),
            IllegalStateException.class);
    }

    @Test
    public static void testHashCode(Testing t) {
        // The standard formats shouldn't have trivial collisions.
        var buckets = new FrameFormat[1024];
        int mask = buckets.length - 1;
        for (var format : FrameFormat.standardFormats()) {
            t.examine(format)
                .mapToInt(fmt -> fmt.hashCode() & mask)
                .mapToObj(idx -> buckets[idx])
                .isNull();
            buckets[format.hashCode() & mask] = format;
        }
    }

    @Test
    public static void testStandardFormats(Testing t) {
        t.examine(FrameFormat.standardFormats().contains(
                new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN)))
            .is(true);
    }

    @Test
    public static void testOrder() {
        var order = new Order<FrameFormat>();
        for (var format : FrameFormat.standardFormats())
            order.add(format);
        order.group()
            .add(new FrameFormat(20, 2, ByteOrder.LITTLE_ENDIAN))
            .add(new FrameFormat(20, 2, ByteOrder.LITTLE_ENDIAN))
            .close();
        order.check();
    }
}
