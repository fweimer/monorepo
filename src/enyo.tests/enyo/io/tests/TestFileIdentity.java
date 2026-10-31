package enyo.io.tests;

import enyo.io.FileIdentity;
import enyo.io.TempDirectory;
import enyo.io.function.IOConsumer;
import enyo.testing.Cost;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class TestFileIdentity {
    @Test
    public void testOfSymlink(Testing t) throws IOException {
        try (var dir = TempDirectory.withPrefix("TestFileIdentity-")) {
            Path link = dir.resolve("link");
            try {
                Files.createSymbolicLink(link, Path.of("target"));
            } catch (UnsupportedOperationException e) {
                // Cannot test due to missing symbolic link support.
                return;
            }

            var attrs = Files.readAttributes(link, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
            t.assertTrue(attrs.isSymbolicLink());
            t.assertFalse(FileIdentity.hasFileIdentity(attrs));

            t.expect(() -> FileIdentity.of(attrs),
                IllegalArgumentException.class)
		.map(Exception::getMessage).is("Symbolic link: " + attrs);
            t.expect(() -> FileIdentity.of(link),
                IllegalArgumentException.class)
                .map(Exception::getMessage).is("Symbolic link: " + link);
        }
    }

    @Test
    public void testSize(Testing t) throws IOException {
        try (var dir = TempDirectory.withPrefix("TestFileIdentity-")) {
            Path file = dir.resolve("file");

            Files.write(file, new byte[0]);
            var initial = FileIdentity.of(file);
            t.examine(initial.size()).is(0);
            t.examine(initial).is(FileIdentity.of(file));

            Files.write(file, new byte[1]);
            var size1 = FileIdentity.of(file);
            t.examine(size1.size()).is(1);
            t.examine(size1).isnt(initial);
            t.examine(size1.fileKey()).is(initial.fileKey());
        }
    }

    @Test
    public void testIsDirectory(Testing t) throws IOException {
        try (var dir = TempDirectory.withPrefix("TestFileIdentity-")) {
            Path file = dir.resolve("file");
            Files.write(file, new byte[0]);

            t.assertTrue(FileIdentity.of(dir.path()).isDirectory());
            t.assertFalse(FileIdentity.of(file).isDirectory());
        }
    }

    @Test
    public void testToString(Testing t) {
        List.of(false, true).stream().forEach(directory ->
            t.examine(FileIdentity.of(new Attributes(directory, 12345,
                        FileTime.from(1578343813, TimeUnit.SECONDS), 6789))
                .toString())
            .is("6789/2020-01-06T20:50:13Z/12345"));
    }

    @Test
    @Cost(2)
    public void testLastModified(Testing t)
        throws IOException, InterruptedException {
        try (var dir = TempDirectory.withPrefix("TestFileIdentity-")) {
            Path file = dir.resolve("file");

            Files.write(file, new byte[0]);
            var initial = FileIdentity.of(file);
            t.examine(initial.size()).is(0);
            t.examine(initial).is(FileIdentity.of(file));

            // Check that a change in the file modification time
            // changes the file identity.  Assume at least 2 second
            // resolution in the file system.
            long start = System.currentTimeMillis();
            do {
                Files.write(file, new byte[0]);
                var id = FileIdentity.of(file);
                t.examine(id.size()).is(0);
                t.examine(id.fileKey()).is(initial.fileKey());
                if (!id.lastModifiedTime().equals(
                        initial.lastModifiedTime())) {
                    t.examine(id).isnt(initial);
                    return;
                }
                Thread.sleep(100);
            } while (System.currentTimeMillis() < start + 3000);
            throw new AssertionError(
                "File modification not changed within 3 seconds");
        }
    }

    @Test
    public void testOrder(Testing t) {
        var order = new Order<BasicFileAttributes>();
        order.group()
            .add(new Attributes(false, 1, FileTime.fromMillis(1000), 0))
            .add(new Attributes(false, 1,
                    FileTime.from(1, TimeUnit.SECONDS), 0))
            .add(new Attributes(false, 1,
                    FileTime.from(1000 * 1000, TimeUnit.MICROSECONDS), 0))
            .add(new Attributes(false, 1,
                    FileTime.from((long) 1e9, TimeUnit.NANOSECONDS), 0))
            .close();
        order.group()
            .add(new Attributes(false, 1, FileTime.fromMillis(1000), 1))
            .add(new Attributes(false, 1,
                    FileTime.from(1, TimeUnit.SECONDS), 1))
            .add(new Attributes(false, 1,
                    FileTime.from(1000 * 1000, TimeUnit.MICROSECONDS), 1))
            .add(new Attributes(false, 1,
                    FileTime.from((long) 1e9, TimeUnit.NANOSECONDS), 1))
            .close();
        order.group()
            .add(new Attributes(false, 2, FileTime.fromMillis(1000), 1))
            .add(new Attributes(false, 2,
                    FileTime.from(1, TimeUnit.SECONDS), 1))
            .add(new Attributes(false, 2,
                    FileTime.from(1000 * 1000, TimeUnit.MICROSECONDS), 1))
            .add(new Attributes(false, 2,
                    FileTime.from((long) 1e9, TimeUnit.NANOSECONDS), 1))
            .close();
        order.group()
            .add(new Attributes(false, null, FileTime.fromMillis(1000), 1))
            .add(new Attributes(false, null,
                    FileTime.from(1, TimeUnit.SECONDS), 1))
            .add(new Attributes(false, null,
                    FileTime.from(1000 * 1000, TimeUnit.MICROSECONDS), 1))
            .add(new Attributes(false, null,
                    FileTime.from((long) 1e9, TimeUnit.NANOSECONDS), 1))
            .close();
        order.add(new Attributes(true, 2, FileTime.fromMillis(1000), 0));
        order.add(new Attributes(false, 3, FileTime.fromMillis(1000), 1));
        order.add(new Attributes(false, 2, FileTime.fromMillis(1001), 1));
        order.add(new Attributes(false, 2, FileTime.fromMillis(1001), 2));

        var list1 = List.of(2);
        var list2 = List.of(2, 2).subList(0, 1);
        var list3 = List.of(2);
        t.examine(list1).is(list2);
        t.<Class<?>>examine(list1.getClass()).isnt(list2.getClass());
        t.examine(list1).isNotSame(list3);
        order.group()
            .add(new Attributes(false, list1, FileTime.fromMillis(1001), 2))
            .add(new Attributes(false, list2, FileTime.fromMillis(1001), 2))
            .add(new Attributes(false, list3, FileTime.fromMillis(1001), 2))
            .close();

        var order1 = order.map(FileIdentity::of);

        FileIdentity cwd;
        try {
            cwd = FileIdentity.of(Path.of("."));
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        order1.group()
            .add(cwd)
            .add(FileIdentity.of(
                    new Attributes(true, cwd.fileKey(), cwd.lastModifiedTime(),
                        cwd.size())))
            .close();
        order1.add(FileIdentity.of(
                new Attributes(false, cwd.fileKey(), cwd.lastModifiedTime(),
                    cwd.size())));
        order1.add(FileIdentity.of(
                new Attributes(true, cwd.fileKey(), cwd.lastModifiedTime(),
                    cwd.size() + 1)));

        order1.check();
    }

    @Test
    public void testOfOther(Testing t) throws IOException {
        List.of("/dev/null", "COM1:", "NUL").stream()
            .map(Path::of)
            .forEach(IOConsumer.of(path -> {
                        BasicFileAttributes attrs;
                        try {
                            attrs = Files.readAttributes(path,
                                BasicFileAttributes.class,
                                LinkOption.NOFOLLOW_LINKS);
                        } catch (NoSuchFileException e) {
                            return;
                        }
                        if (!attrs.isOther())
                            return;

                        t.expect(() -> FileIdentity.of(attrs),
                            IllegalArgumentException.class)
			    .map(Exception::getMessage)
                            .is("File without identity: " + attrs);
                        t.expect(() -> FileIdentity.of(path),
                            IllegalArgumentException.class)
			    .map(Exception::getMessage)
                            .is("File without identity: " + path);
                    }));
    }

    @Test
    public void testOfPathMissing(Testing t) throws IOException {
        try (var dir = TempDirectory.withPrefix("TestFileIdentity-")) {
            for (Path path : List.of(
                    dir.resolve("file"),
                    dir.resolve("subdirectory").resolve("file")))
                t.expect(() -> FileIdentity.of(path),
                    NoSuchFileException.class)
		    .map(Exception::getMessage).is(path.toString());
        }
    }

    private final class Attributes implements BasicFileAttributes {
        private final boolean directory;
        private final Object fileKey;
        private final FileTime lastModifiedTime;
        private final long size;

        Attributes(boolean directory, Object fileKey,
            FileTime lastModifiedTime, long size) {
            this.directory = directory;
            this.fileKey = fileKey;
            this.lastModifiedTime = lastModifiedTime;
            this.size = size;
        }

        @Override
        public final FileTime creationTime() {
            throw new AssertionError();
        }

        @Override
        public final Object fileKey() {
            return fileKey;
        }

        @Override
        public final boolean isDirectory() {
            return directory;
        }

        @Override
        public final boolean isOther() {
            throw new AssertionError();
        }

        @Override
        public final boolean isRegularFile() {
            return !directory;
        }

        @Override
        public final boolean isSymbolicLink() {
            throw new AssertionError();
        }

        @Override
        public final FileTime lastAccessTime() {
            throw new AssertionError();
        }

        @Override
        public final FileTime lastModifiedTime(){
            return lastModifiedTime;
        }

        @Override
        public final long size() {
            return size;
        }
    }

    // FIXME: remove
    private static <T extends Serializable> T passThroughSerialization(T obj)
        throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(obj);
        }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(
                    bytes.toByteArray()))) {
            try {
                @SuppressWarnings("unchecked")
                    T result = (T) in.readObject();
                return result;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
    }
}
