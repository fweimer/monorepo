package enyo.io.function.tests;

import enyo.testing.Testing;

import java.io.IOException;
import java.io.UncheckedIOException;

final class Helpers {
    @FunctionalInterface
    interface ExceptionConsumer {
        void doThrow(IOException e) throws IOException;
    }

    static void checkExceptionWrapping(Testing t, ExceptionConsumer callback) {
        IOException exception = new IOException();
        t.expect(
            () -> callback.doThrow(exception),
            UncheckedIOException.class,
            e -> t.examine(e.getCause()).isSame(exception));
    }

    private Helpers() { } // no instances
}
