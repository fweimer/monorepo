package enyo.io.function.tests;

import enyo.io.function.IOSupplier;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class TestIOSupplier {
    @Test
    public void testExample(Testing t) {
        Path path = null;
        Optional<InputStream> optionalStream =
            Optional.of(InputStream.nullInputStream());

        // Does not actually open anything because there is a default.
        InputStream in = optionalStream.orElseGet(
            IOSupplier.of(() -> Files.newInputStream(path)));
        t.assertSame(optionalStream.get(), in);
    }

    @Test
    public void testException(Testing t) {
        Helpers.checkExceptionWrapping(t, e ->
            IOSupplier.of(() -> {
                    throw e;
                }).get());
    }
}
