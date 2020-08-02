package enyo.io;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

/**
 * Surrogate-escaping variant of UTF-8.
 */
final class UTF8SCharset extends Charset {
    @Override
    public boolean contains(Charset cs) {
        return cs.getClass() == UTF8SCharset.class
            || StandardCharsets.UTF_8.contains(cs);
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    protected UTF8SCharset() {
        super("UTF-8S", new String[] { "UTF_8S", "UTF-8b" });
    }

    private final static class Decoder extends CharsetDecoder {
        private Decoder(Charset cs) {
            super(cs, 1.0f, 4.0f);
        }

        /**
         * Contains the partially read codepoint, already decoded.
         * The most recently read follower byte is stored in the
         * lowest six bits.  Only valid if {@link #bytesNeeded} is
         * positive.
         */
        private int value;

        /**
         * The total number of bytes needed to complete {@link #value}.
         * Only valid if {@link #bytesNeeded} is positive.
         */
        private byte bytesNeeded;

        /**
         * The number of bytes stored in {@link #value} so far.
         * Only valid if {@link #bytesNeeded} is positive.
         */
        private byte bytesStored;

        /**
         * Write the contents of {@link #value} to the buffer and
         * reset the internal state.  The operation either completely
         * flushes all bytes, or writes nothing.  {@link #bytesNeeded}
         * must be positive and is reset to zero on success (if false
         * is returned).
         *
         * @return true if an overflow occurred (and nothing has been
         *   written), false otherwise.
         */
        private boolean flushValueEscaped(CharBuffer out) {
            if (out.remaining() < bytesStored)
                // Output overflow.
                return true;

            // Reconstruct the original start byte.
            int shift = (bytesStored - 1) * 6;
            int initialByte = (value >> shift) | ((-256) >> bytesNeeded);
            putEscaped(out, (byte) initialByte);
            shift -= 6;

            // And the follower bytes.
            while (shift >= 0) {
                int bits = (value >> shift) & 0b0011_1111;
                putEscaped(out, (byte) (0b1000_0000 | bits));
                shift -= 6;
            }

            // The value has been flushed.
            bytesNeeded = 0;
            return false;
        }

        private CoderResult intermediateFlush(ByteBuffer in, CharBuffer out) {
            // Try to complete the value.
            while (bytesStored < bytesNeeded) {
                if (!in.hasRemaining())
                    return CoderResult.UNDERFLOW;
                byte b = in.get();
                if (isFollower(b)) {
                    // Followers can be stored.
                    value = (value << 6) | (b & 0b0011_1111);
                    ++bytesStored;
                } else {
                    // The byte will be handled by the main loop in
                    // decodeLoop().
                    backup(in);
                    // The stored value is invalid.
                    if (flushValueEscaped(out))
                        return CoderResult.OVERFLOW;
                    return null;
                }
            }

            // All bytes have been obtained.  Check that this is not
            // an overlong or otherwise invalid encoding.
            boolean valid;
            switch (bytesNeeded) {
            case 2:
                valid = 0x0080 <= value && value <= 0x07FF;
                break;
            case 3:
                valid = 0x0800 <= value && value <= 0xFFFF
                    && !isSurrogate(value);
                break;
            case 4:
            default: // Cannot happen.
                valid = 0x01_0000 <= value && value <= 0x10_FFFF;
                break;
            }
            if (!valid) {
                // Invalid values need to be escaped.
                if (flushValueEscaped(out))
                    return CoderResult.OVERFLOW;
                return null;
            } else {
                if (value <= 0xFFFF) {
                    if (!out.hasRemaining())
                        return CoderResult.OVERFLOW;
                    out.put((char) value);
                } else {
                    if (out.remaining() < 2)
                        return CoderResult.OVERFLOW;
                    putSurrogates(out, value);
                }
                bytesNeeded = 0;
                return null;
            }
        }

        @Override
        protected void implReset() {
            bytesNeeded = 0;
        }

        /**
         * Called when there is an incomplete UTF-8 sequence at the
         * end of the input.  All buffered bytes need to be flushed.
         */
        @Override
        protected CoderResult implFlush(CharBuffer out) {
            if (bytesNeeded > 0 && flushValueEscaped(out))
                return CoderResult.OVERFLOW;
            else
                return CoderResult.UNDERFLOW;
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            // Flush any pending bytes.
            if (bytesNeeded > 0) {
                CoderResult result = intermediateFlush(in, out);
                if (result != null)
                    return result;
            }

           while (in.hasRemaining() && out.hasRemaining()) {
                byte b1raw = in.get();
                if (b1raw >= 0) {
                    // Every ASCII byte is encoded as itself.
                    out.put((char) b1raw);
                    continue;
                }

                int b1 = Byte.toUnsignedInt(b1raw);
                if (b1 < 0b1100_0000) {
                    // Lone follower byte.
                    putEscaped(out, b1raw);
                } else if (b1 < 0b1110_0000) {
                    // Two-byte encoding.
                    int ch = b1 & 0b0001_1111;
                    if (!in.hasRemaining()) {
                        value = ch;
                        bytesNeeded = 2;
                        bytesStored = 1;
                        return CoderResult.UNDERFLOW;
                    }
                    byte b2 = in.get();
                    ch = (ch << 6) | (b2 & 0b0011_1111);
                    if (!isFollower(b2) || ch < 0x80) {
                        // Not a follower byte, or overlong encoding.
                        // Re-decode second byte.
                        putEscaped(out, b1raw);
                        backup(in);
                    } else
                        // ch can only go up to 2047, so no surrogate
                        // check needed.
                        out.put((char) ch);
                } else if (b1 < 0b1111_0000) {
                    // Three-byte encoding.
                    int ch = b1 & 0b0000_1111;
                    if (in.remaining() < 2) {
                        if (in.hasRemaining()) {
                            // One more byte is available.  Store it
                            // if it is a follower.
                            byte b2 = in.get();
                            if (isFollower(b2)) {
                                value = (ch << 6) | (b2 & 0b0011_1111);
                                bytesStored = 2;
                            } else {
                                // Not a follower byte.  Cannot store
                                // it.  Escape the first byte and
                                // re-decode the second byte.
                                putEscaped(out, b1raw);
                                backup(in);
                                continue;
                            }
                        } else {
                            // Only the first byte is available.
                            value = ch;
                            bytesStored = 1;
                        }
                        bytesNeeded = 3;
                        return CoderResult.UNDERFLOW;
                    }
                    byte b2 = in.get();
                    byte b3 = in.get();
                    ch = (ch << 12)
                        | ((b2 & 0b0011_1111) << 6)
                        | (b3 & 0b0011_1111);
                    if (!isFollower(b2) || !isFollower(b3) || ch < 0x0800
                        || isSurrogate(ch)) {
                        // Not a follower byte, or overlong encoding,
                        // or surrogate.  Re-decode second and third
                        // byte.
                        putEscaped(out, b1raw);
                        backup(in, 2);
                    } else
                        out.put((char) ch);
                } else if (b1 < 0b1111_1000) {
                    // Four-byte encoding.
                    int ch = b1 & 0b0000_0111;
                    if (in.remaining() < 3) {
                        if (in.hasRemaining()) {
                            byte b2 = in.get();
                            if (isFollower(b2)) {
                                if (in.hasRemaining()) {
                                    byte b3 = in.get();
                                    if (isFollower(b3)) {
                                        // All bytes are followers.
                                        // Store them.
                                        value = (ch << 12)
                                            | ((b2 & 0b0011_1111) << 6)
                                            | (b3 & 0b0011_1111);
                                        bytesNeeded = 4;
                                        bytesStored = 3;
                                        return CoderResult.UNDERFLOW;
                                    } else {
                                        // Third byte is not a follower.
                                        // The sequence is invalid.  Escape
                                        // the first byte and re-decode the
                                        // second (for simplicity, will fail
                                        // as well).
                                        putEscaped(out, b1raw);
                                        backup(in, 2);
                                    }
                                } else {
                                    // Two bytes present, second byte is a
                                    // follower.
                                    value = (ch << 6) | (b2 & 0b0011_1111);
                                    bytesNeeded = 4;
                                    bytesStored = 2;
                                }
                            } else {
                                // Second byte is not a follower.  Escape
                                // first byte and re-decoded the second.
                                putEscaped(out, b1raw);
                                backup(in);
                            }
                        } else {
                            // Only the first byte is present.  Store it.
                            value = ch;
                            bytesNeeded = 4;
                            bytesStored = 1;
                            return CoderResult.UNDERFLOW;
                        }
                    } else {
                        // Three more bytes are available in the input.
                        byte b2 = in.get();
                        byte b3 = in.get();
                        byte b4 = in.get();
                        ch = (ch << 18)
                            | ((b2 & 0b0011_1111) << 12)
                            | ((b3 & 0b0011_1111) << 6)
                            | (b4 & 0b0011_1111);
                        if (!isFollower(b2) || !isFollower(b3)
                            || !isFollower(b4)
                            || ch < 0x01_0000 || ch > 0x10_FFFF) {
                            // Not a follower byte, or overlong encoding, or
                            // out of UTF-16 range.  Re-decode starting with
                            // the second byte.
                            putEscaped(out, b1raw);
                            backup(in, 3);
                        } else {
                            // Split into high and low surrogate.
                            if (out.remaining() < 2)
                                return backup(in, 4);
                            putSurrogates(out, ch);
                        }
                    }
                } else
                    // Everything else is invalid.
                    putEscaped(out, b1raw);
            }
            if (in.hasRemaining())
                return CoderResult.OVERFLOW;
            else
                return CoderResult.UNDERFLOW;
        }
    }

    private final static class Encoder extends CharsetEncoder {
        private Encoder(Charset cs) {
            super(cs, 1.0f, 4.0f);
        }

        /**
         * Outputs the two characters as a surrogate pair, resultinmg
         * in four output bytes.
         */
        private static void putSurrogatePair(ByteBuffer out,
            char ch1, char ch2) {
            int value =
                0x01_0000 + ((ch1 - 0xD800) << 10) + (ch2 - 0xDC00);
            out.put((byte) (0b1111_0000 | (value >> 18)));
            out.put((byte) (0b1000_0000 | ((value >> 12) & 0b0011_1111)));
            out.put((byte) (0b1000_0000 | ((value >> 6) & 0b0011_1111)));
            out.put((byte) (0b1000_0000 | (value & 0b0011_1111)));
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.hasRemaining() && out.hasRemaining()) {
                char ch = in.get();
                if (ch < '\u0080')
                    // ASCII characters are encoded as themselves.
                    out.put((byte) ch);
                else if (ch < '\u0800') {
                    // Character in the two-byte range.
                    if (out.remaining() < 2)
                        return backup(in);
                    out.put((byte) (0b1100_0000 | (ch >> 6)));
                    out.put((byte) (0b1000_0000 | (ch & 0b0011_1111)));
                } else if (ch < '\uD800' || ch >= '\uE000') {
                    // Three byte encoding, not a surrogate.
                    if (out.remaining() < 3)
                        return backup(in);
                    out.put((byte) (0b1110_0000 | (ch >> 12)));
                    out.put((byte) (0b1000_0000 | ((ch >> 6) & 0b0011_1111)));
                    out.put((byte) (0b1000_0000 | (ch & 0b0011_1111)));
                } else if (isEscapingSurrogate(ch))
                    out.put((byte) (ch - BYTE_SURROGATE_OFFSET));
                else if (ch < '\uDC00') {
                    // High surrogate.  Should be followed by a low
                    // surrogate.
                    if (in.hasRemaining()) {
                       char ch2 = in.get();
                       if (Character.isLowSurrogate(ch2)) {
                           if (out.remaining() < 4)
                               return backup(in, 2);
                           putSurrogatePair(out, ch, ch2);
                       } else {
                           // Lone high surrogate.  Malformed.
                           backup(in, 2);
                           return CoderResult.malformedForLength(1);
                       }
                    } else {
                        // High surrogate with no immediatelly following low
                        // surrogate.  We need more data to see if this
                        // valid.
                        backup(in);
                        return CoderResult.UNDERFLOW;
                    }
                } else {
                    // Non-escaping leading low surrogate.  Always
                    // malformed.
                    backup(in);
                    return CoderResult.malformedForLength(1);
                }
            }
            if (in.hasRemaining())
                return CoderResult.OVERFLOW;
            else
                return CoderResult.UNDERFLOW;
        }
    }

