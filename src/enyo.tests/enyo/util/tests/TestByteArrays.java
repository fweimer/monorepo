package enyo.util.tests;

import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.util.ByteArrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExitDuringTest
public final class TestByteArrays {
    @FunctionalInterface
    interface TerFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    @Test
    public static void testQuoteEmpty(Testing t) {
        t.examine(ByteArrays.quote(new byte[0])).is("");
    }

    @Test
    public static void testQuoteOneByte(Testing t) {
        for (int i = 0; i < 256; ++i)
            t.examine(ByteArrays.quote(new byte[] { (byte) i }))
                .is(ONE_BYTE.get(i));
    }

    @Test
    public static void testQuoteTwoBytes(Testing t) {
        for (int i = 0; i < 256; ++i)
            for (int j = 0; j < 256; ++j)
                t.examine(ByteArrays.quote(new byte[] { (byte) i, (byte) j }))
                    .is(ONE_BYTE.get(i) + ONE_BYTE.get(j));
    }

    @Test
    public static void testQuoteThreeBytes(Testing t) {
        for (int i = 0; i < 256; ++i)
            for (int j = 0; j < 256; ++j)
                t.examine(ByteArrays.quote(
                        new byte[] { '@', (byte) i, (byte) j }))
                    .is('@' + ONE_BYTE.get(i) + ONE_BYTE.get(j));
    }

    @Test
    public static void testQuoteFourBytes(Testing t) {
        for (int i = 0; i < 256; ++i)
            for (int j = 0; j < 256; ++j)
                t.examine(ByteArrays.quote(
                        new byte[] { '@', (byte) i, (byte) j, '!' }))
                    .is('@' + ONE_BYTE.get(i) + ONE_BYTE.get(j) + '!');
    }

    @Test
    public static void testQuoteRangeEmpty(Testing t) {
        t.examine(ByteArrays.quote(new byte[0], 0, 0)).is("");
        t.examine(ByteArrays.quote(new byte[1], 0, 0)).is("");
        t.examine(ByteArrays.quote(new byte[1], 1, 1)).is("");
    }

    @Test
    public static void testQuoteRangeOneByte(Testing t) {
        byte[] array = new byte[4];
        for (int shift = 0; shift <= 2; ++shift) {
            Arrays.fill(array, (byte) '.');
            for (int i = 0; i < 256; ++i) {
                array[shift] = (byte) i;
                t.examine(ByteArrays.quote(array, shift, shift + 1))
                    .is(ONE_BYTE.get(i));
            }
        }
    }

    @Test
    public static void testQuoteRangeTwoBytes(Testing t) {
        byte[] array = new byte[5];
        for (int shift = 0; shift <= 2; ++shift) {
            Arrays.fill(array, (byte) '.');
            for (int i = 0; i < 256; ++i) {
                array[shift] = (byte) i;
                for (int j = 0; j < 256; ++j) {
                    array[shift + 1] = (byte) j;
                    t.examine(ByteArrays.quote(array, shift, shift + 2))
                        .is(ONE_BYTE.get(i) + ONE_BYTE.get(j));
                }
            }
        }
    }

    @Test
    public static void testQuoteRangeThreeBytes(Testing t) {
        byte[] array = new byte[5];
        for (int shift = 0; shift <= 2; ++shift) {
            Arrays.fill(array, (byte) '.');
            array[shift] = '@';
            for (int i = 0; i < 256; ++i) {
                array[shift + 1] = (byte) i;
                for (int j = 0; j < 256; ++j) {
                    array[shift + 2] = (byte) j;
                    t.examine(ByteArrays.quote(array, shift, shift + 3))
                        .is('@' + ONE_BYTE.get(i) + ONE_BYTE.get(j));
                }
            }
        }
    }

    @Test
    public static void testQuoteRangeFourBytes(Testing t) {
        byte[] array = new byte[6];
        for (int shift = 0; shift <= 2; ++shift) {
            Arrays.fill(array, (byte) '.');
            array[shift] = '@';
            array[shift + 3] = '!';
            for (int i = 0; i < 256; ++i) {
                array[shift + 1] = (byte) i;
                for (int j = 0; j < 256; ++j) {
                    array[shift + 2] = (byte) j;
                    t.examine(ByteArrays.quote(array, shift, shift + 4))
                        .is('@' + ONE_BYTE.get(i) + ONE_BYTE.get(j) + '!');
                }
            }
        }
    }

