package enyo.sqlite.tests;

import enyo.sqlite.Database;
import enyo.sqlite.OpenFlag;
import enyo.sqlite.Statement;
import enyo.sqlite.SQLiteException;

import enyo.io.TempDirectory;

import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

public class TestThreads {
    // This is hopefully a good compromise between testing overhead and
    // exposing concurrency bugs.
    private static final int THREADS = 5;
    private static final int ROWS = 1000;

    @Test
    @Resources(threads=THREADS)
    public static void testParallelDatabse(Testing t) throws Exception {
        try (TempDirectory dir = TempDirectory.withPrefix("enyo-sqlite-")) {
            var path = dir.resolve("db").toString(); // FIXME: add Path support

            var threads = new Thread[THREADS];
            var barrier = new CyclicBarrier(THREADS);
            for (int i = 0; i < THREADS; ++i) {
                int i0 = i;
                threads[i0] = new Thread(
                    () -> {try {
                        parallelDatabase(t, path, barrier, i0);
                    } catch (Throwable e) {
                        e.printStackTrace(); } });
                threads[i0].start();
            }
            for (int i = 0; i < THREADS; ++i)
                threads[i].join();

            try (var db = Database.of(path, OpenFlag.READONLY)) {
                checkResult(t, db);
            }
        }
    }

    @Test
    @Resources(threads=THREADS)
    public static void testSharedDatabase(Testing t) throws Exception {
        try (var db = Database.inMemory()) {
            db.exec("""
                CREATE TABLE IF NOT EXISTS tab
                (x INTEGER NOT NULL, y INTEGER NOT NULL)"""); //");

            var threads = new Thread[THREADS];
            var barrier = new CyclicBarrier(THREADS);
            for (int i = 0; i < THREADS; ++i) {
                int i0 = i;
                threads[i0] = new Thread(
                    () -> {try {
                        sharedDatabase(t, db, barrier, i0);
                    } catch (Throwable e) {
                        e.printStackTrace(); } });
                threads[i0].start();
            }
            for (int i = 0; i < THREADS; ++i)
                threads[i].join();

            checkResult(t, db);
        }
    }

    private static void checkResult(Testing t, Database db) {
        record Pair(long x, long y) {}
        var resultSet = new HashSet<Pair>();
        try (var stmt = db.prepare("SELECT * FROM tab")) {
            while (stmt.step())
                resultSet.add(new Pair(
                        stmt.columnLong(0),
                        stmt.columnLong(1)));
        }
        t.examine(resultSet.size()).is(THREADS * ROWS);
        for (int x = 0; x < THREADS; ++x)
            for (int y = 0; y < ROWS; ++y)
                t.examine(new Pair(x, y))
                    .map(p -> t.examine(resultSet.contains(p)).is(true));
    }

    private static void parallelDatabase(Testing t,
        String path, CyclicBarrier barrier, int i) {
        // Make it more likely that the code below runs
        // concurrently.
        try {
            barrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            throw new AssertionError(e);
        }
        try (var db = Database.of(
                path, OpenFlag.READWRITE, OpenFlag.CREATE)) {
            execCarefully(t, db, """
                CREATE TABLE IF NOT EXISTS tab
                (x INTEGER NOT NULL, y INTEGER NOT NULL)"""); //");
            try (var stmt = prepareCarefully(
                    t, db, "INSERT INTO tab VALUES (?, ?)")) {
                stmt.bind(1, i);
                for (int row = 0; row < ROWS; ++row) {
                    stmt.reset();
                    stmt.bind(2, row);
                    stepCarefully(t, stmt);
                }
            }
        }
    }

    private static void sharedDatabase(Testing t,
        Database db, CyclicBarrier barrier, int i) {
        // Make it more likely that the code below runs
        // concurrently.
        try {
            barrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            throw new AssertionError(e);
        }
        // Statement-level locking prevents BUSY errors.
        try (var stmt = db.prepare("INSERT INTO tab VALUES (?, ?)")) {
            stmt.bind(1, i);
            for (int row = 0; row < ROWS; ++row) {
                stmt.reset();
                stmt.bind(2, row);
                stmt.step();
            }
        }
    }

    private static void execCarefully(
        Testing t, Database db, String sql) {
        while (true) {
            try {
                db.exec(sql);
                break;
            } catch (SQLiteException e) {
                t.examine(e)
                    .map(SQLiteException::errorCode)
                    .is(5); // FIXME BUSY
            }
        }
    }

    private static Statement prepareCarefully(
        Testing t, Database db, String sql) {
        while (true) {
            try {
                return db.prepare(sql);
            } catch (SQLiteException e) {
                t.examine(e)
                    .map(SQLiteException::errorCode)
                    .is(5); // FIXME BUSY
            }
        }
    }

    private static void stepCarefully(Testing t, Statement stmt) {
        while (true) {
            try {
                stmt.step();
                break;
            } catch (SQLiteException e) {
                t.examine(e)
                    .map(SQLiteException::errorCode)
                    .is(5); // FIXME BUSY
            }
        }
    }

    @Test
    @Resources(threads=2)
    public static void testBusyTimeout(Testing t) throws Exception {
        try (TempDirectory dir = TempDirectory.withPrefix("enyo-sqlite-")) {
            var path = dir.resolve("db").toString(); // FIXME: add Path support

            try (var db1 = Database.of(
                    path, OpenFlag.READWRITE, OpenFlag.CREATE);
                var db2 = Database.of(path, OpenFlag.READWRITE)) {
                // Create an exclusive transaction.
                db2.exec("BEGIN IMMEDIATE");

                // The first database will not be able to proceed.
                t.expect(() ->
                    db1.exec(
                        "CREATE TABLE t (rowid INTEGER NOT NULL PRIMARY KEY)"),
                    SQLiteException.class,
                    e -> t.examine(e)
                    .map(Exception::getMessage, e1 -> e1.is(
                            "database is locked [5]"))
                    .map(SQLiteException::errorCode, e1-> e1.is(5)));

                db1.busyTimeout(Integer.MAX_VALUE);

                var barrier = new CyclicBarrier(2);
                var thr = new Thread(
                    () -> {
                        try {
                            barrier.await();
                            // Slight delay, so that the db1.exec(String)
                            // call below has a chance to fail.
                            Thread.sleep(100);
                        } catch (BrokenBarrierException
                            | InterruptedException e) {
                            throw new AssertionError(e);
                        }
                        db2.exec("COMMIT");
                    });
                thr.start();
                barrier.await();

                // This is now expected to wait and succeed ventually.
                db1.exec("CREATE TABLE t (rowid INTEGER NOT NULL PRIMARY KEY)");

                thr.join();
            }
	}
    }
}
