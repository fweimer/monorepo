package enyo.util.argparse.tests;

import enyo.util.argparse.ArgParser;
import enyo.util.argparse.ArgumentListException;
import enyo.util.argparse.Default;
import enyo.util.argparse.Help;
import enyo.util.argparse.HelpException;
import enyo.util.argparse.MetaVar;
import enyo.util.argparse.Name;
import enyo.util.argparse.Option;
import enyo.util.argparse.Positional;
import enyo.util.argparse.Required;

import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import static java.lang.invoke.MethodHandles.lookup;

public final class TestBasic {
    interface ArgLong {
        String arg();
    }

    // Used to test long-only and combined version.
    private static void checkLong(Testing t, ArgParser<? extends ArgLong> p) {
        t.examine(p.parse(new String[0]))
            .map(ArgLong::arg).isNull();
        t.examine(p.parse(new String[] {"--"}))
            .map(ArgLong::arg).isNull();
        t.examine(p.parse(new String[] {"--arg", "1"}))
            .map(ArgLong::arg).is("1");
        t.examine(p.parse(new String[] {"--arg", ""}))
            .map(ArgLong::arg).is("");
        t.examine(p.parse(new String[] {"--arg="}))
            .map(ArgLong::arg).is("");
        t.examine(p.parse(new String[] {"--arg=2"}))
            .map(ArgLong::arg).is("2");
        t.examine(p.parse(new String[] {"--arg=3", "--arg", "4"}))
            .map(ArgLong::arg).is("4");
        t.examine(p.parse(new String[] {"--arg", "--arg=3"}))
            .map(ArgLong::arg).is("--arg=3");
        t.examine(p.parse(new String[] {"--arg", "--arg", "--arg=3"}))
            .map(ArgLong::arg).is("3");
        t.expect(() -> p.parse(new String[] {"--arg"}),
            ArgumentListException.class)
            .map(Exception::getMessage)
            .is("option --arg needs a value");
        t.expect(() -> p.parse(new String[] {"--arg=3", "4"}),
            ArgumentListException.class)
            .map(Exception::getMessage)
            .is("unrecognized argument: \"4\"");
        t.expect(() -> p.parse(new String[] {"--not-known"}),
            ArgumentListException.class)
            .map(Exception::getMessage)
            .is("unknown option: --not-known");
        t.expect(() -> p.parse(new String[] {"--arg=151", "extra", "special"}),
            ArgumentListException.class)
            .map(Exception::getMessage)
            .is("unrecognized positional arguments: \"extra\", \"special\"");
        t.expect(() -> p.parse(new String[] {"--help"}),
            HelpException.class);
    }

    @Test
    public static void test1(Testing t) {
       record R(String arg) implements ArgLong {}
       var p = new ArgParser<>(lookup(), R.class);
       t.examine(p.options().size()).is(1);
       t.examine(p.options().get(0))
           .map(Option::primaryName).is("--arg");
       t.examine(p.options().get(0))
           .map(Option::names).is(List.of("--arg"));
       t.examine(p.options().get(0))
           .map(Option::needsValue).is(true);
       checkLong(t, p);
       checkHelp(t, R.class, """
           Options:

             --arg=ARG
               (undocumented)
           """);
    }

    interface ArgShort {
        String arg();
    }

    // Used to test short-only and combined version.
    private static void checkShort(Testing t, ArgParser<? extends ArgShort> p) {
       t.examine(p.parse(new String[0]))
           .map(ArgShort::arg).isNull();
       t.examine(p.parse(new String[] {"--"}))
           .map(ArgShort::arg).isNull();
       t.examine(p.parse(new String[] {"-a", "1"}))
           .map(ArgShort::arg).is("1");
       t.examine(p.parse(new String[] {"-a", ""}))
           .map(ArgShort::arg).is("");
       t.examine(p.parse(new String[] {"-a2"}))
           .map(ArgShort::arg).is("2");
       t.examine(p.parse(new String[] {"-a3", "-a", "4"}))
           .map(ArgShort::arg).is("4");
       t.examine(p.parse(new String[] {"-a", "-a3"}))
           .map(ArgShort::arg).is("-a3");
       t.examine(p.parse(new String[] {"-a", "-a", "-a3"}))
           .map(ArgShort::arg).is("3");
       t.expect(() -> p.parse(new String[] {"-a"}),
           ArgumentListException.class)
           .map(Exception::getMessage)
           .is("option -a needs a value");
       t.expect(() -> p.parse(new String[] {"-a3", "4"}),
           ArgumentListException.class)
           .map(Exception::getMessage)
           .is("unrecognized argument: \"4\"");
       t.expect(() -> p.parse(new String[] {"-a151", "extra", "special"}),
           ArgumentListException.class)
           .map(Exception::getMessage)
           .is("unrecognized positional arguments: \"extra\", \"special\"");
    }


