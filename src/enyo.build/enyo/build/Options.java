package enyo.build;

import enyo.lang.SystemExit;
import enyo.util.argparse.ArgParser;
import enyo.util.argparse.Help;
import enyo.util.argparse.MetaVar;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * Command line options for the main build program.
 *
 * @see Build#main(String[])
 */
record Options(
    @Help("perform only one compile-test cycle")
    boolean once,
    @Help("run tests in the same VM as the compilation")
    boolean testSameVm,
    @Help("text file with additional arguments for the test VM")
    @MetaVar("PATH")
    String testVmArgsFile) {

    static Options of(String[] args) {
        return SystemExit.call(() -> parser.run(Main.class, args));
    }

    private static final ArgParser<Options> parser =
        new ArgParser<>(lookup(), Options.class);
}
