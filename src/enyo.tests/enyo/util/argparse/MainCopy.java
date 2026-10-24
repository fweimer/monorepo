package enyo.util.argparse.tests;

import enyo.util.argparse.ArgParser;
import enyo.util.argparse.Help;
import enyo.util.argparse.Name;
import enyo.util.argparse.Positional;
import enyo.util.argparse.Required;

import enyo.lang.SystemExit;

import static java.lang.invoke.MethodHandles.lookup;

public class MainCopy {
    record Options(
        @Name("--progress") @Name("-p")
        @Help("display a progress bar")
        boolean progress,
        @Positional
        @Required @Help("path to the source file") String from,
        @Required @Help("path to the destination file") String to) {}

    public static void main(String[] args) {
        var parser = new ArgParser<>(lookup(), Options.class);
        var options = parser.run(MainCopy.class, args);
        System.out.format("progress: %s%n", options.progress());
        System.out.format("from: %s%n", options.from());
        System.out.format("to: %s%n", options.to());
    }
}
