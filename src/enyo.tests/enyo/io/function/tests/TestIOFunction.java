package enyo.io.function.tests;

import enyo.io.function.IOFunction;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class TestIOFunction {
    @Test
    public void testExample(Testing t) {
        List<Path> files = List.of(Path.of("bootstrap", "Bootstrap.java"));

        List<byte[]> fileContents = files.stream()
            .map(IOFunction.of(Files::readAllBytes))
            .collect(Collectors.toList());
        t.examine(fileContents.size()).is(1);
        t.assertTrue(fileContents.get(0).length > 0);
    }

    @Test
    public void testException(Testing t) {
        Helpers.checkExceptionWrapping(t, e ->
            IOFunction.of(e0 -> {
                    throw e;
                }).apply(null));
    }
}
