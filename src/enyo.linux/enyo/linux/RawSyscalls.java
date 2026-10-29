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
     * @see #closeThrow(int)
     * @throws SyscallException the descriptor is invalid ({@code EBADF})
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
     * @throws SyscallException the read operation failed.
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

    private static void checkWritable(MemorySegment buf) {
        if (buf.isReadOnly())
            throw new IllegalArgumentException(
                "Writable memory segment required: "+ buf);
    }

    private RawSyscalls() {}
}
