package enyo.io.function.tests;

import enyo.io.function.IOConsumer;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public final class TestIOConsumer {
    @Test
    public void testExample() {
        List<Path> files = List.of();

        // Does not actually deleted anything due to the empty list.
        files.stream().forEach(IOConsumer.of(Files::delete));
    }

    @Test
    public void testException(Testing t) {
        Helpers.checkExceptionWrapping(t, e ->
            IOConsumer.of(e0 -> {
                    throw e;
                }).accept(null));
    }
}
