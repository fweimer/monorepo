package enyo.lang.tests;

import enyo.lang.RelaunchVM;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TestRelaunchVM {
    @Test
    public static void test(Testing t)
        throws IOException, InterruptedException {
        ProcessBuilder builder;
        if (TestRelaunchVM.class.getModule().isNamed())
            builder = RelaunchVM.processBuilder(
                List.of("-m",
                    TestRelaunchVM.class.getModule().getName()
                    + '/' + TestRelaunchVM.class.getName(),
                    "zero", "one", "two"));
        else
            builder = RelaunchVM.processBuilder(
                List.of(TestRelaunchVM.class.getName(),
                    "zero", "one", "two"));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        byte[] output;
        int status;
        try {
            output = process.getInputStream().readAllBytes();
            status = process.waitFor();
        } finally {
            process.destroy();
        }
        // Our output can be hidden among log messages.
        t.examine(new String(output))
            .test(s ->
                s.indexOf("Hello, world!\n0: zero\n1: one\n2: two\n") >= 0);
        t.examine(status).is(17);
    }

    @Test
    public static void testMainClassArguments(Testing t) {
        List<String> expected;
        if (TestRelaunchVM.class.getModule().isNamed())
            expected = List.of(
                "--module", "enyo.tests/enyo.lang.tests.TestRelaunchVM");
        else
            expected = List.of("enyo.lang.tests.TestRelaunchVM");
        t.examine(RelaunchVM.mainClassArguments(TestRelaunchVM.class))
            .is(expected);
    }

    @Test
    public static void testMainClass(Testing t)
        throws IOException, InterruptedException {
        ProcessBuilder builder = RelaunchVM.processBuilder(
            TestRelaunchVM.class, List.of("zero", "one", "two"));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        byte[] output;
        int status;
        try {
            output = process.getInputStream().readAllBytes();
            status = process.waitFor();
        } finally {
            process.destroy();
        }
        // Our output can be hidden among log messages.
        t.examine(new String(output))
            .test(s ->
                s.indexOf("Hello, world!\n0: zero\n1: one\n2: two\n") >= 0);
        t.examine(status).is(17);
    }

    @Test
    public static void testMainClassWithVMArguments(Testing t)
        throws IOException, InterruptedException {
        String setting = TestRelaunchVM.class.getName() + "=testKeyValue";
        ProcessBuilder builder = RelaunchVM.processBuilder(
            List.of("-D" + setting),
            TestRelaunchVM.class, List.of("zero", "one", "two"));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        byte[] output;
        int status;
        try {
            output = process.getInputStream().readAllBytes();
            status = process.waitFor();
        } finally {
            process.destroy();
        }
        // Our output can be hidden among log messages.
        t.examine(new String(output))
            .test(s -> s.indexOf("Hello, world!\n"
                    + setting + '\n'
                    + "0: zero\n1: one\n2: two\n") >= 0);
        t.examine(status).is(17);
    }

    /**
     * Parse the output of {@code java -help} and {@code java -X} and
     * verify that options with arguments are listed.
     */
    @Test
    public static void testOptionsWithArgument(Testing t)
        throws IOException, InterruptedException {
        HashSet<String> knownOptions = new HashSet<>(
            RelaunchVM.optionsWithArgument());
        knownOptions.addAll(RelaunchVM.optionsToDropWithArgument());

        String launcher = RelaunchVM.currentLauncher().toString();
        List<String> outputHelp = lines(t, List.of(launcher, "-help"));
        List<String> outputX = lines(t, List.of(launcher, "-X"));

        Pattern reOption = Pattern.compile(
            "(?:^\\s*|.*\\[options\\]\\s+)(-[^ <]+)\\s+<.*");
        boolean matches = false;
        for (List<String> list : List.of(outputHelp, outputX))
            for (String line : list) {
                Matcher m = reOption.matcher(line);
                if (m.matches()) {
                    matches = true;
                    t.examine(m.group(1))
                        .test(option -> knownOptions.contains(option));
                }
            }
        t.assertTrue(matches);
    }

    @Test
    public static void testDefaultLauncher(Testing t)
        throws IOException, InterruptedException {
        String defaultVersion = getVersion(t, RelaunchVM.defaultLauncher());
        String currentVersion = getVersion(t,
            RelaunchVM.currentLauncher().toString(), "-Xlog:disable");
        t.examine(currentVersion).is(defaultVersion);
    }

    @Test
    public static void testOnlyVMArgumentsJAR(Testing t) throws Throwable {
        t.examine(RelaunchVM.onlyVMArguments(
                List.of("java", "-Xlog:gc",
                    "-jar", "build/bootstrap.jar"),
                true))
            .is(List.of());
        t.examine(RelaunchVM.onlyVMArguments(
                List.of("java", "-Xlog:gc",
                    "-jar", "build/bootstrap.jar"),
                false))
            .is(List.of("-Xlog:gc"));
    }

    @Test
    public static void testOnlyVMArgumentsModules(Testing t) throws Throwable {
        for (boolean minimum : List.of(true, false))
            t.examine(RelaunchVM.onlyVMArguments(
                    List.of("java", "--module-path", "build/bootstrap",
                        "-m", "enyo/enyo.lang.BuildDaemon"),
                    minimum))
                .is(List.of("--module-path", "build/bootstrap"));
    }

    /**
     * Called by the test class to check that new VMs can be launched.
     */
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        {
            String key = TestRelaunchVM.class.getName();
            String value = System.getProperty(key);
            if (value != null)
                System.out.format("%s=%s%n", key, value);
        }
        for (int i = 0; i < args.length; ++i)
            System.out.format("%d: %s%n", i, args[i]);
        System.exit(17);
    }

    private static Set<String> getPrivateStringSet(String name) {
        try {
            var lookup = MethodHandles.privateLookupIn(RelaunchVM.class,
                MethodHandles.lookup());
            var getter = lookup.findStaticGetter(
                RelaunchVM.class, name, Set.class);
            @SuppressWarnings("unchecked")
            Set<String> result = (Set<String>) getter.invoke();
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> lines(Testing t, List<String> command)
        throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try {
            process.getOutputStream().close();
            List<String> result = new BufferedReader(
                new InputStreamReader(process.getInputStream()))
                .lines()
                .collect(Collectors.toUnmodifiableList());
            t.examine(process.waitFor()).is(0);
            return result;
        } finally {
            process.destroy();
        }
    }

    private static String getVersion(Testing t, String... command)
        throws IOException, InterruptedException {
        ArrayList<String> allArguments = new ArrayList<>(List.of(command));
        allArguments.add("-version");
        ProcessBuilder builder = new ProcessBuilder(allArguments);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try {
            process.getOutputStream().close();
            output = new String(process.getInputStream().readAllBytes());
            t.examine(process.waitFor())
                .context(output)
                .is(0);
        } finally {
            process.destroy();
        }
        return output;
    }
}
