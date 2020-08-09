package enyo.io.tests;

import enyo.io.ExtendedCharsets;
import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.ByteArrays;
import static enyo.io.tests.TestUTF8SCharset.compositions;
import static enyo.io.tests.TestUTF8SCharset.escape;
import static enyo.io.tests.TestUTF8SCharset.feedComposition;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// On OpenJDK, ZGC can run this test with -Xmx400m in a reasonable
// time (although a larger heap can significantly reduce testing time
// with ZGC).  -Xmx350m works as well for ZGC, but is really slow.
// Therefore, use this as the conservative choice.
@Resources(memory=400)
// Account for the creation of the map decoder.
@Cost(0.5)
@ExitDuringTest
public final class TestUTF8SCharsetMapDecoder {
    // Some of these tests should probably reduced in scope, to speed
    // them up.  Increased coverage may be possible if we can run
    // tests in parallel.

    @Test
    public static void testMapDecoder(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        t.assertEquals("", mapDecoder.decode(new byte[0]));
        t.assertEquals("A", mapDecoder.decode("A".getBytes()));
        t.assertEquals("\udc80", mapDecoder.decode(new byte[] { (byte) 0x80 }));
        t.assertEquals("\udc81", mapDecoder.decode(new byte[] { (byte) 0x81 }));
        t.assertEquals("\udcfe", mapDecoder.decode(new byte[] { (byte) 0xfe }));
        t.assertEquals("\udcff", mapDecoder.decode(new byte[] { (byte) 0xff }));
    }

    @Test
    public static void testDecoderCoverage1(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        byte[] array = new byte[1];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(1);
        for (int b1 = 0; b1 < 256; ++b1) {
            in.clear();
            out.clear();
            array[0] = (byte) b1;

            var decoder = ExtendedCharsets.UTF_8S.newDecoder();
            t.assertEquals(CoderResult.UNDERFLOW,
                decoder.decode(in, out, true));
            t.assertFalse(in.hasRemaining());
            t.assertEquals(CoderResult.UNDERFLOW, decoder.flush(out));
            t.assertEquals(1, out.position());

            in.position(0);
            var expected = mapDecoder.decode(in);
            out.flip();
            var actual = out.toString();
            t.assertEquals(expected.charAt(0), actual.charAt(0));
            t.assertEquals(expected, actual);
            t.assertByteArrayEquals(array,
                expected.getBytes(ExtendedCharsets.UTF_8S));
        }
    }

    @Test
    public static void testDecoderCoverage2(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        byte[] array = new byte[2];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(2);
        for (int b1 = 0; b1 < 256; ++b1)
            for (int b2 = 0; b2 < 256; ++b2) {
                in.clear();
                out.clear();
                array[0] = (byte) b1;
                array[1] = (byte) b2;

                var decoder = ExtendedCharsets.UTF_8S.newDecoder();
                t.assertEquals(CoderResult.UNDERFLOW,
                    decoder.decode(in, out, true));
                t.assertFalse(in.hasRemaining());
                t.assertEquals(CoderResult.UNDERFLOW, decoder.flush(out));

                in.position(0);
                var expected = mapDecoder.decode(in);
                out.flip();
                var actual = out.toString();
                t.assertEquals(expected.charAt(0), actual.charAt(0));
                t.assertEquals(expected, actual);
                t.assertByteArrayEquals(array,
                    expected.getBytes(ExtendedCharsets.UTF_8S));

                // Piece-wise decoding.
                in.clear();
                in.limit(1);
                out.clear();
                decoder = ExtendedCharsets.UTF_8S.newDecoder();
                t.assertEquals(CoderResult.UNDERFLOW,
                    decoder.decode(in, out, false));
                // The decoder must always consume all bytes (to
                // support escaping pending bytes on flush).
                t.assertEquals(0, in.remaining());
                in.position(1);
                in.limit(2);
                t.assertEquals(CoderResult.UNDERFLOW,
                    decoder.decode(in, out, true));
                t.assertEquals(0, in.remaining());
                t.assertEquals(CoderResult.UNDERFLOW, decoder.flush(out));
                out.flip();
                actual = out.toString();
                t.assertEquals(expected.charAt(0), actual.charAt(0));
                if (expected.length() >= 2 && actual.length() >= 2)
                    t.assertEquals(expected.charAt(1), actual.charAt(1));
                t.assertEquals(expected, actual);
            }
    }

