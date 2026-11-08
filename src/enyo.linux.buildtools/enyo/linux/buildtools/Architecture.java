package enyo.linux.buildtools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Architectures supported by {@link enyo.linux}.
 *
 * The constants should be upper-case versions of the {@code os.arch}
 * system properties.  See {@code make/autoconf/platform.m4},
 * {@code VAR_CPU} and {@code OPENJDK_$1_CPU_OSARCH}
 * in the OpenJDK sources.
 */
enum Architecture {
    AARCH64("aarch64", null, "arm", "aarch64", List.of("-D__arch64__")),
    AMD64("x86_64", "64", "x86", "x86_64", List.of("-D__x86_64__")),
    LOONGARCH64("loongarch", "lp64", "loongarch", "loongarch64",
        List.of("-D__loongarch__", "-D__loongarch_grlen=64")),
    PPC64LE("powerpc", "powerpc64", "powerpc", "powerpc64le",
        List.of("-D__powerpc__", "-D__powerpc64__")),
    RISCV64("riscv", "rv64", "riscv", "riscv64",
        List.of("-D_riscv")),
    S390X("s390", "s390-64", "s390", "s390x",
        List.of("-D__s390__", "-D__s390x__")),
    ;

    String titleCase() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    private final String glibcBase;
    private final String glibcAlternate;


    // Architecture name used in configure triplets.
    final String gnuArch;

    // Architecture name used in the Linux source tree.
    final String linuxArch;

    private Architecture(String glibcBase, String glibcAlternate,
        String linuxArch, String gnuArch, List<String> cppFlags) {
        this.glibcBase = glibcBase;
        this.glibcAlternate = glibcAlternate;
        this.gnuArch = gnuArch;
        this.linuxArch = linuxArch;
    }

    /**
     * The C compiler command for this architecture.
     */
    String cc() {
        return gnuArch + "-linux-gnu-gcc";
    }

    /**
     * The glibc sysdeps directory for this architecture.
     */
    Path sysdeps(Path glibcTree) {
        var result = glibcTree
            .resolve("sysdeps")
            .resolve("unix")
            .resolve("sysv")
            .resolve("linux")
            .resolve(glibcBase);
        if (!Files.isDirectory(result))
            throw new IllegalArgumentException(
            "Architecture directory not found: " + result);
        return result;
    }

    /**
     * Obtains a file under glibc sysdeps.  Attempts to use the
       alternate subdirectory.
     */
    Path sysdepsFile(Path glibcTree, Path file) {
        Path sysdeps = sysdeps(glibcTree);
        Path candidate = sysdeps.resolve(file);
        if (Files.exists(candidate))
            return candidate;
        if (glibcAlternate != null)
            candidate = sysdeps.resolve(glibcAlternate).resolve(file);
        if (Files.exists(candidate))
            return candidate;
        throw new IllegalArgumentException(
            "Missing sysdeps file: " + candidate);
    }
}
