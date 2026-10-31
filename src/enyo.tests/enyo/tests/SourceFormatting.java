package enyo.tests;

import enyo.io.function.IOConsumer;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public final class SourceFormatting {
    @Test
    public void testWhitespace(Testing t) throws Exception {
        var errors = new AtomicInteger();
        Files.walk(Path.of("src"))
            .forEach(IOConsumer.of(path ->  {
            // Only check Java source files.
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                || !path.getFileName().toString().endsWith(".java"))
                return;
            try (var rd = Files.newBufferedReader(path)) {
                boolean wasEmpty = false;
                int lineno = 0;
                while (true) {
                    String line = rd.readLine();
                    if (line == null)
                        break;
                    ++lineno;
                    if (line.isEmpty()) {
                        wasEmpty = true;
                        continue;
                    }
                    String noTrailing = line.stripTrailing();
                    if (noTrailing.isEmpty()) {
                        errors.incrementAndGet();
                        err(path, lineno, "line consists only of whitespace");
                        wasEmpty = true;
                        continue;
                    }
                    wasEmpty = false;
                    if (noTrailing.length() != line.length()) {
                        errors.incrementAndGet();
                        err(path, lineno, "line has trailing whitespace");
                    }
                    if (noTrailing.indexOf('\t') >= 0) {
                        errors.incrementAndGet();
                        err(path, lineno, "line contains tab characters");
                    }
                }
                if (wasEmpty) {
                    errors.incrementAndGet();
                    err(path, lineno - 1, "file has trailing empty lines");
                }
            }
        }));
        t.examine(errors.get()).is(0);
    }

    private static void err(Path path, int lineno, String message) {
        System.err.format("%s:%d: %s%n", path, lineno, message);
    }
}