    @Test
    public static void testSignedStream(Testing t) {
        t.expect(() -> ByteArrays.signedStream(null),
            NullPointerException.class);
        t.examine(ByteArrays.signedStream(new byte[0]).count()).is(0);
        for (int i = -128; i <= 127; ++i) {
            t.examine(
                ByteArrays.signedStream(new byte[] { (byte) i })
                .boxed()
                .collect(Collectors.toList()))
                .is(List.of(i));
            for (int j = -128; j <= 127; ++j)
                t.examine(ByteArrays.signedStream(new byte[] {
                            (byte) i, (byte) j })
                    .boxed()
                    .collect(Collectors.toList()))
                    .is(List.of(i, j));
        }
        var expected = IntStream.range(0, 256)
            .map(b -> (byte) b)
            .boxed()
            .collect(Collectors.toList());
        var bytes = new byte[256];
        for (int i = 0; i < 256; ++i)
            bytes[i] = (byte) i;
        t.examine(ByteArrays.signedStream(bytes)
            .boxed()
            .collect(Collectors.toList()))
            .is(expected);
    }

    @Test
    public static void testSignedStream3(Testing t) {
        for (int i = -128; i <= 127; ++i) {
            t.examine(ByteArrays.signedStream(new byte[] {
                        (byte) (i - 1), (byte) i, 0 }, 1, 2)
                .boxed()
                .collect(Collectors.toList()))
                .is(List.of(i));
            for (int j = -128; j <= 127; ++j)
                t.examine(ByteArrays.signedStream(new byte[] {
                            (byte) (i + 1), (byte) i, (byte) j, -1, -2 }, 1, 3)
                    .boxed()
                    .collect(Collectors.toList()))
                    .is(List.of(i, j));
        }
        var expected = IntStream.range(0, 256)
            .map(b -> (byte) b)
            .boxed()
            .collect(Collectors.toList());
        testStream(t, expected, ByteArrays::signedStream);
    }

    @Test
    @Cost(0.5)
    public static void testSignedSpliterator(Testing t) {
        var expected = IntStream.range(0, 256)
            .map(b -> (byte) b)
            .boxed()
            .collect(Collectors.toList());
        testSpliterator(t, expected, ByteArrays::signedSpliterator);
    }

    @Test
    public static void testUnsignedStream(Testing t) {
        t.expect(() -> ByteArrays.unsignedStream(null),
            NullPointerException.class);
        t.examine(ByteArrays.unsignedStream(new byte[0]).count()).is(0);
        for (int i = 0; i <= 255; ++i) {
            t.examine(ByteArrays.unsignedStream(new byte[] { (byte) i })
                .boxed()
                .collect(Collectors.toList()))
                .is(List.of(i));
            for (int j = 0; j <= 255; ++j)
                t.examine(ByteArrays.unsignedStream(new byte[] {
                            (byte) i, (byte) j })
                    .boxed()
                    .collect(Collectors.toList()))
                    .is(List.of(i, j));
        }
        var expected = IntStream.range(0, 256)
            .boxed()
            .collect(Collectors.toList());
        var bytes = new byte[256];
        for (int i = 0; i < 256; ++i)
            bytes[i] = (byte) i;
        t.examine(ByteArrays.unsignedStream(bytes)
            .boxed()
            .collect(Collectors.toList()))
            .is(expected);
    }

    @Test
    public static void testUnsignedStream3(Testing t) {
        for (int i = 0; i <= 255; ++i) {
            t.examine(ByteArrays.unsignedStream(new byte[] {
                        (byte) (i - 1), (byte) i, 0 }, 1, 2)
                .boxed()
                .collect(Collectors.toList()))
                .is(List.of(i));
            for (int j = 0; j <= 255; ++j)
                t.examine(ByteArrays.unsignedStream(new byte[] {
                            (byte) (i + 1), (byte) i, (byte) j, -1, -2 }, 1, 3)
                    .boxed()
                    .collect(Collectors.toList()))
                    .is(List.of(i, j));
        }
        var expected = IntStream.range(0, 256)
            .boxed()
            .collect(Collectors.toList());
        testStream(t, expected, ByteArrays::unsignedStream);
    }

    @Test
    @Cost(0.5)
    public static void testUnsignedSpliterator(Testing t) {
        var expected = IntStream.range(0, 256)
            .boxed()
            .collect(Collectors.toList());
        testSpliterator(t, expected, ByteArrays::unsignedSpliterator);
    }

