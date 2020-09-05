package enyo.io.terminfo.tests;

import enyo.io.TempDirectory;
import enyo.io.terminfo.KeyCapability;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestStringCapability {
    @Test
    public static void testIndex(Testing t) {
        // Check that the capabilities are sorted in increasing order
        // of their indices.
        t.examine(StringCapability.values())
            .check(Checks.strictlyMonotonic(Comparator.comparing(
                        StringCapability::index)));

        // Test that all indices are less than the total number of
        // capabilities.
        int count = 394;
        StringCapability.values().stream()
            .forEach(cap ->
                t.examine(cap)
                .mapToInt(StringCapability::index)
                .check(Checks.inRange(0, count)));

        // Check that there are no gaps.
        HashSet<Integer> missing = IntStream.range(0, count)
            .boxed()
            .collect(Collectors.toCollection(HashSet::new));
        StringCapability.values().stream()
            .map(StringCapability::index)
            .forEach(missing::remove);
        Arrays.stream(KeyCapability.values())
            .map(KeyCapability::index)
            .forEach(missing::remove);
        for (int i : missing)
            t.failedCheck("missing string capability", i);
    }

    @Test
    public static void testOf(Testing t) {
        for (StringCapability cap : StringCapability.values()) {
            t.examine(cap)
                .map(StringCapability::toString)
                .is(cap.name())
                .map(StringCapability::of)
                .is(cap);
        }
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
            StringCapability.values().stream()
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
        testAgainstTic(t, StringCapability.values(), false);
    }

    @Test
    public static void testAgainstTicC(Testing t)
        throws IOException, InterruptedException {
        // Use C identifiers in the terminfo source file.
        testAgainstTic(t, StringCapability.values(), true);
    }

    static <E extends Enum<E>> void testAgainstTic(Testing t,
        List<? extends StringCapability> caps, boolean cMode)
        throws IOException, InterruptedException {
        try (TempDirectory dir = TempDirectory.withPrefix("terminfo-")) {
            // Create a terminfo source file with all string
            // capabilities, mapping to a unique capability string.
            Path src = dir.resolve("terminfo.src");
            try (BufferedWriter wr = Files.newBufferedWriter(src)) {
                wr.write("test|terminal test,");
                wr.newLine();
                int index = 0;
                for (StringCapability cap : caps) {
                    wr.write('\t');
                    if (cMode)
                        wr.write(cap.name().toLowerCase());
                    else
                        wr.write(cap.terminfoName());
                    wr.write('=');
                    wr.write("c" + index);
                    wr.write(',');
                    wr.newLine();
                    ++index;
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
            int index = 0;
            for (StringCapability cap : caps) {
                t.examine(cap)
                    .map(term::get)
                    .isNotNull()
                    .barrier()
                    .map(String::new)
                    .is("c" + index);
                t.fullBarrier();
                ++index;
            }
        }
    }
}
