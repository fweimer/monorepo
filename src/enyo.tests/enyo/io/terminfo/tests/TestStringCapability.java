package enyo.io.terminfo.tests;

import enyo.io.TempDirectory;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.TermFile;
import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TestStringCapability {
    @Test
    public static void testIndex(Testing t) {
        for (StringCapability cap : StringCapability.values())
            t.examine(cap)
                .mapToInt(StringCapability::index)
                .is(cap.ordinal());
    }

    @Test
    public static void testTerminfoCollision(Testing t) {
        Map<String, List<StringCapability>> byTerminfo =
            Arrays.stream(StringCapability.values())
            .collect(Collectors.groupingBy(StringCapability::terminfoName,
                    Collectors.toList()));
        byTerminfo.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .forEach(e -> {
                    ArrayList<Object> info = new ArrayList<>();
                    info.add(e.getKey());
                    info.addAll(e.getValue());
                    t.failedCheck("terminfo name collision", info.toArray());
                });
    }

    @Test
    public static void testAgainstTic(Testing t)
        throws IOException, InterruptedException {
        // Use terminfo identifiers in the terminfo source file.
        testAgainstTic(t, false);
    }

    @Test
    public static void testAgainstTicC(Testing t)
        throws IOException, InterruptedException {
        // Use C identifiers in the terminfo source file.
        testAgainstTic(t, true);
    }

    private static void testAgainstTic(Testing t, boolean cMode)
        throws IOException, InterruptedException {
        try (TempDirectory dir = TempDirectory.withPrefix("terminfo-")) {
            // Create a terminfo source file with all string
            // capabilities, mapping to a unique capability string.
            Path src = dir.resolve("terminfo.src");
            try (BufferedWriter wr = Files.newBufferedWriter(src)) {
                wr.write("test|terminal test,");
                wr.newLine();
                for (StringCapability cap : StringCapability.values()) {
                    wr.write('\t');
                    if (cMode)
                        wr.write(cap.name().toLowerCase());
                    else
                        wr.write(cap.terminfoName());
                    wr.write('=');
                    // Use a numeric capability for testing because
                    // the full capability names would exceed the
                    // terminal file size limit.
                    wr.write("c" + cap.ordinal());
                    wr.write(',');
                    wr.newLine();
                }
            }

            // Compile the terminfo file.
            {
                Process proc;
                try {
                    proc = new ProcessBuilder(
                        "tic", "-o", dir.toString(),
                        src.toString())
                        .inheritIO()
                        .start();
                } catch (IOException e) {
                    t.unsupported("cannot launch tic: " + e.getMessage());
                    return;
                }
                int status = proc.waitFor();
                if (status != 0) {
                    t.failedCheck("tic failed to compile source file", status);
                    return;
                }
            }

            // This can be used to dump the generated terminal file.
            if (false)
                new ProcessBuilder("infocmp", "-A", dir.toString(),
                    "test")
                    .inheritIO()
                    .start()
                    .waitFor();

            // Check that the string capabilities have their expected
            // values.
            TermFile term = TermFile.of(
                Files.readAllBytes(dir.resolve("t").resolve("test")));
            for (StringCapability cap : StringCapability.values()) {
                t.examine(cap)
                    .map(term::get)
                    .isNotNull()
                    .barrier()
                    .map(String::new)
                    .is("c" + cap.ordinal());
                t.fullBarrier();
            }
        }
    }
}
