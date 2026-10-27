package enyo.sqlite;

/**
 * SQLite database creation flags.
 *
 * The documentation of these flags was downloaded from
 * <a href="https://www.sqlite.org/c3ref/open.html">sqlite.org</a>
 * on 2026-10-26, and is included here based on SQLite's copyright
 * disclaimer.
 *
 * @see Database#of(String,OpenFlag,OpenFlag[])
 */
public enum OpenFlag {
    /**
     * The database is opened in read-only mode. If the database
     * does not already exist, an error is returned.
     */
    READONLY(Constants.OPEN_READONLY),

    /**
     * The database is opened for reading and writing if possible, or
     * reading only if the file is write protected by the operating
     * system. In either case the database must already exist,
     * otherwise an error is returned. For historical reasons, if
     * opening in read-write mode fails due to OS-level permissions,
     * an attempt is made to open it in read-only
     * mode. {@link Database#readOnly(String) db.readOnly(db.databaseName(0))}
     * can be used to determine whether
     * the database is actually read-write.
     */
    READWRITE(Constants.OPEN_READWRITE),

    /**
     * Specified in addition to {@link #READWRITE}.  The database is
     * opened for reading and writing, and is created if it does not
     * already exist.
     */
    CREATE(Constants.OPEN_CREATE),

    /**
     * The filename can be interpreted as a URI if this flag is set.
     */
    URI(Constants.OPEN_URI),

    // MEMORY is omitted because we use Database#inMemory() instead.
    // Shared-cache mode is discouraged, so the name does not provide

    // NOMUTEX, FULLMUTEX not included.  We unconditionally use NOMUTEX
    // mode.

    /**
     * The database filename is not allowed to contain a symbolic link.
     */
    NOFOLLOW(Constants.OPEN_NOFOLLOW),

    // EXRESCODE is not included because it used by default.
    ;

    private OpenFlag(int flag) {
        this.flag = flag;
    }

    final int flag;

    static int convert(OpenFlag start, OpenFlag[] flags) {
        int result = start.flag;
        for (OpenFlag flag : flags)
            result |= flag.flag;
        return result;
    }
}
