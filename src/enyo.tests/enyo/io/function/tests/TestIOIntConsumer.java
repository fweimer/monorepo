package enyo.io.function.tests;

import enyo.io.TempDirectory;
import enyo.io.function.IOIntConsumer;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

public final class TestIOIntConsumer {
    @Test
    public void testExample(Testing t) throws IOException {
        try (TempDirectory dir =
            TempDirectory.withPrefix("IOIntConsumer-")) {
            Path path;

            path = dir.path();

            IntStream.range(0, 10)
                .forEach(IOIntConsumer.of(i ->
                        Files.write(path.resolve(Integer.toString(i)),
                            new byte[0])));

            for (int i = 0; i < 10; ++i)
                t.assertTrue(Files.exists(path.resolve(Integer.toString(i))));
        }
    }

    @Test
    public void testException(Testing t) {
        Helpers.checkExceptionWrapping(t, e ->
            IOIntConsumer.of(value -> {
                    throw e;
                }).accept(0));
    }
}
