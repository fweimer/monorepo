package enyo.io.tests;

import enyo.io.ExtendedCharsets;
import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExitDuringTest
public final class TestUTF8SCharset {
    @Test
    public static void testForName(Testing t) {
        t.assertSame(ExtendedCharsets.UTF_8S, Charset.forName("UTF-8S"));
        t.assertSame(ExtendedCharsets.UTF_8S, Charset.forName("UTF-8b"));
    }

    @Test
    @Cost(0.3)
    public void testEncoderCoverage2Surrogates(Testing t) {
        char[] array = new char[2];
        CharBuffer in = CharBuffer.wrap(array);
        ByteBuffer out = ByteBuffer.allocate(4);
        for (char ch1 = Character.MIN_HIGH_SURROGATE;
             ch1 <= Character.MAX_HIGH_SURROGATE; ++ch1)
            for (char ch2 = Character.MIN_LOW_SURROGATE;
                 ch2 <= Character.MAX_LOW_SURROGATE; ++ch2) {
                in.clear();
                out.clear();
                array[0] = ch1;
                array[1] = ch2;

                var encoder = ExtendedCharsets.UTF_8S.newEncoder();
                t.assertEquals(CoderResult.UNDERFLOW,
                    encoder.encode(in, out, true));
                t.assertEquals(4, out.position());
                byte[] expected = String.valueOf(array).getBytes(
                    StandardCharsets.UTF_8);
                out.flip();
                byte[] actual = new byte[out.remaining()];
                out.get(actual);
                t.assertByteArrayEquals(expected, actual);

                // Feed the data piecewise.
                encoder = ExtendedCharsets.UTF_8S.newEncoder();
                in.clear();
                out.clear();
                Arrays.fill(out.array(), (byte) 0);
                in.limit(1);
                t.assertEquals(CoderResult.UNDERFLOW,
                    encoder.encode(in, out, false));
                // The position should not have advanced.
                t.assertEquals(0, in.position());
                t.assertEquals(0, out.position());
                // Feed both characters.
                in.limit(2);
                t.assertEquals(CoderResult.UNDERFLOW,
                    encoder.encode(in, out, true));
                t.assertEquals(4, out.position());
                out.flip();
                out.get(actual);
                t.assertByteArrayEquals(expected, actual);
            }
    }

    @Test
    public static void testEscape(Testing t) {
        t.assertEquals('\udc80', escape((byte) 0x80));
        t.assertEquals('\udc81', escape((byte) 0x81));
        t.assertEquals('\udcfe', escape((byte) 0xfe));
        t.assertEquals('\udcff', escape((byte) 0xff));
    }

    static char escape(byte b) {
        int value = Byte.toUnsignedInt(b);
        if (value < 0x80)
            throw new AssertionError();
        return (char) (0xDC00 + value);
    }

    static void feedComposition(Testing t, ByteBuffer in, CharBuffer out,
        CharsetDecoder decoder, List<Integer> inComposition,
        List<Integer> outComposition) {
        int inLimit = in.limit();
        in.limit(in.position());
        int inCompositionSize = inComposition.size();
        int outLimit = out.limit();
        out.limit(out.position() + outComposition.get(0));
        int outCompositionIndex = 1;
        for (int inCompositionIndex = 0; inCompositionIndex < inCompositionSize;
             ++inCompositionIndex) {
            int length = inComposition.get(inCompositionIndex);
            if (in.hasRemaining())
                // If the input was not fully consumed, we cannot
                // advance the position.
                in.limit(in.limit() + length);
            else {
                int oldLimit = in.limit();
                in.limit(oldLimit + length);
                in.position(oldLimit);
            }
            t.assertTrue(in.limit() <= inLimit);
            while (true) {
                boolean end = inCompositionIndex + 1 == inCompositionSize;
                CoderResult result = decoder.decode(in, out, end);
                if (result == CoderResult.UNDERFLOW)
                    break;
                t.assertEquals(CoderResult.OVERFLOW, result);
                out.limit(out.limit()
                    + outComposition.get(outCompositionIndex));
                ++outCompositionIndex;
                t.assertTrue(out.limit() <= outLimit);
            }
        }
        t.assertEquals(0, in.remaining());
        while (true) {
            CoderResult result = decoder.flush(out);
            if (result == CoderResult.UNDERFLOW)
                break;
            t.assertEquals(CoderResult.OVERFLOW, result);
            out.limit(out.limit()
                + outComposition.get(outCompositionIndex));
            ++outCompositionIndex;
            t.assertTrue(out.limit() <= outLimit);
        }
    }

    @Test
    public static void testFeedComposition1(Testing t) {
        testFeedComposition(t, "ABCDEF");
    }

    @Test
    public static void testFeedComposition2(Testing t) {
        testFeedComposition(t, "AB\u00FFX");
    }

    @Test
    public static void testFeedComposition3(Testing t) {
        testFeedComposition(t, "AB\u0800D");
    }

    @Test
    public static void testFeedComposition4(Testing t) {
        testFeedComposition(t, "AB\u0800");
    }

    private static void testFeedComposition(Testing t, String expected) {
        byte[] array = expected.getBytes(StandardCharsets.UTF_8);
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(expected.length());
        for (List<Integer> inComposition : compositions(array.length))
            for (List<Integer> outComposition : compositions(out.capacity())) {
                in.clear();
                out.clear();
                feedComposition(t, in, out, StandardCharsets.UTF_8.newDecoder(),
                    inComposition, outComposition);
                t.assertEquals(0, out.remaining());
                out.clear();
                t.assertEquals(expected, out.toString());
            }
    }

    /**
     * Computes the compositions of the argument: all tuples of
     * positive integers whose sum is the argument.
     */
    static List<List<Integer>> compositions(int n) {
        if (n < 0)
            throw new IllegalArgumentException("n: " + n);
        var accumulator = new ArrayList<List<Integer>>();
        var current = new ArrayList<Integer>();
        compositions0(n, current, accumulator);
        return List.copyOf(accumulator);
    }

    private static void compositions0(int n, ArrayList<Integer> current,
        ArrayList<List<Integer>> accumulator) {
        int index = current.size();
        current.add(0);
        for (int i = 1; i < n; ++i) {
            current.set(index, i);
            compositions0(n - i, current, accumulator);
        }
        current.set(index, n);
        accumulator.add(List.copyOf(current));
        current.remove(index);
    }

    private static List<ByteBuffer> decompose(
        ByteBuffer buffer, List<Integer> composition) {
        buffer = buffer.slice();
        var result = new ArrayList<ByteBuffer>();
        int position = 0;
        for (int length : composition) {
            buffer.limit(position + length);
            buffer.position(position);
            result.add(buffer.slice());
            position += length;
        }
        return List.copyOf(result);
    }

    @Test
    public static void testCompositions(Testing t) {
        t.expect(() -> compositions(-1),
            IllegalArgumentException.class);
        t.assertEquals(List.of(List.of(0)), compositions(0));
        t.assertEquals(List.of(List.of(1)), compositions(1));
        t.assertEquals(List.of(List.of(1, 1), List.of(2)), compositions(2));
        t.assertEquals(List.of(List.of(1, 1, 1), List.of(1, 2), List.of(2, 1),
                List.of(3)), compositions(3));
        t.assertEquals(List.of(
                List.of(1, 1, 1, 1),
                List.of(1, 1, 2),
                List.of(1, 2, 1),
                List.of(1, 3),
                List.of(2, 1, 1),
                List.of(2, 2),
                List.of(3, 1),
                List.of(4)),
            compositions(4));
    }
}
