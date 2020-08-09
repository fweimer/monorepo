package enyo.io.tests;

import enyo.io.TempDirectory;
import enyo.testing.Cost;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class TestTempDirectory {
    @Test
    public static void testEmpty(Testing t) throws IOException {
        Path path;
        try (TempDirectory dir = TempDirectory.withPrefix(
                "test-tempdir-")) {
            path = dir.path();
            t.assertTrue(Files.exists(path));
            t.examine(path.toString()).is(dir.toString());
        }
        t.assertFalse(Files.exists(path));
    }

    @Test
    public static void testOneFile(Testing t) throws IOException {
        Path path;
        try (TempDirectory dir = TempDirectory.withPrefix(
                "test-tempdir-")) {
            path = dir.path();
            t.assertTrue(Files.exists(path));
            Path file = path.resolve("file");
            Files.write(file, "data".getBytes());
        }
        t.assertFalse(Files.exists(path));
    }

    private static void testHierarchy(Testing t, boolean withThrow)
        throws IOException {
        Path path = null;

        @SuppressWarnings("serial")
        class Marker extends Exception {
        }
        Marker thrown = null;

        try {
            try (TempDirectory dir = TempDirectory.withPrefix(
                    "test-tempdir-")) {
                path = dir.path();
                t.assertTrue(Files.exists(path));

                String[] names = {
                    "file",
                    "empty/.",
                    "dir1/dir2/dir3/file",
                    "dir4/dir5/file1",
                    "dir4/dir5/file2",
                    "dir4/dir6/file1",
                    "dir4/dir6/file2",
                    "dir7/dir8/empty/."
                };
                FileSystem fs = path.getFileSystem();
                for (String name : names) {
                    Path p = path.resolve(fs.getPath(name));
                    Path parent = p.getParent();
                    if (parent != null)
                        Files.createDirectories(parent);
                    if (!p.getFileName().toString().equals("."))
                        Files.write(p, name.getBytes());
                }
                if (withThrow) {
                    thrown = new Marker();
                    throw thrown;
                }
            }
            if (withThrow)
                t.assertFail("Marker exception not thrown");
        } catch (Marker m) {
            t.examine(thrown).isSame(m);
        }
        t.assertFalse(Files.exists(path));
    }

    @Test
    public static void testHierarchy(Testing t) throws IOException {
        testHierarchy(t, false);
    }

    @Test
    public static void testHierarchyException(Testing t) throws IOException {
        testHierarchy(t, true);
    }

    @Test
    @Cost(1.0)
    public static void testGarbageCollection(Testing t) throws Exception {
        var helper = new Object() {
                volatile TempDirectory dir =
                    TempDirectory.withPrefix("enyo-TestTempDirectory-");
            };
        Path path = helper.dir.path();
        t.assertTrue(Files.isDirectory(path));
        helper.dir = null;
        // The TempDirectory object should be eligible for garbage
        // collection after this point.  Wait for at most 60 seconds.
        long start = System.currentTimeMillis();
        for (int i = 1; i <= 600; ++i) {
            if (!Files.exists(path))
                break;
            // Periodically trigger garbage collection.
            if ((i % 10) == 0)
                System.gc();
            Thread.sleep(100);
        }
        t.assertFalse(path.toString(), Files.exists(path));
    }
}
