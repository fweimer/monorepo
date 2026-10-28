package enyo.sqlite;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.function.BiFunction;

@SuppressWarnings("restricted")
final class Native {
    static {
        var linker = Linker.nativeLinker();
        var arena = Arena.global();
        var lib = SymbolLookup.libraryLookup("libsqlite3.so.0", arena);
        BiFunction<String, FunctionDescriptor, MethodHandle> lookup =
            (name, descr) -> linker.downcallHandle(
                lib.find("sqlite3_" + name).get(), descr);

        // Parameter binding and column access functions do not result
        // in side effects, so mark them as critical.  Likewise for
        // various inspection functions.
        //
        /// Note that we cannot support upcalls from bound values as a
        /// result because binding could trigger upcalls.
        var critical = Linker.Option.critical(false);
        BiFunction<String, FunctionDescriptor, MethodHandle> criticalLookup =
            (name, descr) -> linker.downcallHandle(
                lib.find("sqlite3_" + name).get(), descr, critical);

        // Likewise for invocations with heap access.
        var heap = Linker.Option.critical(true);
        BiFunction<String, FunctionDescriptor, MethodHandle> heapLookup =
            (name, descr) -> linker.downcallHandle(
                lib.find("sqlite3_" + name).get(), descr, heap);

        // A multi-threaded build is required.
        {
            var threadsafe = lookup.apply("threadsafe", FunctionDescriptor.of(
                    ValueLayout.JAVA_INT));
            int mode = 0;
            try {
                mode = (int) threadsafe.invokeExact();
            } catch (Throwable e) {
                // Reported below.
            }
            if (mode == 0)
                throw new SQLiteException(
                    "Installed SQLite library lacks threading support");
        }

        var descrIntDb = FunctionDescriptor.of(
            ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
        var descrIntDbInt = FunctionDescriptor.of(
            ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
        var descrPtrDbInt = FunctionDescriptor.of(
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

        // Assign in declaration (alphabetical) order.
        bind_blob = heapLookup.apply("bind_blob",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS));
        bind_double = criticalLookup.apply("bind_double",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.JAVA_DOUBLE));
        bind_int64 = criticalLookup.apply("bind_int64",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG));
        bind_parameter_count = criticalLookup.apply(
	    "bind_parameter_count", descrIntDb);
        bind_text = criticalLookup.apply("bind_text",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS));
	busy_timeout = lookup.apply("busy_timeout", descrIntDbInt);
        close_v2 = lookup.apply("close_v2", descrIntDb);
        column_blob = criticalLookup.apply("column_blob", descrPtrDbInt);
        column_bytes = criticalLookup.apply("column_bytes", descrIntDbInt);
        column_count = criticalLookup.apply("column_count", descrIntDb);
        column_double = criticalLookup.apply("column_double",
            FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        column_int64 = criticalLookup.apply("column_int64",
            FunctionDescriptor.of(ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        column_type = criticalLookup.apply("column_type", descrIntDbInt);
        db_name = criticalLookup.apply("db_name", descrPtrDbInt);
        db_readonly = criticalLookup.apply("db_readonly",
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS));
        errmsg = criticalLookup.apply("errmsg", FunctionDescriptor.of(
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        extended_errcode = criticalLookup.apply("extended_errcode",
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        finalize = lookup.apply("finalize", descrIntDb);
        open_v2 = lookup.apply("open_v2", FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS));
        prepare_v2 = lookup.apply("prepare_v2", FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS));
	// sqlite3_reset perfors rollback etc., which can involve
	// significant I/O, so it should not be critical.
        reset = lookup.apply("reset", descrIntDb);
        sql = criticalLookup.apply("sql", FunctionDescriptor.of(
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        step = lookup.apply("step", descrIntDb);
    }

    // Keep in alphabetical order.
    static final MethodHandle bind_blob;
    static final MethodHandle bind_double;
    static final MethodHandle bind_int64;
    static final MethodHandle bind_parameter_count;
    static final MethodHandle bind_text;
    static final MethodHandle busy_timeout;
    static final MethodHandle close_v2;
    static final MethodHandle column_blob;
    static final MethodHandle column_bytes;
    static final MethodHandle column_count;
    static final MethodHandle column_double;
    static final MethodHandle column_int64;
    static final MethodHandle column_type;
    static final MethodHandle db_name;
    static final MethodHandle db_readonly;
    static final MethodHandle errmsg;
    static final MethodHandle extended_errcode;
    static final MethodHandle finalize;
    static final MethodHandle open_v2;
    static final MethodHandle prepare_v2;
    static final MethodHandle reset;
    static final MethodHandle sql;
    static final MethodHandle step;

    static final MemorySegment TRANSIENT = MemorySegment.ofAddress(-1);

    private Native() {} // No instances.
}
