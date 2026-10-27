package enyo.sqlite.tests;

import enyo.util.argparse.ArgParser;
import enyo.sqlite.Database;

import static java.lang.invoke.MethodHandles.lookup;

public final class BenchIncrement {
    record Options(
        boolean reprepare,
        boolean noClose,
        int count) {}

    public static void main(String[] args) {
        var options = new ArgParser<>(lookup(), Options.class).run(
            BenchIncrement.class, args);
        try (var db = Database.inMemory()) {
            if (options.reprepare) {
                for (int i = 0; i < options.count(); ++i ) {
                    var stmt = db.prepare("SELECT ? + 1");
                    stmt.bind(1, i);
                    if (!stmt.step())
                        throw new AssertionError();
                    if (stmt.columnLong(0) != i + 1L)
                        throw new AssertionError();
                    if (stmt.step())
                        throw new AssertionError();
                    if (!options.noClose())
                        stmt.close();
                }
            } else {
                try (var stmt = db.prepare("SELECT ? + 1")) {
                    for (int i = 0; i < options.count(); ++i ) {
                        stmt.bind(1, i);
                        if (!stmt.step())
                            throw new AssertionError();
                        if (stmt.columnLong(0) != i + 1L)
                            throw new AssertionError();
                        stmt.reset();
                    }
                }
            }
        }
    }
}
