package enyo.lang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Restarting this VM with the same parameters as a separate process.
 *
 * <p><b>Note:</b> All VM parameters are preserved (except for launch
 * control parameters such as {@code -jar}).  If parameters are
 * present which cause files to be overwritten, the launched VM will
 * overwrite them as well.
 */
public final class RelaunchVM {
    /**
     * Creates a process builder for a VM launch with the specified arguments.
     *
     * @param arguments the new arguments: VM options, class name
     *   (if not implied by the VM options), application arguments (if any)
     * @return a new process builder
     */
    public static ProcessBuilder processBuilder(List<String> arguments) {
        ArrayList<String> combined = new ArrayList<String>();
        combined.add(currentLauncher().toString());
        combined.addAll(currentVMArguments());
        combined.addAll(additionalVMArguments());
        combined.addAll(arguments);
        return new ProcessBuilder(combined);
    }

    /**
     * Creates a process builder for a VM launch of the specified class.
     *
     * @param mainClass the class to use as a main class
     *   (must be loadable with the current VM options)
     * @param arguments the applications to pass (after the main class name)
     * @return a new process builder
     */
    public static ProcessBuilder processBuilder(
        Class<?> mainClass, List<String> arguments) {
        ArrayList<String> combined = new ArrayList<String>();
        combined.add(currentLauncher().toString());
        combined.addAll(currentVMArguments());
        combined.addAll(additionalVMArguments());
        combined.addAll(mainClassArguments(mainClass));
        combined.addAll(arguments);
        return new ProcessBuilder(combined);
    }

    /**
     * Creates a process builder for a VM launch.
     *
     * @param vmArguments arguments for controlling VM behavior
     *   (passed before the main class name)
     * @param mainClass the class to use as a main class
     *   (must be loadable with the current VM options)
     * @param arguments the applications to pass (after the main class name)
     * @return a new process builder
     */
    public static ProcessBuilder processBuilder(List<String> vmArguments,
        Class<?> mainClass, List<String> arguments) {
        ArrayList<String> combined = new ArrayList<String>();
        combined.add(currentLauncher().toString());
        combined.addAll(currentVMArguments());
        combined.addAll(additionalVMArguments());
        combined.addAll(vmArguments);
        combined.addAll(mainClassArguments(mainClass));
        combined.addAll(arguments);
        return new ProcessBuilder(combined);
    }

    /**
     * Returns the arguments for identifying the main class.
     * The command line syntax depends on whether the main class is in
     * a named module or not.
     * @param mainClass the main class to configure for launching
     * @return an immutable list of strings
     */
    public static List<String> mainClassArguments(Class<?> mainClass) {
        String moduleName = mainClass.getModule().getName();
        String className = mainClass.getName();
        if (moduleName == null)
            return List.of(className);
        else
            return List.of( "--module", moduleName + '/' + className);
    }

    /**
     * Returns the VM-specific arguments for this VM.
     *
     * @return the VM-specific arguments, without the launcher program,
     *   options identifying the main class, or applications arguments
     * @throws UncheckedIOException if it is not possible to determine
     *   required information from the file system
     */
    public static List<String> currentVMArguments() {
        return onlyVMArguments(currentCommandLine(), true);
    }