    @Test
    @Cost(6)
    public static void testDecoderCoverage3(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        byte[] array = new byte[3];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(3);
        for (int b1 = 0; b1 < 256; ++b1)
            for (int b2 = 0; b2 < 256; ++b2)
                for (int b3 = 0; b3 < 256; ++b3) {
                    try {
                        in.clear();
                        out.clear();
                        array[0] = (byte) b1;
                        array[1] = (byte) b2;
                        array[2] = (byte) b3;

                        var decoder = ExtendedCharsets.UTF_8S.newDecoder();
                        t.assertEquals(CoderResult.UNDERFLOW,
                            decoder.decode(in, out, true));
                        t.assertFalse(in.hasRemaining());
                        t.assertEquals(CoderResult.UNDERFLOW,
                            decoder.flush(out));

                        in.position(0);
                        var expected = mapDecoder.decode(in);
                        out.flip();
                        var actual = out.toString();
                        t.assertEquals(expected.charAt(0), actual.charAt(0));
                        t.assertEquals(expected, actual);
                        t.assertByteArrayEquals(array,
                            expected.getBytes(ExtendedCharsets.UTF_8S));
                    } catch (AssertionError e) {
                        throw new AssertionError(String.format(
                                "b1=0x%02x b2=0x%02x b3=0x%02x", b1, b2, b3),
                            e);
                    }
                }
    }

    @Test
    public static void testDecoderCoverage3Decomposed(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        byte[] array = new byte[3];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(3);
        for (List<Integer> inComposition : compositions(array.length))
            for (List<Integer> outComposition : compositions(out.capacity()))
                for (byte b1 : CRITICAL_BYTES)
                    for (byte b2 : CRITICAL_BYTES)
                        for (byte b3 : REALLY_CRITICAL_BYTES) {
                            in.clear();
                            out.clear();
                            array[0] = b1;
                            array[1] = b2;
                            array[2] = b3;

                            var decoder = ExtendedCharsets.UTF_8S.newDecoder();
                            feedComposition(t, in, out, decoder,
                                inComposition, outComposition);

                            in.position(0);
                            var expected = mapDecoder.decode(in);
                            out.flip();
                            var actual = out.toString();
                            t.assertEquals(expected.charAt(0),
                                actual.charAt(0));
                            t.assertEquals(expected, actual);
                        }
    }

    @Test
    public static void testDecoderCoverage4(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        byte[] array = new byte[4];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(4);
        for (byte b1 : CRITICAL_BYTES)
            for (byte b2 : CRITICAL_BYTES)
                for (byte b3 : CRITICAL_BYTES)
                    for (byte b4 : REALLY_CRITICAL_BYTES) {
                        in.clear();
                        out.clear();
                        array[0] = b1;
                        array[1] = b2;
                        array[2] = b3;
                        array[3] = b4;

                        var decoder = ExtendedCharsets.UTF_8S.newDecoder();
                        t.assertEquals(CoderResult.UNDERFLOW,
                            decoder.decode(in, out, true));
                        t.assertFalse(in.hasRemaining());
                        t.assertEquals(CoderResult.UNDERFLOW,
                            decoder.flush(out));

                        in.position(0);
                        var expected = mapDecoder.decode(in);
                        out.flip();
                        var actual = out.toString();
                        t.assertEquals(expected.charAt(0), actual.charAt(0));
                        t.assertEquals(expected, actual);
                        t.assertByteArrayEquals(array,
                            expected.getBytes(ExtendedCharsets.UTF_8S));
                    }
    }

