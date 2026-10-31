package enyo.crypto.tests;

import enyo.crypto.DigestAlgorithm;
import enyo.crypto.FileDigest;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public final class TestFileDigest {
    @Test
    public void testOf(Testing t) {
        byte[] d = digest(DigestAlgorithm.MD5, "\n");
        FileDigest fd = FileDigest.of(DigestAlgorithm.MD5, 1, d);
        t.examine(fd.algorithm()).isSame(DigestAlgorithm.MD5);
        t.examine(fd.length()).is(1);
        t.examine(d).isNotSame(fd.digest());
        t.assertByteArrayEquals(d, fd.digest());
        byte[] d1 = fd.digest();
        t.assertByteArrayEquals(d1, fd.digest());
        Arrays.fill(d, (byte) -1);
        t.assertByteArrayEquals(d1, fd.digest());
    }

    @Test
    public void testOfHexDigest(Testing t) {
        byte[] d = digest(DigestAlgorithm.MD5, "\n");
        String hexDigest = "68b329da9893e34099c7d8ad5cb9c940";
        FileDigest fd = FileDigest.ofHexDigest(
            DigestAlgorithm.MD5, 1, hexDigest);
        t.examine(fd.algorithm()).isSame(DigestAlgorithm.MD5);
        t.examine(fd.length()).is(1);
        t.examine(d).isNotSame(fd.digest());
        t.assertByteArrayEquals(d, fd.digest());
        t.examine(fd.hexDigest()).is(hexDigest);
        t.examine(fd).is(FileDigest.of(DigestAlgorithm.MD5, 1, d));
    }

    @Test
    public void testOfHexDigestInvalid(Testing t) {
        t.expect(
            () -> FileDigest.ofHexDigest(DigestAlgorithm.MD5, 1,
                "68b329da9893e34099c7d8ad5cb9c94"),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "Digest of odd length: 68b329da9893e34099c7d8ad5cb9c94");
        t.expect(
            () -> FileDigest.ofHexDigest(DigestAlgorithm.MD5, 1,
                "68b329da9893e34099c7d8ad5cb9c9"),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "Invalid digest length 15 for MD5, should be 16:"
                + " 68b329da9893e34099c7d8ad5cb9c9");
        t.expect(
            () -> FileDigest.ofHexDigest(DigestAlgorithm.MD5, 1,
                "68b329da9893e34099c7d8ad5cb9c94000"),
            IllegalArgumentException.class)
            .map(Exception::getMessage).is(
                "Invalid digest length 17 for MD5, should be 16:"
                + " 68b329da9893e34099c7d8ad5cb9c94000");
    }

    @Test
    public void testToString(Testing t) {
        byte[] d = digest(DigestAlgorithm.MD5, "\n");
        FileDigest fd = FileDigest.of(DigestAlgorithm.MD5, 1, d);
        t.examine(fd.toString()).is("MD5/1/68b329da9893e34099c7d8ad5cb9c940");
    }

    @Test
    public void testOrder1() {
        // Actually valid digests.
        Order<FileDigest> order = new Order<>();
        for (var algorithm : new DigestAlgorithm[] {
                // In the lexicographic order of the unsigned digest values.
                DigestAlgorithm.SHA256,
                DigestAlgorithm.MD5,
                DigestAlgorithm.SHA1,
                DigestAlgorithm.SHA512
            }) {
            var fd = FileDigest.of(algorithm, 1, digest(algorithm, "\n"));
            order.group()
                .add(fd)
                .add(deserialized(fd))
                .close();
        }
        order.checkComparable(FileDigest.class);
    }

    @Test
    public void testOrder2() {
        // Fictitious digests.
        Order<FileDigest> order = new Order<>();
        var digestLengths = List.of(16, 20, 32, 64);
        Stream.of(
            // Zero digests sort first.
            digestLengths.stream().map(length -> new byte[length]),
            Stream.of(
                // In the lexicographic order of the unsigned digest values.
                DigestAlgorithm.SHA256,
                DigestAlgorithm.MD5,
                DigestAlgorithm.SHA1,
                DigestAlgorithm.SHA512)
            .map(algorithm -> digest(algorithm, "\n")),
            // All-ones digests sort last.
            digestLengths.stream().map(length -> {
                    byte[] digest = new byte[length];
                    Arrays.fill(digest, (byte) -1);
                    return digest;
                }))
            .flatMap(Function.identity())
            .forEachOrdered(digest -> {
                    LongStream.of(0, 1, 2, Integer.MAX_VALUE,
                        1L + Integer.MAX_VALUE,
                        Long.MAX_VALUE)
                        .forEachOrdered(length -> {
                                Arrays.stream(DigestAlgorithm.values())
                                    .filter(algorithm ->
                                        algorithm.lengthInBytes()
                                        == digest.length)
                                    .sorted(Comparator.comparing(
                                            DigestAlgorithm::name))
                                    .forEachOrdered(algorithm -> {
                                            var fd = FileDigest.of(
                                                        algorithm, length,
                                                        digest);
                                            order.group()
                                                .add(fd)
                                                .add(deserialized(fd))
                                                .add(FileDigest.of(
                                                        algorithm, length,
                                                        digest.clone()))
                                                .close();
                                        });
                            });
                    });
    }

    private static FileDigest deserialized(FileDigest digest) {
        try {
            var out = new ByteArrayOutputStream();
            try (var oout = new ObjectOutputStream(out)) {
                oout.writeObject(digest);
            }
            return (FileDigest) new ObjectInputStream(
                new ByteArrayInputStream(out.toByteArray()))
                .readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static byte[] digest(DigestAlgorithm algorithm, String input) {
        return algorithm.newDigest().digest(
            input.getBytes(StandardCharsets.UTF_8));
    }
}
