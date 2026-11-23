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
    private final Path includeDir;

    LinuxData(Path linuxTree) throws IOException {
        this.sourceTree = linuxTree;
        this.tempDir = TempDirectory.withPrefix(
            getClass().getPackage().getName() + '-');
        buildTree = tempDir.resolve("build");
        for (var arch : Architecture.values())
            installArch(arch);
        // Some kernel headers include <stdlib.h> and <time.h>.
        // Produce a fake version of <time.h> corresponding to time64 mode.
        includeDir = tempDir.resolve("include");
        Files.createDirectories(includeDir);
        Files.writeString(includeDir.resolve("time.h"), """
            #ifndef _TIME_H
            #define _TIME_H
            typedef long long time_t;
            struct timespec {
                time_t tv_sec;
                long long tv_nsec;
            };
            #endif
            """);
        // These UAPI headers also require <stddef.h>.
        Files.writeString(includeDir.resolve("stdlib.h"), """
            #ifndef _STDLIB_H
            #define _STDLIB_H
            #include <stddef.h>
            #endif
            """);
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
            "-I" + includeDir,
            "-I" + sysroot(arch).resolve("usr").resolve("include"));
    }

    Map<String, Macro> macros(Architecture arch, String fragment,
        Predicate<String> filter)
        throws IOException {
        var args = new ArrayList<String>();
        // Mask the inclusion of <stddef.h> by our <stdlib.h> because
        // -nosdinc makes in unavailable.
        args.addAll(List.of(arch.cc(), "-E", "-x", "c", "-nostdinc",
                            "-D_STDLIB_H"));
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
                    String expansion = components[2];
                    // Do not include function-like macros, macros that
                    // expand to strings, and filtered macros.
                    if (macroName.indexOf('(') >= 0
                        || expansion.startsWith("\"")
                        || !filter.test(macroName))
                        return Stream.of();
                    return Stream.of(new Macro(macroName, expansion));
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
                                  Collection<? extends String> macros)
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
