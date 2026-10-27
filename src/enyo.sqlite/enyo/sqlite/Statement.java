package enyo.sqlite;

import static enyo.sqlite.Database.primary;
import enyo.io.ExtendedCharsets;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A compiled prepared statement.
 *
 * @see Database#prepare(String)
 */
public final class Statement implements AutoCloseable {
    @Override
    public void close() {
        var stmt = lockUnchecked();
        try {
            if (stmt == MemorySegment.NULL)
                return;
            // Let the cleaner do the work.  Ignore errors.  Pending
            // errors have been reported through an exception before.
            cleanable.clean();
        } finally {
            unlock();
        }
    }

    /**
     * Executes this statement once.
     *
     * If the statement is not immediately {@linkplain #close() closed}
     * after execution, the caller has to invoke {@code step()} repeatedly
     * until it returns false, so that all locks are released.
     * Alternatively, the {@link #reset()} method can be called to end
     * the execution immediately.
     *
     * @return true if the statement produced a data row, otherwise false
     * @throws SQLiteException an error occurred during execution
     */
    public boolean step() {
        var stmt = lock();
        try {
            haveColumns = -1;
            int errcode = (int) Native.step.invokeExact(stmt);
            switch (primary(errcode)) {
            case Constants.ROW:
                haveColumns = columnCount;
                return true;
            case Constants.DONE:
                return false;
            default:
                // The error is reported through an exception.  Clear
                // the error indicator, so that it safe to call
                // reset(), without producing another exception.
                try {
                    int ignored = (int) Native.reset.invokeExact(stmt);
                } catch (Throwable e) {
                    // More precise exception follows.
                }
                throw db.exception(errcode);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
    }

    /**
     * Terminates execution of this statement.
     *
     * Called after {@link #step()}, this method resets the prepared
     * statement status to its original state prior to starting
     * exection.  Parameter bindings are kept, but return column
     * values are discarded.
     *
     * @throws SQLiteException if there are any pending errors
     */
    public void reset() {
        var stmt = lock();
        try {
            haveColumns = -1;
            int errcode = (int) Native.reset.invokeExact(stmt);
            db.check(errcode);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            // This should not happen.
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
    }

    /**
     * Binds a parameter to a slice of a byte array.
     * @param index the parameter index; the first parameter has index 1
     * @param value the byte array from which to extract bytes
     * @param off the first byte to extract
     * @param len the number of bytes to extract
     * @throws NullPointerException {@code value} is null
     * @see #bind(int,byte[])
     */
    public void bind(int index, byte[] value, int off, int len) {
        var segment = MemorySegment.ofArray(value).asSlice(off, len);
        var stmt = lock();
        try {
            int errcode;
            try {
                errcode = (int) Native.bind_blob.invokeExact(
                    stmt, index, segment, len, Native.TRANSIENT);
                checkBindIndex(errcode, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Binds a parameter to a copy of a byte array.
     * @param index the parameter index; the first parameter has index 1
     * @param value the byte array to copy
     * @throws NullPointerException {@code value} is null
     * @see #bind(int,byte[],int,int)
     */
    public void bind(int index, byte[] value) {
        bind(index, value, 0, value.length);
    }

    /**
     * Binds a parameter to a {@code double} value.
     * @param index the parameter index; the first parameter has index 1
     * @param value the parameter value
     */
    public void bind(int index, double value) {
        var stmt = lock();
        try {
            int errcode;
            try {
                errcode = (int) Native.bind_double.invokeExact(
                    stmt, index, value);
                checkBindIndex(errcode, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Binds a parameter to an {@code long} value.
     * @param index the parameter index; the first parameter has index 1
     * @param value the parameter value
     */
    public void bind(int index, long value) {
        var stmt = lock();
        try {
            int errcode;
            try {
                errcode = (int) Native.bind_int64.invokeExact(
                    stmt, index, value);
                checkBindIndex(errcode, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Binds a parameter to a substring of a {@link String}.
     * The conversion uses the {@linkplain ExtendedCharsets#UTF_8S UTF-8S}
     * character set.
     * @param index the parameter index; the first parameter has index 1
     * @param value the parameter value
     * @param off the first character that should be transfered to the parameter
     * @param len the number of characters that should be transfered
     * @throws NullPointerException {@code value} is null
     * @see #bind(int,String)
     */
    public void bind(int index, String value, int off, int len) {
        var stmt = lock();
        try {
            int errcode;
            try (var arena = Arena.ofConfined()) {
                var valueN = arena.allocateFrom(value,
                    ExtendedCharsets.UTF_8S, off, len);
                errcode = (int) Native.bind_text.invokeExact(
                    stmt, index, valueN, (int) valueN.byteSize(),
                    Native.TRANSIENT);
                checkBindIndex(errcode, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Binds a parameter to a {@link String}.
     * The conversion uses the {@linkplain ExtendedCharsets#UTF_8S UTF-8S}
     * character set.
     * @param index the parameter index; the first parameter has index 1
     * @param value the parameter value
     * @throws NullPointerException {@code value} is null
     * @see #bind(int,String,int,int)
     */
    public void bind(int index, String value) {
        bind(index, value, 0, value.length());
    }

    /**
     * Returns the number of result columns.
     * Even though this value is generally positive, it does not mean
     * that results are available.  For this to be the case, {@link #step()}
     * must have been called, and must have returned true.
     * @return the number of columns
     */
    public int columnCount() {
        return columnCount;
    }

    /**
     * Returns the type of a result column.
     * Type information is only available after {@link #step()} has
     * been called and it returned true.
     * @param index the column index; the first column has index zero
     * @return the column type; {@code NULL} is represented as
     *  {@link ValueType#NULL}, and not a null reference
     * @throws IndexOutOfBoundsException if {@code index} is greater
     *   than {@link #columnCount()}
     * @throws IllegalStateException if no row data is available
     */
    public ValueType columnType(int index) {
        int type;
        var stmt = lock();
        try {
            checkColumnIndex(index);
            type = (int) Native.column_type.invokeExact(stmt, index);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
        return ValueType.fromConstant(type);
    }

    /**
     * Returns the value of a result column as a byte array.
     * @param index the column index; the first column has index zero
     * @return a fresh byte array, or null if the column value is {@code NULL}
     * @throws IndexOutOfBoundsException if {@code index} is greater
     *   than {@link #columnCount()}
     * @throws IllegalStateException if no row data is available
     */
    public byte[] columnBlob(int index) {
        var stmt = lock();
        try {
            MemorySegment blobPtr;
            int bytes;
            try {
                checkColumnIndex(index);
                blobPtr = (MemorySegment) Native.column_blob.invokeExact(
                    stmt, index);
                if (blobPtr.address() == 0) {
                    switch ((int) Native.column_type.invokeExact(stmt, index)) {
                    case Constants.NULL:
                        return null;
                    case Constants.BLOB:
                    case Constants.TEXT:
                        // An empty blob (possibly after conversion) is
                        // reported as a null pointer.
                        return new byte[0];
                    default:
                        db.checkError();
                    }
                    return null; // Not reached.
                }
                bytes = (int) Native.column_bytes.invokeExact(stmt, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
            // Data extraction must be performed under the lock.
            @SuppressWarnings("restricted")
            var blobPtrResized = blobPtr.reinterpret(bytes);
            return blobPtrResized.toArray(ValueLayout.JAVA_BYTE);
        } finally {
            unlock();
        }
    }

    /**
     * Returns the value of a result column as a {@code double}.
     * @param index the column index; the first column has index zero
     * @return the double value; a {@code NULL} column results in {@code 0.0}
     * @throws IndexOutOfBoundsException if {@code index} is greater
     *   than {@link #columnCount()}
     * @throws IllegalStateException if no row data is available
     */
    public double columnDouble(int index) {
        var stmt = lock();
        try {
            try {
                checkColumnIndex(index);
                return (double) Native.column_double.invokeExact(stmt, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Returns the value of a result column as a {@code long}.
     *
     * If the column has a different {@linkplain ValueType type}, conversion
     * occurs, with the follow special cases:
     * <ul>
     * <li>If the value is not recognized as a number, the result is zero.
     * <li>If the value s a number that is out of bounds for {@code long},
     *   {@link Long#MIN_VALUE} or {@link Long#MAX_VALUE} is returned,
     *   depending which bound is exceeded.
     * <li>Trailing characters after numbers are ignored and do not
     *   result in conversion failure.
     * <li>{@code NULL} is represented as zero
     * </ul>
     *
     * <p>The underlying SQLite function is called {@code sqlite3_column_int64}.
     *
     * @param index the column index; the first column has index zero
     * @return the integer value
     * @throws IndexOutOfBoundsException if {@code index} is greater
     *   than {@link #columnCount()}
     * @throws IllegalStateException if no row data is available
     */
    public long columnLong(int index) {
        var stmt = lock();
        try {
            try {
                checkColumnIndex(index);
                return (long) Native.column_int64.invokeExact(stmt, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
        } finally {
            unlock();
        }
    }

    /**
     * Returns the value of a result column as a {@link String}.
     *
     * The conversion uses the {@linkplain ExtendedCharsets#UTF_8S UTF-8S}
     * character set.
     * @param index the column index; the first column has index zero
     * @return the string; a {@code NULL} column results in a null reference
     * @throws IndexOutOfBoundsException if {@code index} is greater
     *   than {@link #columnCount()}
     * @throws IllegalStateException if no row data is available
     */
    public String columnString(int index) {
        var stmt = lock();
        try {
            MemorySegment blobPtr;
            int bytes;
            try {
                checkColumnIndex(index);
                blobPtr = (MemorySegment) Native.column_blob.invokeExact(
                    stmt, index);
                if (blobPtr.address() == 0) {
                    switch ((int) Native.column_type.invokeExact(stmt, index)) {
                    case Constants.NULL:
                        return null;
                    case Constants.BLOB:
                    case Constants.TEXT:
                        // An empty string (possibly after conversion) is
                        // reported as a null pointer.
                        return "";
                    default:
                        db.checkError();
                    }
                    return null; // Not reached.
                }
                bytes = (int) Native.column_bytes.invokeExact(stmt, index);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new SQLiteException(e);
            }
            // Data extraction must be performed under the lock.
            @SuppressWarnings("restricted")
            var blobPtrResized = blobPtr.reinterpret(bytes);
            return blobPtrResized.getString(0, ExtendedCharsets.UTF_8S, bytes);
        } finally {
            unlock();
        }
    }

    /**
     * Returns the SQL statement for this object.
     * Parameters are not expanded.
     * @return the SQL query string
     * @see Database#prepare(String)
     */
    @SuppressWarnings("restricted") // Use a string that came from SQLite.
    public String sql() {
        var stmt = lock();
        try {
            var ret = (MemorySegment) Native.sql.invokeExact(stmt);
            return ret.reinterpret(Long.MAX_VALUE).getString(0);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new SQLiteException(e);
        } finally {
            unlock();
        }
    }

    /**
     * Create the new statement.  The database object must be locked.
     */
    Statement(Database db, ReentrantLock lock, MemorySegment stmt)
        throws Throwable {
        // Exceptions are handled in Database#prepare(String).
        this.db = db;
        state = new State(lock, stmt);
        columnCount = (int) Native.column_count.invokeExact(stmt);
        // Must come last, to prevent double-finalization.
        cleanable = Database.cleaner.register(this, state);
    }

    private static final class State implements Runnable {
	// Lock for the related database object.
	private final ReentrantLock lock;
	// sqlite3_stmt pointer.
        private MemorySegment stmt;

        private State(ReentrantLock lock, MemorySegment stmt) {
	    this.lock = lock;
            this.stmt = stmt;
        }

        public void run() {
            var stmt0 = stmt;
            if (stmt0 == MemorySegment.NULL)
                return;
            stmt = MemorySegment.NULL;
	    // Acquire the database lock.  Access to the statement
	    // object (including finalization) counts as access to the
	    // database object, and we need to prevent concurrent
	    // access.  The lock prevents application access.
	    // We do not have a strong reference to the Database
	    // object that would keep it alive, but this is fine:
	    // Cleaner objects have only a single processing thread,
	    // so Statement and Database cleanup are ordered.
	    lock.lock();
            try {
		// We have to ignore any errors, no way to report
		// errors from this point.
                int ignore = (int) Native.finalize.invokeExact(stmt0);
            } catch (Throwable e) {
            } finally {
		lock.unlock();
	    }
        }
    }

    MemorySegment lockUnchecked() {
        db.lockUnchecked();
        return state.stmt;
    }

    MemorySegment lock() {
        db.lock();
        var stmt = state.stmt;
        if (stmt == MemorySegment.NULL) {
            db.unlock();
            throw new IllegalStateException("Statement is closed");
        }
        return stmt;
    }

    void unlock() {
        Reference.reachabilityFence(this);
        db.unlock();
    }

    private void checkColumnIndex(int index) throws Throwable {
        if (index < haveColumns)
            return;
        else
            checkColumnIndex0(index);
    }

    private void checkColumnIndex0(int index) throws Throwable {
        if (index < columnCount)
            // Index is valid, but there is no data row.
            throw new IllegalStateException(
                "No data row during execution of SQL statement: " + sql());
        else
            throw new IndexOutOfBoundsException(String.format(
                    "SQL statement produces %d columns: %s",
                    columnCount, sql()));
    }

    private void checkBindIndex(int errcode, int index) throws Throwable {
        if (primary(errcode) == Constants.RANGE)
            throw new IndexOutOfBoundsException(String.format(
                    "SQL statement has %d parameters, not %d: %s",
                    columnCount, index, sql()));
        db.check(errcode);
    }

    private final Database db;
    private final State state;
    private final Cleaner.Cleanable cleanable;

    /**
     * Cached column count.
     */
    private final int columnCount;

    /**
     * Indicates whether row data is available.  Set to -1 if there is
     * no data, and to columnCount if data is available, so that a
     * column index can be validated with a single comparison.
     *
     * This extra check is necessary because SQLite documentation
     * states: “If the SQL statement does not currently point to a
     * valid row, or if the column index is out of range, the result
     * is undefined.”
     */
    private int haveColumns;
}
