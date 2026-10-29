package enyo.linux.buildtools;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record CExtract(String cc) {
    CExtract() {
        var cc = System.getenv("CC");
        if (cc == null)
            cc = "gcc";
        this(cc);
    }

    Map<String, Long> macroValues(
        Collection<? extends LinuxData.Macro> macros) throws IOException {
        var macroNames = macros.stream()
            .map(LinuxData.Macro::name)
            .collect(Collectors.toUnmodifiableSet());

        var outPath = Files.createTempFile("enyo-linux-cpp-", ".c");
        try {
            // Create the file which directs GCC to produce assembler
            // output with a specific pattern.
            try (var out = new PrintWriter(Files.newBufferedWriter(outPath))) {
                // The macros definitions.
                for (var macro : macros)
                    out.write("#define " + macro.name() + ' '
                              + macro.expansion() + '\n');

                // Pseudo-assembler directives that force constant evaluation.
                out.write("void global_function(void) {\n");
                for (var macro : macros) {
                    if (macro.expansion().strip().isEmpty())
                        continue;
                    out.format("""
  __asm__ volatile (".asciiz \\"@@@EXPORT@@@ @%s@ @%%0@\\""
                    :: "i" ((long) (%s))); /* %s */
""",
                        macro.name(), macro.expansion(), macro.name());
                }
                out.write("}\n");
            }

            var result = new HashMap<String, Long>();
            var args = List.of(cc, "-S", "-o-", outPath.toString());
            var pb = new ProcessBuilder(args)
                .redirectError(ProcessBuilder.Redirect.INHERIT);
            try (var proc = pb.start()) {
                proc.getOutputStream().close();

                final String pattern = "@@@EXPORT@@@ @";
                var reader = proc.inputReader();
                while (true) {
                    String line = reader.readLine();
                    if (line == null)
                        break;
                    int pos = line.indexOf(pattern);
                    if (pos < 0)
                        continue;
                    try {
                        pos += pattern.length();

                        // The macro name.
                        int next = line.indexOf('@', pos);
                        String name = line.substring(pos, next);
                        if (!macroNames.contains(name))
                            throw new IOException("Unexpected macro: " + name);

                        // The compiler-evaluated value.
                        pos = next + 1;
                        if (!" @".equals(line.substring(pos, pos + 2)))
                            throw new IOException(
                                "Malformed: " + line.substring(pos));
                        pos += 3;
                        // Skip over $ etc. in front of immediate operands.
                        while (!Character.isDigit(line.charAt(pos))
                               && line.charAt(pos) != '-')
                            ++pos;
                        next = line.indexOf('@', pos);
                        if (next < 0)
                            throw new IOException(
                                "Malformed: " + line.substring(pos));
                        long value = Long.decode(line.substring(pos, next));

                        // Store the result.
                        result.put(name, value);
                    } catch (Exception e) {
                        throw new IOException("Line: " + line, e);
                    }
                }
                int status = proc.waitFor();
                if (status != 0)
                    throw new IOException(String.format(
                        "Exit status: %d %s", status, args));
            }

            return Map.copyOf(result);
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        } finally {
            Files.delete(outPath);
        }
    }
}
