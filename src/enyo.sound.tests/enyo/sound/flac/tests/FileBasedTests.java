package enyo.sound.flac.tests;

import enyo.sound.flac.FlacInput;
import enyo.io.BufferInput;
import enyo.io.function.IOSupplier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

// Tests that require example FLAC files.
public final class FileBasedTests {
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            throw new IllegalArgumentException(
                "Some input files are required");

        var errors = new AtomicLong();
        for (String arg : args)
            process(Path.of(arg), errors);
        if (errors.get() > 0) {
            System.out.format("errors: %d%n", errors.get());
            System.exit(1);
        }
    }

    private static void process(Path path, AtomicLong errors)
        throws IOException {
        VerifyDigest.process(path, errors);
        run(path, errors, FileBasedTests::seekMiddle);
    }

    interface TestCase {
        void run(Supplier<FlacInput> makeFlac) throws IOException;
    }

    private static void run(Path path, AtomicLong errors, TestCase test) {
        try {
            test.run(IOSupplier.of(
                    () -> new FlacInput(BufferInput.wrap(
                            FileChannel.open(path)))));
        } catch (Throwable e) {
            errors.getAndIncrement();
            e.printStackTrace();
        }
    }

    // Seek right into the middle of the stream.
    private static void seekMiddle(Supplier<FlacInput> makeFlac)
        throws IOException {
        byte[] whole = readAll(makeFlac.get());
        var in = makeFlac.get();
        int skip = whole.length / 2;
        in.skipNBytes(skip);
        byte[] remaining = readAll(in);
        if (remaining.length != whole.length - skip)
            throw new AssertionError(String.format(
                    "whole: %d, skip: %d, remaining: %d",
                    whole.length, skip, remaining.length));
        if (!Arrays.equals(whole, skip, whole.length,
                remaining, 0, remaining.length))
            throw new AssertionError();
    }

    private static byte[] readAll(BufferInput in) throws IOException {
        var out = new ByteArrayOutputStream();
        while (true) {
            ByteBuffer buffer = in.fillBuffer(0);
            if (!buffer.hasRemaining())
                break;
            while (buffer.hasRemaining())
                out.write(buffer.get());
        }
        return out.toByteArray();
    }
}
