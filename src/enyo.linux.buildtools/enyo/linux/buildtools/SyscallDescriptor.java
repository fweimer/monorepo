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
    private final static String PREFIX = "__NR_";

    static SyscallDescriptor of(Map.Entry<String, Long> macro) {
        if (!macro.getKey().startsWith(PREFIX))
            throw new IllegalArgumentException(macro.toString());
        String name = macro.getKey().substring(PREFIX.length());
        return new SyscallDescriptor(name, Math.toIntExact(macro.getValue()));
    }

    static Map<Architecture, Map<String, SyscallDescriptor>> allSyscalls(
        LinuxData linuxData) {
        return Arrays.stream(Architecture.values())
            .collect(Collectors.toUnmodifiableMap(
                    arch -> arch,
                    IOFunction.of(arch -> {
                            String fragment = "#include <asm/syscall.h>";
                            var macros = linuxData.macros(
                                arch, fragment,
                                name -> name.startsWith(PREFIX));
                            var macroValues = linuxData.macroValues(
                                arch, fragment, macros.values());
                            return macroValues.entrySet().stream()
                                .map(SyscallDescriptor::of)
                                .collect(Collectors.toUnmodifiableMap(
                                        SyscallDescriptor::name,
                                        e -> e));
                        })));
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