    /**
     * Returns the command line for the VM invocation.
     *
     * @return a list containing the entire command line: the launcher
     *   program, VM-specific options, the main-class, application arguments
     *   etc.
     * @throws UncheckedIOException if the required information cannot be
     *   obtained from the file system
     */
    public static List<String> currentCommandLine() {
        ArrayList<String> result = new ArrayList<String>();
        byte[] command;
        try {
            command = Files.readAllBytes(Path.of("/proc/self/cmdline"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        int start = 0;
        for (int i = 0; i < command.length; ++i) {
            if (command[i] == 0) {
                result.add(new String(command, start, i - start));
                start = i + 1;
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns the path of the current launcher.
     * This is the main program that runs this VM.
     *
     * @return the path of the VM launcher (or main program)
     * @throws UncheckedIOException if the required information cannot be
     *   obtained from the file system
     * @see #defaultLauncher()
     */
    public static Path currentLauncher() {
        try {
            return Files.readSymbolicLink(new File("/proc/self/exe").toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Return the path to the default launcher program for this installation.
     *
     * This is not necessarily the same as the currently active
     * launcher.
     *
     * @return a file system path, as a string
     * @see #currentLauncher()
     */
    public static String defaultLauncher() {
        String command = "java";
        String home = System.getProperty("java.home");
        if (home == null)
            return command;
        else
            return home + File.separator + "bin" + File.separator + command;
    }

    /**
     * Extracts the VM arguments from a full command line.
     *
     * @param commandLine a list containing the launcher program, VM
     *   arguments, ways to identify the main class, and application
     *   arguments
     * @param minimum if true, only include the arguments which configure
     *   the class path and the module path
     * @return an immutable list of strings
     */
    public static List<String> onlyVMArguments(List<String> commandLine,
                                                boolean minimum) {
        ArrayList<String> result = new ArrayList<String>();

        // 0: scanning, 1: drop option, 2: add option argument.
        int state = 0;

        // Skip the first argument (which is usually not a full path,
        // so not particular useful).  See currentLauncher() instead.
        OUTER: for (int i = 1; i < commandLine.size(); ++i) {
            String arg = commandLine.get(i);
            switch (state) {
            case 0:
                if (arg.startsWith("-")) {
                    if (OPTIONS_WITH_ARGUMENT.contains(arg)) {
                        if (minimum && !MINIMUM_OPTIONS.contains(arg)) {
                            // Drop this argument.
                            state = 1;
                            continue OUTER;
                        } else
                            state = 2;
                    }
                    else if(OPTIONS_TO_DROP_WITH_ARGUMENT.contains(arg)) {
                        state = 1;
                        continue OUTER;
                    }
                } else
                    break OUTER;
                if (!minimum || MINIMUM_OPTIONS.contains(arg))
                    result.add(arg);
                break;
            case 1:
                state = 0;
                break;
            case 2:
                state = 0;
                result.add(arg);
                break;
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns the VM options which take an argument on the command line.
     * @return an immutable set of strings
     */
    public static Set<String> optionsWithArgument() {
        return OPTIONS_WITH_ARGUMENT;
    }

    /**
     * Returns the options with arguments which need to be dropped.
     * Otherwise, the requested class cannot be launched.
     * @return an immutable set strings
     */
    public static Set<String> optionsToDropWithArgument() {
        return OPTIONS_TO_DROP_WITH_ARGUMENT;
    }

    private static final Set<String> MINIMUM_OPTIONS =
        Set.of(
            "-cp", "-classpath", "--class-path",
            "-p", "--module-path",
            "--upgrade-module-path",
            "--add-modules",
            "--enable-native-access",
            // -X-level options:
            "--add-reads",
            "--add-exports",
            "--add-opens",
            "--limit-modules",
            "--patch-module",
            "--source");


    /**
     * Options which have a parameter after them. Both option and
     * argument need to be copied.
     */
    private static final Set<String> OPTIONS_WITH_ARGUMENT;
    static {
        ArrayList<String> set = new ArrayList<>(MINIMUM_OPTIONS);
        set.add("-d");
        set.add("--describe-module");
        set.add("--enable-final-field-mutation");
        OPTIONS_WITH_ARGUMENT = Set.copyOf(set);
    }

    /**
     * Options which need to be dropped during a relaunch, so that a
     * different main class can be invoked.
     */
    private static final Set<String> OPTIONS_TO_DROP_WITH_ARGUMENT =
        Set.of("-jar", "-m", "--module");

    private static final String MODULE_PATH_PROPERTY =
        RelaunchVM.class.getName() + ".modulePath";

    private RelaunchVM() {
        // No instances.
    }

    /**
     * Returns additional VM arguments that are necessary to re-launch
     * the VM in special environments.  These arguments may depend
     * on system properties.
     */
    private static List<String> additionalVMArguments() {
        String modulePath = System.getProperty(MODULE_PATH_PROPERTY);
        if (modulePath != null)
            return List.of("--module-path", modulePath);
        else
            return List.of();
    }
}
