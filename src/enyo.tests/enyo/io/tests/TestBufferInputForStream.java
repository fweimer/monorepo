package enyo.io.tests;

import enyo.io.BufferInput;
import enyo.io.function.IOBiConsumer;
import enyo.io.function.IOConsumer;
import enyo.io.function.IOFunction;
import enyo.io.function.IOPredicate;

import enyo.testing.Checks;
import enyo.testing.Cost;
import enyo.testing.ExitDuringTest;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

public final class TestBufferInputForStream {
    @Test
    public static void testNullInputStream(Testing t) throws IOException {
        TestBufferInput.testEmpty(t,
            () -> BufferInput.wrap(InputStream.nullInputStream()));
    }

    @Test
    public static void testArrayBased(Testing t) throws IOException {
        TestBufferInput.arrayBasedTests(t,
            array -> BufferInput.wrap(new ByteArrayInputStream(array)));
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
    public static void testSourceFilesByByte(Testing t) throws IOException {
        testSourceFiles(t, TestBufferInput::copyByte);
    }

    @Test
    @ExitDuringTest
    @Cost(1)
    public static void testSourceFilesByByteAndHalf(Testing t)
        throws IOException {
        testSourceFiles(t, TestBufferInput::copyByteAndHalf);
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

        // Input stream wrappers.
        var wrappers = List.<IOFunction<InputStream, ? extends InputStream>>of(
            in -> in,
            // Mask the actual implementation
            in -> new FilterInputStream(in) { },
            // One-byte-add-a-timer stream.
            in -> new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return in.read();
                    }

                    @Override
                    public void close() throws IOException {
                        in.close();
                    }
                });

        // Choices for the minRemaining parameter.
        var fillSizes = List.<IntSupplier>of(
            () -> 0,
            () -> actual.size() < expected.length ? 1 : 0,
            () -> actual.size() < expected.length - 2 ? 2 : 0,
            () -> (expected.length - actual.size()) / 3,
            () -> (expected.length - actual.size()) / 2,
            () -> expected.length - actual.size());

        for (var wrapper : wrappers)
            for (var fillSize : fillSizes) {
                actual.reset();
                try (var inp = BufferInput.wrap(
                        wrapper.apply(Files.newInputStream(path)))) {
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
