package enyo.io.tests;

import enyo.io.TempFile;
import enyo.testing.Checks;
import enyo.testing.Cost;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public final class TestTempFile {
    @Test
    public static void testOf(Testing t) throws IOException {
        Path path = Files.createTempFile("enyo-TestTempFile-", ".tmp");
        try (TempFile file = TempFile.of(path)) {
            t.examine(file.path()).is(path);
            t.examine(path).test(Files::exists);
        }
        t.examine(path).testNot(Files::exists);
    }

    @Test
    public static void testEquals(Testing t) throws IOException {
        try (TempFile file1 = TempFile.create("enyo-TestTempFile-", ".tmp");
            TempFile file2 = TempFile.of(file1.path())) {
            t.examine(file1).isnt(file2);
        }
    }

    @Test
    public static void testClose(Testing t) throws IOException {
        Path path;
        try (TempFile file = TempFile.create("enyo-TestTempFile-", ".tmp")) {
            path = file.path();
            t.examine(path).test(Files::exists);
        }
        t.examine(path).testNot(Files::exists);
    }

    @Test
    public static void testCloseRepeat(Testing t) throws IOException {
        // Verify that a repeated close() does not delete the file again.
        Path tempDir = Files.createTempDirectory("Enyo-TestTempFile-");
        try {
            TempFile file =
                TempFile.create(tempDir, "enyo-TestTempFile-", ".tmp");
            Path path = file.path();
            t.examine(path).test(Files::exists);
            file.close(); // First call.
            t.examine(path).testNot(Files::exists);
            Files.writeString(path, "");
            t.examine(path).test(Files::exists);
            file.close(); // Second call.
            t.examine(path).test(Files::exists);
            Files.delete(path);
        } finally {
            Files.delete(tempDir);
        }
    }

    @Test
    public static void testCloseNotFound(Testing t) throws IOException {
        // Deleting a non-existing temporary file should not raise an
        // exception.
        try (TempFile file = TempFile.create("enyo-TestTempFile-", ".tmp")) {
            Path path = file.path();
            Files.delete(path);
            t.examine(path).testNot(Files::exists);
        }
    }

    @Test
    public static void testCloseException(Testing t) throws IOException {
        Path tempDir = Files.createTempDirectory("Enyo-TestTempFile-");
        try {
            var perms = Files.getPosixFilePermissions(tempDir);
            boolean canTest = false;
            {
                Path file = tempDir.resolve("file");
                try {
                    Files.writeString(file, "");
                    Files.setPosixFilePermissions(tempDir, Set.of());
                    try {
                        Files.delete(file);
                        // If this deletion succeeds: We cannot perform the
                        // test because a DAC override is active (e.g.,
                        // running as root).
                    } catch (IOException e) {
                        // The NoSuchFileException is indistinguishable from
                        // the already-deleted file.
                        canTest = !(e instanceof NoSuchFileException);
                    }
                } finally {
                    Files.setPosixFilePermissions(tempDir, perms);
                    Files.delete(file);
                }
            }
            if (!canTest)
                return;

            TempFile file = TempFile.create(
                tempDir, "enyo-TestTempFile-", ".tmp");
            Path path = file.path();
            try {
                t.examine(path).test(Files::exists);
                Files.setPosixFilePermissions(tempDir, Set.of());
                t.examine(path).testNot(Files::exists);
                t.expect(() -> file.close(),
                    AccessDeniedException.class)
		    .map(Exception::getMessage).is(path.toString());
            } finally {
                Files.setPosixFilePermissions(tempDir, perms);
                t.examine(path).test(Files::exists);
                Files.delete(path);
            }
        } finally {
            Files.delete(tempDir);
        }
    }

    @Test
    public static void testPrefixSuffix(Testing t) throws IOException {
        for (int i = 0; i < 2; ++i) {
            String prefix = "Enyo-TestTempFile-" + i + "-";
            String suffix = "-" + i + ".tmp";
            try (TempFile file = TempFile.create(prefix, suffix)) {
                String fileName = file.path().getFileName().toString();
                t.examine(fileName)
                    .check(Checks.startsWith(prefix))
                    .check(Checks.endsWith(suffix));
            }
            Path tempDir = Files.createTempDirectory("Enyo-TestTempFile-");
            try {
                try (TempFile file = TempFile.create(
                        tempDir, prefix, suffix)) {
                    String fileName = file.path().getFileName().toString();
                    t.examine(fileName)
                        .check(Checks.startsWith(prefix))
                        .check(Checks.endsWith(suffix));
                }
            } finally {
                Files.delete(tempDir);
            }
        }
    }

    @Test
    @Cost(1.0)
    public static void testGarbageCollection(Testing t) throws Exception {
        var helper = new Object() {
                volatile TempFile file =
                    TempFile.create("enyo-TestTempFile-", ".tmp");
            };
        Path path = helper.file.path();
        t.examine(path).test(Files::exists);
        helper.file = null;
        // The TempFile object should be eligible for garbage
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
        t.examine(path).testNot(Files::exists);
    }
}
