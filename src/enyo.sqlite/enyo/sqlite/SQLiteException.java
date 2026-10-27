package enyo.sqlite;

import java.lang.foreign.MemorySegment;

/**
 * An error during a SQLite operation.
 */
public final class SQLiteException extends RuntimeException {
    SQLiteException(Throwable cause) {
        super(cause);
        this.errcode = 0;
    }

    SQLiteException(String message) {
        super(message);
        this.errcode = 0;
    }

    /**
     * Returns the SQLite error code.
     * This can be zero if the error was a higher-level error detected
     * by the wrapper
     * @return the error code, if available
     */
    public int errorCode() {
        return errcode;
    }

    @SuppressWarnings("restricted")
    SQLiteException(MemorySegment database, int errcode) {
        String msg;
        int extendedErrcode;
        try {
            msg = ((MemorySegment) Native.errmsg.invokeExact(database))
                .reinterpret(Long.MAX_VALUE).getString(0);
        } catch (Throwable e) {
            // This is supposed to be unreachable.
            msg = "Unknown error";
        }
        this(errcode != 0 ? String.format("%s [%d]", msg, errcode) : msg,
            errcode);
    }

    private SQLiteException(String message, int errcode) {
        super(message);
        this.errcode = errcode;
    }

    /**
     * The extended SQLite error code.
     */
    private final int errcode;

    private static final long serialVersionUID = 0xe610406d2dcfccbaL;
}
