package enyo.hash;

import java.lang.invoke.VarHandle;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

/**
 * The XXH32 non-cryptographic hashing algorithm.
 * @see <a href="https://www.ietf.org/archive/id/draft-josefsson-xxhash-00.html#name-xxh32-algorithm-description">draft-josefsson-xxhash-00: XXH32 Algorithm Description</a>
 */
public final class XXH32 {
    /**
     * Creates a new hashing object with seed 0.
     */
    public XXH32() {
        reset(0);
    }

    /**
     * Creates a new hashing object with the specified seed.
     * @param seed the seed to use
     */
    public XXH32(int seed) {
        reset(seed);
    }

    /**
     * Resets this hashing object using the specified seed.
     *
     * Processing proceeds as if the {@link #XXH32(int) X32(seed)}
     * constructor had been used with a new object.
     * @param seed the seed to use
     */
    public void reset(int seed) {
        acc1 = seed + PRIME32_1 + PRIME32_2;
        acc2 = seed + PRIME32_2;
        acc3 = seed;
        acc4 = seed - PRIME32_1;
        stored = 0;
        length = 0;
    }

    /**
     * Hashes the bytes in the memory segment.
     * @param buf the bytes to process
     * @throws NullPointerException if {@code buf} is null
     */
    public void update(MemorySegment buf) {
        long bufPos;
        if (stored > 0) {
            long toCopy = Math.min(buf.byteSize(), BUFSIZE - stored);
            MemorySegment.copy(buf, 0, storage, stored, toCopy);
            stored += (int) toCopy;
            if (stored == BUFSIZE) {
                update0(storage, 0);
                bufPos = toCopy;
                // stored is reset below.
            } else {
                // The buffer was not filled.  No hashing at this time.
                length += toCopy;
                return;
            }
        } else
            bufPos = 0;

        // The loop processes 16 bytes at a time.
        long bufEnd = bufPos + (((buf.byteSize() - bufPos) >> 4) << 4);
        while (bufPos < bufEnd) {
            update0(buf, bufPos);
            bufPos += BUFSIZE;
        }

        // Store the leftover.
        long remaining = buf.byteSize() - bufPos;
        MemorySegment.copy(buf, bufPos, storage, 0, remaining);
        stored = (int) remaining;
        length += buf.byteSize();
    }

    /**
     * Hashes a single byte.
     * @param b the byte to hash
     */
    public void update(byte b) {
        storage.set(ValueLayout.JAVA_BYTE, stored++, b);
        ++length;
        if (stored == BUFSIZE) {
            update0(storage, 0);
            stored = 0;
        }
    }

    /**
     * Hashes the bytes in a byte array.
     * @param b the bytes to process
     * @throws NullPointerException if {@code b} is null
     */
    public void update(byte[] b) {
        update(MemorySegment.ofArray(b));
    }

    /**
     * Hashes the bytes in a byte array.
     * @param b the array which provides the bytes to process
     * @param off the first byte in {@code b} to hash
     * @param len the number of bytes in {@code b} to hash
     * @throws NullPointerException if {@code b} is null
     * @throws IndexOutOfBoundsException if {@code off}, {@code len}
     *   do not match {@code b}
     */
    public void update(byte[] b, int off, int len) {
        update(MemorySegment.ofArray(b).asSlice(off, len));
    }

    private void update0(MemorySegment buf, long bufPos) {
        // This is currently (early OpenJDK 28) not well-handed
        // by C2.  A lot of calls remain in the generated code.
        int lane1 = (int) lane.get(buf, bufPos);
        int lane2 = (int) lane.get(buf, bufPos + 4);
        int lane3 = (int) lane.get(buf, bufPos + 8);
        int lane4 = (int) lane.get(buf, bufPos + 12);

        acc1 += lane1 * PRIME32_2;
        acc1 = Integer.rotateLeft(acc1, 13);
        acc1 *= PRIME32_1;

        acc2 += lane2 * PRIME32_2;
        acc2 = Integer.rotateLeft(acc2, 13);
        acc2 *= PRIME32_1;

        acc3 += lane3 * PRIME32_2;
        acc3 = Integer.rotateLeft(acc3, 13);
        acc3 *= PRIME32_1;

        acc4 += lane4 * PRIME32_2;
        acc4 = Integer.rotateLeft(acc4, 13);
        acc4 *= PRIME32_1;
    }

    /**
     * Returns the computed hash.
     * After calling this method, further data can be hashed using
     * {@link #update(MemorySegment)} and related methods.  Calling
     * this method does not alter the outcome of future {@code finish()}
     * invocations.
     * @return the hash
     */
    public int finish() {
        int acc;
        if (length < BUFSIZE)
            // acc3 was initialized with the seed.
            acc = acc3 + PRIME32_5;
        else
            acc = Integer.rotateLeft(acc1, 1)
                + Integer.rotateLeft(acc2, 7)
                + Integer.rotateLeft(acc3, 12)
                + Integer.rotateLeft(acc4, 18);
        acc += (int) length;

        // Remaining bytes.  This covers short inputs, too.
        int end = (stored >> 2) << 2;
        for (int pos = 0; pos < end; pos += 4) {
            acc += (int) lane.get(storage, pos) * PRIME32_3;
            acc = Integer.rotateLeft(acc, 17) * PRIME32_4;
        }
        for (int pos = end; pos < stored; ++pos) {
            acc += (storage.get(ValueLayout.JAVA_BYTE, pos) & 0xff) * PRIME32_5;
            acc = Integer.rotateLeft(acc, 11) * PRIME32_1;
        }

        acc ^= (acc >>> 15);
        acc *= PRIME32_2;
        acc ^= (acc >>> 13);
        acc *= PRIME32_3;
        acc ^= acc >>> 16;

        return acc;
    }

    private int acc1;
    private int acc2;
    private int acc3;
    private int acc4;

    // Operate on 16 byte blocks only.  Short inputs are stored here.
    private static final int BUFSIZE = 16;
    private final MemorySegment storage =
        MemorySegment.ofArray(new byte[BUFSIZE]);
    private int stored;

    // Bytes hashed so far.  Includes stored bytes.
    private long length;

    private static final VarHandle lane = ValueLayout.JAVA_INT_UNALIGNED
         .withOrder(ByteOrder.LITTLE_ENDIAN).varHandle();

    private static final int PRIME32_1 = 0x9E3779B1;
    private static final int PRIME32_2 = 0x85EBCA77;
    private static final int PRIME32_3 = 0xC2B2AE3D;
    private static final int PRIME32_4 = 0x27D4EB2F;
    private static final int PRIME32_5 = 0x165667B1;
}
