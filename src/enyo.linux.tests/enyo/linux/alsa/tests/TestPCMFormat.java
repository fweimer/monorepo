package enyo.sound.alsa.tests;

import enyo.linux.alsa.PCM;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

public final class TestPCMFormat {
    @Test
    public static void testOrdinal(Testing t) {
        testOrdinal(t, "S8", 0);
        testOrdinal(t, "U8", 1);
        testOrdinal(t, "S16_LE", 2);
        testOrdinal(t, "S16_BE", 3);
        testOrdinal(t, "U16_LE", 4);
        testOrdinal(t, "U16_BE", 5);
        testOrdinal(t, "S24_LE", 6);
        testOrdinal(t, "S24_BE", 7);
        testOrdinal(t, "U24_LE", 8);
        testOrdinal(t, "U24_BE", 9);
        testOrdinal(t, "S32_LE", 10);
        testOrdinal(t, "S32_BE", 11);
        testOrdinal(t, "U32_LE", 12);
        testOrdinal(t, "U32_BE", 13);
        testOrdinal(t, "FLOAT_LE", 14);
        testOrdinal(t, "FLOAT_BE", 15);
        testOrdinal(t, "FLOAT64_LE", 16);
        testOrdinal(t, "FLOAT64_BE", 17);
        testOrdinal(t, "IEC958_SUBFRAME_LE", 18);
        testOrdinal(t, "IEC958_SUBFRAME_BE", 19);
        testOrdinal(t, "MU_LAW", 20);
        testOrdinal(t, "A_LAW", 21);
        testOrdinal(t, "IMA_ADPCM", 22);
        testOrdinal(t, "MPEG", 23);
        testOrdinal(t, "GSM", 24);
        testOrdinal(t, "S20_LE", 25);
        testOrdinal(t, "S20_BE", 26);
        testOrdinal(t, "U20_LE", 27);
        testOrdinal(t, "U20_BE", 28);
        testOrdinal(t, "SPECIAL", 31);
        testOrdinal(t, "S24_3LE", 32);
        testOrdinal(t, "S24_3BE", 33);
        testOrdinal(t, "U24_3LE", 34);
        testOrdinal(t, "U24_3BE", 35);
        testOrdinal(t, "S20_3LE", 36);
        testOrdinal(t, "S20_3BE", 37);
        testOrdinal(t, "U20_3LE", 38);
        testOrdinal(t, "U20_3BE", 39);
        testOrdinal(t, "S18_3LE", 40);
        testOrdinal(t, "S18_3BE", 41);
        testOrdinal(t, "U18_3LE", 42);
        testOrdinal(t, "U18_3BE", 43);
        testOrdinal(t, "G723_24", 44);
        testOrdinal(t, "G723_24_1B", 45);
        testOrdinal(t, "G723_40", 46);
        testOrdinal(t, "G723_40_1B", 47);
        testOrdinal(t, "DSD_U8", 48);
        testOrdinal(t, "DSD_U16_LE", 49);
        testOrdinal(t, "DSD_U32_LE", 50);
        testOrdinal(t, "DSD_U16_BE", 51);
        testOrdinal(t, "DSD_U32_BE", 52);
    }

    private static void testOrdinal(Testing t, String name, int value) {
        t.examine(name)
            .map(PCM.Format::valueOf)
            .mapToInt(PCM.Format::ordinal)
            .is(value);
    }

    @Test
    public static void testEndian(Testing t) {
        for (PCM.Format e : PCM.Format.values()) {
            if (e.name().endsWith("LE"))
                t.examine(e)
                    .map(PCM.Format::endian)
                    .is(PCM.Endian.LITTLE);
            if (e.name().endsWith("BE"))
                t.examine(e)
                    .map(PCM.Format::endian)
                    .is(PCM.Endian.BIG);
        }
    }

    @Test
    public static void testFloat(Testing t) {
        for (PCM.Format e : PCM.Format.values())
            if (e.name().indexOf("FLOAT") >= 0)
                t.examine(e)
                    .map(PCM.Format::encoding, e1 -> e1.is(PCM.Encoding.FLOAT))
                    .map(PCM.Format::endian, e1 -> e1.isnt(PCM.Endian.NEITHER));
    }

    @Test
    public static void testSize(Testing t) {
        for (PCM.Format e : PCM.Format.values())
            t.examine(e)
                .mapToInt(PCM.Format::bits,
                    e1 -> e1.check(Checks.greaterThanOrEquals(0)))
                .mapToInt(PCM.Format::bytes,
                    e1 -> e1.check(Checks.greaterThanOrEquals(0)))
                .mapToInt(e1 -> e1.bits(),
                    e1 -> e1.check(Checks.lessThanOrEquals(8 * e.bytes())))
                .test(e1 -> (e1.bits() != 0) == (e1.bytes() != 0));
    }
}
