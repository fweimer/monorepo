package enyo.io.terminfo.tests;

import enyo.io.function.IOFunction;
import enyo.io.terminfo.ScriptAnalysis;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.UntypedStringCapability;
import enyo.io.terminfo.TermFile;

import static enyo.io.terminfo.tests.FindTypeConflicts.Parsed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;

/**
 * Helper class to find capabilities with a specific type.
 */
public final class FindTypes {
    public static void main(String[] args) throws IOException {
        if (args.length != 1 || (args.length > 0
                && (args[0].isEmpty()
                    || args[0].length() > 9))) {
            System.err.format("usage: %s TYPE-SPECIFIER%n",
                FindTypes.class.getName());
            System.exit(1);
        }

        String types = args[0];
        types.chars()
            .filter(ch -> "is".indexOf(ch) < 0)
            .findFirst()
            .ifPresent(ch -> {
                    System.err.format("error: invalid type specifier: %c%n",
                        ch);
                    System.exit(1);
                });

        // The capabilities that have the right number of parameters.
        List<StringCapability> relevant =
            Arrays.stream(UntypedStringCapability.values())
            .filter(cap -> cap.parameters() == types.length())
            .collect(Collectors.toList());

        Map<StringCapability, List<Parsed>> found =
            FindTypeConflicts.paths().stream()
            .flatMap(IOFunction.of(path -> {
                        byte[] blob = Files.readAllBytes(path);
                        TermFile term = TermFile.of(blob);

                        return relevant.stream()
                            .flatMap(cap -> parsed(types, path, term, cap));
                    }))
            .collect(Collectors.groupingBy(
                    parsed -> parsed.cap,
                    Collectors.toList()));
        found.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().index()))
            .forEach(e -> {
                    System.out.println(e.getKey());
                    Map<Map<Integer, ScriptAnalysis.ParameterType>,
                        List<Parsed>> grouped =
                        e.getValue().stream()
                        .collect(Collectors.groupingBy(
                                parsed -> parsed.analysis.parameterTypes(),
                                Collectors.toList()));
                    grouped.entrySet().stream().forEach(e2 -> {
                            System.out.format("  %s%n", e2.getKey());
                            e2.getValue().stream().forEach(p ->
                                System.out.format("    %s%n", p.path));
                        });
                });
    }

    private static Stream<Parsed> parsed(String types, Path path,
        TermFile term, StringCapability cap) {
        byte[] capData = term.get(cap);
        if (capData != null
            && !TestScriptAnalysis.brokenScript(capData)) {
            Parsed p = new Parsed(path, cap, capData);
            if (matches(types,
                    p.analysis.parameterTypes()))
                return Stream.of(p);
        }
        return Stream.of();
    }

    // Checks if the type specifier from the command line matches
    // the types computed by ScriptAnalysis.
    private static boolean matches(String expected,
        Map<Integer, ScriptAnalysis.ParameterType> actual) {
        for (int i = 0; i < expected.length(); ++i) {
            ScriptAnalysis.ParameterType type = actual.get(i + 1);
            if (type == null)
                continue;
            switch (expected.charAt(i)) {
            case 'i':
                if (type == ScriptAnalysis.ParameterType.INTEGER)
                    continue;
                break;
            case 's':
                if (type == ScriptAnalysis.ParameterType.STRING)
                    continue;
                break;
            }
            return false;
        }
        return true;
    }

    private FindTypes() { } // No instances.
}
