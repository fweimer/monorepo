package enyo.io.function.tests;

import enyo.io.TempDirectory;
import enyo.io.function.IORunnable;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestIORunnable {
    @Test
    public void testExample(Testing t) throws IOException {
        try (TempDirectory dir =
            TempDirectory.withPrefix("IORunnable-")) {
            Cleaner cleaner;
            Path path;

            cleaner = Cleaner.create();
            path = dir.resolve("file");
            Files.write(path, new byte[0]);

            Cleaner.Cleanable cleanable = cleaner.register(
                path, IORunnable.of(() -> Files.deleteIfExists(path)));
            t.assertTrue(Files.exists(path));
            cleanable.clean();
            t.assertFalse(Files.exists(path));
        }
    }

    @Test
    public void testException(Testing t) {
        Helpers.checkExceptionWrapping(t, e ->
            IORunnable.of(() -> {
                    throw e;
                }).run());
        // Due to the lack of type parameters, a cast works as well.
        Helpers.checkExceptionWrapping(t, e ->
            ((IORunnable) () -> {
                throw e;
            }).run());
    }
}
