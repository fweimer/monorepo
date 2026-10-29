package enyo.linux.buildtools;

import enyo.io.function.IOFunction;
import enyo.util.argparse.ArgParser;
import enyo.util.argparse.Help;
import enyo.util.argparse.MetaVar;
import enyo.util.argparse.Required;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * The tool for updating {@code enyo.linux} sources.
 */
public final class Main {
    private record Options(
        @Required @MetaVar("PATH")
        @Help("the glibc source checkout")
        String glibcTree,
        @Help("the Linux source checkout")
        String linuxTree,
        @Help("list system calls shared by all architectures")
        boolean listCommonSyscalls,
        @Help("list errno constants that do not exist on all architectures")
        boolean listUncommonErrno,
        @Help("update the syscalls files in the source tree")
        boolean updateSyscalls,
        @Help("update the macr classes")
        boolean updateMacroClasses
    ) {}

    @SuppressWarnings("doclint:missing")
    public static void main(String[] args) throws Exception {
        var options = new ArgParser<>(MethodHandles.lookup(), Options.class)
            .run(Main.class, args);
        var glibcTree = Path.of(options.glibcTree());
        var linuxTree =
            options.linuxTree() != null ? Path.of(options.linuxTree()) : null;
        if (options.listCommonSyscalls())
            SyscallDescriptor.commonSyscalls(
                SyscallDescriptor.allSyscalls(glibcTree))
                .stream()
                .forEach(System.out::println);
        if (options.listUncommonErrno())
            listUncommonMacros(glibcTree, linuxTree,
                "#include <linux/errno.h>",
                Pattern.compile("^E[A-Z0-9]"));
        if (options.updateSyscalls)
            updateSyscalls(glibcTree);
        if (options.updateMacroClasses()) {
            writeMacroClass(linuxTree, "Errno",
                "#include <linux/errno.h>",
                Pattern.compile("^E[A-Z0-9]"));
            writeMacroClass(linuxTree, "Fcntl",
                "#include <linux/fcntl.h>",
                Pattern.compile("^(__)?([OF]|AT)_[A-Z0-9]"));
            writeMacroClass(linuxTree, "Mman",
                "#include <linux/mman.h>",
                Pattern.compile("^(HUGETLB|MAP|PROT)_[A-Z0-9]"));

        }
    }

    private static void listUncommonMacros(Path glibcTree, Path linuxTree,
        String fragment, Pattern regexp) throws Exception {
        var linuxData = new LinuxData(linuxTree);
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

    private static void updateSyscalls(Path glibcTree) throws Exception {
        String targetPrefix = "src/enyo.linux/enyo/linux/SyscallConstants";
        var allSyscalls = SyscallDescriptor.allSyscalls(glibcTree);
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

    private static void writeMacroClass(Path linuxTree, String className,
        String fragment, Pattern pattern) throws Exception {
        var linuxData = new LinuxData(linuxTree);
        var cExtract = new CExtract();
        var allMacros = linuxData.allMacros(fragment, pattern.asPredicate())
            .entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    IOFunction.of(
                        e -> cExtract.macroValues(e.getValue().values()))));
        Updater.writeMacroClass(className, allMacros,
            pattern.asPredicate());
    }

    private Main() {}
}
