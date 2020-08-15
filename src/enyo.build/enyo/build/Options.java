package enyo.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.util.Optional;

/**
 * Command line options for the main build program.
 *
 * @see Build#main(String[])
 */
final class Options {
    Options(String[] args) throws Exception {
        // This should use a real command line parser, but the path to
        // that is complicated, so use a custom parser for now.

        boolean once0 = false;
        boolean testSameVm0 = false;
        Path testVmArgsFile0 = null;

        for (int index = 0; index < args.length; ++index) {
            String arg = args[index];
            if (!arg.startsWith("--"))
                throw new Exception("option expected", arg);

            String option = arg.substring(2);
            String value;
            {
                int equalSign = option.indexOf('=');
                if (equalSign >= 0) {
                    value = option.substring(equalSign + 1);
                    option = option.substring(0, equalSign);
                } else
                    value = null;
            }

            switch (option) {
            case "once":
                noValue(arg, value);
                once0 = true;
                break;
            case "daemon":
                noValue(arg, value);
                once0 = false;
                break;
            case "test-same-vm":
                testSameVm0 = true;
                break;
            case "test-other-vm":
                testSameVm0 = false;
                break;
            case "test-vm-args-file":
                needsValue(arg, value);
                try {
                    testVmArgsFile0 = Path.of(value);
                } catch (InvalidPathException e) {
                    throw new Exception("invalid path", arg);
                }
                if (!Files.isReadable(testVmArgsFile0))
                    throw new Exception("cannot read arguments file", arg);
                break;
            default:
                throw new Exception("unrecognized option", arg);
            }

        }

        once = once0;
        testSameVm = testSameVm0;
        testVmArgsFile = testVmArgsFile0;
    }

    boolean once() {
        return once;
    }

    boolean testSameVm() {
        return testSameVm;
    }

    Optional<Path> testVmArgsFile() {
        return Optional.ofNullable(testVmArgsFile);
    }

    private final boolean once;
    private final boolean testSameVm;
    private final Path testVmArgsFile;

    private static void noValue(String arg, String value) throws Exception {
        if (value != null)
            throw new Exception("option does not expect a value", arg);
    }

    private static void needsValue(String arg, String value) throws Exception {
        if (value == null)
            throw new Exception("option needs a value", arg);
    }

    /** (work around javaocd bug)
     */
    final static class Exception extends java.lang.Exception {
        private Exception(String error, String argument) {
            super(error + ": " + argument);
            this.error = error;
            this.argument = argument;
        }

        String error() {
            return error;
        }

        String argument() {
            return argument;
        }

        private final String error;
        private final String argument;

        private static final long serialVersionUID = 0xea2f711baa7299d8L;
    }
}
