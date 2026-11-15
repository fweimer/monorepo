package enyo.sound.flac.tests;

import enyo.io.BufferInput;
import enyo.io.function.IOFunction;
import enyo.sound.flac.BitInput;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class TestBitInput {
    @Test
    public static void test2(Testing t) throws IOException {
        var src = new byte[128];
        Arrays.fill(src, (byte) 27);
        var bits = new BitInput(BufferInput.wrap(src));
        for (int j = 0; j < 128; ++j)
            for (int i = 0; i < 4; ++i)
                t.examine(bits.getBits(2)).is(i);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }

    @Test
    public static void test3(Testing t) throws IOException {
        var src = new ByteArrayOutputStream();
        src.write(new byte[] { 5, 57, 119 });
        src.write(src.toByteArray());
        src.write(src.toByteArray());
        src.write(src.toByteArray());
        var bits = new BitInput(BufferInput.wrap(src.toByteArray()));
        for (int j = 0; j < 8; ++j)
            for (int i = 0; i < 8; ++i)
                t.examine(bits.getBits(3)).is(i);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }

    @Test
    public static void test4(Testing t) throws IOException {
        var src = new ByteArrayOutputStream();
        src.write(new byte[] { 1, 35, 69, 103, -119, -85, -51, -17 });
        src.write(src.toByteArray());
        src.write(src.toByteArray());
        src.write(src.toByteArray());
        var bits = new BitInput(BufferInput.wrap(src.toByteArray()));
        for (int j = 0; j < 8; ++j)
            for (int i = 0; i < 16; ++i)
                t.examine(bits.getBits(4)).is(i);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }

    @Test
    public static void test5(Testing t) throws IOException {
        var src = new ByteArrayOutputStream();
        src.write(new byte[] {
                0, 68, 50, 20, -57, 66, 84, -74, 53, -49, -124, 101, 58,
                86, -41, -58, 117, -66, 119, -33
            });
        src.write(src.toByteArray());
        src.write(src.toByteArray());
        src.write(src.toByteArray());
        var bits = new BitInput(BufferInput.wrap(src.toByteArray()));
        for (int j = 0; j < 8; ++j)
            for (int i = 0; i < 32; ++i)
                t.examine(bits.getBits(5)).is(i);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }


    @Test
    public static void testExpGolomb20_0(Testing t) throws IOException {
        var src = new ByteArrayOutputStream();
        src.write(new byte[] {
                -112, -127, 0, -128, 16, 0, -128, 1, 0, 0, -128, 0, 16,
                0, 0, -128, 0, 1, 0, 0, 0, -128, 0, 0, 16, 0, 0, 0, -128,
                0, 0, 1, 0, 0, 0, 0, -128, 0, 0, 0, 16, 0, 0, 0, 0, -128,
                0, 0, 0, 1
            });
        var bits = new BitInput(BufferInput.wrap(src.toByteArray()));
        for (int i = 0; i < 20; ++i)
            t.examine(bits.getRice(0)).is(i);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }

    @Test
    public static void testExpGolomb20_6(Testing t) throws IOException {
        var src = new ByteArrayOutputStream();
        src.write(new byte[] {
                -128, -124, 68, 17, -126, 64, 37, 1, 48, 4, -32, 10, 0,
                10, 64, 5, 64, 1, 88, 0, 44, 0, 2, -48, 0, 23, 0, 0, 94,
                0, 0, -64, 0, 0, -60, 0, 0, 100, 0, 0, 25, -128,
            });
        var bits = new BitInput(BufferInput.wrap(src.toByteArray()));
        for (int i = 0; i < 20; ++i)
            t.examine(bits.getRice(6)).is((i << 5) | i);
        t.examine(bits.getBits(6)).is(0);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }

    @Test
    public static void testExpGolomb130_1(Testing t) throws IOException {
        var src = new ByteArrayOutputStream();
        src.write(new byte[] {
                -111, 8, 32, 64, 64, 32, 8, 1, 0, 16, 0, -128, 2, 0,
                4, 0, 4, 0, 2, 0, 0, -128, 0, 16, 0, 1, 0, 0, 8, 0,
                0, 32, 0, 0, 64, 0, 0, 64, 0, 0, 32, 0, 0, 8, 0, 0,
                1, 0, 0, 0, 16, 0, 0, 0, -128, 0, 0, 2, 0, 0, 0, 4,
                0, 0, 0, 4, 0, 0, 0, 2, 0, 0, 0, 0, -128, 0, 0, 0,
                16, 0, 0, 0, 1, 0, 0, 0, 0, 8, 0, 0, 0, 0, 32, 0, 0,
                0, 0, 64, 0, 0, 0, 0, 64, 0, 0, 0, 0, 32, 0, 0, 0,
                0, 8, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0,
                0, -128, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 4, 0, 0, 0,
                0, 0, 4, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, -128,
                0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
                0, 8, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 64, 0,
                0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0,
                0, 0, 8, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
                16, 0, 0, 0, 0, 0, 0, 0, -128, 0, 0, 0, 0, 0, 0, 2,
                0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 4, 0,
                0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, -128,
                0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 1, 0,
                0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 32,
                0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0,
                64, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0,
                0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, -128, 0, 0,
                0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -128, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -128, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -128, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16
            });
        var bits = new BitInput(BufferInput.wrap(src.toByteArray()));
        for (int i = 0; i < 130; ++i)
            t.examine(bits.getRice(1)).is(i);
        t.examine(bits.getBits(3)).is(0);
        t.examine(bits.unwrap())
            .map(IOFunction.of(in -> in.fillBuffer(0)))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
    }
}
