package enyo.sound.pcm.tests;

import enyo.io.BufferInput;
import enyo.sound.pcm.FrameFormat;
import enyo.sound.pcm.PCMTransformation;
import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class TestPCMTransformation {
    @Test
    public static void testFindMissing(Testing t) throws IOException {
        t.examine(PCMTransformation.find(
                new FrameFormat(16, 1, ByteOrder.LITTLE_ENDIAN),
                new FrameFormat(15, 1, ByteOrder.LITTLE_ENDIAN)::equals))
            .is(Optional.empty());
    }

    @Test
    public static void testFindNoop(Testing t) throws IOException {
        var cdda = new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN);
        t.examine(PCMTransformation.find(cdda, cdda::equals))
            .map(Optional::get)
            .map(PCMTransformation::source, e -> e.is(cdda))
            .map(PCMTransformation::destination, e -> e.is(cdda))
            .mapToInt(PCMTransformation::cost, e -> e.is(1));
        t.examine(PCMTransformation.find(cdda, cdda::equals, List.of()))
            .map(Optional::get)
            .map(PCMTransformation::source, e -> e.is(cdda))
            .map(PCMTransformation::destination, e -> e.is(cdda))
            .mapToInt(PCMTransformation::cost, e -> e.is(1));
    }

    @Test
    public static void testMonoToStereo(Testing t) throws IOException {
        var expected = new byte[][] {
            { 1, 1, 2, 2, 3, 3 },
            { 1, 2, 1, 2, 3, 4, 3, 4, 5, 6, 5, 6 },
            { 1, 2, 3, 1, 2, 3, 4, 5, 6, 4, 5, 6, 7, 8, 9, 7, 8, 9 },
            { 1, 2, 3, 4, 1, 2, 3, 4,
              5, 6, 7, 8,  5, 6, 7, 8,
              9, 10, 11, 12, 9, 10, 11, 12 },
        };

        for (var endian : List.of(ByteOrder.LITTLE_ENDIAN,
                ByteOrder.BIG_ENDIAN))
            for (int inputSize = 1; inputSize <= 4; ++inputSize) {
                check(t,
                    new FrameFormat(inputSize * 8, 1, endian),
                    new FrameFormat(inputSize * 8, 2, endian),
                    inputSize, expected[inputSize - 1]);
            }
    }

    @Test
    public static void testPad(Testing t) throws IOException {
        check(t,
            new FrameFormat(8, 1, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(16, 1, ByteOrder.LITTLE_ENDIAN),
            1, new byte[] { 0, 1, 0, 2, 0, 3 });
        check(t,
            new FrameFormat(8, 2, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN),
            2, new byte[] { 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6 });
        check(t,
            new FrameFormat(8, 1, ByteOrder.BIG_ENDIAN),
            new FrameFormat(16, 1, ByteOrder.BIG_ENDIAN),
            1, new byte[] { 1, 0, 2, 0, 3, 0 });
        check(t,
            new FrameFormat(8, 2, ByteOrder.BIG_ENDIAN),
            new FrameFormat(16, 2, ByteOrder.BIG_ENDIAN),
            2, new byte[] { 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0 });
        check(t,
            new FrameFormat(16, 1, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(24, 1, ByteOrder.LITTLE_ENDIAN),
            2, new byte[] { 0, 1, 2, 0, 3, 4, 0, 5, 6 });
        check(t,
            new FrameFormat(16, 2, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(24, 2, ByteOrder.LITTLE_ENDIAN),
            4, new byte[] { 0, 1, 2, 0, 3, 4, 0, 5, 6,
                            0, 7, 8, 0, 9, 10, 0, 11, 12 });
        check(t,
            new FrameFormat(16, 1, ByteOrder.BIG_ENDIAN),
            new FrameFormat(24, 1, ByteOrder.BIG_ENDIAN),
            2, new byte[] { 1, 2, 0, 3, 4, 0, 5, 6, 0 });
        check(t,
            new FrameFormat(16, 2, ByteOrder.BIG_ENDIAN),
            new FrameFormat(24, 2, ByteOrder.BIG_ENDIAN),
            4, new byte[] { 1, 2, 0, 3, 4, 0, 5, 6, 0,
                            7, 8, 0, 9, 10, 0, 11, 12, 0 });
        check(t,
            new FrameFormat(24, 1, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(32, 1, ByteOrder.LITTLE_ENDIAN),
            3, new byte[] { 0, 1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9 });
        check(t,
            new FrameFormat(24, 2, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(32, 2, ByteOrder.LITTLE_ENDIAN),
            6, new byte[] { 0, 1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9,
                            0, 10, 11, 12, 0, 13, 14, 15, 0, 16, 17, 18 });
        check(t,
            new FrameFormat(24, 1, ByteOrder.BIG_ENDIAN),
            new FrameFormat(32, 1, ByteOrder.BIG_ENDIAN),
            3, new byte[] { 1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9, 0 });
        check(t,
            new FrameFormat(24, 2, ByteOrder.BIG_ENDIAN),
            new FrameFormat(32, 2, ByteOrder.BIG_ENDIAN),
            6, new byte[] { 1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9, 0,
                            10, 11, 12, 0, 13, 14, 15, 0, 16, 17, 18, 0 });
    }

    @Test
    public static void testMultiple(Testing t) throws IOException {
        check(t,
            new FrameFormat(8, 1, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(16, 2, ByteOrder.BIG_ENDIAN),
            1, new byte[] { 1, 0, 1, 0, 2, 0, 2, 0, 3, 0, 3, 0 });
        check(t,
            new FrameFormat(8, 1, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(24, 2, ByteOrder.BIG_ENDIAN),
            1, new byte[] { 1, 0, 0, 1, 0, 0, 2, 0, 0, 2, 0, 0,
                            3, 0, 0, 3, 0, 0 });
        check(t,
            new FrameFormat(16, 1, ByteOrder.BIG_ENDIAN),
            new FrameFormat(24, 2, ByteOrder.LITTLE_ENDIAN),
            2, new byte[] { 0, 2, 1, 0, 2, 1, 0, 4, 3, 0, 4, 3,
                            0, 6, 5, 0, 6, 5 });
        check(t,
            new FrameFormat(16, 1, ByteOrder.LITTLE_ENDIAN),
            new FrameFormat(24, 2, ByteOrder.BIG_ENDIAN),
            2, new byte[] { 2, 1, 0, 2, 1, 0, 4, 3, 0, 4, 3, 0,
                            6, 5, 0, 6, 5, 0 });
    }

    @Test
    public static void testSwapByteOrder(Testing t) throws IOException {
        var endians = new ByteOrder[] {
                ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN };

        for (int idx = 0 ; idx < 2; ++idx) {
            check(t,
                new FrameFormat(8, 1, endians[idx]),
                new FrameFormat(8, 1, endians[1 - idx]),
                1, new byte[] { 1, 2, 3 });
            check(t,
                new FrameFormat(8, 2, endians[idx]),
                new FrameFormat(8, 2, endians[1 - idx]),
                2, new byte[] { 1, 2, 3, 4, 5, 6 });
            check(t,
                new FrameFormat(16, 1, endians[idx]),
                new FrameFormat(16, 1, endians[1 - idx]),
                2, new byte[] { 2, 1, 4, 3, 6, 5 });
            check(t,
                new FrameFormat(16, 2, endians[idx]),
                new FrameFormat(16, 2, endians[1 - idx]),
                4, new byte[] { 2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11 });
            check(t,
                new FrameFormat(24, 1, endians[idx]),
                new FrameFormat(24, 1, endians[1 - idx]),
                3, new byte[] { 3, 2, 1, 6, 5, 4, 9, 8, 7 });
            check(t,
                new FrameFormat(24, 2, endians[idx]),
                new FrameFormat(24, 2, endians[1 - idx]),
                6, new byte[] { 3, 2, 1, 6, 5, 4, 9, 8, 7,
                                12, 11, 10, 15, 14, 13, 18, 17, 16 });
            check(t,
                new FrameFormat(32, 1, endians[idx]),
                new FrameFormat(32, 1, endians[1 - idx]),
                4, new byte[] { 4, 3, 2, 1, 8, 7, 6, 5, 12, 11, 10, 9 });
            check(t,
                new FrameFormat(32, 2, endians[idx]),
                new FrameFormat(32, 2, endians[1 - idx]),
                8, new byte[] { 4, 3, 2, 1, 8, 7, 6, 5, 12, 11, 10, 9,
                                16, 15, 14, 13, 20, 19, 18, 17,
                                24, 23, 22, 21 });
        }
    }

    // Generates three input frames and checks that they convert to
    // the expected bytes.
    private static void check(Testing t,
        FrameFormat source, FrameFormat destination,
        int inputFrameSize, byte[] expected) throws IOException {
        check(t, source, destination, inputFrameSize, expected, false);
        check(t, source, destination, inputFrameSize, expected, true);
    }

    private static void check(Testing t,
        FrameFormat source, FrameFormat destination,
        int inputFrameSize, byte[] expected, boolean zeroMinRemaining)
        throws IOException {
        var transformation = PCMTransformation.find(
            source, destination::equals)
            .orElseGet(() -> {
                    t.failedCheck("PCMTransformation.find()",
                        source, destination);
                    t.fullBarrier();
                    return null;
                });
        t.examine(transformation)
            .map(PCMTransformation::source)
            .is(source);
        t.examine(transformation)
            .map(PCMTransformation::destination)
            .is(destination);
        t.examine(transformation)
            .mapToInt(PCMTransformation::cost)
            .check(Checks.greaterThan(0));

        var input = new byte[inputFrameSize * 3];
        for (int i = 0; i < input.length; ++i)
            input[i] = (byte) (i + 1);
        var out = transformation.transformer().apply(BufferInput.wrap(input));
        var actual = new byte[expected.length];
        int outFrameSize = expected.length / 3;
        for (int i = 0; i < actual.length; ) {
            var buffer = out.fillBuffer(zeroMinRemaining ? 0 : 1);
            t.examine(buffer)
                .mapToInt(ByteBuffer::remaining)
                .check(Checks.greaterThanOrEquals(outFrameSize));
            for (int j = 0; j < outFrameSize; ++j)
                actual[i++] = buffer.get();
        }
        t.examine(out.fillBuffer(0))
            .mapToInt(ByteBuffer::remaining)
            .is(0);
        t.examine(actual)
            .check(Checks.byteArrayEquals(expected));
    }
}
