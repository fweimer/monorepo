package enyo.crypto.tests;

import enyo.crypto.DigestAlgorithm;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public final class TestDigestAlgorithm {
    @Test
    public void testNewDigest(Testing t) {
        for (DigestAlgorithm algo : DigestAlgorithm.values()) {
            t.assertEquals(algo.newDigest().digest().length,
                algo.lengthInBytes());
        }
    }

    @Test
    public void testAlgorithmsDistinct(Testing t) {
        t.assertEquals((long) DigestAlgorithm.values().length,
            Arrays.stream(DigestAlgorithm.values()).distinct().count());
        t.assertEquals((long) DigestAlgorithm.values().length,
            Arrays.stream(DigestAlgorithm.values())
            .map(algo -> algo.newDigest().getAlgorithm())
            .distinct().count());
    }

    @Test
    public void testEmpty(Testing t) {
        var emptyDigests = Map.of(
            DigestAlgorithm.MD5, "d41d8cd98f00b204e9800998ecf8427e",
            DigestAlgorithm.SHA1, "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            DigestAlgorithm.SHA224,
            "d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f",
            DigestAlgorithm.SHA256,
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            DigestAlgorithm.SHA384,
            "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da"
            + "274edebfe76f65fbd51ad2f14898b95b",
            DigestAlgorithm.SHA512,
            "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a92"
            + "1d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a"
            + "81a538327af927da3e");

        for (var e : emptyDigests.entrySet()) {
            byte[] actual = e.getKey().newDigest().digest();
            byte[] expected = new byte[e.getValue().length() / 2];
            for (int i = 0; i < expected.length; ++i)
                expected[i] = (byte)
                    (16 * Character.digit(e.getValue().charAt(2 * i), 16)
                        + Character.digit(e.getValue().charAt(2 * i + 1), 16));
            t.assertByteArrayEquals(expected, actual);
        }

        t.assertEquals(emptyDigests.size(), DigestAlgorithm.values().length);
    }

    @Test
    public void testIndependent(Testing t) {
        for (DigestAlgorithm algo : DigestAlgorithm.values()) {
            byte[] empty = algo.newDigest().digest();
            var digest1 = algo.newDigest();
            var digest2 = algo.newDigest();
            digest1.update((byte) 1);
            digest2.update((byte) 2);
            byte[] one = digest1.digest();
            byte[] two = digest2.digest();
            t.assertEquals(3L,
                Stream.of(empty, one, two)
                .map(ByteBuffer::wrap)
                .distinct()
                .count());

            byte[] oneIsolated = algo.newDigest().digest(new byte[] { 1 });
            byte[] twoIsolated = algo.newDigest().digest(new byte[] { 2 });
            t.assertByteArrayEquals(oneIsolated, one);
            t.assertByteArrayEquals(twoIsolated, two);
        }
    }
}
