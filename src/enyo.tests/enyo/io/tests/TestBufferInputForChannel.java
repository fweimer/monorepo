package enyo.io.tests;

import enyo.io.BufferInput;
import enyo.io.TempDirectory;
import enyo.io.function.IOBiConsumer;
import enyo.io.function.IOConsumer;
import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;
import enyo.io.function.IORunnable;

import enyo.testing.Checks;
import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

public final class TestBufferInputForChannel {
    @Test
    public static void testEmptyChannel(Testing t) throws IOException {
        TestBufferInput.testEmpty(t, () -> {
                var pipe = Pipe.open();
                pipe.sink().close();
                return BufferInput.wrap(pipe.source());
            });
        TestBufferInput.testEmpty(t,
            () -> BufferInput.wrap(Channels.newChannel(
                    InputStream.nullInputStream())));
    }

    @Test
    public static void testArrayBased(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> BufferInput.wrap(Channels.newChannel(
                    new ByteArrayInputStream(array))));
        TestBufferInput.arrayBasedTests(t,
            TestBufferInputForChannel::wrapArrayViaPipe);
        try (var wrapper = new FileWrapper()) {
            TestBufferInput.arrayBasedTests(t, wrapper::wrap);
        }
    }

    @Test
    @ExitDuringTest
    public static void testPipeAllAtOnce(Testing t)
        throws IOException, InterruptedException {
        testPipe(t, TestBufferInput::copyAll);
    }

    @Test
    @ExitDuringTest
    public static void testPipeByByte(Testing t)
        throws IOException, InterruptedException {
        testPipe(t, TestBufferInput::copyByte);
    }

    @Test
    @ExitDuringTest
    public static void testPipeByByteAndHalf(Testing t)
        throws IOException, InterruptedException {
        testPipe(t, TestBufferInput::copyByteAndHalf);
    }

    @Test
    @ExitDuringTest
    @Cost(1)
    public static void testSourceFilesAllAtOnce(Testing t) throws IOException {
        testSourceFiles(t, TestBufferInput::copyAll);
    }

    @Test
    @ExitDuringTest
    @Cost(1)
    public static void testSourceFilesByByteAndHalf(Testing t)
        throws IOException {
        testSourceFiles(t, TestBufferInput::copyByte);
    }

    @Test
    @ExitDuringTest
    @Cost(1)
    public static void testSourceFilesByHalf(Testing t)
        throws IOException {
        testSourceFiles(t, TestBufferInput::copyByteAndHalf);
    }

    private static BufferInput wrapArrayViaPipe(byte[] array)
        throws IOException {
        var pipe = Pipe.open();
        var sink = pipe.sink();
        var copyThread = new Thread(IORunnable.of(() -> {
                    try {
                        var buf = ByteBuffer.wrap(array);
                        while (buf.hasRemaining())
                            sink.write(buf);
                    } catch (ClosedByInterruptException e) {
                        // Ignore.
                    } finally {
                        sink.close();
                    }
                }));
        copyThread.start();
        var source = pipe.source();
        var channel = new ReadableByteChannel() {
            @Override
            public int read(ByteBuffer buffer) throws IOException {
                return source.read(buffer);
            }

            @Override
            public boolean isOpen() {
                return source.isOpen();
            }

            @Override
            public void close() throws IOException {
                try {
                    copyThread.interrupt();
                    try {
                        copyThread.join();
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                } finally {
                    try {
                        source.close();
                    } finally {
                        sink.close();
                    }
                }
            }
        };
        return BufferInput.wrap(channel);
    }

    private static class FileWrapper implements Closeable {
        private final TempDirectory dir;
        private final AtomicInteger counter = new AtomicInteger();

        FileWrapper() throws IOException {
            dir = TempDirectory.withPrefix("TestBufferInputForChannel-");
        }

        BufferInput wrap(byte[] array) throws IOException {
            // We want to use a real FileChannel, but do not implement
            // the dozen methods a file channel has.  This is way we
            // cannot simply delete the file on close() and have to
            // use this class.
            var path = dir.resolve(
                Integer.toString(counter.incrementAndGet()));
            var file = FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
            Channels.newOutputStream(file).write(array);
            file.position(0);
            return BufferInput.wrap(file);
        }

        @Override
        public void close() throws IOException {
            dir.close();
        }
    }

    private static void testPipe(Testing t,
        IOBiConsumer<ByteBuffer, ByteArrayOutputStream> copier)
        throws IOException, InterruptedException {
        var expected = new byte[1024 * 1024];
        ThreadLocalRandom.current().nextBytes(expected);
        var actual = new ByteArrayOutputStream();

        for (int fillSize : new int[] { 0, 1, 2, 1000, 10000, 100000 }) {
            actual.reset();
            try (var inp = wrapArrayViaPipe(expected)) {
                while (true) {
                    int minRemaining = Math.min(
                        fillSize, expected.length - actual.size());
                    var buffer = inp.fillBuffer(minRemaining);
                    t.examine(buffer.remaining())
                        .check(Checks.greaterThanOrEquals(minRemaining));
                    t.examine(buffer)
                        .map(ByteBuffer::order)
                        .is(ByteOrder.BIG_ENDIAN);
                    if (!buffer.hasRemaining())
                        break;
                    copier.accept(buffer, actual);
                }
            }
            t.examine(actual.toByteArray())
                .check(Checks.byteArrayEquals(expected));
        }
    }

    private static void testSourceFiles(Testing t,
        IOBiConsumer<ByteBuffer, ByteArrayOutputStream> copier)
        throws IOException {
        // Test using a random subset of the files.
        int subsetSize = 30;
        ArrayList<Path> paths;
        try (var stream = Files.walk(Path.of("src"))) {
            paths = stream
                .filter(IOPredicate.of(Files::isRegularFile))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        Collections.shuffle(paths);
        paths.stream()
            .limit(subsetSize)
            .forEach(IOConsumer.of(path -> checkOneFile(t, path, copier)));
    }

    // Read the file contents with various splitters and check the
    // results.
    private static void checkOneFile(Testing t, Path path,
        IOBiConsumer<ByteBuffer, ByteArrayOutputStream> copier)
        throws IOException {
        var expected = Files.readAllBytes(path);
        var actual = new ByteArrayOutputStream();

        // Choices for the minRemaining parameter.
        var fillSizes = List.<IntSupplier>of(
            () -> 0,
            () -> actual.size() < expected.length ? 1 : 0,
            () -> actual.size() < expected.length - 2 ? 2 : 0,
            () -> (expected.length - actual.size()) / 3,
            () -> (expected.length - actual.size()) / 2,
            () -> expected.length - actual.size());

        for (var fillSize : fillSizes) {
            actual.reset();
            try (var inp = BufferInput.wrap(FileChannel.open(path))) {
                while (true) {
                    var buffer = inp.fillBuffer(fillSize.getAsInt());
                    t.examine(buffer)
                        .map(ByteBuffer::order)
                        .is(ByteOrder.BIG_ENDIAN);
                    if (!buffer.hasRemaining())
                        break;
                    copier.accept(buffer, actual);
                }
            }
            t.examine(actual.toByteArray())
                .context(path.toString())
                .check(Checks.byteArrayEquals(expected));
        }
    }
}
