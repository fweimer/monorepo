package enyo.linux.buildtools;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CExtract {
    private CExtract() {}

    // Extract macro constants using a cross-compiler.
    // Uses the fragment to provide definitions.
    static Map<String, Long> macroValues(Architecture arch,
        List<? extends String> cflags, String fragment,
        Collection<? extends String> macros) throws IOException {

        var outPath = Files.createTempFile("enyo-linux-cc-", ".c");
        try {
            try (var out = new PrintWriter(Files.newBufferedWriter(outPath))) {
                out.println(fragment);
                asmExtraction(out, macros);
            }
            return runExtraction(arch, cflags, outPath, macros);
        } finally {
            Files.delete(outPath);
        }
    }

    static void asmExtraction(PrintWriter out,
        Collection<? extends String> macros)
        throws IOException {
        // Pseudo-assembler directives that force constant evaluation.
        out.write("void global_function(void) {\n");
        for (var macro : macros) {
            out.format("""
  __asm__ volatile (".asciiz \\"@@@EXPORT@@@ @%s@ @%%0@\\""
                    :: "i" ((long) (%s))); /* %s */
""",
                        macro, macro, macro);
                }
                out.write("}\n");
    }


    static Map<String, Long> runExtraction(Architecture arch,
        List<? extends String> cflags, Path outPath,
        Collection<? extends String> macros) throws IOException {
        var macroNames = macros.stream()
            .collect(Collectors.toUnmodifiableSet());

        var result = new HashMap<String, Long>();
        var args = new ArrayList<String>();
        args.add(arch.cc());
        args.addAll(cflags);
        args.add("-S");
        args.add("-o-");
        args.add(outPath.toString());
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

                    // Extract the result of the compiler evaluation.
                    pos = next + 1;
                    if (!" @".equals(line.substring(pos, pos + 2)))
                        throw new IOException(
                            "Malformed: " + line.substring(pos));
                    pos += 2;
                    // Skip over $ etc. in front of immediate operands.
                    while (!(Character.isDigit(line.charAt(pos))
                            || line.charAt(pos) == '-'))
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
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        } catch (Throwable e) {
            throw new IOException("Running: " + args, e);
        }

        return Map.copyOf(result);
    }
}