    private static void testStream(Testing t, List<Integer> expected,
        TerFunction<byte[], Integer, Integer, IntStream> mk) {
        t.expect(() -> mk.apply(null, 0, 0),
            NullPointerException.class);

        t.expect(() -> mk.apply(new byte[0], -1, 0),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[0], 0, -1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[0], 1, -1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[0], 1, 0),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[0], 0, 1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[1], -1, 0),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[1], 0, -1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> mk.apply(new byte[1], 1, -1),
            ArrayIndexOutOfBoundsException.class);

        for (int i = 0; i <= 3; ++i) {
            IntStream  stream = mk.apply(new byte[i], 0, 0);
            t.assertFalse(stream.isParallel());
            t.examine(stream.count()).is(0);
        }

        var bytes = new byte[256];
        for (int i = 0; i < 256; ++i)
            bytes[i] = (byte) i;
        for (int fromIndex = 0; fromIndex < 256; ++fromIndex)
            for (int toIndex = fromIndex; toIndex < 256; ++toIndex) {
                IntStream stream = mk.apply(bytes, fromIndex, toIndex);
                t.assertFalse(stream.isParallel());
                t.examine(stream.boxed()
                    .collect(Collectors.toList()))
                    .is(expected.subList(fromIndex, toIndex));
            }
    }

    private static void testSpliterator(Testing t, List<Integer> expected,
        TerFunction<byte[], Integer, Integer, Spliterator.OfInt> mk) {
        t.expect(() -> ByteArrays.signedSpliterator(null, 0, 0),
            NullPointerException.class);

        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[0], -1, 0),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[0], 0, -1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[0], 1, -1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[0], 1, 0),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[0], 0, 1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[1], -1, 0),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[1], 0, -1),
            ArrayIndexOutOfBoundsException.class);
        t.expect(() -> ByteArrays.signedSpliterator(
                new byte[1], 1, -1),
            ArrayIndexOutOfBoundsException.class);

        for (int i = 0; i <= 3; ++i) {
            var sp = ByteArrays.signedSpliterator(new byte[i], 0, 0);
            t.examine(sp.estimateSize()).is(0);
            t.examine(sp.getExactSizeIfKnown()).is(0);
            t.assertTrue(sp.hasCharacteristics(
                    Spliterator.SIZED | Spliterator.SUBSIZED));
            sp.forEachRemaining((int b) -> {
                    throw new AssertionError("Unexpected element: " + b);
                });
            sp = ByteArrays.signedSpliterator(new byte[i], 0, 0);
            t.assertFalse(sp.tryAdvance((int b) -> {
                        throw new AssertionError("Unexpected element: " + b);
                    }));
        }

        var bytes = new byte[256];
        for (int i = 0; i < 256; ++i)
            bytes[i] = (byte) i;
        var actual = new ArrayList<Integer>(256);
        var actual2 = new ArrayList<Integer>(256);
        for (int fromIndex = 0; fromIndex < 256; ++fromIndex)
            for (int toIndex = fromIndex; toIndex < 256; ++toIndex)
                for (int spAdvance = 0; spAdvance < 2; ++spAdvance) {
                    var sp = mk.apply(bytes, fromIndex, toIndex);
                    t.assertTrue(sp.hasCharacteristics(
                            Spliterator.SIZED | Spliterator.SUBSIZED));
                    actual.clear();
                    transfer(t, sp, actual, spAdvance != 0);
                    t.<List<?>>examine(actual).is(
                        expected.subList(fromIndex, toIndex));

                    for (int sp2First = 0; sp2First < 2; ++sp2First)
                        for (int sp2Advance = 0; sp2Advance < 2;
                             ++sp2Advance) {
                            sp = mk.apply(bytes, fromIndex, toIndex);
                            t.examine(sp.estimateSize())
                                .is(toIndex - fromIndex);
                            t.examine(sp.getExactSizeIfKnown())
                                .is(toIndex - fromIndex);

                            var sp2 = sp.trySplit();
                            if (toIndex - fromIndex <= 1) {
                                t.examine(sp2).isNull();
                                actual.clear();
                                transfer(t, sp, actual, spAdvance != 0);
                                t.<List<?>>examine(actual)
                                    .is(expected.subList(fromIndex, toIndex));
                                continue;
                            }

                            t.assertTrue(sp2.estimateSize() > 0);
                            t.assertTrue(sp2.getExactSizeIfKnown() > 0);
                            t.assertTrue(sp.estimateSize()
                                < toIndex - fromIndex);
                            t.assertTrue(sp.getExactSizeIfKnown()
                                < toIndex - fromIndex);
                            t.examine(sp.estimateSize() + sp2.estimateSize())
                                .is(toIndex - fromIndex);
                            t.examine(sp.getExactSizeIfKnown()
                                + sp2.getExactSizeIfKnown())
                                .is(toIndex - fromIndex);

                            actual.clear();
                            if (sp2First != 0) {
                                transfer(t, sp2, actual, sp2Advance != 0);
                                transfer(t, sp, actual, spAdvance != 0);
                            } else {
                                actual2.clear();
                                transfer(t, sp, actual2, spAdvance != 0);
                                transfer(t, sp2, actual, sp2Advance != 0);
                                actual.addAll(actual2);
                            }

                            t.<List<?>>examine(actual)
                                .is(expected.subList(fromIndex, toIndex));
                        }
            }
    }

    private static void transfer(Testing t, Spliterator.OfInt sp,
        ArrayList<Integer> target, boolean useAdvance) {
        if (useAdvance) {
            int initialTargetSize = target.size();
            int count = 0;
            while (sp.tryAdvance((int b) -> target.add(b))) {
                ++count;
                t.examine(target.size()).is(initialTargetSize + count);
            }
            t.examine(target.size()).is(initialTargetSize + count);
        } else
            sp.forEachRemaining((int b) -> target.add(b));

        t.assertFalse(sp.tryAdvance(
                (int b) -> t.assertFail("Unexpected element: " + b)));
    }

    // These strings have been checked using Python 2:
    // [(i, ONE_BYTE[i]) for i in range(256)
    //  if chr(i) != eval('"' + ONE_BYTE[i] + '"')]
    private static final List<String> ONE_BYTE = List.of(
        "\\000", "\\001", "\\002", "\\003", "\\004", "\\005", "\\006", "\\007",
        "\\010", "\\t", "\\n", "\\013", "\\014", "\\r", "\\016", "\\017",
        "\\020", "\\021", "\\022", "\\023", "\\024", "\\025", "\\026", "\\027",
        "\\030", "\\031", "\\032", "\\033", "\\034", "\\035", "\\036", "\\037",
        " ", "!", "\\\"", "#", "$", "%", "&", "\\'",
        "(", ")", "*", "+", ",", "-", ".", "/",
        "0", "1", "2", "3", "4", "5", "6", "7",
        "8", "9", ":", ";", "<", "=", ">", "?",
        "@", "A", "B", "C", "D", "E", "F", "G",
        "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W",
        "X", "Y", "Z", "[", "\\\\", "]", "^", "_",
        "`", "a", "b", "c", "d", "e", "f", "g",
        "h", "i", "j", "k", "l", "m", "n", "o",
        "p", "q", "r", "s", "t", "u", "v", "w",
        "x", "y", "z", "{", "|", "}", "~", "\\177",
        "\\200", "\\201", "\\202", "\\203", "\\204", "\\205", "\\206", "\\207",
        "\\210", "\\211", "\\212", "\\213", "\\214", "\\215", "\\216", "\\217",
        "\\220", "\\221", "\\222", "\\223", "\\224", "\\225", "\\226", "\\227",
        "\\230", "\\231", "\\232", "\\233", "\\234", "\\235", "\\236", "\\237",
        "\\240", "\\241", "\\242", "\\243", "\\244", "\\245", "\\246", "\\247",
        "\\250", "\\251", "\\252", "\\253", "\\254", "\\255", "\\256", "\\257",
        "\\260", "\\261", "\\262", "\\263", "\\264", "\\265", "\\266", "\\267",
        "\\270", "\\271", "\\272", "\\273", "\\274", "\\275", "\\276", "\\277",
        "\\300", "\\301", "\\302", "\\303", "\\304", "\\305", "\\306", "\\307",
        "\\310", "\\311", "\\312", "\\313", "\\314", "\\315", "\\316", "\\317",
        "\\320", "\\321", "\\322", "\\323", "\\324", "\\325", "\\326", "\\327",
        "\\330", "\\331", "\\332", "\\333", "\\334", "\\335", "\\336", "\\337",
        "\\340", "\\341", "\\342", "\\343", "\\344", "\\345", "\\346", "\\347",
        "\\350", "\\351", "\\352", "\\353", "\\354", "\\355", "\\356", "\\357",
        "\\360", "\\361", "\\362", "\\363", "\\364", "\\365", "\\366", "\\367",
        "\\370", "\\371", "\\372", "\\373", "\\374", "\\375", "\\376",
        "\\377");
}