    @Test
    public static void testDecoderCoverage4Decomposed(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        byte[] array = new byte[4];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(4);
        for (List<Integer> inComposition : compositions(array.length))
            for (List<Integer> outComposition : compositions(out.capacity()))
                for (byte b1 : List.of((byte) 0xE0, (byte) 0xF0))
                    for (byte b2 : CRITICAL_BYTES)
                        for (byte b3 : CRITICAL_BYTES)
                            for (byte b4 : REALLY_CRITICAL_BYTES) {
                                in.clear();
                                out.clear();
                                array[0] = b1;
                                array[1] = b2;
                                array[2] = b3;
                                array[3] = b4;

                                var decoder =
                                    ExtendedCharsets.UTF_8S.newDecoder();
                                feedComposition(t, in, out, decoder,
                                    inComposition, outComposition);

                                in.position(0);
                                var expected = mapDecoder.decode(in);
                                out.flip();
                                var actual = out.toString();
                                t.assertEquals(expected.charAt(0),
                                    actual.charAt(0));
                                t.assertEquals(expected, actual);
                            }
    }

    @Test
    public static void testDecoderCoverage5(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        List<Byte> bytes = REALLY_CRITICAL_BYTES;
        byte[] array = new byte[5];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(5);
        for (byte b1 : List.of((byte) '@', (byte) 0xF0, (byte) 0xF4))
            for (byte b2 : bytes)
                for (byte b3 : bytes)
                    for (byte b5 : bytes)
                        for (byte b4 : bytes) {
                            in.clear();
                            out.clear();
                            array[0] = b1;
                            array[1] = b2;
                            array[2] = b3;
                            array[3] = b4;
                            array[4] = b5;

                            var decoder =
                                ExtendedCharsets.UTF_8S.newDecoder();
                            t.assertEquals(CoderResult.UNDERFLOW,
                                decoder.decode(in, out, true));
                            t.assertFalse(in.hasRemaining());
                            t.assertEquals(CoderResult.UNDERFLOW,
                                decoder.flush(out));

                            in.position(0);
                            var expected = mapDecoder.decode(in);
                            out.flip();
                            var actual = out.toString();
                            t.assertEquals(expected.charAt(0),
                                actual.charAt(0));
                            t.assertEquals(expected, actual);
                            t.assertByteArrayEquals(array,
                                expected.getBytes(
                                    ExtendedCharsets.UTF_8S));
                        }

    }

    @Test
    @Cost(3)
    public static void testDecoderCoverage5Decomposed(Testing t) {
        MapDecoder mapDecoder = MapDecoder.get();

        List<Byte> bytes = REALLY_CRITICAL_BYTES;
        byte[] array = new byte[5];
        ByteBuffer in = ByteBuffer.wrap(array);
        CharBuffer out = CharBuffer.allocate(5);
        for (List<Integer> inComposition : compositions(array.length))
            for (List<Integer> outComposition : compositions(out.capacity()))
                for (byte b1 : List.of((byte) 0xF0))
                    for (byte b2 : bytes)
                        for (byte b3 : bytes)
                            for (byte b5 : bytes)
                                for (byte b4 : bytes) {
                                    array[0] = b1;
                                    array[1] = b2;
                                    array[2] = b3;
                                    array[3] = b4;
                                    array[4] = b5;
                                    testDecoderCoverage5Decomposed(t,
                                        array, in, out, inComposition,
                                        outComposition);
                                }
    }

    private static void testDecoderCoverage5Decomposed(Testing t, byte[] array,
        ByteBuffer in, CharBuffer out, List<Integer> inComposition,
        List<Integer> outComposition) {
        MapDecoder mapDecoder = MapDecoder.get();

        in.clear();
        out.clear();

        var decoder = ExtendedCharsets.UTF_8S.newDecoder();
        feedComposition(t, in, out, decoder, inComposition, outComposition);

        in.position(0);
        var expected = mapDecoder.decode(in);
        out.flip();
        var actual = out.toString();

        t.assertEquals(actual, expected);
    }

