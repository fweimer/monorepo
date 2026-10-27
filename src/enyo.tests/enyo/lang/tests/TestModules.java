package enyo.lang.tests;

import enyo.lang.Modules;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.lang.module.ModuleFinder;

public final class TestModules {
    public static void testNeedsNativeAccess(Testing t) {
        String[] paths = System.getProperty("jdk.module.path").split(":");
        var finder = ModuleFinder.of(
            Arrays.stream(paths)
            .map(Paths::get)
            .toList().toArray(Path[]::new));
        // Use enyo.sqlite as a module with known native access.
        t.examine(finder.find("enyo.sqlite"))
            .map(Optional::get)
            .map(Modules::needsNativeAccess)
            .is(true);
    }
}
