package enyo.linux.buildtools;

import enyo.io.function.IOFunction;
import enyo.util.argparse.ArgParser;
import enyo.util.argparse.Help;
import enyo.util.argparse.MetaVar;
import enyo.util.argparse.Required;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The tool for updating {@code enyo.linux} sources.
 */
public final class Main {
    private record Options(
        @Required @MetaVar("PATH")
        @Help("the Linux source checkout")
        String linuxTree,
        @Help("list system calls shared by all architectures")
        boolean listCommonSyscalls,
        @Help("list errno constants that do not exist on all architectures")
        boolean listUncommonErrno,
        @Help("update the syscalls files in the source tree")
        boolean updateSyscalls,
        @Help("update the macro classes")
        boolean updateMacroClasses
    ) {}

    @SuppressWarnings("doclint:missing")
    public static void main(String[] args) throws Exception {
        var options = new ArgParser<>(MethodHandles.lookup(), Options.class)
            .run(Main.class, args);
        checkCrossCompilers();
        var linuxTree =Path.of(options.linuxTree());

        try (var linuxData = new LinuxData(linuxTree)) {
            if (options.listCommonSyscalls())
                SyscallDescriptor.commonSyscalls(
                    SyscallDescriptor.allSyscalls(linuxData))
                    .stream()
                    .forEach(System.out::println);
            if (options.listUncommonErrno())
                listUncommonMacros(linuxData,
                    "#include <linux/errno.h>",
                    Pattern.compile("^E[A-Z0-9]"));
            if (options.updateSyscalls)
                updateSyscalls(linuxData);
            if (options.updateMacroClasses()) {
                writeMacroClass(linuxData, "Errno", true,
                    "#include <linux/errno.h>",
                    Pattern.compile("^E[A-Z0-9]"));
                writeMacroClass(linuxData, "Fcntl", false,
                    "#include <linux/fcntl.h>",
                    Pattern.compile("^(__)?([OF]|AT)_[A-Z0-9]"));
                writeMacroClass(linuxData, "Mman", false,
                    "#include <linux/mman.h>",
                    Pattern.compile("^(HUGETLB|MAP|PROT)_[A-Z0-9]"));

            }
        }
    }

    private static void generateUapiHeaders(Path linuxTree) throws Exception {
        if (linuxTree == null) {
            System.err.println(
                "error: --update-uapi-headers requires --linux-tre");
            System.exit(1);
        }
        for (Architecture arch : Architecture.values()) {
            try (var proc = new ProcessBuilder(
                "make", "ARCH=" + arch.linuxArch, "uapi-asm-generic")
                .directory(linuxTree.toFile())
                .inheritIO()
                .start()) {
                int rc = proc.waitFor();
                if (rc != 0)
                    System.exit(rc);
            }
        }
    }

    private static void checkCrossCompilers() throws Exception {
        String path = System.getenv("PATH");
        if (path == null)
            throw new Exception("PATH is not set");
        String dirs[] = path.split(File.pathSeparator);
        for (var arch : Architecture.values()) {
            String command = arch.cc();
            for (String dir : dirs) {
                if (dir.isEmpty())
                    dir = ".";
                File f = new File(dir, command);
                if (f.isFile() && f.canExecute())
                    return;
            }
            throw new Exception(String.format(
                    "Cannot find program \"%s\" on PATH: %s", command, path));
        }
    }

    private static void listUncommonMacros(LinuxData linuxData,
        String fragment, Pattern regexp) throws Exception {
        var allMacros = linuxData.allMacros(fragment, regexp.asPredicate());
        allMacros.values().stream()
            .map(Map::keySet)
            .flatMap(Set::stream)
            .sorted()
            .distinct()
            .filter(regexp.asPredicate())
            .filter(macro -> !allMacros.values().stream()
                .allMatch(map -> map.containsKey(macro)))
            .forEach(macro -> {
                    System.out.print(macro);
                    System.out.print(':');
                    allMacros.entrySet().stream()
                        .filter(e -> e.getValue().containsKey(macro))
                        .map(Map.Entry::getKey)
                        .forEach(arch -> System.out.print(' ' + arch.name()));
                    System.out.println();
                });
    }

    private static void updateSyscalls(LinuxData linuxData) throws Exception {
        String targetPrefix = "src/enyo.linux/enyo/linux/SyscallConstants";
        var allSyscalls = SyscallDescriptor.allSyscalls(linuxData);
        List<String> commonSyscalls =
            SyscallDescriptor.commonSyscalls(allSyscalls);
        Updater.patchSegments(targetPrefix + ".java",
            Map.of(
                "syscallArray",
                Arrays.stream(Architecture.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .flatMap(arch -> List.of(
                    String.format("if (osArch.equals(\"%s\"))", arch),
                    String.format(
                        "    syscallArray = SyscallConstants%s.get();",
                        Character.toTitleCase(arch.charAt(0))
                        + arch.substring(1))).stream())
                .toList(),

                "initSyscallFields",
                commonSyscalls.stream()
                    .map(syscall ->
                        syscall + " = syscallArray[syscallIndex++];")
                    .toList(),

                "syscallFields",
                commonSyscalls.stream()
                    .map(syscall -> "final static int " + syscall + ';')
                    .toList()));

        String header = """
package enyo.linux;

final class SyscallConstants%s {
    static int[] get() {
        return new int[] {
""";

        String element = """
            %d, // %s
""";

        String footer =
"""
        };
    }
}
""";
        for (var arch : Architecture.values()) {
            var perArch = allSyscalls.get(arch);
            String contents =
                String.format(header, arch.titleCase())
                + commonSyscalls.stream()
                    .map(perArch::get)
                    .map(sd -> String.format(element, sd.number(), sd.name()))
                    .collect(Collectors.joining())
                + footer;
            Files.writeString(
                Path.of(targetPrefix + arch.titleCase() + ".java"),
                contents);
        }
    }

    private static void writeMacroClass(LinuxData linuxData, String className,
        boolean isPublic, String fragment, Pattern pattern) throws Exception {
        var allMacros = linuxData.allMacros(fragment, pattern.asPredicate())
            .entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    IOFunction.of(
                        e -> CExtract.macroValues(
                            e.getKey(),
                            linuxData.includes(e.getKey()),
                            fragment,
                            e.getValue().keySet()))));
        Updater.writeMacroClass(className, isPublic, allMacros,
            pattern.asPredicate());
    }

    private Main() {}
}
