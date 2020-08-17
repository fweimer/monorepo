package enyo.build;

import enyo.io.FileIdentity;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class BuildNative {
    static boolean build(DirectoryLayout dirs,
        Map<Path, FileIdentity> sources)
        throws IOException, InterruptedException {
        long nanoStart = System.nanoTime();

        Map<String, List<Map.Entry<Path, FileIdentity>>>
            nativeSourcesByModule = sources.entrySet().stream()
            .filter(e -> nativeSource(e.getKey()))
            .collect(Collectors.groupingBy(
                    e -> {
                        Path path = e.getKey();
                        if (!path.startsWith(dirs.sources()))
                            throw new IllegalStateException(
                                "Unrecognizable source path: " + path);
                        int sourcePathLength = dirs.sources().getNameCount();
                        Path moduleComponent = path.subpath(
                            sourcePathLength, sourcePathLength + 1);
                        return moduleComponent.toString();
                    }));

        if (nativeSourcesByModule.isEmpty())
            // No native components.
            return true;

        Files.createDirectory(dirs.libraryGeneration());

        boolean result;
        try {
            result = nativeSourcesByModule.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .allMatch(e -> {
                        String moduleName = e.getKey();
                        List<Path> sourcePaths = e.getValue().stream()
                            .map(Map.Entry::getKey)
                            .sorted()
                            .collect(Collectors.toList());
                        int status;
                        try {
                            status = compileModuleDSO(
                                dirs, moduleName, sourcePaths);
                        } catch (IOException e1) {
                            throw new UncheckedIOException(e1);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                        return status == 0;
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        if (Thread.interrupted())
            throw new InterruptedException();

        if (result) {
            Path latestSymlink = dirs.build().resolve("latest-native");
            Files.deleteIfExists(latestSymlink);
            Files.createSymbolicLink(latestSymlink,
                dirs.build().relativize(dirs.libraryGeneration()));

            long nanoEnd = System.nanoTime();
            System.err.format(
                "info: native build completed successfully (%.3f s)%n",
                (nanoEnd - nanoStart) * 1e-9);
        }

        return result;
    }

    /**
     * Returns the DSO name for a module.
     * @return a file name
     */
    private static String moduleDSO(String moduleName) {
        return "libjni-" + moduleName + ".so";
    }

    private static int compileModuleDSO(DirectoryLayout dirs,
        String moduleName, List<Path> sourcePaths)
        throws IOException, InterruptedException {
        Path targetDSO = dirs.libraryGeneration()
            .resolve(moduleDSO(moduleName));
        Path includePath = dirs.headerGeneration().resolve(moduleName);

        String[] cppCompiler = System.getenv().getOrDefault("CXX", "g++")
            .split("\\s+");
        String[] cppFlags = System.getenv().getOrDefault("CXXFLAGS",
            "-O2 -g -Wall -W -Wmissing-declarations")
            .split("\\s+");
        String[] ldFlags = System.getenv().getOrDefault("LDFLAGS", "")
            .split("\\s");

        ArrayList<String> command = new ArrayList<>();
        command.addAll(List.of(cppCompiler));
        command.addAll(List.of(cppFlags));
        command.add("-fPIC");
        if (ldFlags.length > 0 && !ldFlags[0].isEmpty())
            command.addAll(List.of(ldFlags));
        command.add("-shared");
        command.add("-I" + includePath.toString());
        command.addAll(jniIncludeFlags());
        command.add("-o");
        command.add(targetDSO.toString());
        sourcePaths.stream()
            .map(Path::toString)
            .forEach(command::add);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.inheritIO();
        int status;
        Process process = builder.start();
        try {
            status = process.waitFor();
        } finally {
            process.destroy();
        }
        return status;
    }

    private static List<String> jniIncludeFlags() {
        String home = System.getProperty("java.home");
        String os = System.getProperty("os.name");
        return List.of(
            String.format("-I%s/include", home),
            String.format("-I%s/include/%s", home, os.toLowerCase()));
    }

    static boolean nativeSource(Path path) {
        return nativeSource(path.getFileName().toString());
    }

    static boolean nativeSource(String fileName) {
        return fileName.endsWith(".cpp")
            || fileName.endsWith(".cc")
            || fileName.endsWith(".c");
    }

    private BuildNative() { } // No instances.
}
