package enyo.io.function.tests;

import enyo.io.function.IOBiConsumer;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public final class TestIOBiConsumer {
    @Test
    public void testExample() {
        Map<Path, Long> files = Map.of();

        // Does not actually change anything due to the empty map.
        files.forEach(IOBiConsumer.of((path, size) -> {
                try (var ch = FileChannel.open(
                        path, StandardOpenOption.WRITE)) {
                    ch.truncate(size);
                }
            }));
    }

    @Test
    public void testException(Testing t) {
        Helpers.checkExceptionWrapping(t, e ->
            IOBiConsumer.of((e0, e1) -> {
                    throw e;
                }).accept(null, null));
    }
}
