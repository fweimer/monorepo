package enyo.io.terminfo.tests;

import enyo.io.function.IOFunction;
import enyo.io.terminfo.ScriptAnalysis;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.TermFile;
import enyo.io.terminfo.TerminalScriptException;

import enyo.testing.Cost;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Check that string capabilities in terminfo files have compatible
 * parameter types.
 *
 * @see TestScriptVisitor#INTERSTING_CAPS
 */
public final class FindTypeConflicts {
    @Test
    @Cost(0.3)
    @Resources(memory=50)
    public void test(Testing t) throws IOException {
        List<Path> paths = paths();
        if (paths.isEmpty()) {
            t.unsupported("No terminfo files found");
            return;
        }

        run(paths, (cap, left, right) ->
            t.failedCheck("terminfo parameter type conflict",
                cap, left.path, left.analysis.parameterTypes(),
                right.path, right.analysis.parameterTypes()));
    }

    /**
     * Alternative main entry point, for running from the command
     * line.
     */
    public static void main(String[] args) throws IOException {
        List<Path> paths = paths(args);
        run(paths(args),
            (cap, left, right) -> System.out.format(
                "error: type conflict for %s: %s has %s, %s has %s%n",
                cap, left.path, left.analysis.parameterTypes(),
                right.path, right.analysis.parameterTypes()));
    }

    static List<Path> paths() throws IOException {
        List<Path> roots =
            List.of("/lib/terminfo", "/usr/share/terminfo")
            .stream()
            .map(Path::of)
            .filter(Files::isDirectory)
            .collect(Collectors.toList());

        List<Path> paths;
        try (Stream<Path> stream = roots.stream()
            .flatMap(IOFunction.of(Files::walk))) {
            paths = stream
                .filter(Files::isRegularFile)
                .unordered()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        }
        return paths;
    }

    interface ReportIncompatibility {
        void report(StringCapability cap, Parsed left, Parsed right);
    }

    private static void run(List<Path> files, ReportIncompatibility reporter)
            throws IOException {
        List<Parsed> analysis = files.stream()
            .flatMap(IOFunction.of(FindTypeConflicts::parseFile))
            .collect(Collectors.toList());

        Map<StringCapability, Set<Map<Integer, ScriptAnalysis.ParameterType>> >
            typeMap = analysis.stream()
            .collect(Collectors.groupingBy(parsed -> parsed.cap,
                    Collectors.collectingAndThen(Collectors.toList(),
                        list -> list.stream()
                        .map(parsed -> parsed.analysis.parameterTypes())
                        .collect(Collectors.toSet()))));
        for (var e : typeMap.entrySet()) {
            // First short cut: There must be more than one parameter
            // map.
            if (e.getValue().size() == 1)
                continue;

            // Second short cut: If the types across all parameter
            // type maps are all INTEGER or all STRING, there can be
            // no conflict.
            if (e.getValue()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()).size() < 2)
                continue;

            // Freeze an iteration ordering.
            List<Map<Integer, ScriptAnalysis.ParameterType>> list =
               e.getValue().stream().collect(Collectors.toList());

            // Perform pair-wise compatibility check.
            for (int i = 0; i < list.size(); ++i) {
                Map<Integer, ScriptAnalysis.ParameterType> left =
                    list.get(i);
                for (int j = i + 1; j < list.size(); ++j) {
                    Map<Integer, ScriptAnalysis.ParameterType> right =
                        list.get(j);
                    if (!compatibleTypes(left, right)) {
                        StringCapability cap = e.getKey();
                        Parsed leftParsed = findExample(analysis, cap, left);
                        Parsed rightParsed = findExample(analysis, cap, right);
                        reporter.report(e.getKey(), leftParsed, rightParsed);
                    }
                }
            }
        }
    }

    // Return a stream of the parsed string capabilities in the file.
    // Capabilities that cannot be analyzed are skipped.
    private static Stream<Parsed> parseFile(Path path) throws IOException {
        byte[] blob = Files.readAllBytes(path);
        TermFile term = TermFile.of(blob);

        return StringCapability.values().stream()
            .flatMap(cap -> {
                    byte[] capData = term.get(cap);
                    if (capData != null)
                        try {
                            return Stream.of(new Parsed(path, cap, capData));
                        } catch (TerminalScriptException e) {
                            // Ignore parse failures, in case of a
                            // capability that does not actually
                            // contain a script.
                        }
                    return Stream.of();
                });
    }

    // Returns true if all parameters that are present in both maps
    // have the same types in both.
    private static boolean compatibleTypes(
        Map<Integer, ScriptAnalysis.ParameterType> left,
        Map<Integer, ScriptAnalysis.ParameterType> right) {
        for (int i = 1; i <= 9; ++i) {
            ScriptAnalysis.ParameterType l = left.get(i);
            ScriptAnalysis.ParameterType r = right.get(i);
            if (l != null && r != null && l != r)
                return false;
        }
        return true;
    }

    static final class Parsed {
        final Path path;
        final StringCapability cap;
        final byte[] blob;
        final ScriptAnalysis analysis;

        Parsed(Path path, StringCapability cap, byte[] blob) {
            this.path = path;
            this.cap = cap;
            this.blob = blob;
            try {
                this.analysis = new ScriptAnalysis(blob);
            } catch (TerminalScriptException e) {
                throw new TerminalScriptException(path + ": " + cap, e);
            }
        }
    }

    private static List<Path> paths(String[] args) throws IOException {
        HashSet<Path> paths = new HashSet<>();
        for (String arg : args) {
            Path p = Path.of(arg);
            if (Files.isDirectory(p)) {
                try (Stream<Path> stream = Files.walk(p)) {
                    stream
                        .filter(Files::isRegularFile)
                        .forEach(paths::add);
                }
            } else
                paths.add(p);
        }
        return paths.stream()
            .sorted()
            .collect(Collectors.toList());
    }

    private static void reportIncompatibility(List<Parsed> analysis,
        StringCapability cap, Map<Integer, ScriptAnalysis.ParameterType> left,
        Map<Integer, ScriptAnalysis.ParameterType> right) {
        // Find the full information with path information.
        Parsed leftParsed = findExample(analysis, cap, left);
        Parsed rightParsed = findExample(analysis, cap, right);
    }

    private static Parsed findExample(List<Parsed> analysis,
        StringCapability cap, Map<Integer,
        ScriptAnalysis.ParameterType> types) {
        return analysis.stream()
            .filter(parsed -> parsed.cap.equals(cap)
                && parsed.analysis.parameterTypes().equals(types))
            .findFirst()
            .get();
    }
}
