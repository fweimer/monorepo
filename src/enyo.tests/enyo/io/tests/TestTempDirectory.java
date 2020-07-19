package enyo.io.tests;

import enyo.io.TempDirectory;
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
            t.assertEquals(dir.toString(), path.toString());
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
            t.assertSame(m, thrown);
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
}
