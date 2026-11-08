package enyo.linux.buildtools;

import enyo.io.TempDirectory;
import enyo.io.function.IOFunction;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LinuxData implements Closeable{
    private final Path sourceTree;
    private final TempDirectory tempDir;
    private final Path buildTree;

    LinuxData(Path linuxTree) throws IOException {
        this.sourceTree = linuxTree;
        this.tempDir = TempDirectory.withPrefix(
            getClass().getPackage().getName() + '-');
        buildTree = tempDir.resolve("build");
        for (var arch : Architecture.values())
            installArch(arch);
    }

    @Override
    public void close() throws IOException {
        tempDir.close();
    }

    Path sysroot(Architecture arch) {
        return tempDir.resolve("sysroot").resolve(arch.name());
    }

    private void installArch(Architecture arch) throws IOException {
        var errOut = tempDir.resolve("install.log");
        var args = List.of(
            "make",
            "-j" + Runtime.getRuntime().availableProcessors(),
            "O=" + buildTree,
            "ARCH=" + arch.linuxArch,
            "CROSS_COMPILE=" + arch.gnuArch + "-linux-gnu-",
            "INSTALL_HDR_PATH=" + sysroot(arch).resolve("usr"),
            "headers_install");
        try (var proc = new ProcessBuilder(args)
            .directory(sourceTree.toFile())
            .redirectOutput(errOut.toFile())
            .redirectError(errOut.toFile())
            .start()) {
            int rc = proc.waitFor();
            if (rc != 0) {
                System.err.write(Files.readAllBytes(errOut));
                System.err.format("error: command failed with status %d: %s",
                    rc, args);
                System.exit(rc);
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }

    record Macro(String name, String expansion) {}

    List<String> includes(Architecture arch) {
        return List.of(
            "-I" + sysroot(arch).resolve("usr").resolve("include"));
    }

    Map<String, Macro> macros(Architecture arch, String fragment,
        Predicate<String> filter)
        throws IOException {
        var args = new ArrayList<String>();
        args.addAll(List.of(arch.cc(), "-E", "-x", "c", "-nostdinc"));
        args.addAll(includes(arch));
        args.add("-dM");
        args.add("-");

        var outPath = Files.createTempFile("enyo-linux-cpp-", ".i");
        try {
            var pb = new ProcessBuilder(args)
                .redirectOutput(ProcessBuilder.Redirect.to(outPath.toFile()))
                .redirectError(ProcessBuilder.Redirect.INHERIT);
            try (var proc = pb.start()) {
                proc.outputWriter().write((fragment.strip() + '\n'));
                proc.outputWriter().close();

                int status = proc.waitFor();
                if (status != 0)
                    throw new IOException(String.format(
                        "Exit status: %d %s", status, args));
            }

            return Files.readAllLines(outPath).stream()
            .flatMap(line -> {
                try {
                    String[] components = line.split(" ", 3);
                    if (!components[0].equals("#define"))
                        throw new IllegalArgumentException();
                    String macroName = components[1];
                    // Do not include function-like macros and
                    // predefined macros not suppressed by -undef.
                    if (macroName.indexOf('(') >= 0
                        || macroName.equals("_STDC_PREDEF_H")
                        || macroName.startsWith("__STDC_")
                        || !filter.test(macroName))
                        return Stream.of();
                    return Stream.of(new Macro(components[1], components[2]));
                } catch (Exception e) {
                    throw new RuntimeException("Line: " + line, e);
                }
            })
            .collect(Collectors.toUnmodifiableMap(
                Macro::name, m -> m));
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        } finally {
            Files.delete(outPath);
        }
    }

    Map<String, Long> macroValues(Architecture arch, String fragment,
                                  Collection<? extends LinuxData.Macro> macros)
        throws IOException {
        return CExtract.macroValues(arch, includes(arch), fragment, macros);
    }

    Map<Architecture, Map<String, Macro>> allMacros(
        String fragment, Predicate<String> filter) throws Exception {
        return Arrays.stream(Architecture.values())
            .collect(Collectors.toUnmodifiableMap(
                    arch -> arch,
                    IOFunction.of(arch -> macros(
                            arch, fragment, filter))));
    }
}
