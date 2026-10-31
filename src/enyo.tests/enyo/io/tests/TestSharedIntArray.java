package enyo.io.tests;

import enyo.io.SharedIntArray;
import enyo.io.TempDirectory;
import enyo.io.TempFile;
import enyo.lang.RelaunchVM;
import enyo.testing.Cost;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class TestSharedIntArray {
    @Test
    public static void testLocal(Testing t) throws IOException {
        try (TempFile tmp = TempFile.create("TestSharedIntArray-", ".tmp")) {
            SharedIntArray array = SharedIntArray.open(tmp.path(), 1,
                StandardOpenOption.CREATE, StandardOpenOption.READ,
                StandardOpenOption.WRITE);
            t.examine(Files.size(tmp.path())).is(4);
            t.examine(array.path()).isSame(tmp.path());
            t.examine(array.buffer().capacity()).is(1);
            t.examine(array.buffer().order()).is(ByteOrder.nativeOrder());
            t.examine(array.size()).is(1);
            t.examine(array.get(0)).is(0);
            array.set(0, 1);
            t.examine(array.get(0)).is(1);

            // Check that updates are propagated across different
            // array objects.
            SharedIntArray array2 = SharedIntArray.open(tmp.path(), 1,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
            t.examine(array2.path()).isSame(tmp.path());
            t.examine(array2.buffer().capacity()).is(1);
            t.examine(array2.size()).is(1);
            t.examine(array2.get(0)).is(1);
            array2.set(0, 2);
            t.examine(array.buffer().get()).is(2);
            array.buffer().put(3);
            t.examine(array2.get(0)).is(3);

            t.expect(() -> array.get(-1), IndexOutOfBoundsException.class);
            t.expect(() -> array.set(-1, -1), IndexOutOfBoundsException.class);
            t.expect(() -> array.get(5), IndexOutOfBoundsException.class);
            t.expect(() -> array.set(5, -1), IndexOutOfBoundsException.class);
        }
    }

    @Test
    public static void testMissing(Testing t) throws IOException {
        try(TempDirectory tmp = TempDirectory.withPrefix(
                "TestSharedIntArray-")) {
            Path path = tmp.path().resolve("array");
            t.expect(() -> SharedIntArray.open(path, 1,
                    StandardOpenOption.READ, StandardOpenOption.WRITE),
                NoSuchFileException.class)
		.map(Exception::getMessage).is(path.toString());
            t.expect(() -> SharedIntArray.open(tmp.path(), 1,
                    StandardOpenOption.READ, StandardOpenOption.WRITE),
                FileSystemException.class)
		.map(FileSystemException::getMessage,
		    e -> e.is(tmp.path() + ": Is a directory"))
		.map(FileSystemException::getFile,
		    e -> e.is(tmp.path().toString()))
		.map(FileSystemException::getReason,
		    e -> e.is("Is a directory"));
        }
    }

    @Test
    public static void testSizeMismatch(Testing t) throws IOException {
        try (TempFile tmp = TempFile.create("TestSharedIntArray-", ".tmp")) {
            Files.writeString(tmp.path(), "123");
            t.expect(() -> SharedIntArray.open(tmp.path(), 1,
                    StandardOpenOption.READ, StandardOpenOption.WRITE),
                IOException.class)
	    .map(Exception::getMessage)
	    .test(s -> s.endsWith(
                ": invalid file size 3, expected 4 bytes (1 elements)"));
        }
    }

    @Test
    public static void testReadOnly(Testing t) throws IOException {
        try (TempFile tmp = TempFile.create("TestSharedIntArray-", ".tmp")) {
            int value = 0x1234578;
            {
                byte[] nativeIntBlob = new byte[4];
                ByteBuffer.wrap(nativeIntBlob)
                    .order(ByteOrder.nativeOrder())
                    .putInt(value);
                Files.write(tmp.path(), nativeIntBlob);
            }
            SharedIntArray array = SharedIntArray.open(tmp.path(), 1,
                StandardOpenOption.READ);
            IntBuffer buffer = array.buffer();
            t.examine(array.buffer()).isNotSame(buffer);
            t.examine(buffer.get()).is(value);
            buffer.clear();
            t.expect(() -> buffer.put(0), ReadOnlyBufferException.class);
            t.examine(array.get(0)).is(value);
            t.expect(() -> array.set(0, 0),
                UnsupportedOperationException.class)
		.map(Exception::getCause)
                .isExactClass(ReadOnlyBufferException.class);
        }
    }

    @Test
    @Resources(threads=2)
    @Cost(0.5)
    public static void testRemote(Testing t)
        throws IOException, InterruptedException {
        try (TempFile tmp = TempFile.create("TestSharedIntArray-", ".tmp")) {
            SharedIntArray array = SharedIntArray.open(tmp.path(), 2,
                StandardOpenOption.CREATE, StandardOpenOption.READ,
                StandardOpenOption.WRITE);
            t.examine(Files.size(tmp.path())).is(8);
            t.examine(array.buffer().capacity()).is(2);
            t.examine(array.size()).is(2);
            array.set(0, 1);
            array.set(1, 2);
            ProcessBuilder pb = RelaunchVM.processBuilder(
                TestSharedIntArray.class,
                List.of("remote", array.path().toString()));
            pb.inheritIO();
            Process process = pb.start();
            while (true) {
                int value = array.get(1);
                if (value == 2)
                    continue;
                else {
                    t.examine(value).is(4);
                    break;
                }
            }
            array.set(0, 3);
            t.examine(process.waitFor()).is(0);
            t.examine(array.get(1)).is(6);
        }
    }

    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "remote":
                testRemote0(Path.of(args[1]));
                break;
        default:
            throw new IllegalArgumentException(args[0]);
        }
    }

    private static void testRemote0(Path path) throws IOException {
        var array = SharedIntArray.open(path, 2,
            StandardOpenOption.READ, StandardOpenOption.WRITE);
        if (array.size() != 2)
            throw new AssertionError();
        if (array.get(0) != 1)
            throw new AssertionError();
        if (array.get(1) != 2)
            throw new AssertionError();
        array.set(1, 4);
        while (true) {
            int value = array.get(0);
            if (value == 1)
                continue;
            else {
                if (value != 3)
                    throw new AssertionError();
                break;
            }
        }
        array.set(1, 6);
    }
}
