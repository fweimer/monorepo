package enyo.io.terminfo.tests;

import enyo.io.function.IOFunction;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.UntypedStringCapability;
import enyo.io.terminfo.TermFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Comparator;
import java.io.IOException;

/**
 * Helper class to find capabilities which are completely unused.
 */
public final class FindMissing {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            System.err.format("usage: %s%n", FindMissing.class.getName());
            System.exit(1);
        }

        HashSet<StringCapability> missing =
            new HashSet<>(StringCapability.values());

        for (Path path : FindTypeConflicts.paths()) {
            byte[] blob = Files.readAllBytes(path);
            TermFile term = TermFile.of(blob);
            // Avoiding changing the missing set during
            // the iteration.
            HashSet<StringCapability> present = new HashSet<>();
            for (StringCapability cap : missing)
                if (term.get(cap) != null)
                    present.add(cap);
            missing.removeAll(present);
        }

        missing.stream()
            .sorted(Comparator.comparing(StringCapability::parameters)
                .thenComparing(StringCapability::index))
            .forEach(cap ->
                System.out.format("%s %s, parameter count %d%n",
                    cap.toString(), cap.terminfoName(), cap.parameters()));
    }

    private FindMissing() { } // No instances.
}
