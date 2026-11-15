package enyo.linux;

import java.lang.foreign.MemorySegment;
import java.util.List;

/**
 * Access to the Linux {@code ioctl} system call.
 *
 * <h2>Safety isssues when using {@code ioctl}</h2>
 *
 * This system call is generally unsafe:
 * <ul>
 * <li>There is no expicit size argument.  The request argument
 *  was originally intended to encode the size of the data read or written
 *  by tehe system call, but in many cases, it does not reflect
 *  what the system call actually does.
 * <li>The passed data may contain pointers to other data that the
 *  system call accesses.
 * <li>The nature of the {@code ioctl} operation could mean that it
 *  destabilizes the current process if used incorrectly.
 * </ul>
 */
public final class Ioctl {
    private Ioctl() {}

    /** Direction of an {@link Ioctl} operation.
     * The direction determines whether the operation
     * writes to the passed data buffer, reads from it, does both, or
     * neither.
     */
    enum Direction {
        /**
         * The operation does not read or write the data buffer.
         * This has no direct correspondence among the Linux UAPI
         * constants.
         */
        NONE(IoctlConstants._IOC_NONE),
        /**
         * The operation reads the data buffer.
         * This corresponds to {@code IOC_OUT}.
         */
        IN(IoctlConstants.IOC_IN),
        /**
         * The operation writes the data buffer.
         * This corresponds to {@code IOC_OUT}.
         */
        OUT(IoctlConstants.IOC_OUT),
        /**
         * The operation reads and writes the data buffer.
         * This corresponds to {@code IOC_INOUT}.
         */
        INOUT(IoctlConstants.IOC_INOUT)
        ;

        private final int value;

        private Direction(int value) {
            this.value = value;
        }

        private static Direction of(int request) {
            // This is not compatible with SPARC.
            int dir = request
                & (IoctlConstants._IOC_DIRMASK << IoctlConstants._IOC_DIRSHIFT);
            if (dir == IoctlConstants._IOC_NONE)
                return NONE;
            if (dir == IoctlConstants.IOC_IN)
                return IN;
            if (dir == IoctlConstants.IOC_OUT)
                return OUT;
            if (dir == IoctlConstants.IOC_INOUT)
                return INOUT;
            throw new IllegalArgumentException(String.format(
                    "Ioctl request 0x%s has unknown direction %d",
                    Integer.toHexString(request),
                    dir >> IoctlConstants._IOC_DIRSHIFT));
        }

        private final void check(int request, MemorySegment buf) {
            if (this == NONE)
                return;
            int size = (request & IoctlConstants.IOCSIZE_MASK)
                >>> IoctlConstants.IOCSIZE_SHIFT;
            if (buf.byteSize() != size)
                throw new IllegalArgumentException(String.format(
                    "Ioctl request 0x%s has size %d, incompatible with %s",
                    Integer.toHexString(request), size, buf));
            if (this == IN)
                // IN transfers data to the kernel.
                return;
            if (buf.isReadOnly())
                throw new UnsupportedOperationException(String.format(
                    "Ioctl request 0x%s requires writable memory segment %s",
                    Integer.toHexString(request), buf));
        }
    }

    /**
     * Performs a partially checked I/O control operation.
     *
     * This method attempts to validate the request against the passed memory
     * segment.  However, validation is incomplete; see {@link Ioctl}.
     *
     * @param fd the file descriptor on which the {@code ioctl} is performed
     * @param request the {@code ioctl} request number
     * @param buf the memory segment with the data for the
     *   {@code ioctl} operation;
     *   can be {@link MemorySegment#NULL} for some request types
     * @return the raw system call return value
     * @throws NullPointerException if {@code buf} is null and the
     *   direction is not {@link Direction#NONE}
     * @throws IllegalArgumentException if the {@linkplain Direction direction}
     *   of the request cannot be determined
     * @throws UnsupportedOperationException if the request requires
     *   a writable buffer, but {@code} is read-only
     * @throws SyscallException if the {@code ioctl} operation failed
     * @see ioctlUnchecked(int,int,MemorySegment)
     */
    public static long ioctl(int fd, int request, MemorySegment buf) {
        Direction.of(request).check(request, buf);
        return ioctlUnchecked(fd, request, buf);
    }

    /**
     * Performs a totally unchecked I/O control operation.
     *
     * This method does not validate the request against the memory segment.
     *
     * @param fd the file descriptor on which the {@code ioctl} is performed
     * @param request the {@code ioctl} request number
     * @param buf the memory segment with the data for the
     *   {@code ioctl} operation;
     *   can be {@link MemorySegment#NULL} for some request types
     * @return the raw system call return value
     * @throws SyscallException if the {@code ioctl} operation failed
     * @see ioctl(int,int,MemorySegment)
     */
    public static long ioctlUnchecked(
        int fd, int request, MemorySegment buf) {
        long result;
        try {
            result = (long) Trampolines.LLM.invokeExact(
                (long) fd, (long) request, buf, SyscallConstants.ioctl);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (SyscallException.isError(result))
            throw new SyscallException(SyscallConstants.ioctl,
                List.of((long) fd, (long) request), List.of(buf), result);
        return result;
    }

    /**
     * Performs a totally unchecked I/O control operation.
     *
     * This method does not validate the value against the request.
     *
     * <p><b>This method must not be used with a raw memory address.</b>
     * use the {@linkplain ioctlUnchecked(int,int,MemorySegment) memory
     * segment version} instead.
     *
     * @param fd the file descriptor on which the {@code ioctl} is performed
     * @param request the {@code ioctl} request number
     * @param value the non-pointer argument for the {@code ioctl} operation
     * @return the raw system call return value
     * @see ioctl(int,int,MemorySegment)
     */
    public static long ioctlUnchecked(
        int fd, int request, int value) {
        long result;
        try {
            result = (long) Trampolines.LLL.invokeExact(
                (long) fd, (long) request, (long) value,
                SyscallConstants.ioctl);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        if (SyscallException.isError(result))
            throw new SyscallException(SyscallConstants.ioctl,
                List.of((long) fd, (long) request, (long) value), List.of(), result);
        return result;
    }

    /**
     * Returns a new {@link SyscallException} after an {@code ioctl} error.
     * @param fd the file descriptor
     * @param request the {@code ioctl} request
     * @param buf the memory segment; can be null
     * @param result the raw system call result
     * @return the exception object
     */
    public static SyscallException newException(
        int fd, int request, MemorySegment buf, long result) {
        return new SyscallException(SyscallConstants.ioctl,
            List.of((long) fd, (long) request), List.of(buf), result);
    }

    /**
     * Returns a new {@link SyscallException} after an {@code ioctl} error.
     * @param fd the file descriptor
     * @param request the {@code ioctl} request
     * @param value the value argument used
     * @param result the raw system call result
     * @return the exception object
     */
    public static SyscallException newException(
        int fd, int request, int value, long result) {
        return new SyscallException(SyscallConstants.ioctl,
            List.of((long) fd, (long) request, (long) value), List.of(),
            result);
    }
}