    private static final int BYTE_SURROGATE_OFFSET = 0xDD00;

    private static void putEscaped(CharBuffer out, byte b) {
        // Byte 0x80 is mapped to 0xDC80.
        // Byte 0xFF is mapped to 0xDCFF.
        out.put((char) (BYTE_SURROGATE_OFFSET + b));
    }

    private static boolean isEscapingSurrogate(char ch) {
        return '\uDC80' <= ch && ch <= '\uDCFF';
    }

    private static void putSurrogates(CharBuffer out, int ch) {
        int offset = ch - 0x01_0000;
        out.put((char) (0xD800 + (offset >> 10)));
        out.put((char) (0xDC00 + (offset & 0b0011_1111_1111)));
    }

    private static boolean isFollower(byte b) {
        return (b & 0b1100_0000) == 0b1000_0000;
    }

    private static boolean isSurrogate(int ch) {
        return 0xD800 <= ch && ch <= 0xDFFF;
    }

    /**
     * Moves the buffer back by one element and returns
     * {@link CoderResult.OVERFLOW}.
     */
    private static CoderResult backup(Buffer buffer) {
        return backup(buffer, 1);
    }

    /**
     * Moves the buffer back by the requested number of elements and
     * returns {@link CoderResult.OVERFLOW}.
     */
    private static CoderResult backup(Buffer buffer, int amount) {
        buffer.position(buffer.position() - amount);
        return CoderResult.OVERFLOW;
    }
}