    @Test
    public static void test1Short(Testing t) {
       record R(@Name("-a") String arg) implements ArgShort{}
       var p = new ArgParser<>(lookup(), R.class);
       t.examine(p.options().size()).is(1);
       t.examine(p.options().get(0))
           .map(Option::primaryName).is("-a");
       t.examine(p.options().get(0))
           .map(Option::names).is(List.of("-a"));
       t.examine(p.options().get(0))
           .map(Option::needsValue).is(true);
       checkShort(t, p);
    }

    @Test
    public static void test1Both(Testing t) {
       record R(@Name("--arg") @Name("-a") String arg)
           implements ArgLong, ArgShort {}
       var p = new ArgParser<>(lookup(), R.class);
       t.examine(p.options().size()).is(1);
       t.examine(p.options().get(0))
           .map(Option::primaryName).is("--arg");
       t.examine(p.options().get(0))
           .map(Option::names).is(List.of("--arg", "-a"));
       t.examine(p.options().get(0))
           .map(Option::needsValue).is(true);
       checkLong(t, p);
       checkShort(t, p);
    }

    @Test
    public static void test2(Testing t) {
       record R(
           @Help("the string argument")
           @MetaVar("STRING")
           String arg,

           @Help("the extra flag")
           boolean extra) {}
       var p = new ArgParser<>(lookup(), R.class);
       t.examine(p.parse(new String[0]))
           .map(R::arg).isNull();
       t.examine(p.parse(new String[] {"--arg", "1"}))
           .map(R::arg).is("1");
       t.examine(p.parse(new String[] {"--arg", ""}))
           .map(R::arg).is("");
       t.examine(p.parse(new String[] {"--arg="}))
           .map(R::arg).is("");
       t.examine(p.parse(new String[] {"--arg=2"}))
           .map(R::arg).is("2");
       t.examine(p.parse(new String[] {"--arg=3", "--arg", "4"}))
           .map(R::arg).is("4");
       t.examine(p.parse(new String[] {"--arg", "--arg=3"}))
           .map(R::arg).is("--arg=3");
       t.examine(p.parse(new String[] {"--arg", "--arg", "--arg=3"}))
           .map(R::arg).is("3");
       t.examine(p.parse(new String[] {"--arg", "--extra", "--arg=4"}))
           .map(R::arg, e -> e.is("4"))
           .map(R::extra, e -> e.is(false));
       t.examine(p.parse(new String[] {"--arg=3", "--extra", "--arg=4"}))
           .map(R::arg, e -> e.is("4"))
           .map(R::extra, e-> e.is(true));
       checkHelp(t, R.class, """
           Options:

             --arg=STRING
               the string argument
             --extra
               the extra flag
           """);
    }

    @Test
    public static void test3(Testing t) {
       record R(int arg) {}
       var p = new ArgParser<>(lookup(), R.class);
       t.examine(p.parse(new String[] {"--arg", "1"}))
           .map(R::arg).is(1);
       t.examine(p.parse(new String[] {"--arg", "0xf"}))
           .map(R::arg).is(15);
       t.examine(p.parse(new String[] {"--arg", "1", "--"}))
           .map(R::arg).is(1);
       t.expect(() -> p.parse(new String[0]),
           ArgumentListException.class)
           .map(Exception::getMessage)
           .is("option --arg is missing");
       t.expect(() -> p.parse(new String[] {"--arg=X"}),
           ArgumentListException.class)
           .map(Exception::getMessage)
           .is("invalid value for option --arg: X");
       // FIXME: Arguably, this should pass.
       t.expect(() -> p.parse(new String[] {"--arg", "0xffffffff"}),
           ArgumentListException.class)
           .map(Exception::getMessage)
           .is("invalid value for option --arg: 0xffffffff");
    }

