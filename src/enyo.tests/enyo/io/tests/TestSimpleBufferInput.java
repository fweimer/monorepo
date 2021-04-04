package enyo.io.tests;

import enyo.io.SimpleBufferInput;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TestSimpleBufferInput {
    // Returns successive buffers from an iterator.
    final static class IterableBufferInput extends SimpleBufferInput {
        private final Iterator<ByteBuffer> buffers;

        IterableBufferInput(Iterable<ByteBuffer> buffers) {
            this.buffers = buffers.iterator();
        }

        IterableBufferInput(byte[] array) {
            this(split(array));
        }

        @Override
        protected ByteBuffer fillBuffer() {
            if (buffers.hasNext())
                return buffers.next();
            else
                return ByteBuffer.allocate(0);
        }

        @Override
        public void close() {
        }

        private static List<ByteBuffer> split(byte[] array) {
            var random = ThreadLocalRandom.current();
            var result = new ArrayList<ByteBuffer>();
            for (int pos = 0; pos < array.length; ) {
                int remaining = array.length - pos;
                int length;
                if (remaining > 30)
                    length = Math.min(random.nextInt(remaining / 15) + 1,
                        remaining);
                else
                    length = Math.min(random.nextInt(remaining) + 1, remaining);
                result.add(ByteBuffer.wrap(array, pos, length).slice());
                pos += length;
            }
            return result;
        }
    }

    // Copies a random amount of bytes into the same result buffer.
    static class ArraySplitter extends SimpleBufferInput {
        protected final byte[] array;
        protected final ByteBuffer buffer;
        protected int pos;

        ArraySplitter(byte[] array) {
            this.array = array;
            buffer = ByteBuffer.allocate(array.length);
        }

        @Override
        protected ByteBuffer fillBuffer() {
            int remaining = array.length - pos;
            if (remaining == 0)
                return buffer.limit(0);

            var random = ThreadLocalRandom.current();
            int length;
            if (remaining > 30)
                length = Math.min(random.nextInt(remaining / 15) + 1,
                    remaining);
            else
                length = Math.min(random.nextInt(remaining) + 1, remaining);
            buffer
                .clear()
                .put(array, pos, length)
                .flip();
            pos += length;
            return buffer;
        }

        @Override
        public void close() {
        }
    }

    static class ArraySplitterWithSplit extends ArraySplitter {
        ArraySplitterWithSplit(byte[] array) {
            super(array);
        }

        @Override
        protected ByteBuffer skipNBytesImpl(long n) throws IOException {
            if (n > array.length - pos)
                throw new EOFException();
            pos += (int) n;
            return null;
        }
    }

    @Test
    public static void testEmpty(Testing t) throws IOException {
        TestBufferInput.testEmpty(t, () -> new IterableBufferInput(List.of()));
    }

    @Test
    public static void testArrayBased1(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> new IterableBufferInput(List.of(ByteBuffer.wrap(array))),
            false);
    }

    @Test
    public static void testArrayBased2(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> new IterableBufferInput(array), false);
    }

    @Test
    public static void testArrayBased3(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> {
                if (array.length > 2) {
                    int midPoint = array.length / 2;
                    return new IterableBufferInput(List.of(
                            ByteBuffer.wrap(array, 0, midPoint),
                            ByteBuffer.wrap(
                                array, midPoint, array.length - midPoint)));
                } else
                    return new IterableBufferInput(
                        List.of(ByteBuffer.wrap(array)));
            }, false);
    }

    @Test
    public static void testArrayBased4(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> new ArraySplitter(array), false);
    }

    @Test
    public static void testArrayBased5(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> new ArraySplitterWithSplit(array), false);
    }
}
