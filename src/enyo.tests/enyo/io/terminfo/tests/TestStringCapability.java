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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TestStringCapability {
    @Test
    public static void testIndex(Testing t) {
        // Check that the capabilities are sorted in increasing order
        // of their indices.
        t.examine(StringCapability.values())
            .check(Checks.strictlyMonotonicArray(Comparator.comparing(
                        StringCapability::index)));
    }

    @Test
    public static void testKey(Testing t) {
        // Check that no capabilities start with KEY_.
        for (StringCapability cap : StringCapability.values())
            t.examine(cap)
                .map(StringCapability::toString)
                // KEY_MOUSE is not really a key, it's a prefix of the
                // escape sequence that starts a mouse position report.
                .test(s -> s.equals("KEY_MOUSE") || !s.startsWith("KEY_"));
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
        testAgainstTic(t, StringCapability.class,
            StringCapability::terminfoName, TermFile::get, false);
    }

    @Test
    public static void testAgainstTicC(Testing t)
        throws IOException, InterruptedException {
        // Use C identifiers in the terminfo source file.
        testAgainstTic(t, StringCapability.class,
            StringCapability::terminfoName, TermFile::get, true);
    }

    static <E extends Enum<E>> void testAgainstTic(Testing t,
        Class<E> cls, Function<E, String> terminfoName,
        BiFunction<TermFile, E, byte[]> getCapability,
        boolean cMode)
        throws IOException, InterruptedException {
        try (TempDirectory dir = TempDirectory.withPrefix("terminfo-")) {
            // Create a terminfo source file with all string
            // capabilities, mapping to a unique capability string.
            Path src = dir.resolve("terminfo.src");
            try (BufferedWriter wr = Files.newBufferedWriter(src)) {
                wr.write("test|terminal test,");
                wr.newLine();
                for (E cap : cls.getEnumConstants()) {
                    wr.write('\t');
                    if (cMode)
                        wr.write(cap.name().toLowerCase());
                    else
                        wr.write(terminfoName.apply(cap));
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
            for (E cap : cls.getEnumConstants()) {
                t.examine(cap)
                    .map(cap1 -> getCapability.apply(term, cap1))
                    .isNotNull()
                    .barrier()
                    .map(String::new)
                    .is("c" + cap.ordinal());
                t.fullBarrier();
            }
        }
    }
}
