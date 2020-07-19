package enyo.debian.control.tests;

import enyo.debian.control.PackageName;
import enyo.io.TempFile;
import enyo.testing.Cost;
import enyo.testing.Order;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TestPackageName {
    @Test
    public static void testInvalid(Testing t) {
        Consumer<String> check = name -> {
            t.expect(name, () -> PackageName.of(name),
                IllegalArgumentException.class);
        };
        check.accept("");
        check.accept(" ");
        check.accept(" ab");
        check.accept("ab ");
        check.accept("ab:");
        check.accept(":ab");
        check.accept("a:b");
        check.accept("Ab");
        check.accept("+ab");
        check.accept("-ab");
        check.accept(".ab");
        check.accept("ab_");
        check.accept("ab_c");
    }

    @Test
    @Cost(0.2)
    public static void testStatus(Testing t) throws IOException {
        Path statusPath = Path.of("/var/lib/dpkg/status");
        if (!Files.isReadable(statusPath)) {
            t.unsupported(statusPath + " is not readable");
            return;
        }

        String packagePrefix = "Package:";
        HashSet<String> set = new HashSet<>();
        Files.lines(statusPath).forEach(line -> {
                if (line.startsWith(packagePrefix))
                    set.add(line.substring(packagePrefix.length()).trim());
            });

        // The package names should be all distinct.
        Order<PackageName> order = new Order<>();
        set.stream()
            .sorted()
            .map(PackageName::of)
            .forEach(order::add);
        order.checkComparable(PackageName.class);
    }

    @Test
    public static void testOrder()
    {
        Order<String> order = new Order<>();
        order.add("0ad");
        order.add("a+");
        order.add("a-");
        order.add("a.");
        order.add("ab");
        order.add("ac");
        order.add("aca");
        order.map(PackageName::of).checkComparable(PackageName.class);
    }
}
