package enyo.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Well-known digest algorithms.
 *
 * These algorithms can safely be instantiated using the {@link MessageDigest}
 * class in the Java Cryptography Architecture (JCA).  They have names
 * which can safely be used in file system paths, too.
 *
 * <p><b>Note:</b> The ordinal number of individual digests can change.
 * The ordering does not imply anything about the algorithm strength.
 *
 * @see MessageDigest
 */
public enum DigestAlgorithm {
    /**
     * The MD5 message digest algorithm.
     *
     * @see <a href="https://tools.ietf.org/html/rfc1321">RFC 1321</a>
     */
    MD5(16),

    /**
     * The SHA-1 message digest algorithm.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3174">RFC 3174</a>
     */
    SHA1(20),

    /**
     * The SHA-224 message digest algorithm.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4634">RFC 4634</a>
     */
    SHA224(28, "SHA-224"),

    /**
     * The SHA-256 message digest algorithm.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4634">RFC 4634</a>
     */
    SHA256(32, "SHA-256"),

    /**
     * The SHA-384 message digest algorithm.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4634">RFC 4634</a>
     */
    SHA384(48, "SHA-384"),

    /**
     * The SHA-256 message digest algorithm.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4634">RFC 4634</a>
     */
    SHA512(64, "SHA-512"),
    ;

    // Byte lengths are cached to avoid instantiating message digest objects.
    private final int lengthInBytes;
    private final String algorithm;

    // Cache for cloneable message digests.  Cloning digest is faster
    // than going through the provider mechanism.
    private volatile MessageDigest cache;

    // Set to true if cloning the digest failed.  Not volatile because
    // an incorrect false value is benign.
    private boolean cloneNotSupported;

    /**
     * The length in bytes of the digest.
     * @return the number of bytes returned by {@link MessageDigest#digest()}.
     */
    public final int lengthInBytes() {
        return lengthInBytes;
    }

    /**
     * The algorithm name as seen by the Java Cryptography Architecture (JCA).
     * @return a string, suitable as input to
     *   {@link MessageDigest#getInstance(String)}.
     */
    public final String algorithm() {
        return algorithm;
    }

    /**
     * Return a message digetst object for computing the digest.
     *
     * All digest algorithms are expected to be present in the
     * implementation.
     *
     * @return a new message digest object, instantiated as if by
     *   {@link MessageDigest#getInstance(String)}.
     */
    public MessageDigest newDigest() {
        MessageDigest local = cache;
        if (local != null)
            try {
                return (MessageDigest) local.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }

        // Cache is empty.  Instantiate a new digest object.
        try {
            local = MessageDigest.getInstance(algorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(
                "Unsupported digest algorithm: " + name() + '/' + algorithm);
        }

        // Attempt to populate the cache.  This also ensures that the
        // cache contains a pristine message digest.
        if (!cloneNotSupported)
            try {
                cache = (MessageDigest) local.clone();
            } catch (CloneNotSupportedException e) {
                // Record the clone failure so that we do not attempt
                // to clone the digest again.
                cloneNotSupported = true;
            }

        return local;
    }

    DigestAlgorithm(int lengthInBytes) {
        this.lengthInBytes = lengthInBytes;
        algorithm = name();
    }

    DigestAlgorithm(int lengthInBytes, String algorithm) {
        this.lengthInBytes = lengthInBytes;
        this.algorithm = algorithm;
    }
}
