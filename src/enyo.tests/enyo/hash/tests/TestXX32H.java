package enyo.hash.tests;

import enyo.hash.XXH32;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class TestXX32H {
    private static final int SEED = 0xf0e1d2b3;

    @Test
    public static void testEmpty(Testing t) {
        for (var empty : List.of(
                MemorySegment.ofArray(new byte[0]),
                MemorySegment.ofArray(new byte[1]).asSlice(1))) {
            XXH32 h = new XXH32();
            t.examine(h.finish()).is(0x2cc5d05);

            h = new XXH32(0);
            h.update(empty);
            t.examine(h.finish()).is(0x2cc5d05);

            h = new XXH32(1);
            h.update(empty);
            t.examine(h.finish()).is(0xb2cb792);

            h.reset(SEED);
            h.update(empty);
            t.examine(h.finish()).is(0x4d57cc10);
        }
    }

    private static final byte[] BYTES = {
        0, 1, 2, 17, 63, 64, 65, 109, 126, 127, (byte) 128,
        (byte) 129, (byte) 151, (byte) 191, (byte) 192,
        (byte) 193, (byte) 253, (byte) 245, (byte) 255 };

    @Test
    public static void testOne(Testing t) {
        int[] seed0 = {
            0xcf65b03e, 0x3892f731, 0xd5403278, 0xb804f774, 0xfa21d689,
            0x54ba69c1, 0x10659a4d, 0x305b2089, 0x580a02a4, 0xd13ceeff,
            0x2dbed8f6, 0xe4c6c134, 0x6fb02018, 0xeec39d11, 0x7109a965,
            0x7a949bc, 0x12b23d40, 0x30ca3aeb, 0x96bcd8cc };
        int[] seed1 = {
            0x2db596b, 0x26281093, 0x6d33c977, 0x32812d35, 0xa64f4c77,
            0x7705b83d, 0xd7e0b3c2, 0xe6b70d33, 0xa7474e8e, 0x96216170,
            0xf80f72b2, 0xcb543695, 0x74b5cd48, 0x1bdeeac8, 0xc64bb980,
            0x76df6939, 0x5faf7413, 0x1bc21e51, 0xd710150 };
        int[] seedf = {
            0x3f957d73, 0xf89e4b96, 0x1d736bd2, 0xb1d57dde, 0xb4ff8c9b,
            0x3ea215d4, 0x10691303, 0x63294b58, 0xb92a7bb2, 0xe2a5d583,
            0xe3da3efb, 0xc3406217, 0x831597f, 0x1ee8bdfa, 0xedd19484,
            0x7b4378d5, 0xd2739c2f, 0xe3d72757, 0x80647cef };
        for (int i = 0; i < BYTES.length; ++i) {
            t.examine(i)
                .mapToObj(i0 -> {
                        var h = new XXH32();
                        h.update(MemorySegment.ofArray(
                                new byte[] {BYTES[i0]}));
                        return h;
                    })
                .mapToInt(XXH32::finish)
                .is(seed0[i]);
            int seed1_ = seed1[i];
            t.examine(i)
                .mapToObj(i0 -> {
                        var h = new XXH32(1);
                        h.update(MemorySegment.ofArray(
                                new byte[] {BYTES[i0]}));
                        return h;
                    })
                .map(XXH32::finish, e -> e.is(seed1_))
                .mapToInt(XXH32::finish)
                .is(seed1_);
            t.examine(i)
                .mapToObj(i0 -> {
                        var h = new XXH32();
                        h.update(MemorySegment.ofArray(new byte[1]));
                        h.reset(SEED);
                        h.update(MemorySegment.ofArray(new byte[0]));
                        h.update(MemorySegment.ofArray(
                                new byte[] {BYTES[i0]}));
                        h.update(MemorySegment.ofArray(new byte[0]));
                        return h;
                    })
                .mapToInt(XXH32::finish)
                .is(seedf[i]);
        }
    }

    @Test
    public static void testBytes(Testing t) {
        var seg = MemorySegment.ofArray(BYTES);

        XXH32 h = new XXH32();
        h.update(seg);
        t.examine(h.finish()).is(0xcf3de516);

        h = new XXH32();
        h.update(seg.asSlice(0, 4));
        t.examine(h.finish()).is(0x2d31068e);

        h.reset(1);
        h.update(seg.asSlice(1));
        t.examine(h.finish()).is(0x2942cd96);

        h.reset(SEED);
        h.update(seg);
        t.examine(h.finish()).is(0x5e63327b);
        h.update(seg);
        t.examine(h.finish()).is(0x4a456a14);

        h.reset(SEED);
        for (int i = 0; i < 255; ++i)
            h.update(seg);
        t.examine(h.finish()).is(0xe37bc2a6);

        h.reset(0);
        for (byte b: BYTES)
            h.update(b);
        t.examine(h.finish()).is(0xcf3de516);

        h.reset(0);
        h.update(BYTES);
        t.examine(h.finish()).is(0xcf3de516);

        h.reset(0);
        h.update(BYTES, 0, 4);
        t.examine(h.finish()).is(0x2d31068e);

        h.reset(1);
        h.update(BYTES, 1, BYTES.length - 1);
        t.examine(h.finish()).is(0x2942cd96);
    }
}
