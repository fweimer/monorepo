package enyo.io.tests;

import enyo.io.BufferInput;
import enyo.io.function.IOBiConsumer;
import enyo.io.function.IOFunction;
import enyo.io.function.IOSupplier;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TestBufferInput {
    @Test
    public static void testEmptyByteArray(Testing t) throws IOException {
        testEmpty(t, () -> BufferInput.wrap(new byte[0]));
        testEmpty(t, () -> BufferInput.wrap(new byte[1], 0, 0));
        testEmpty(t, () -> BufferInput.wrap(new byte[1], 1, 0));
    }

    @Test
    public static void testEmptyBuffer(Testing t) throws IOException {
        testEmpty(t, () -> BufferInput.wrap(ByteBuffer.allocate(0)));
        testEmpty(t, () -> BufferInput.wrap(
                ByteBuffer.allocate(1).limit(0)));
        testEmpty(t, () -> BufferInput.wrap(
                ByteBuffer.allocate(1).position(1)));
    }

    @Test
    public static void testArrayBased(Testing t) throws IOException {
        arrayBasedTests(t, BufferInput::wrap);
        arrayBasedTests(t, array -> {
                var copy = new byte[array.length + 4];
                System.arraycopy(array, 0, copy, 2, array.length);
                return BufferInput.wrap(copy, 2, array.length);
            });
        arrayBasedTests(t, array -> BufferInput.wrap(ByteBuffer.wrap(array)));
        arrayBasedTests(t, array -> {
                var copy = ByteBuffer.allocate(array.length + 4)
                    .putShort((short) -1)
                    .put(array)
                    .position(2)
                    .limit(2 + array.length);
                return BufferInput.wrap(copy);
            });
    }

    static void testEmpty(Testing t, IOSupplier<BufferInput> supplier)
        throws IOException {
        try (var inp = supplier.get()) {
            t.examine(inp.fillBuffer(0))
                .map(ByteBuffer::order, e -> e.is(ByteOrder.BIG_ENDIAN))
                .testNot(buffer -> buffer.hasRemaining());
        }
        try (var inp = supplier.get()) {
            t.expect(() -> inp.fillBuffer(1), EOFException.class);
        }
        try (var inp = supplier.get()) {
            // Allocate the buffer first.
            t.examine(inp.fillBuffer(0))
                .map(ByteBuffer::order, e -> e.is(ByteOrder.BIG_ENDIAN))
                .testNot(buffer -> buffer.hasRemaining());
            t.expect(() -> inp.fillBuffer(1), EOFException.class);
        }
        try (var inp = supplier.get()) {
            t.expect(() -> inp.fillBuffer(Integer.MAX_VALUE),
                EOFException.class);
        }
        try (var inp = supplier.get()) {
            t.expect(() -> inp.fillBuffer(-1), IllegalArgumentException.class);
        }
        try (var inp = supplier.get()) {
            inp.skipNBytes(0);
        }
        try (var inp = supplier.get()) {
            t.expect(() -> inp.skipNBytes(1), EOFException.class);
        }
        try (var inp = supplier.get()) {
            t.expect(() -> inp.skipNBytes(Long.MAX_VALUE),
                EOFException.class);
        }
        // Skipping negative amounts is allowed for compatibility with
        // InputStream.skipNBytes(0.
        try (var inp = supplier.get()) {
            inp.skipNBytes(-1);
        }
        try (var inp = supplier.get()) {
            inp.skipNBytes(Long.MIN_VALUE);
        }
    }

    static void arrayBasedTests(Testing t,
        IOFunction<byte[], BufferInput> wrapper) throws IOException {
        arrayBasedTests(t, wrapper, true);
    }

    // stickyBuffer indicates whether buffers returned from
    // fillBuffer(int) change or are always reused if possible.
    static void arrayBasedTests(Testing t,
        IOFunction<byte[], BufferInput> wrapper, boolean stickyBuffer)
        throws IOException {
        testEmpty(t, () -> wrapper.apply(new byte[0]));
        testDataAndHugeException(t, wrapper);
        testLargeData(t, wrapper);
        testReallocateOrder(t, wrapper);
        testSkip(t, wrapper, TestBufferInput::copyAll, stickyBuffer);
        testSkip(t, wrapper, TestBufferInput::copyByte, stickyBuffer);
        testSkip(t, wrapper, TestBufferInput::copyByteAndHalf, stickyBuffer);
    }

    static void testDataAndHugeException(Testing t,
        IOFunction<byte[], BufferInput> wrapper) throws IOException {
        int largeSize = 100 * 1000;
        var expected = new byte[largeSize];
        ThreadLocalRandom.current().nextBytes(expected);

        try (var inp = wrapper.apply(expected)) {
            var buffer = inp.fillBuffer(0);
            t.examine(buffer)
                .map(b -> b.mismatch(ByteBuffer.wrap(expected)),
                    e -> e.test(val -> val == -1 || val == buffer.remaining()))
                .map(ByteBuffer::order, e -> e.is(ByteOrder.BIG_ENDIAN))
                .mapToInt(ByteBuffer::remaining, e -> e
                    .check(Checks.greaterThan(0))
                    .check(Checks.lessThanOrEquals(largeSize)));

            // This should throw EOFException without allocating too much.
            t.expect(() -> inp.fillBuffer(Integer.MAX_VALUE),
                EOFException.class);
        }
    }

    static void testLargeData(Testing t,
        IOFunction<byte[], BufferInput> wrapper) throws IOException {
        int largeSize = 512 * 1024;
        var blob = new byte[largeSize];
        blob[largeSize - 1] = 17;
        for (boolean atEnd : List.of(false, true)) {
            try (var inp = wrapper.apply(blob)) {
                var buffer = inp.fillBuffer(largeSize - 1);
                t.examine(buffer)
                    .mapToInt(ByteBuffer::remaining, e -> e
                        .check(Checks.inRangeClosed(largeSize - 1, largeSize))
                        .is(buffer.capacity()));

                if (buffer.remaining() < largeSize) {
                    // Discard the buffer contents.
                    if (atEnd)
                        buffer.position(buffer.limit());
                    else
                        buffer.limit(0);

                    // Read the one remaining byte.
                    t.examine(buffer.remaining()).is(0);
                    var buffer1 = inp.fillBuffer(0);
                    t.examine(buffer1)
                        .mapToInt(ByteBuffer::remaining)
                        .is(1);
                    t.examine(buffer1.get()).is(17);
                }
            }
        }
    }

    static void testReallocateOrder(Testing t,
        IOFunction<byte[], BufferInput> wrapper) throws IOException {
        int largeSize = 1000 * 1000;
        var expected = new byte[largeSize];
        ThreadLocalRandom.current().nextBytes(expected);
        try (var inp = wrapper.apply(expected)) {
            var previous = inp.fillBuffer(0);
            {
                var prev = previous;
                t.examine(prev)
                    .map(b -> b.mismatch(ByteBuffer.wrap(expected)),
                        e -> e.test(val ->
                            val == -1 || val == prev.remaining()));
            }

            // If the buffer immediately reveals the entire input,
            // this test cannot proceed meaningfully because there is
            // no way to trigger buffer reallocation.
            if (previous.remaining() == largeSize)
                return;

            previous.order(ByteOrder.LITTLE_ENDIAN);
            boolean mismatchWithPrevious = false;
            for (int i = 1; i < largeSize; ) {
                var buffer = inp.fillBuffer(i);
                t.examine(buffer)
                    .map(ByteBuffer::order, e -> e
                        .is(ByteOrder.LITTLE_ENDIAN))
                    .map(b -> b.mismatch(ByteBuffer.wrap(expected)),
                        e -> e.test(val ->
                            val == -1 || val == buffer.remaining()));
                if (mismatchWithPrevious) {
                    t.examine(buffer)
                        .isNotSame(previous)
                        .mapToInt(ByteBuffer::capacity)
                        .is(i);
                    previous = buffer;
                    i = Math.min(i * 11 / 10, largeSize);
                } else {
                    if (buffer != previous)
                        mismatchWithPrevious = true;
                    ++i;
                }
            }
            t.examine(mismatchWithPrevious).is(true);
        }
    }

    static void testSkip(Testing t,
        IOFunction<byte[], BufferInput> wrapper,
        IOBiConsumer<ByteBuffer, ByteArrayOutputStream> copier,
        boolean stickyBuffer) throws IOException {
        int largeSize = 200 * 1000;
        var expected = new byte[largeSize];
        ThreadLocalRandom.current().nextBytes(expected);

        var actual = new ByteArrayOutputStream();
        for (int initialRead : new int[] { -1, 0, 1, 2, 900, 10000 })
            for (int skip : new int[] { 0, 1, 1000, 12345, 100000 }) {
                actual.reset();
                try (var inp = wrapper.apply(expected)) {
                    ByteBuffer firstBuffer = null;
                    if (initialRead >= 0) {
                        firstBuffer = inp.fillBuffer(initialRead);
                        for (int i = 0; i < initialRead; ++i)
                            t.examine(firstBuffer.get())
                                .is(expected[i]);
                    } else
                        initialRead = 0;
                    inp.skipNBytes(skip);
                    while (true) {
                        var buffer = inp.fillBuffer(0);
                        if (firstBuffer != null) {
                            if (stickyBuffer)
                                t.examine(buffer).isSame(firstBuffer);
                        } else
                            firstBuffer = buffer;
                        if (!buffer.hasRemaining())
                            break;
                        copier.accept(buffer, actual);
                    }
                }
                t.examine((long) actual.size())
                    .is(expected.length - initialRead - skip);
                t.examine(actual.toByteArray())
                    .check(Checks.byteArrayEquals(
                            Arrays.copyOfRange(expected,
                                initialRead + skip, expected.length)));
            }
    }

    static void copyAll(ByteBuffer buffer, ByteArrayOutputStream out) {
        while (buffer.hasRemaining())
            out.write(buffer.get());
    }

    static void copyByte(ByteBuffer buffer, ByteArrayOutputStream out) {
        out.write(buffer.get());
    }

    static void copyByteAndHalf(ByteBuffer buffer, ByteArrayOutputStream out) {
        out.write(buffer.get());
        int target = buffer.remaining() / 2;
        for (int i = 0; i < target; ++i)
            out.write(buffer.get());
    }
}