    @Test
    public static void testEncoderCoverage1(Testing t) {
        char[] array = new char[1];
        CharBuffer in = CharBuffer.wrap(array);
        ByteBuffer out = ByteBuffer.allocate(3);
        nonSurrogateCharacters().forEach(ch -> {
                in.clear();
                out.clear();
                array[0] = (char) ch;

                try {
                    var encoder = ExtendedCharsets.UTF_8S.newEncoder();
                    t.assertEquals(CoderResult.UNDERFLOW,
                        encoder.encode(in, out, true));
                    t.assertEquals(CoderResult.UNDERFLOW, encoder.flush(out));
                    byte[] expected = String.valueOf(array)
                        .getBytes(StandardCharsets.UTF_8);
                    out.flip();
                    byte[] actual = new byte[out.remaining()];
                    out.get(actual);
                    t.assertByteArrayEquals(expected, actual);
                } catch (AssertionError e) {
                    throw new AssertionError(String.format("U+%04X", ch), e);
                }
            });

        // Escaping surrogates.
        IntStream.rangeClosed(0xDC80, 0xDCFF) .forEach(ch -> {
                in.clear();
                out.clear();
                array[0] = (char) ch;

                try {
                    var encoder = ExtendedCharsets.UTF_8S.newEncoder();
                    t.assertEquals(CoderResult.UNDERFLOW,
                        encoder.encode(in, out, true));
                    t.assertEquals(CoderResult.UNDERFLOW, encoder.flush(out));
                    byte[] expected = new byte[] { (byte) (ch - 0xDD00) };
                    out.flip();
                    byte[] actual = new byte[out.remaining()];
                    out.get(actual);
                    t.assertByteArrayEquals(expected, actual);
                } catch (AssertionError e) {
                    throw new AssertionError(String.format("U+%04X", ch), e);
                }
            });

        // Malformed surrogates.
        IntStream.concat(
            IntStream.rangeClosed(0xDC00, 0xDC7F),
            IntStream.rangeClosed(0xDD00, 0xDFFF))
            .forEach(ch -> {
                in.clear();
                out.clear();
                array[0] = (char) ch;

                try {
                    var encoder = ExtendedCharsets.UTF_8S.newEncoder();
                    CoderResult result = encoder.encode(in, out, true);
                    if (result == CoderResult.UNDERFLOW)
                        result = encoder.flush(out);
                    t.assertTrue(result.isMalformed());
                } catch  (AssertionError e) {
                    throw new AssertionError(String.format("U+%04X", ch), e);
                }
                });
    }

    /**
     * Alternative, table-based decoder implementation.  These tables
     * are larger, so the decoder is only allocated while tests are
     * running.
     */
    private static final class MapDecoder {
        private static WeakReference<MapDecoder> cache;

        private static synchronized MapDecoder get() {
            MapDecoder result = null;
            if (cache != null)
                result = cache.get();
            if (result == null) {
                result = new MapDecoder();
                cache = new WeakReference<>(result);
            }
            return result;
        }

        private final HashMap<ByteBuffer, String> map;
        private final HashSet<ByteBuffer> prefixSet;

        private MapDecoder() {
            map = buildDecodeMap();
            prefixSet = buildPrefixSet(map.keySet());
        }

        private String decode(byte[] input) {
            return decode(ByteBuffer.wrap(input));
        }

        private String decode(ByteBuffer input) {
            input = input.slice();
            var result = new StringBuilder();
            while (input.hasRemaining()) {
                // Search for the next sequence.
                input.limit(input.position());
                while (true) {
                    if (input.limit() == input.capacity()) {
                        // Incomplete prefix of a valid sequence at the
                        // end.  Consume one byte.
                        result.append(escape(input.get()));
                        break;
                    }

                    input.limit(input.limit() + 1);
                    String decoded = map.get(input);
                    if (decoded != null) {
                        result.append(decoded);
                        // Consume current sequence.
                        input.position(input.limit());
                        break;
                    }
                    if (!prefixSet.contains(input)) {
                        // This prefix can never become part of a
                        // sequence.  Consume one byte.
                        result.append(escape(input.get()));
                        break;
                    }
                }

                input.limit(input.capacity());
            }
            return result.toString();
        }