    @Test
    public static void test4(Testing t) {
        record R(
            @Name("--progress") @Name("-p")
            @Help("display a progress bar")
            boolean progress,
            @Positional
            @Required @Help("path to the source file") String from,
            @Required @Help("path to the destination file") String to) {}
        var p = new ArgParser<>(lookup(), R.class);
        t.examine(p.options())
            .map(e -> e.get(0), e -> e.map(Option::required).is(false));
        t.examine(p.options())
            .map(opts -> opts.get(1),
                from -> from.map(Option::primaryName, e -> e.is("from"))
                .map(Option::positional, e -> e.is(true))
                .map(Option::required, e -> e.is(true)))
            .map(opts -> opts.get(2),
                to -> to.map(Option::primaryName, e -> e.is("to"))
                .map(Option::positional, e -> e.is(true))
                .map(Option::required, e -> e.is(true)));
        t.examine(p.parse(new String[] {"source", "destination"}))
            .map(R::progress, e -> e.is(false))
            .map(R::from, e -> e.is("source"))
            .map(R::to, e -> e.is("destination"));
        t.examine(p.parse(new String[] {"--progress", "source", "destination"}))
            .map(R::progress, e -> e.is(true))
            .map(R::from, e -> e.is("source"))
            .map(R::to, e -> e.is("destination"));
        t.examine(p.parse(new String[] {"source", "--progress", "destination"}))
            .map(R::progress, e -> e.is(true))
            .map(R::from, e -> e.is("source"))
            .map(R::to, e -> e.is("destination"));
        t.examine(p.parse(new String[] {"source", "destination", "--progress"}))
            .map(R::progress, e -> e.is(true))
            .map(R::from, e -> e.is("source"))
            .map(R::to, e -> e.is("destination"));
        t.examine(p.parse(new String[] {"--", "--progress", "destination"}))
            .map(R::progress, e -> e.is(false))
            .map(R::from, e -> e.is("--progress"))
            .map(R::to, e -> e.is("destination"));
        t.examine(p.parse(new String[] {"source", "--", "--progress"}))
            .map(R::progress, e -> e.is(false))
            .map(R::from, e -> e.is("source"))
            .map(R::to, e -> e.is("--progress"));
        t.expect(() -> p.parse(new String[0]),
            ArgumentListException.class)
            .map(ArgumentListException::errors).is(List.of(
                "argument from is missing",
                "argument to is missing"));
        t.expect(() -> p.parse(new String[] { "--progress" }),
            ArgumentListException.class)
            .map(ArgumentListException::errors).is(List.of(
                "argument from is missing",
                "argument to is missing"));
       checkHelp(t, R.class, """
           Options:

             --progress, -p
               display a progress bar

           Arguments:

             from
               path to the source file
             to
               path to the destination file
           """);
    }

    @Test
    public static void test5(Testing t) {
        record R(@Name("--first") @Name("--initial") int field) {}
        var p = new ArgParser<>(lookup(), R.class);
        t.examine(p.options().get(0))
            .map(Option::names)
            .is(List.of("--first", "--initial"));
        t.examine(p.parse(new String[] {"--first=1"}))
            .map(R::field, e -> e.is(1));
        t.examine(p.parse(new String[] {"--initial=2"}))
            .map(R::field, e -> e.is(2));
        t.examine(p.parse(new String[] {"--initial=2", "--first=3"}))
            .map(R::field, e -> e.is(3));
    }

    @Test
    public static void test6(Testing t) {
        record R(@Default("17") int class_) {}
        var p = new ArgParser<>(lookup(), R.class);
        t.examine(p.options().get(0))
            .map(Option::defaultObject).is(17);
        t.examine(p.parse(new String[0]))
            .map(R::class_, e -> e.is(17));
        t.examine(p.parse(new String[] {"--"}))
            .map(R::class_, e -> e.is(17));
        t.examine(p.parse(new String[] {"--class=19"}))
            .map(R::class_, e -> e.is(19));
    }

    @Test
    public static void test7(Testing t) {
        record R(String longOption) {}
        var p = new ArgParser<>(lookup(), R.class);
        t.examine(p.options().get(0))
            .map(Option::names, e -> e.is(List.of("--long-option")))
            .map(Option::primaryName, e -> e.is("--long-option"));
        t.examine(p.parse(new String[0]))
            .map(R::longOption, e -> e.isNull());
        t.examine(p.parse(new String[] {"--"}))
            .map(R::longOption, e -> e.isNull());
        t.examine(p.parse(new String[] {"--long-option=very-long-value"}))
            .map(R::longOption, e -> e.is("very-long-value"));
    }

    static void checkHelp(
        Testing t, Class<? extends Record> cls, String message) {
        var p = new ArgParser<>(lookup(), cls);
        var out = new ByteArrayOutputStream();
        p.help(new PrintStream(out));
        t.examine(new String(out.toByteArray())).is(message);
    }
}
