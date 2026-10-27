package enyo.sqlite;

final class Constants {
    // Primary result codes.
    final static int OK = 0;
    final static int ERROR      =  1;  // Generic error
    final static int INTERNAL   =  2;  // Internal logic error in SQLite
    final static int PERM       =  3;  // Access permission denied
    final static int ABORT      =  4;  // Callback routine requested an abort
    final static int BUSY       =  5;  // The database file is locked
    final static int LOCKED     =  6;  // A table in the database is locked
    final static int NOMEM      =  7;  // A malloc() failed
    final static int READONLY   =  8;  // Attempt to write a readonly database
    final static int INTERRUPT  =  9;  // Terminated by sqlite3_interrupt()
    final static int IOERR      = 10;  // Some kind of disk I/O error occurred
    final static int CORRUPT    = 11;  // The database disk image is malformed
    final static int NOTFOUND   = 12;  // Unknown op in sqlite3_file_control()
    final static int FULL       = 13;  // Insertion failed bc/database full
    final static int CANTOPEN   = 14;  // Unable to open the database file
    final static int PROTOCOL   = 15;  // Database lock protocol error
    final static int EMPTY      = 16;  // No content
    final static int SCHEMA     = 17;  // The database schema changed
    final static int TOOBIG     = 18;  // String or BLOB exceeds size limit
    final static int CONSTRAINT = 19;  // Abort due to constraint violation
    final static int MISMATCH   = 20;  // Data type mismatch
    final static int MISUSE     = 21;  // Library used incorrectly
    final static int NOLFS      = 22;  // Uses OS features not supported on host
    final static int AUTH       = 23;  // Authorization denied
    final static int FORMAT     = 24;  // Not used
    final static int RANGE      = 25;  // 2nd sqlite3_bind parameter invalid
    final static int NOTADB     = 26;  // File opened that is not a database
    final static int NOTICE     = 27;  // Notifications from sqlite3_log()
    final static int WARNING    = 28;  // Warnings from sqlite3_log()
    final static int ROW        = 100; // sqlite3_step() has another row ready
    final static int DONE       = 101; // sqlite3_step() has finished executing

    // Type flags
    final static int INTEGER = 1;
    final static int FLOAT = 2;
    final static int TEXT = 3;
    final static int BLOB = 4;
    final static int NULL = 5;

    // Open flags.  Useable with sqlite3_open_v2 unless otherwise noted.
    // See OpenFlag in case this is updated.
    final static int OPEN_READONLY      = 0x00000001;
    final static int OPEN_READWRITE     = 0x00000002;
    final static int OPEN_CREATE        = 0x00000004;
    final static int OPEN_DELETEONCLOSE = 0x00000008;  // VFS only
    final static int OPEN_EXCLUSIVE     = 0x00000010;  // VFS only
    final static int OPEN_AUTOPROXY     = 0x00000020;  // VFS only
    final static int OPEN_URI           = 0x00000040;
    final static int OPEN_MEMORY        = 0x00000080;
    final static int OPEN_MAIN_DB       = 0x00000100;  // VFS only
    final static int OPEN_TEMP_DB       = 0x00000200;  // VFS only
    final static int OPEN_TRANSIENT_DB  = 0x00000400;  // VFS only
    final static int OPEN_MAIN_JOURNAL  = 0x00000800;  // VFS only
    final static int OPEN_TEMP_JOURNAL  = 0x00001000;  // VFS only
    final static int OPEN_SUBJOURNAL    = 0x00002000;  // VFS only
    final static int OPEN_SUPER_JOURNAL = 0x00004000;  // VFS only
    final static int OPEN_NOMUTEX       = 0x00008000;
    final static int OPEN_FULLMUTEX     = 0x00010000;
    final static int OPEN_SHAREDCACHE   = 0x00020000;
    final static int OPEN_PRIVATECACHE  = 0x00040000;
    final static int OPEN_WAL           = 0x00080000;  // VFS only
    final static int OPEN_NOFOLLOW      = 0x01000000;
    final static int OPEN_EXRESCODE     = 0x02000000;  // Extended result codes
}