        private static HashMap<ByteBuffer, String> buildDecodeMap() {
            var result = new HashMap<ByteBuffer, String>();
            var in = CharBuffer.allocate(2);
            Supplier<ByteBuffer> getByteBuffer;
            {
                var out = ByteBuffer.allocate(4);
                var encoder = StandardCharsets.UTF_8.newEncoder();
                getByteBuffer = () -> {
                    encoder.reset();
                    out.clear();
                    if (!CoderResult.UNDERFLOW.equals(
                            encoder.encode(in, out, true)))
                        throw new AssertionError();
                    out.flip();
                    byte[] bytes = new byte[out.remaining()];
                    if (bytes.length == 0)
                        throw new AssertionError();
                    out.get(bytes);
                    return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
                };
            }

            nonSurrogateCharacters().forEach(ch -> {
                    in.clear();
                    in.put((char) ch);
                    in.flip();
                    result.put(getByteBuffer.get(),
                        Character.toString((char) ch));
                });

            for (char ch1 = Character.MIN_HIGH_SURROGATE;
                 ch1 <= Character.MAX_HIGH_SURROGATE; ++ch1)
                for (char ch2 = Character.MIN_LOW_SURROGATE;
                     ch2 <= Character.MAX_LOW_SURROGATE; ++ch2) {
                    in.clear();
                    in.put(ch1);
                    in.put(ch2);
                    in.flip();
                    var decoded = in.toString();
                    result.put(getByteBuffer.get(), decoded);
                }

            return result;
        }

        private static HashSet<ByteBuffer> buildPrefixSet(
            Collection<ByteBuffer> strings) {
            var result = new HashSet<ByteBuffer>();
            for (ByteBuffer string : strings) {
                ByteBuffer buffer = string.duplicate();
                while (buffer.hasRemaining()) {
                    buffer.limit(buffer.limit() - 1);
                    if (result.add(buffer))
                        // Once the buffer is in the set, it must no be
                        // modified.
                        buffer = buffer.duplicate();
                    else
                        // All shorter prefixes have already been added.
                        break;
                }
            }
            return result;
        }
    }

    /**
     * List of bytes which are on valid/invalid boundaries.
     */
    private static final List<Byte> CRITICAL_BYTES;


    /**
     * Subset of {@link #CRITICAL_BYTES} for even faster tests.
     */
    private static final List<Byte> REALLY_CRITICAL_BYTES;

    static {
        int[] rangeStart = {
            0xc2, 0x80, 0xe0, 0xa0, 0xe1, 0xed, 0xee, 0xf0, 0xf1, 0xf4
        };
        int[] rangeEnd = {
            0xc2, 0x80, 0xe0, 0xa0, 0xe1, 0xed, 0xee, 0xf0, 0xf1, 0xf4
        };

        CRITICAL_BYTES = List.copyOf(IntStream.concat(
                IntStream.concat(
                    Arrays.stream(rangeStart).flatMap(
                        i -> IntStream.of(i - 1, i)),
                    Arrays.stream(rangeEnd).flatMap(
                        i -> IntStream.of(i, i + 1))),
                // Exotics.
                IntStream.of(0x00, 0x3f, 0x7f, 0xf1, 0xf3, 0xf4, 0xf5, 0xff))
            .mapToObj(i -> (byte) i)
            .collect(Collectors.toCollection(TreeSet::new)));
        REALLY_CRITICAL_BYTES = List.copyOf(IntStream.concat(
                IntStream.concat(
                    Arrays.stream(rangeStart),
                    Arrays.stream(rangeEnd)),
                IntStream.of(0x00, 0xf1, 0xf3, 0xf4))
            .mapToObj(i -> (byte) i)
            .collect(Collectors.toCollection(TreeSet::new)));

    }

    private static IntStream nonSurrogateCharacters() {
        return IntStream.concat(
            // Before-surrogate range.
            IntStream.range(0, Character.MIN_SURROGATE),
            // After-surrogate range.
            IntStream.rangeClosed(Character.MAX_SURROGATE + 1,
                Character.MAX_VALUE));
    }
}
