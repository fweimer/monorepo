package enyo.linux.buildtools;

import enyo.io.function.IOFunction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record SyscallDescriptor(String name, int number) {
    static SyscallDescriptor parse(String line) {
        String NR = "__NR_";
        String[] components = line.strip().split(" ");
        if (line.startsWith("#define ")
            && components.length == 3
            && components[1].startsWith(NR))
            return new SyscallDescriptor(
                components[1].substring(NR.length()),
                Integer.parseInt(components[2]));
        throw new IllegalArgumentException("Malformed line: " + line.strip());
    }

    static Map<String, SyscallDescriptor> fromFile(Path path)
        throws IOException {
        try {
            return Files.readAllLines(path).stream()
                .filter(line -> !line.startsWith("/*"))
                .map(SyscallDescriptor::parse)
                .collect(Collectors.toUnmodifiableMap(
                        SyscallDescriptor::name,
                        d -> d));
        } catch (Exception e) {
            throw new IOException("Reading: " + path, e);
        }
    }

    static Map<String, SyscallDescriptor> fromFile(
        Path glibcTree, Architecture arch) throws IOException {
        return SyscallDescriptor.fromFile(arch.sysdepsFile(
                glibcTree, Path.of("arch-syscall.h")));
    }

    static Map<Architecture, Map<String, SyscallDescriptor>> allSyscalls(
        Path glibcTree) {
        return Arrays.stream(Architecture.values())
            .collect(Collectors.toUnmodifiableMap(
                    arch -> arch,
                    IOFunction.of(arch ->
                        SyscallDescriptor.fromFile(arch.sysdepsFile(
                                glibcTree, Path.of("arch-syscall.h"))))));
    }

    /**
     * Returns a list of system calls defined on all supported architectures.
     */
    static List<String> commonSyscalls(
        Map<Architecture, Map<String, SyscallDescriptor>> syscalls) {
        return syscalls.get(Architecture.values()[0])
            .keySet()
            .stream()
            .filter(syscall ->
                syscalls.values()
                .stream()
                .allMatch(map -> map.containsKey(syscall)))
            .sorted()
            .toList();
    }
}
