package enyo.sqlite;

import enyo.io.ExtendedCharsets;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An SQLite database object.
 *
 * <h2 id=charset>Character set</h2>
 *
 * SQL code uses the standard Java UTF-8 encoding.  Text values used
 * in statement parameters and result columns use the
 * {@linkplain ExtendedCharsets#UTF_8S UTF-8S} that can encode
 * arbitrary byte sequences.
 */
public final class Database implements AutoCloseable {
    /**
     * Creates a new in-memory database.
     * This database corresponds to the special {@code ":memory:"} database
     * that SQLite provides.
     * @return a new object for a non-persistent database
     * @throws SQLiteException the database could not be created
     * @see #temporary()
     */
    public static Database inMemory() {
        return new Database(":memory:", null,
            Constants.OPEN_READWRITE | Constants.OPEN_CREATE);
    }

    /**
     * Creates a temporary database.
     * Unlike the database returned from {@link inMemory()}, this
     * database is stored on disk, in a temporary file.
     * This database corresponds to the special {@code ""} database
     * that SQLite provides.
     * @return a new object for a non-persistent database
     * @throws SQLiteException the database could not be created
     * @see #inMemory()
     */
    public static Database temporary() {
        return new Database("", null,
            Constants.OPEN_READWRITE | Constants.OPEN_CREATE);
    }

    /**
     * Creates a new database object.
     *
     * Note that the special names {@code ":memory:"} and {@code ""} are
     * not supported; use {@link #inMemory()} and {@link #temporary()} instead.
     *
     * @param name the file name of the database (or URI if
     *   {@link OpenFlag#URI} is used)
     * @param flag there must be at least one flag, either
     *   {@link OpenFlag#READONLY} or {@link OpenFlag#READWRITE}
     *   (but it could be included later in {@code flags})
     * @return the new database object
     * @param flags other optional flags
     * @throws SQLiteException the database could not be opened
     * @throws NullPointerException any of the arguments are null
     */
    public static Database of(String name, OpenFlag flag, OpenFlag... flags) {
        // Always use NOMUTEX mode.  Different locking strategies are
        // implemented at the Java layer.
        // Always use extended result codes, including for the open operation.
        return new Database(name, null, OpenFlag.convert(flag, flags));
    }

    /**
     * Checks if the database of this connection is read-only or read-write.
     * @param name the name of the database to check
     * @return true if the database is not writable (read-only), false
     *   if it is read-write
     * @see #databaseName(int)
     */
    public boolean readOnly(String name) {
        int ret;
        var db = lock();
        try {
            try (var arena = Arena.ofConfined()) {
                var nameN = arena.allocateFrom(name);
                try {
                    ret = (int) Native.db_readonly.invokeExact(db, nameN);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new SQLiteException(e);
                }
            }
        } finally {
            unlock();
        }
        switch (ret) {
        case 1:
            return true;
        case 0:
            return false;
        default: // Includes the documented -1 value.
            throw new SQLiteException("Not the name of a database: " + name);
        }
    }

    /**
     * Converts a database index to its name.
     * @param index the number of the database
     * @return the name, typically {@code "main"} for the first database
     * @throws NullPointerException the database index is not valid
     */
    @SuppressWarnings("restricted") // Use a string that came from SQLite.
    public String databaseName(int index) {
        var db = lock();
        try {
            var ret = (MemorySegment) Native.db_name.invokeExact(db, index);
            if (ret.address() == 0)
                throw new SQLiteException("Unknown database index: " + index);
            return ret.reinterpret(Long.MAX_VALUE).getString(0);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
    }

    private Database(String name, String vfs, int flags) {
        // Always used for opening the database.
        final int baseFlags =
            Constants.OPEN_NOMUTEX
            | Constants.OPEN_EXRESCODE
            | Constants.OPEN_PRIVATECACHE;
        flags |= baseFlags;

        MemorySegment resultPtr;
        try (var arena = Arena.ofConfined()) {
            var nameN = arena.allocateFrom(name);
            var resultPtrN = arena.allocate(ValueLayout.ADDRESS);
            var vfsN =
                vfs != null ? arena.allocateFrom(vfs) : MemorySegment.NULL;
            int errcode;
            try {
                errcode = (int) Native.open_v2.invokeExact(
                    nameN, resultPtrN, flags, vfsN);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                // Not expected to happen.
                throw new SQLiteException(e);
            }
            resultPtr = resultPtrN.get(ValueLayout.ADDRESS, 0);
            if (primary(errcode) != Constants.OK) {
                if (MemorySegment.NULL.equals(resultPtr))
                    throw new OutOfMemoryError();
                var e = new SQLiteException(resultPtr, errcode);
                try {
                    var _ = (int) Native.close_v2.invokeExact(resultPtr);
                } catch (Throwable e1) {
                    // Not expected to happen.  We already have a more
                    // precise exception to throw, so use that below.
                }
                throw e;
            }
        }
        this(resultPtr);
    }

    /**
     * Closes the database connection.
     *
     * This operation is not expected to fail, but there could be a VM
     * error if the foreign function linking fails.
     */
    @Override
    public void close() {
        var db = lockUnchecked();
        try {
            if (db == MemorySegment.NULL)
                return;
            closeLock.lock();
            try {
                int errcode = (int) Native.close_v2.invokeExact(db);
                // A failure to close the database does not invalidate the
                // pointer.  Using close_v2 means that there should be no
                // busy error due to lingering statement objects.
                if (primary(errcode) != Constants.OK)
                    throw exception(errcode);
                // Mark this database object as closed.
                state.db = MemorySegment.NULL;
                // This is a no-op because of the assignment above, but it
                // frees up the cleanable.
                cleanable.clean();
            } finally {
                closeLock.unlock();
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            // Not expected to happen.
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
    }

    /**
     * Executes one or more SQL statements.
     *
     * <p><strong>Note:</strong>
     * You should use the {@link #prepare(String)} if the SQL statement
     * contains parameters.</p>
     * @param sql a string containing one or more SQL statements
     * @throws NullPointerException {@code sql} is null
     * @throws SQLiteException the statement could not be executed
     */
    public void exec(String sql) {
        checkNoNUL(sql);
        try (var arena = Arena.ofConfined()) {
            var sqlN = arena.allocateFrom(sql);
            var stmtPtrN = arena.allocate(ValueLayout.ADDRESS);
            var tailPtrN = arena.allocate(ValueLayout.ADDRESS);
            var db = lock();
            try {
                while (true) {
                    // Compile the statement.
                    int errcode;
                    errcode = (int) Native.prepare_v2.invokeExact(
                        db, sqlN, -1, stmtPtrN, tailPtrN);
                    check(errcode);
                    var stmt = stmtPtrN.get(ValueLayout.ADDRESS, 0);
                    if (stmt.address() == 0)
                        break;
                    try {
                        // Iterate until all rows have been consumed.
                        STEP: while (true) {
                            errcode = (int) Native.step.invokeExact(stmt);
                            switch (primary(errcode)) {
                            case Constants.ROW:
                                continue;
                            case Constants.DONE:
                            case Constants.OK:
                                break STEP;
                            default:
                                throw new SQLiteException(db, errcode);
                            }
                        }
                    } finally {
                        int ignored = (int) Native.finalize.invokeExact(stmt);
                    }
                    var tailPtr = getTail(
                        sqlN, tailPtrN.get(ValueLayout.ADDRESS, 0));
                    if (tailPtr.get(ValueLayout.JAVA_BYTE, 0) == 0)
                        // End of string reached.
                        break;
                    // Continue with the tail.
                    sqlN = tailPtr;
                }
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            } finally {
                unlock();
            }
        }
    }

    // SQLite does not handle NUL bytes in SQL statements.
    private static void checkNoNUL(String sql) {
        if (sql.indexOf(0) >= 0)
            throw new SQLiteException("NUL byte in SQL statement");
    }

    /**
     * Creates a new prepared statement.
     *
     * @param sql the SQL statement
     * @return the new compiled prepared statement
     * @throws NullPointerException {@code sql} is null
     * @throws SQLiteException the statement could not be created,
     *  or {@code sql} contains more than one statement
     */
    public Statement prepare(String sql) {
        checkNoNUL(sql);
        try (var arena = Arena.ofConfined()) {
            var sqlN = arena.allocateFrom(sql);
            var stmtPtrN = arena.allocate(ValueLayout.ADDRESS);
            var tailPtrN = arena.allocate(ValueLayout.ADDRESS);
            var db = lock();
            try {
                int errcode = (int) Native.prepare_v2.invokeExact(
                    db, sqlN, -1, stmtPtrN, tailPtrN);
                check(errcode);
                var stmt = stmtPtrN.get(ValueLayout.ADDRESS, 0);
                if (stmt.address() == 0)
                    throw new SQLiteException("Empty prepared statement");
                // Only one statement is supported.
                var tailPtr = getTail(
                    sqlN, tailPtrN.get(ValueLayout.ADDRESS, 0));
                // Minor optimization: if we encountered the terminating
                // NUL, no need to check for the tail the hard way.
                if (tailPtr.get(ValueLayout.JAVA_BYTE, 0) != 0)
                    prepareCheckNoTail(db, sql, stmtPtrN, stmt, tailPtr);
                try {
                    return new Statement(this, lock, stmt);
                } catch (RuntimeException | Error e) {
                    int ignored = (int) Native.finalize.invokeExact(stmt);
                    throw e;
                } catch (Throwable e) {
                    int ignored = (int) Native.finalize.invokeExact(stmt);
                    throw e;
                }
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            } finally {
                unlock();
            }
        }
    }

    // Check if another prepared statement follows the specified statement.
    private static void prepareCheckNoTail(MemorySegment db, String sql,
        MemorySegment stmtPtrN, MemorySegment firstStmt, MemorySegment tail)
        throws Throwable {
        int errcode;
        try {
            errcode = (int) Native.prepare_v2.invokeExact(
                db, tail, -1, stmtPtrN, MemorySegment.NULL);
        } catch (Throwable e) {
            int ignored = (int) Native.finalize.invokeExact(firstStmt);
            throw e;
        }
        var secondStmt = stmtPtrN.get(ValueLayout.ADDRESS, 0);
        boolean haveSecond = secondStmt.address() != 0;
        if (primary(errcode) == Constants.OK && !haveSecond)
            // NULL denotes an empty statement, which is acceptable because
            // we already have the first statement.
            return;
        try {
            int ignored = (int) Native.finalize.invokeExact(secondStmt);
        } finally {
            int ignored = (int) Native.finalize.invokeExact(firstStmt);
        }
        throw new SQLiteException(
            (haveSecond ? "Multiple SQL statements in prepare: "
                : "Trailing characters after SQL statement: ")+ sql);
    }

    /**
     * Sets the busy timeout for this database.
     * By default, if the database is locked,
     * operations will fail with a {@link SQLiteException} and
     * an error code of {@code BUSY} or one of its
     * associated extended error codes.  If a busy timeout
     * is specified, SQLite will wait for this time period before
     * reporting a busy error.
     *
     * <p>There is currently no way to block indefinitely.  The
     * maximum wait time is about 24.85 days (corresponding to
     * {@link Integer#MAX_VALUE}).
     *
     * @param ms the timeout, measured in milliseconds; if negative,
     *   all busy handlers are turned off
     */
    public void busyTimeout(int ms) {
        var db = lock();
        try {
            // The return value is undocumented, and always OK today.
            int ignored = (int) Native.busy_timeout.invokeExact(db, ms);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
    }

    /**
     * Interrupts a database operation.
     *
     * Unlike in the underlying C API, it is safe to call this method
     * concurrently with {@link close()}.  Interrupting a closed database
     * does nothing.
     */
    public void interrupt() {
        // Do not use the regular lock here, so that it is actually
        // possible to interrupt a running operation from another thread.
        closeLock.lock();
        try {
            var db = state.db;
            if (db == MemorySegment.NULL)
                return;
            Native.interrupt.invokeExact(db);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLiteException(e);
        } finally {
            Reference.reachabilityFence(this);
            closeLock.unlock();
        }
    }

    MemorySegment lock() {
        lock.lock();
        var db = state.db;
        if (db == MemorySegment.NULL) {
            lock.unlock();
            throw new IllegalStateException("Database is closed");
        }
        return db;
    }

    MemorySegment lockUnchecked() {
        lock.lock();
        return state.db;
    }

    void unlock() {
        Reference.reachabilityFence(this);
        lock.unlock();
    }

    private Database(MemorySegment db) {
        try {
            state = new State(db);
            cleanable = cleaner.register(this, state);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            // Creation of the cleaner failed.  We need to deallocate
            // the native resources.
            try {
                int _ = (int) Native.close_v2.invokeExact(db);
            } catch (Throwable e1) {
                // Not expected to happen.  We already have a more
                // precise exception to throw, so use that below.
            }
            throw e;
        }
    }

    private static final class State implements Runnable {
        private MemorySegment db;

        private State(MemorySegment db) {
            this.db = db;
        }

        public void run() {
            // No locking: The database object has become
            // phantom-reachable at this point, so there are no
            // concurrent operations on it (due to the reachability
            // fences).  The Statement object has a strong reference
            // to the Database object and fences of its own, so there
            // cannot be any interference from application operations
            // on statements, either.
            var db0 = db;
            if (db0 == MemorySegment.NULL)
                return;
            db = MemorySegment.NULL;
            // We have to ignore any errors.  sqlite3_close_v2 at
            // least marks the database as pending for destruction
            // once all statements are gone.
            try {
                int errcode = (int) Native.close_v2.invokeExact(db0);
            } catch (Throwable e) {
            }
        }
    }

    // Computes the primary error code from the extended error code.
    static int primary(int errcode) {
        return errcode & 0xff;
    }

    // Check the extended error code and throw SQLiteException with it
    // if not SQLITE_OK.  The database object must still be locked.
    void check(int errcode) throws Throwable {
        if (primary(errcode) != Constants.OK)
            throw exception(errcode);
    }

    // Check using the implicitly stored error code.  Used in case
    // the error-producing interface does not provide an error code
    // (e.g., sqlite3_column_blob).
    void checkError() throws Throwable {
        int errcode = (int) Native.extended_errcode.invokeExact(state.db);
        check(errcode);
    }

    // Creates an exception with the stored error message.
    // The database object must still be locked.
    SQLiteException exception(int errcode) throws Throwable {
        return new SQLiteException(state.db, errcode);
    }

    // Obtain the tail of the memory segment starting at base, which
    // must have the right extent.
    static final MemorySegment getTail(MemorySegment base, MemorySegment tail) {
        return base.asSlice(tail.address() - base.address());
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    // General lock that protects all operations.  Statement
    // operations must use this lock, too.
    private final ReentrantLock lock = new ReentrantLock();

    // Special lock acquired during close() to lock out concurrent
    // interrupt().  State.run() (the cleaner) does not acquire
    // this lock because there cannot be another thread that
    // runs interrupt() due to the reachability fences.
    private final ReentrantLock closeLock = new ReentrantLock();

    // A single Cleaner is shared between Database and Statement
    // objects.  This is required for synchronization of State
    // cleanup.
    static final Cleaner cleaner = Cleaner.create();
}
