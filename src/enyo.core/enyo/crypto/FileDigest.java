package enyo.crypto;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Digest values with explicit lengths.
 *
 * This class combines digest algorithms, digest values, and digest
 * lengths.  The explicit length information can be used to validate
 * data from an untrusted data source if the digest information itself
 * is trusted because it is possible to stop processing if the specified
 * number of bytes is exceeded.
 *
 * <p>File digest objects are immutable.
 */
public final class FileDigest implements Comparable<FileDigest>, Serializable {
    /**
     * Returns a file digest object for the specified combination of
     * algorithm, length, and digest bytes.
     *
     * @param algorithm the digest algorithm used to compute the digest
     * @param length the number of bytes over which the digest has been
     *   computed
     * @param digest the value of the digest, as returned by
     *  {@link java.security.MessageDigest#digest()}.
     * @return a file digest object with the specified values
     * @throws NullPointerException if {@code algorithm} or {@code digest}
     *  is null
     * @throws IllegalArgumentException if {@code length} is negative
     *  or the length of {@code digest} does not match {@code algorithm}
     * @see #ofHexDigest(DigestAlgorithm,long,String)
     */
    public static FileDigest of(DigestAlgorithm algorithm, long length,
        byte[] digest) {
        if (length < 0)
            throw new IllegalArgumentException("length: " + length);
        if (digest.length != algorithm.lengthInBytes())
            throw new IllegalArgumentException(String.format(
                    "Invalid digest length %d for %s, should be %d",
                    digest.length, algorithm, algorithm.lengthInBytes()));
        return new FileDigest(algorithm, length, digest.clone());
    }

    /**
     * Returns a file digest object for the specified combination of
     * algorithm, length, and digest.
     *
     * @param algorithm the digest algorithm used to compute the digest
     * @param length the number of bytes over which the digest has been
     *   computed
     * @param hexDigest the value of the digest, as returned by
     *  {@link java.security.MessageDigest#digest()}, encoded as string
     *  of hexadecimal digits
     * @return a file digest object with the specified values
     * @throws NullPointerException if {@code algorithm} or {@code hexDigest}
     *   is null
     * @throws IllegalArgumentException if {@code length} is negative,
     *   {@code hexDigest} is not a string of hexadecimal digits,
     *   or the length of {@code hexDigest} does not match {@code algorithm}
     * @see #of(DigestAlgorithm,long,byte[])
     */
    public static FileDigest ofHexDigest(DigestAlgorithm algorithm,
        long length, String hexDigest) {
        if (length < 0)
            throw new IllegalArgumentException("length: " + length);
        byte[] digest = parseDigest(hexDigest);
        if (digest.length != algorithm.lengthInBytes())
            throw new IllegalArgumentException(String.format(
                    "Invalid digest length %d for %s, should be %d: %s",
                    digest.length, algorithm, algorithm.lengthInBytes(),
                    hexDigest));
        return new FileDigest(algorithm, length, digest);
    }

    /**
     * Returns the digest algorithm.
     *
     * @return a digest algorithm
     */
    public DigestAlgorithm algorithm() {
        return algorithm;
    }

    /**
     * Returns the number of bytes over which the digest was computed.
     *
     * @return a non-negative number
     */
    public long length() {
        return length;
    }

    /**
     * Returns a copy of raw digest bytes.
     *
     * @return a byte array with the digest bytes.
     * @see java.security.MessageDigest#digest()
     * @see #hexDigest()
     */
    public byte[] digest() {
        return digest.clone();
    }

    /**
     * Returns the digest as a string of hexadecimal digits.
     *
     * @return a non-empty, even-length string of (lower-case)
     *  hexadecimal digits
     * @see #digest()
     */
    public String hexDigest() {
        StringBuilder sb = new StringBuilder(2 * digest.length);
        for (byte b : digest) {
            sb.append(Character.forDigit((b >> 4) & 0b1111, 16));
            sb.append(Character.forDigit(b & 0b1111, 16));
        }
        return sb.toString();
    }

    /**
     * Compares this file digest for equality with the other object.
     *
     * The objects are equal if the other object is a file digest
     * constructed from equal values.
     *
     * @param o the other object
     * @return the result of the comparison
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != FileDigest.class)
            return false;
        FileDigest other = (FileDigest) o;
        return algorithm == other.algorithm && length == other.length
            && Arrays.equals(digest, other.digest);
    }

    @Override
    public int hashCode() {
        return algorithm.hashCode() + (int) length
            + Arrays.hashCode(digest);
    }

    /**
     * Compares file digests.
     *
     * File digests are compared first according to the digest value
     * (as returned by {@link #digest()}), sorted lexicographically as
     * unsigned bytes.
     *
     * @param other the other digest
     * @return a negative value if this digest is less than the other digest,
     *  zero if they are equal, or a positive value if this digest is
     *  greater than the other digest
     */
    @Override
    public int compareTo(FileDigest other) {
        int ret = Arrays.compareUnsigned(digest, other.digest);
        if (ret != 0)
            return ret;
        if (length != other.length)
            return Long.compare(length, other.length);
        // Do not use the ordinal value because it is potential
        // unstable.
        if (algorithm != other.algorithm)
            return algorithm.name().compareTo(other.algorithm.name());
        return 0;
    }

    @Override
    public String toString() {
        return algorithm.name() + '/' + length + '/' + hexDigest();
    }

    /**
     * The digest algorithm.
     * @see #algorithm()
     */
    private final DigestAlgorithm algorithm;

    /**
     * The file length.
     * @see #length()
     */
    private final long length;

    /**
     * The digest value.
     * @see #digest()
     */
    private final byte[] digest;

    private static final long serialVersionUID = 0x88634eff7def778dL;

    private FileDigest(DigestAlgorithm algorithm, long length, byte[] digest) {
        this.algorithm = algorithm;
        this.length = length;
        this.digest = digest;
    }

    private static byte[] parseDigest(String hex) {
        if ((hex.length() % 2) != 0)
            throw new IllegalArgumentException(
                "Digest of odd length: " + hex);
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte) (16 * digitValue(hex.charAt(2 * i))
                + digitValue(hex.charAt(2 * i + 1)));
        }
        return result;
    }

    private static int digitValue(char ch) {
        if ('0' <= ch && ch <= '9')
            return ch - '0';
        else if ('a' <= ch && ch <= 'f')
            return ch - 'a' + 10;
        else if ('A' <= ch && ch <= 'F')
            return ch - 'A' + 10;
        else
            throw new IllegalArgumentException(
                "Invalid hexadecimal digit: " + ch);
    }

    /**
     * Returns an immuatble version of this object.
     * @return a new immutable object
     */
    private Object readResolve() {
        // Validate the data and prevent sharing of the digest array.
        return FileDigest.of(algorithm, length, digest);
    }
}
