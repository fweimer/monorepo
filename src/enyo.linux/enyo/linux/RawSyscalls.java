package enyo.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;

/**
 * Raw system call wrappers.
 *
 * For system call wrappers that use higher-level types, see
 * {@link Syscalls}.  System calls that do not use unboxed descriptors
 * are only included in that class.
 *
 * <p>The methods in this class still perform error checking and throw
 * {@link SyscallException} as appropriate.
 *
 * @see Syscalls
 */
public final class RawSyscalls {
    /**
     * Closes the specified descriptor.
     *
     * <p><b>Warning:</b> Even if {@code closeThrow} throws an exception,
     * the descriptor still has been closed, and should not be closed again.
     *
     * @param fd the descriptor to close
     * @throws SyscallException the descriptor was closed, but an error
     *   was encountered during the operation
     * @see #close(int)
     */
    public static void closeThrow(int fd) {
        long result = close(fd);
        if (result != 0)
            throw new SyscallException(SyscallConstants.close,
                List.of((long) fd), List.of(), result);
    }

    /**
     * Closes the specified descriptor.
     *
     * This variant does not throw an exception if the underlying
     * system call fails, except when the descriptor is invalid.  The
     * descriptor is closed even if the return value is not zero.
     *
     * @param fd the descriptor to close
     * @return the (negative) error code or zero
     * @throws SyscallException the descriptor is invalid ({@code EBADF})
     * @see #closeThrow(int)
     * @see #shutdown(int,int)
     */
    public static long close(int fd) {
        long result;
        try {
            result = (long) Trampolines.L.invokeExact(
                (long) fd, SyscallConstants.close);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (result == -Errno.EBADF)
            throw new SyscallException(SyscallConstants.close,
                List.of((long) fd), List.of(), result);
        return result;
    }

    /**
     * Duplicate the descriptor.
     * @param fd the file descriptor to duplicate
     * @return the new descriptor, at least value 3
     * @throws SyscallException the descriptor could not be created
     */
    public static int dup(int fd) {
        long result;
        try {
            result = (long) Trampolines.LLL.invokeExact(
                (long) fd, (long) Fcntl.F_DUPFD_CLOEXEC, 3L,
                SyscallConstants.fcntl);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (result < 0)
            throw new SyscallException(SyscallConstants.fcntl,
                List.of((long) fd, (long) Fcntl.F_DUPFD_CLOEXEC, 3L),
                List.of(), result);
        return (int) result;
    }

    /**
     * Returns the socket address of the remote end of the socket.
     *
     * Note that if the memory segment {@code address} is not large enough,
     * the address is truncated.
     * This can be detected by inspecting {@code len}.
     *
     * @param fd the socket file descriptor
     * @param address a memory segment where the socket address data is written
     * @param len a memory segment with {@link ValueLayout#JAVA_INT} layout
     *   where the size of the socket address will be written
     * @throws NullPointerException any of the memory segments is null
     * @throws SyscallException the operation fails, for example if {@code fd}
     *   is not a socket.
     */
    public static void getpeername(
        int fd, MemorySegment address, MemorySegment len) {
        getpeersockname(SyscallConstants.getpeername, fd, address, len);
    }

    /**
     * Returns the socket address of the local end of the socket.
     *
     * Note that if the memory segment {@code address} is not large enough,
     * the address is truncated.
     * This can be detected by inspecting {@code len}.
     *
     * @param fd the socket file descriptor
     * @param address a memory segment where the socket address data is written
     * @param len a memory segment with {@link ValueLayout#JAVA_INT} layout
     *   where the size of the socket address will be written
     * @throws NullPointerException any of the memory segments is null
     * @throws SyscallException the operation fails, for example if {@code fd}
     *   is not a socket.
     */
    public static void getsockname(
        int fd, MemorySegment address, MemorySegment len) {
        getpeersockname(SyscallConstants.getsockname, fd, address, len);
    }

    /**
     * Opens a file.
     * @param fd the directory against a relative {@code path} argument
     *   is resolved, or {@code Fcntl.AT_FDCWD} for the current directory
     * @param path the null-terminated pathname of the file to open
     * @param flags the open flags
     * @param mode the open mode, typically 0600; only used if
     *   {@link Fcntl#O_CREAT} is among the open flags
     * @return the file descriptor
     * @throws NullPointerException if {@code path} is null
     * @throws IllegalArgumentException if {@code path} is not NUl-terminated
     */
    public static int openat(int fd, MemorySegment path, int flags, int mode) {
        long result;
        checkNulTerminated(path);
        try {
            result = (long) Trampolines.LMLL.invokeExact(
                (long) fd, path, (long) flags, (long) mode,
                SyscallConstants.openat);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (result < 0)
            throw new SyscallException(SyscallConstants.openat,
                List.of((long) fd, (long) flags, (long) mode),
                List.of(path), result);
        return (int) result;
    }

    /**
     * Creates two pipe descriptors.
     * The descriptors have {@code O_CLOEXEC} set.
     * @return a two-element array containing file descriptors
     * @throws SyscallException the pipe cannot be created
     */
    public static int[] pipe() {
        return pipe2(0);
    }

    /**
     * Creates two pipe descriptors.
     *
     * The descriptors have {@code O_CLOEXEC} set in addition the the
     * specified flags.
     *
     * @param flags the flags to apply during pipe creation
     * @return a two-element array containing file descriptors
     * @throws SyscallException the pipe cannot be created
     */
    public static int[] pipe2(int flags) {
        flags |= Fcntl.O_CLOEXEC;
        var pipe = new int[2]; // Allocate early, to avoid local closing.
        long result;
        try (var arena = Arena.ofConfined()) {
            var fds = arena.allocate(ValueLayout.JAVA_INT, 2);
            result = (long) Trampolines.ML.invokeExact(
                fds, (long) flags, SyscallConstants.pipe2);
            // Copy out the result.  (Might be zero on error.)
            pipe[0] = fds.get(ValueLayout.JAVA_INT, 0);
            pipe[1] = fds.get(ValueLayout.JAVA_INT, 4);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (SyscallException.isError(result))
            throw new SyscallException(SyscallConstants.pipe2,
                List.of((long) flags), List.of(), result);
        return pipe;
    }

    /**
     * Reads bytes into a memory segument.
     *
     * @param fd the descriptor to read from
     * @param buf the memory segment into which the bytes read
     * @return the number of bytes read
     * @throws IllegalArgumentException {@code buf} is not writable
     * @throws SyscallException the read operation failed
     */
    public static long read(int fd, MemorySegment buf) {
        checkWritable(buf);
        return syscallFdBuf(SyscallConstants.read, fd, buf);
    }

    /**
     * Relinquishs the CPU.
     * This pauses the execution of the current thread.
     * @see Thread#yield()
     */
    public static void sched_yield() {
        try {
            long _ = (long) Trampolines.NONE.invokeExact(
                SyscallConstants.sched_yield);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    /**
     * Shuts down the file descriptor.
     *
     * The {@code how} argument is interpreted as follows.
     * <ul>
     * <li>The value 0 ({@code SHUT_RD}) indicates that there will
     *   be no further reads
     * <li>The value 1 ({@code SHUT_WR}) indicates that there will
     *   be no further writes
     * <li>The value 2 ({@code SHUT_RDWR}) indicates that there will
     *   be no further reads or writes, but the descriptor remains open
     * </ul>
     *
     * The shutdown operation is not support for all descriptors.
     *
     * @param fd the descriptor to shut down
     * @param how the type of shutdown to perform, as described above
     * @throws SyscallException the shutdown operation failed
     * @see #close(int)
     */
    public static void shutdown(int fd, int how) {
        long result;
        try {
            result = (long) Trampolines.LL.invokeExact(
                (long) fd, (long) how, SyscallConstants.shutdown);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (result != 0)
            throw new SyscallException(SyscallConstants.shutdown,
                List.of((long) fd, (long) how), List.of(), result);
    }

    /**
     * Writes bytes in a memory segument.
     *
     * @param fd the descriptor to write to
     * @param buf the memory segment whose bytes are written
     * @return the number of bytes written
     * @throws SyscallException the write operation failed.
     */
    public static long write(int fd, MemorySegment buf) {
        return syscallFdBuf(SyscallConstants.write, fd, buf);
    }

    private static long syscallFdBuf(int syscall, int fd, MemorySegment buf) {
        long result;
        try {
            result = (long) Trampolines.LML.invokeExact(
                (long) fd, buf, buf.byteSize(), syscall);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (SyscallException.isError(result))
            throw new SyscallException(
                syscall, List.of((long) fd), List.of(buf), result);
        return result;
    }

    private static void getpeersockname(int syscall, int fd,
        MemorySegment address, MemorySegment len) {
        checkWritable(address);
        long size = Long.min(address.byteSize(), Integer.MAX_VALUE);
        len.set(ValueLayout.JAVA_INT, 0, (int) size);
        long result;
        try {
            result = (long) Trampolines.LMM.invokeExact(
                (long) fd, address, len, syscall);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (SyscallException.isError(result))
            throw new SyscallException(syscall,
                List.of((long) fd, size), List.of(address, len), result);
    }

    private static void checkWritable(MemorySegment buf) {
        if (buf.isReadOnly())
            throw new IllegalArgumentException(
                "Writable memory segment required: "+ buf);
    }

    private static void checkNulTerminated(MemorySegment buf) {
        long size = buf.byteSize();
        for (long i = 0; i < size; ++i)
            if (buf.get(ValueLayout.JAVA_BYTE, i) == 0)
                return;
        throw new IllegalArgumentException(
            "Memory segment is not NUL-terminated: "+ buf);
    }

    private RawSyscalls() {}
}
