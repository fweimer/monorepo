package enyo.sqlite.tests;

import enyo.sqlite.Database;
import enyo.sqlite.OpenFlag;
import enyo.sqlite.ValueType;
import enyo.sqlite.SQLiteException;

import enyo.io.TempDirectory;

import enyo.testing.Check;
import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.charset.StandardCharsets;

public class TestBasic {
    @Test
    public static void testOpenClose(Testing t) {
        try (var db = Database.inMemory()) {
            t.examine(db).isNotNull();
            t.examine(db.databaseName(0)).is("main");
            t.examine(db.readOnly("main")).is(false);
        }
        try (var db = Database.temporary()) {
            t.examine(db).isNotNull();
            t.examine(db.databaseName(0)).is("main");
            t.examine(db.readOnly("main")).is(false);
        }
    }

    @Test
    public static void testPrepareMultiple(Testing t) {
        try (var db = Database.inMemory()) {
            t.expect(() ->  db.prepare("SELECT 1; SELECT 2"),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage, e1 -> e1.is("""
                        Multiple SQL statements in prepare: SELECT 1; SELECT 2\
                        """)) // "))
                .map(SQLiteException::errorCode, e1-> e1.is(0)));
        }
    }

    @Test
    public static void testReal(Testing t) throws Exception {
        try (TempDirectory dir = TempDirectory.withPrefix("enyo-sqlite-")) {
            var path = dir.resolve("db").toString(); // FIXME: add Path support
            testReadOnly(t, path.toString());
            testDatabaseName(t, path.toString(), dir.resolve("db2").toString());
        }
    }

    // Outlined, to ensure that no checked exceptions are trown.
    private static void testReadOnly(Testing t, String path) {
        Database copy;
        try (var db = Database.of(
                path, OpenFlag.READWRITE, OpenFlag.CREATE)) {
            db.exec("CREATE TABLE names (name TEXT NOT NULL PRIMARY KEY)");
            t.examine(db.databaseName(0)).is("main");
            t.examine(db.readOnly("main")).is(false);
            copy = db;
        }
        t.expect(() -> copy.readOnly("main"),
            IllegalStateException.class);
        try (var db = Database.of(path, OpenFlag.READONLY)) {
            t.examine(db.databaseName(0)).is("main");
            t.examine(db.readOnly("main")).is(true);
            try (var stmt = db.prepare("SELECT * FROM names")) {
                t.assertFalse(stmt.step());
            }
            t.expect(() -> db.exec("INSERT INTO names VALUES ('Enyo')"),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage, e1 -> e1.is(
                        "attempt to write a readonly database [8]"))
                .map(SQLiteException::errorCode, e1-> e1.is(8)));
        }
        try (var db = Database.of(path, OpenFlag.READWRITE)) {
            // This verifies that extended error codes are used.
            t.expect(() -> db.exec("""
                    INSERT INTO names VALUES ('Fama');
                    INSERT INTO names VALUES ('Fama');
                    INSERT INTO names VALUES ('Bellona')
                    """), // "),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage, e1 -> e1.is(
                        "UNIQUE constraint failed: names.name [1555]"))
                .map(SQLiteException::errorCode, e1-> e1.is(1555)));
            try (var stmt = db.prepare(
                    "SELECT COUNT(*) FROM names WHERE name = 'Fama'")) {
                t.assertTrue(stmt.step());
                t.examine(stmt.columnLong(0)).is(1L);
            }
        }
    }

    private static void testDatabaseName(
        Testing t, String path1, String path2) {
        try (var db = Database.of(
                path1, OpenFlag.READWRITE, OpenFlag.CREATE)) {
            try (var stmt = db.prepare("ATTACH ? AS second")) {
                stmt.bind(1, path2);
                t.assertFalse(stmt.step());
            }
            t.examine(db.databaseName(0)).is("main");
            t.examine(db.readOnly("main")).is(false);
            t.examine(db.databaseName(1)).is("temp");
            t.examine(db.readOnly("second")).is(false);
            t.examine(db.databaseName(2)).is("second");
            t.examine(db.readOnly("second")).is(false);
        }
    }

    @Test
    public static void testExec(Testing t) {
        try (var db = Database.inMemory()) {
            db.exec("");
            db.exec(" ");
            db.exec(";");
            db.exec("SELECT 1");
            t.expect(() -> db.exec("bad syntax"),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage, e1 -> e1.is(
                        "near \"bad\": syntax error [1]"))
                .map(SQLiteException::errorCode, e1-> e1.is(1)));
            db.exec("""
                CREATE TABLE names (name TEXT NOT NULL PRIMARY KEY);
                INSERT INTO names VALUES ('Ossa');
                INSERT INTO names VALUES ('Pheme');
                INSERT INTO names VALUES ('Enyo');
                """); // ");
            try (var stmt = db.prepare("SELECT * FROM names")) {
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Ossa");
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Pheme");
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Enyo");
                t.assertFalse(stmt.step());
            }
            // This verifies that extended error codes are used.
            t.expect(() -> db.exec("""
                    INSERT INTO names VALUES ('Fama');
                    INSERT INTO names VALUES ('Fama');
                    INSERT INTO names VALUES ('Bellona')
                    """), // "),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage, e1 -> e1.is(
                        "UNIQUE constraint failed: names.name [1555]"))
                .map(SQLiteException::errorCode, e1-> e1.is(1555)));
            try (var stmt = db.prepare(
                    "SELECT COUNT(*) FROM names WHERE name = 'Fama'")) {
                t.assertTrue(stmt.step());
                t.examine(stmt.columnLong(0)).is(1L);
            }
            try (var stmt = db.prepare(
                    "SELECT COUNT(*) FROM names WHERE name = 'Bellona'")) {
                t.assertTrue(stmt.step());
                t.examine(stmt.columnLong(0)).is(0);
            }
        }
    }

    @Test
    public static void testUseAfterClose(Testing t) {
        final String dbClosed = "Database is closed";

        // Database.exec()
        {
            var db = Database.inMemory();
            db.close();
            db.close();         // No-op.
            t.expect(() -> db.exec(""),
                IllegalStateException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(dbClosed));
        }
        // Database.prepare()
        {
            var db = Database.inMemory();
            db.close();
            t.expect(() -> db.prepare("SELECT 1"),
                IllegalStateException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(dbClosed));
        }
        // Statement.step()
        {
            var db = Database.inMemory();
            var stmt = db.prepare("SELECT 1");
            db.close();
            t.examine(stmt.columnCount()).is(1); // Uses cached value.
            t.expect(() -> stmt.step(),
                IllegalStateException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(dbClosed));
            stmt.close();
        }
        // Statement.step()
        {
            var db = Database.inMemory();
            var stmt = db.prepare("SELECT 1");
            db.close();
            t.expect(() -> stmt.reset(),
                IllegalStateException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(dbClosed));
            stmt.close();
        }
        // Statement.bind() (String)
        {
            var db = Database.inMemory();
            var stmt = db.prepare("SELECT ?");
            db.close();
            t.examine(stmt.columnCount()).is(1); // Uses cached value.
            t.expect(() -> stmt.bind(1, "does not matter"),
                IllegalStateException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(dbClosed));
            stmt.close();
        }
        // Statement.step() after statement close
        {
            var db = Database.inMemory();
            var stmt = db.prepare("SELECT ?");
            stmt.close();
            stmt.close(); // No-op
            t.expect(() -> stmt.step(),
                IllegalStateException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is("Statement is closed"));
        }
        {
            var db = Database.inMemory();
            var stmt = db.prepare("SELECT NULL");
            interface Checker {
                Object check(int index);
            }
            Checker[] toCheck = {
                stmt:: columnBlob,
                stmt:: columnDouble,
                stmt:: columnLong,
                stmt:: columnType,
                stmt:: columnString,
            };
            stmt.close();
            for (var c : toCheck)
                t.expect(() -> c.check(0),
                    IllegalStateException.class);
        }
    }

    @Test
    public static void testPrepareEmpty(Testing t) {
        final String empty = "Empty prepared statement";
        try (var db = Database.inMemory()) {
            t.expect(() -> db.prepare(""),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(empty));
            t.expect(() -> db.prepare(" "),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(empty));
            t.expect(() -> db.prepare(";"),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage).is(empty));
        }
    }

    @Test
    public static void testPrepareBadSyntax(Testing t) {
        try (var db = Database.inMemory()) {
            t.expect(() -> db.prepare("bad syntax"),
                SQLiteException.class,
                e -> t.examine(e)
                .map(Exception::getMessage, e1 -> e1.is(
                        "near \"bad\": syntax error [1]"))
                .map(SQLiteException::errorCode, e1-> e1.is(1)));
        }
    }

    @Test
    public static void testUnclosedStatement(Testing t) {
        try (var db = Database.inMemory()) {
            var stmt = db.prepare("SELECT 123");
            t.examine(stmt.sql()).is("SELECT 123");
            // Let the resource leak.  Closing the database should not fail.
        }
    }

    @Test
    public static void testStatementSql(Testing t) {
        try (var db = Database.inMemory();
            var stmt = db.prepare("SELECT 123")) {
            t.examine(stmt.sql()).is("SELECT 123");
        }
    }

    @Test
    public static void testStepReset(Testing t) {
        try (var db = Database.inMemory()) {
            db.exec("CREATE TABLE names (name TEXT NOT NULL PRIMARY KEY)");
            try (var stmt = db.prepare("INSERT INTO names VALUES (?)")) {
                stmt.bind(1, "Enyo");
                t.assertFalse(stmt.step());
                stmt.reset();
                t.expect(() -> stmt.step(),
                    SQLiteException.class,
                    e -> t.examine(e)
                    .map(Exception::getMessage, e1 -> e1.is(
                            "UNIQUE constraint failed: names.name [1555]"))
                    .map(SQLiteException::errorCode, e1-> e1.is(1555)));
                stmt.reset();
                stmt.bind(1, "Bellona");
                t.assertFalse(stmt.step());
            }
            try (var stmt = db.prepare("SELECT * FROM names")) {
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Enyo");
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Bellona");
            }
            try (var stmt = db.prepare("INSERT INTO names VALUES (?)")) {
                stmt.bind(1, "Enyo");
                t.expect(() -> stmt.step(),
                    SQLiteException.class,
                    e -> t.examine(e)
                    .map(Exception::getMessage, e1 -> e1.is(
                            "UNIQUE constraint failed: names.name [1555]"))
                    .map(SQLiteException::errorCode, e1-> e1.is(1555)));
                db.exec("DELETE FROM names WHERE name = 'Enyo'");
                // Insertion now succeds.
                t.assertFalse(stmt.step());
                stmt.reset();
                stmt.bind(1, "Ossa");
                t.assertFalse(stmt.step());
            }
            try (var stmt = db.prepare("SELECT * FROM names")) {
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Bellona");
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Enyo");
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(0)).is("Ossa");
            }
        }
    }

    private static final Check<byte[]> checkÄrger =
        Checks.byteArrayEquals(new byte[] {
                (byte) 0xc3, (byte) 0x84, 'r', 'g', 'e', 'r'
            });

    @Test
    public static void testBindBlob(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT ?, ?")) {
                t.expect(() -> stmt.bind(-1, new byte[0]),
                    IndexOutOfBoundsException.class);
                t.expect(() -> stmt.bind(0, new byte[0]),
                    IndexOutOfBoundsException.class);
                t.expect(() -> stmt.bind(3, new byte[0]),
                    IndexOutOfBoundsException.class);
                t.expect(() -> stmt.bind(3, new byte[0], 0, 0),
                    IndexOutOfBoundsException.class);
                stmt.bind(2, "Ärger".getBytes());
                t.assertTrue(stmt.step());
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnType(1)).is(ValueType.BLOB);
                t.examine(stmt.columnString(1)).is("Ärger");
                stmt.reset();
                stmt.bind(2, "[Ärger]".getBytes(), 1, 6);
                t.assertTrue(stmt.step());
                t.examine(stmt.columnString(1)).is("Ärger");
                stmt.reset();
                byte[] value = new byte[] { '0', 0, '1', -1, 0 };
                stmt.bind(2, value.clone());
                t.assertTrue(stmt.step());
                t.examine(stmt.columnBlob(1)).check(
                    Checks.byteArrayEquals(value));
                stmt.reset();
                t.expect(() -> stmt.bind(2, (byte[]) null),
                    NullPointerException.class);
                t.expect(() -> stmt.bind(2, (byte[]) null, 0, 0),
                    NullPointerException.class);
                t.expect(() -> stmt.bind(2, "123".getBytes(), 4, 0),
                    IndexOutOfBoundsException.class);
            }
        }
    }

    @Test
    public static void testBindDouble(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT ?, ?")) {
                t.expect(() -> stmt.bind(3, 0.0),
                    IndexOutOfBoundsException.class);
                stmt.bind(2, 1.23);
                t.assertTrue(stmt.step());
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnType(1)).is(ValueType.FLOAT);
                t.examine(stmt.columnDouble(1)).is(1.23);
            }
        }
    }

    @Test
    public static void testBindLong(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT ?, ?")) {
                t.expect(() -> stmt.bind(3, 0L),
                    IndexOutOfBoundsException.class);
                stmt.bind(2, 123L);
                t.assertTrue(stmt.step());
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnType(1)).is(ValueType.INTEGER);
                t.examine(stmt.columnLong(1)).is(123L);
            }
        }
    }

    @Test
    public static void testBindString(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT ?, ?")) {
                t.expect(() -> stmt.bind(3, ""),
                    IndexOutOfBoundsException.class);
                t.expect(() -> stmt.bind(3, "", 0, 0),
                    IndexOutOfBoundsException.class);
                stmt.bind(2, "Ärger");
                t.assertTrue(stmt.step());
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnType(1)).is(ValueType.TEXT);
                t.examine(stmt.columnBlob(1)).check(checkÄrger);
                stmt.reset();
                stmt.bind(2, "[Ärger]", 1, 5);
                t.assertTrue(stmt.step());
                t.examine(stmt.columnBlob(1)).check(checkÄrger);
                stmt.reset();
                t.expect(() -> stmt.bind(2, (String) null),
                    NullPointerException.class);
                t.expect(() -> stmt.bind(2, (String) null, 0, 0),
                    NullPointerException.class);
                t.expect(() -> stmt.bind(2, "123", 4, 0),
                    IndexOutOfBoundsException.class);
            }
        }
    }

    @Test
    public static void testColumnBlob(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT X''")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.BLOB);
                t.examine(stmt.columnBlob(0)).check(
                    Checks.byteArrayEquals(new byte[0]));
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT ''")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                t.examine(stmt.columnBlob(0)).check(
                    Checks.byteArrayEquals(new byte[0]));
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT X'300031ff'")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.BLOB);
                t.examine(stmt.columnBlob(0)).check(
                    Checks.byteArrayEquals(new byte[] { '0', 0, '1', -1}));
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT 'Ärger'")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                t.examine(stmt.columnBlob(0)).check(
                    Checks.byteArrayEquals(new byte[] {
                            (byte) 0xc3, (byte) 0x84, 'r', 'g', 'e', 'r'
                        }));
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnBlob(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT NULL")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnBlob(0)).isNull();
            }
        }
    }

    @Test
    public static void testColumnDouble(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT 1.23")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnDouble(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnDouble(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.FLOAT);
                t.examine(stmt.columnDouble(0)).is(1.23);
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnDouble(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT NULL")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnDouble(0)).is(0.0);
            }
            try (var stmt = db.prepare("SELECT 1, 2, 3")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnDouble(0)).is(1.0);
                t.examine(stmt.columnDouble(1)).is(2.0);
                t.examine(stmt.columnDouble(2)).is(3.0);
                t.expect(() -> stmt.columnDouble(3),
                    IndexOutOfBoundsException.class);
            }
        }
    }

    @Test
    public static void testColumnLong(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT 17")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnLong(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnLong(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.INTEGER);
                t.examine(stmt.columnLong(0)).is(17);
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnLong(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT NULL")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnLong(0)).is(0);
            }
            try (var stmt = db.prepare("SELECT 1, 2, 3")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnLong(0)).is(1);
                t.examine(stmt.columnLong(1)).is(2);
                t.examine(stmt.columnLong(2)).is(3);
                t.expect(() -> stmt.columnLong(3),
                    IndexOutOfBoundsException.class);
            }
            try (var stmt = db.prepare("SELECT 0x10")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.INTEGER);
                t.examine(stmt.columnLong(0)).is(16);
            }
            try (var stmt = db.prepare("SELECT '0x10'")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                // 0x10 is parsed as hexadecimal only at the statement level.
                t.examine(stmt.columnLong(0)).is(0);
            }
            try (var stmt = db.prepare("SELECT '12e5'")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                // Trailing unrecognized characters are ignored in integer
                // casting.
                t.examine(stmt.columnLong(0)).is(12);
            }
        }
    }

    @Test
    public static void testColumnString(Testing t) {
        try (var db = Database.inMemory()) {
            try (var stmt = db.prepare("SELECT X''")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.BLOB);
                t.examine(stmt.columnString(0)).is("");
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT ''")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                t.examine(stmt.columnString(0)).is("");
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT X'300031ff'")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.BLOB);
                t.examine(stmt.columnString(0)).is("0\0001\uDCFF");
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
            }
            try (var stmt = db.prepare("SELECT 'Ärger'")) {
                t.examine(stmt.columnCount()).is(1);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(1),
                    IndexOutOfBoundsException.class);
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                t.examine(stmt.columnString(0)).is("Ärger");
                t.examine(stmt.step()).is(false);
                t.expect(() -> stmt.columnType(0),
                    IllegalStateException.class);
                t.expect(() -> stmt.columnString(0),
                    IllegalStateException.class);
            }
            byte[] value = "Ärger".getBytes(StandardCharsets.ISO_8859_1);
            try (var stmt = db.prepare("SELECT CAST(? AS TEXT)")) {
                stmt.bind(1, value.clone());
                t.assertTrue(stmt.step());
                t.examine(stmt.columnType(0)).is(ValueType.TEXT);
                t.examine(stmt.columnString(0)).is("\uDCC4rger");
            }
            // Verify that we actually got a Latin-1 byte sequence.
            try (var stmt = db.prepare("""
                    SELECT LENGTH(CAST(? AS TEXT)),
                    SUBSTR(CAST(? AS TEXT), 1, 1)
                    """)) { // "))
                stmt.bind(1, value);
                stmt.bind(2, value);
                t.assertTrue(stmt.step());
                t.examine(stmt.columnLong(0)).is(5);
                t.examine(stmt.columnBlob(1)).check(
                    Checks.byteArrayEquals(new byte[] { (byte) 0xc4 }));
            }
            try (var stmt = db.prepare("SELECT NULL")) {
                t.examine(stmt.step()).is(true);
                t.examine(stmt.columnType(0)).is(ValueType.NULL);
                t.examine(stmt.columnString(0)).isNull();
            }
        }
    }
}
