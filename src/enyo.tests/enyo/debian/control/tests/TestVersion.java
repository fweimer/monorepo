package enyo.debian.control.tests;

import enyo.debian.control.Version;
import enyo.io.TempFile;
import enyo.testing.Cost;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TestVersion {
    @Test
    public static void testInvalid(Testing t) {
        Consumer<String> check = version -> {
            t.expect(version, () -> Version.of(version),
                IllegalArgumentException.class);
        };
        check.accept("");
        check.accept("1:");
        check.accept("1a:");
        check.accept(":1");
        check.accept("-1");
        check.accept("1-");
        check.accept("1:-1");
        check.accept("1:1-");
        check.accept("1-0:");
    }

    /**
     * Read /var/lib/dpkg/status if it exists, ad use the version data
     * in it for consistency checks.
     */
    @Test
    @Cost(1.5)
    public static void testStatus(Testing t)
        throws IOException, InterruptedException {
        Path statusPath = Path.of("/var/lib/dpkg/status");
        if (!Files.isReadable(statusPath)) {
            t.unsupported(statusPath + " is not readable");
            return;
        }

        String versionPrefix = "Version:";
        HashSet<String> set = new HashSet<>();
        Files.lines(statusPath).forEach(line -> {
                if (line.startsWith(versionPrefix))
                    set.add(line.substring(versionPrefix.length()).trim());
            });

        ArrayList<Version> list = set.stream()
            .map(Version::of)
            .sorted()
            .collect(Collectors.toCollection(ArrayList::new));
        if (list.size() <= 1)
            // Not enough input data for more useful tests.
            return;

        boolean useDpkg;
        if (List.of(0, -1).equals(pythonAptCheck(
                    List.of("1.1", "1.01", "1.2").stream()
                    .map(Version::of)
                    .collect(Collectors.toList())))) {
            List<Integer> fromPython = pythonAptCheck(list);
            t.assertEquals(fromPython.size(), list.size() - 1);
            int i = 0;
            Version last = null;
            for (Version ver : list) {
                if (last != null) {
                    int actual = Integer.signum(last.compareTo(ver));
                    int expected = fromPython.get(i);
                    if (actual != expected)
                        t.assertFail(String.format(
                                "%s <=> %s: expected %d, actual %d",
                                last, ver, actual, expected));
                    ++i;
                }
                last = ver;
            }
            useDpkg = false;
        } else {
            t.unsupported("apt_pkg not available for python3 command");
            useDpkg  = dpkgSupported();
            if (!useDpkg)
                t.unsupported("dpkg command not supported");
        }

        // To some degree, the test below verifies the implementation
        // against itself, so it is of limited value.  But these
        // checks still verify that all values can be compared, and
        // that the comparison is transitive and generally consistent.
        Order<Version> order = new Order<>();
        {
            Version last = null;
            Order<Version>.Group group = null;
            for (Version ver : list) {
                if (ver.equals(last)) {
                    group.add(ver);
                    if (useDpkg && !dpkgCheck(
                            ver.toString(), "=", last.toString()))
                        t.assertFail(String.format(
                                "dpkg says versions are not equal: %s, %s",
                                ver, last));
                } else {
                    if (last != null) {
                        if (useDpkg && !dpkgCheck(
                                last.toString(), "<", ver.toString()))
                            t.assertFail(String.format(
                                    "dpkg says versions are not <: %s, %s",
                                    last, ver));
                    }
                    if (group != null)
                        group.close();
                    group = order.group();
                    group.add(ver);
                    last = ver;
                }
            }
            if (group != null)
                group.close();
        }
        order.checkComparable(Version.class);
    }

    private static boolean dpkgSupported()
        throws InterruptedException {
        Process process;
        try {
            process = new ProcessBuilder("dpkg", "--help").start();
        } catch (IOException e) {
            return false;
        }
        try {
            return process.waitFor() == 0;
        } finally {
            process.destroy();
        }
    }

    private static boolean dpkgCheck(String left, String op, String right)
        throws IOException, InterruptedException {
        Process process = new ProcessBuilder("dpkg", "--compare-versions",
            left, op, right)
            .start();
        try {
            return process.waitFor() == 0;
        } finally {
            process.destroy();
        }
    }

    /**
     * Signums of the pairwise comparisons using apt_pkg.compare_version.
     * Returns an empty list in case the system lacks the required
     * functionality.
     */
    private static List<Integer> pythonAptCheck(List<Version> list)
        throws IOException, InterruptedException {
        String tmpPrefix = TestVersion.class.getName() + '-';
        try (TempFile tmpScript = TempFile.create(tmpPrefix, ".py");
            TempFile tmpList = TempFile.create(tmpPrefix, ".py")) {
            try (BufferedWriter out =
                Files.newBufferedWriter(tmpScript.path())) {
                for (String line : List.of(
                        "import apt_pkg",
                        "import sys",
                        "apt_pkg.init()",
                        "last = None",
                        "with open(sys.argv[1]) as inp:",
                        "  for line in inp:",
                        "    line = line.strip()",
                        "    if last is not None:",
                        "      print(apt_pkg.version_compare(last, line))",
                        "    last = line")) {
                    out.write(line);
                    out.newLine();
                }
            }

            try (BufferedWriter out = Files.newBufferedWriter(tmpList.path())) {
                for (Version ver : list) {
                    out.write(ver.toString());
                    out.newLine();
                }
            }

            Process process;
            try {
                process = new ProcessBuilder(
                    "python3", tmpScript.toString(), tmpList.toString())
                    .start();
            } catch (IOException e) {
                return List.of();
            }
            try {
                List<Integer> result;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    result = reader.lines()
                        .map(line -> Integer.signum(Integer.parseInt(line)))
                        .collect(Collectors.toList());
                }

                if (process.waitFor() != 0)
                    return List.of();
                else
                    return result;
            } finally {
                process.destroy();
            }
        }
    }

    @Test
    public static void testOrder() {
        Order<String> order = new Order<>();
        order.add("~");
        order.add("0.3");
        order.add("0.3-1");
        order.group()
            .add("1")
            .add("01")
            .add("001")
            .add("0:1")
            .add("0:01")
            .add("0:001")
            .add("00:1")
            .add("00:01")
            .add("00:001")
            .close();
        order.add("1.0~rc0-2");
        order.add("1.0~rc1-1");
        order.group()
            .add("1.0")
            .add("1.")
            .add("1.00")
            .add("1.0-0")
            .close();
        order.add("1.0-1~rc0");
        order.add("1.0-1~rc1");
        order.group()
            .add("1.0-1")
            .add("1.0-01")
            .add("0:1.0-1")
            .close();
        order.group()
            .add("1.0-1x")
            .add("1.0-1x0")
            .close();
        order.add("2~~");
        order.add("2~~a");
        order.add("2~");
        order.add("2a");
        order.add("3.0~git2");
        order.add("3.cvs2");
        order.add("99");
        order.add("100");
        order.add("101");
        order.add("A");
        order.add("a");
        order.map(Version::of).checkComparable(Version.class);
    }
}
