package enyo.sound.flac.tests;

import enyo.crypto.DigestAlgorithm;
import enyo.io.BufferInput;
import enyo.io.function.IOFunction;

import enyo.sound.flac.FlacInput;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class VerifyDigest {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.format("usage: %s DIRECTORY-OR-FILE...%n",
                VerifyDigest.class);
            System.exit(1);
        }

        AtomicLong errors = new AtomicLong();

        Arrays.stream(args)
            .map(Path::of)
            .flatMap(IOFunction.of(path -> {
                        // Recursively list subdirectories.
                        if (Files.isDirectory(path))
                            try (Stream<Path> stream = Files.walk(path)) {
                                return stream
                                    .filter(
                                        path1 -> Files.isRegularFile(path1))
                                    .filter(path1 -> path1.toString()
                                        .endsWith(".flac"))
                                    // Create temporary list, so that we
                                    // can close the stream.
                                    .collect(Collectors.toList())
                                    .stream();
                            }
                        else
                            return Stream.of(path);
                    }))
            .collect(Collectors.toList())
            .parallelStream()
            .unordered()
            .forEach(path -> process(path, errors));

        if (errors.get() > 0)
            System.exit(1);
    }

    static void process(Path path, AtomicLong errors) {
        try (FileChannel in0 = FileChannel.open(path);
            FlacInput in = new FlacInput(BufferInput.wrap(in0))) {
            MessageDigest md = DigestAlgorithm.MD5.newDigest();
            while (true) {
                ByteBuffer b = in.fillBuffer(0);
                if (!b.hasRemaining())
                    break;
                md.update(b);
            }
            if (!Arrays.equals(md.digest(), in.streaminfo().digest())) {
                errors.getAndIncrement();
                synchronized (System.err) {
                    System.err.format(
                        "%s: error: computed digest does not match%n",
                        path);
                }
            }
        } catch (IOException | RuntimeException e) {
            errors.getAndIncrement();
            synchronized (System.err) {
                System.err.format("%s: error:%n", path);
                e.printStackTrace(System.err);
            }
        }
    }
}
