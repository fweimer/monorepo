package enyo.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A file descriptor.
 */
public final class Descriptor implements AutoCloseable {
    /**
     * Creates a file descriptor object from an integer.
     *
     * The original descriptor is associated with the object, it is not
     * duplicated.
     *
     * If a descriptor 0, 1, 2, is passed, a shared descriptor object
     * is returned, which does not obtain ownership.  This prevents
     * accidental closing of these standard descriptors.
     *
     * @param fd the descriptor to wrap
     * @return a new descriptor which has ownership of the descriptor
     *   {@code fd}, or the descriptors {@link #stdin()}, {@link #stdout()},
     *   {@link #stderr()} (for raw descriptors 0, 1, 2)
     * @see #dup(int)
     */
    public static Descriptor of(int fd) {
        switch (fd) {
            case 0: return STDIN;
            case 1: return STDOUT;
            case 2: return STDERR;
            default:
                if (fd < 0)
                    throw new IllegalArgumentException(
                        "Invalid descriptor: " + fd);
                return new Descriptor(fd);
        }
    }

    /**
     * Creates a duplicated descriptor.
     *
     * @param fd the descriptor to duplicate
     * @return a new descriptor for a duplicate of {@code fd}
     *   (which has {@code O_CLOEXEC} set)
     * @see #of(int)
     */
    public static Descriptor dup(int fd) {
        int fd0 = RawSyscalls.dup(fd);
        try {
            return of(fd0);
        } catch (Throwable e) {
            RawSyscalls.close(fd0);
            throw e;
        }
    }

    /**
     * Opens the specified file.
     *
     * @param path the file to open
     * @param flag the first flag, typically {@link OpenFlag#O_RDONLY},
     *   {@link OpenFlag#O_WRONLY}, or {@link OpenFlag#O_RDWR}.
     * @param flags additional open flags, including
     *   {@link OpenFlag#O_CREAT(int)} to enable file creation
     * @return a new file descriptor (which has {@code O_CLOEXEC} set)
     * @throws NullPointerException if {@code path} is null
     * @throws SyscallException if the file cannot be opened
     * @see #openat(Path,OpenFlag,OpenOrCreateFlag[])
     */
    public static Descriptor open(Path path, OpenFlag flag,
        OpenOrCreateFlag... flags) {
        return openat0(Fcntl.AT_FDCWD, path, flag, flags);
    }

    /**
     * Opens the specified file, relative to a specified directory.
     *
     * If {@code path} is not absolute, it is interpreted relative
     * to this descriptor's directory.
     *
     * @param path the file to open
     * @param flag the first flag, typically {@link OpenFlag#O_RDONLY},
     *   {@link OpenFlag#O_WRONLY}, or {@link OpenFlag#O_RDWR}.
     * @param flags additional open flags, including
     *   {@link OpenFlag#O_CREAT(int)} to enable file creation
     * @return a new file descriptor (which has {@code O_CLOEXEC} set)
     * @throws NullPointerException if {@code path} is null
     * @throws IllegalStateException if this descriptor is closed
     * @throws SyscallException if the file cannot be opened
     * @see #open(Path,OpenFlag,OpenOrCreateFlag[])
     */
    public Descriptor openat(Path path, OpenFlag flag,
        OpenOrCreateFlag... flags) {
        int dirfd = lock();
        try {
            return openat0(dirfd, path, flag, flags);
        } finally {
            unlock();
        }
    }

    private static Descriptor openat0(int dirfd, Path path, OpenFlag flag,
        OpenOrCreateFlag... flags) {
        int flagsRaw = Fcntl.O_CLOEXEC | flag.value();
        int mode = flag.mode();
        for (var flag0 : flags) {
            flagsRaw |= flag0.value();
            var m = flag0.mode();
            if (m >= 0)
                mode = m;
        }
        int fd;
        try (var arena = Arena.ofConfined()) {
            var path0 = arena.allocateFrom(path.toString());
            fd = RawSyscalls.openat(dirfd, path0, flagsRaw, mode);
        }
        if (fd <= 2)
            // One of the 0, 1, 2 were closed. Descriptor#of(int) does
            // not obtain ownership of such descriptors.  Create a
            // duplicate, so that the return descriptor object has
            // true ownership.
            try {
                return Descriptor.dup(fd);
            } finally {
                // This could theoretically throw (but likely only
                // after file descriptor corruption leading to an
                // incorrect close(), so that we get EBADF here).  A
                // successful dup() has a cleanable, so this will not
                // result in a permanent leak.
                RawSyscalls.close(fd);
            }
        else
            try {
                return Descriptor.of(fd);
            } catch (Throwable e) {
                RawSyscalls.close(fd);
                throw e;
            }
    }

    /**
     * Closes the descriptor.
     *
     * After closing a descriptor, further operations on it result in an
     * {@link IllegalStateException} exception being thrown.
     *
     * The close request may not always result in the underlying descriptor
     * being closed:
     * <ul>
     * <li>If there is a concurrent operation on this descriptor
     *   from another thread, the closing is deferred until after
     *   this operation stops.  A best-effort attempt is made to
     *   cancel the operation, but this depends on the type of
     *   descriptor.  For example, if the descriptor refers to a
     *   regular file, operations cannot be canceled.
     * <li>Standard descriptors ({@link #stdin()}, {@link #stdout()},
     *   {@link #stderr()}) are not closed and the request is ignored.
     * <li>The descriptor is already closed.  This is not treated as an error.
     * </ul>
     * @throws SyscallException the kernel reported an error during the
     *   close operation; note that the descriptor is still closed
     */
    @Override
    public void close() {
        long oldValue;
        long newValue;
        do {
            oldValue = state.fd.getAcquire();
            // Check covers -1 as well.
            if ((oldValue & State.CLOSING_MASK) != 0)
                // Closing a closed descriptor is not an error.
                return;
            if ((oldValue & State.FD_MASK) <= 2)
                // Standard descriptors cannot be closed.
                return;
            // Increment the reference counter, so that a last
            // concurrent unlock() (that would do the actual close())
            // does not race with the shutdown() call below.
            newValue = (oldValue + (1L << 32)) | State.CLOSING_MASK;
            if (newValue < 0)
                throw new InternalError(
                    "Invalid descriptor state: " + oldValue);
        } while (!state.fd.compareAndSet(oldValue, newValue));
        // Mask out CLOSING_MASK.
        int fd = (int) newValue & State.FD_MASK;
        if ((newValue >> 31) == 0b11)
            // Close request posted.  No other thread can perform
            // another system call on this descriptor (see lock() and
            // its CLOSING_MASK check).  The reference count is one
            // (incremented by us), so we can close directly.
            closeAsOwner(fd);
        else {
            // We cannot perform an immediate close.  Shutting down
            // a socket may interrupt pending operations.
            try {
                RawSyscalls.shutdown(fd, 2); // Use SHUT_RDWR.
            } catch (SyscallException e) {
            }
            // Do not use dup2 to install a new descriptor.  There
            // is only a tiny race window (between the state check
            // and the system call) where this would be helpful.
            // We have to live with the other thread blocking
            // on the descriptor.

            // Undo the reference counter increment.  This may trigger
            // the actual close() late, or it could be deferred to
            // another thread.
            unlock();
        }
    }

    /**
     * Reads data into a slice of the array.
     *
     * This method writes at most {@code len} bytes into {@code buf}
     * starting at {@code off}.  Reading may stop prematurely for various
     * reasons, not just an end of file condition.
     *
     * @param buf the byte array into which the data is read
     * @param off index of the byte in {@code buf} which will be overwritten
     * @param len the maximum number of bytes to read
     * @return the number of bytes actually read; 0 for end of file
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException if {@code off} or {@code len}
     *   are out of bounds for {@code buf}
     * @throws SyscallException if the read operation fails
     */
    public int read(byte[] buf, int off, int len) {
        Objects.checkFromIndexSize(off, len, buf.length);
        try (var arena = Arena.ofConfined()) {
            var seg = arena.allocate(len);
            long result = read(seg);
            MemorySegment.copy(
                seg, 0, MemorySegment.ofArray(buf), off, result);
            return (int) result;
        }
    }

    /**
     * Reads data into the array.
     *
     * This method attempts to fill the array {@code buf} with data
     * read from the descriptor.  Reading may stop prematurely for various
     * reasons, not just an end of file condition.
     *
     * @param buf the byte array into which the data is read
     * @return the number of bytes actually read; 0 for end of file
     * @throws NullPointerException if {@code buf} is null
     * @throws SyscallException if the read operation fails
     */
    public int read(byte[] buf) {
        return read(buf, 0, buf.length);
    }

    /**
     * Reads data into the active region of a byte buffer.
     *
     * This method reads from this descriptor at most
     * {@link ByteBuffer#remaining() buf.remaining()}
     * bytes into {@code buf} starting at
     * {@link ByteBuffer#position() buf.position()}
     * (until {@link ByteBuffer#limit() buf.limit()} is reached).  Reading may
     * stop prematurely for various reasons, not just an end of file
     * condition.
     *
     * @param buf the byte buffer into which data is read
     * @return the number of bytes actually read; 0 for end of file
     * @throws NullPointerException if {@code buf} is null
     * @throws SyscallException if the read operation fails
     * @see ByteBuffer#position()
     * @see ByteBuffer#limit()
     * @see ByteBuffer#remaining()
     */
    public int read(ByteBuffer buf) {
        if (buf.isDirect())
            return (int) read(MemorySegment.ofBuffer(buf));
        else
            return read(buf.array(), buf.arrayOffset() + buf.position(),
                buf.remaining());
    }

    /**
     * Reads data into a memory segment.
     *
     * This method reads from this descriptor at most
     * {@link MemorySegment#byteSize() buf.byteSize()}
     * bytes into {@code buf} (the entire segment).  Reading may
     * stop prematurely for various reasons, not just an end of file
     * condition.  Neither position nor limit are updated by this method.
     *
     * @param buf the memory segment into which data is read
     * @return the number of bytes actually read; 0 for end of file
     * @throws NullPointerException if {@code buf} is null
     * @throws UnsupportedOperationException if the memory segment is read-only
     * @throws SyscallException if the read operation fails
     */
    public long read(MemorySegment buf) {
        int fd = lock();
        try {
            return RawSyscalls.read(fd, buf);
        } finally {
            unlock();
        }
    }

    /**
     * Writes bytes from the array to this descriptor.
     *
     * This method writes at most {@code len} bytes to this descriptor,
     * starting at index {@code off} in {@code buf}.
     *
     * <p><b>Note:</b> Unlike other platform interfaces, short writes
     * may occur.
     * @param buf the byte array whose contents is written
     * @param off index of the byte in {@code buf} which will be written
     * @param len the maximum number of bytes to write
     * @return the number of bytes written, which may be less than {@code len}
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException {@code off} or {@code len}
     *   are out of bounds for {@code buf}
     * @throws SyscallException if the write operation fails
     */
    public int write(byte[] buf, int off, int len) {
        Objects.checkFromIndexSize(off, len, buf.length);
        try (var arena = Arena.ofConfined()) {
            var seg = arena.allocate(len);
            MemorySegment.copy(
                MemorySegment.ofArray(buf).asSlice(off, len), 0, seg, 0, len);
            return (int) write(seg);
        }
    }

    /**
     * Writes bytes from the array to this descriptor.
     *
     * This method tries to write all bytes in the array to the descriptor
     *
     * <p><b>Note:</b> Unlike other platform interfaces, short writes
     * may occur.
     * @param buf the byte array whose contents is written
     * @return the number of bytes written, which may be less than
     *  {@code buf.length}
     * @throws NullPointerException if {@code buf} is null
     * @throws SyscallException if the write operation fails
     */
    public int write(byte[] buf) {
        return write(buf, 0, buf.length);
    }


    /**
     * Writes data in the active region of a byte buffer.
     *
     * This method writes to this descriptor at most
     * {@link ByteBuffer#remaining() buf.remaining()}
     * bytes from {@code buf} starting at
     * {@link ByteBuffer#position() buf.position()}
     * (until {@link ByteBuffer#limit() buf.limit()} is reached).  Writing may
     * stop prematurely for various reasons, not just the end of the
     * active buffer region.
     * Neither position mor limit are updated by this method
     *
     * <p><b>Note:</b> Unlike other platform interfaces, short writes
     * may occur.
     * @param buf the byte buffer whose active region is written
     * @return the number of bytes written, which may be less than
     *   {@code buf.remaining()}
     * @throws NullPointerException {@code buf} is null
     * @throws SyscallException if the write operation fails
     * @see ByteBuffer#position()
     * @see ByteBuffer#limit()
     * @see ByteBuffer#remaining()
     */
    public int write(ByteBuffer buf) {
        if (buf.isDirect())
            return (int) write(MemorySegment.ofBuffer(buf));
        else
            return write(buf.array(), buf.arrayOffset() + buf.position(),
                buf.remaining());
    }

    /**
     * Writes data in a memory segment.
     *
     * This method writes to this descriptor at most
     * {@link MemorySegment#byteSize() buf.byteSize()}
     * bytes from {@code buf} (the entire segment).
     * Writing may stop prematurely for various reasons, not just the
     * end of the memory segment.
     *
     * <p><b>Note:</b> Unlike other platform interfaces, short writes
     * may occur.
     *
     * @param buf the memory segment whose bytes are written
     * @return the number of bytes written, which may be less than
     *   {@code buf.byteSize()}
     * @throws NullPointerException {@code buf} is null
     * @throws SyscallException if the write operation fails
     */
    public long write(MemorySegment buf) {
        int fd = lock();
        try {
            return RawSyscalls.write(fd, buf);
        } finally {
            unlock();
        }
    }

    /**
     * Returns a closed descriptor.
     * @return a closed descriptor
     */
    public static Descriptor closed() {
        return CLOSED;
    }

    /**
     * Returns the descriptor for standard input.
     * @return a descriptor, usually intended for reading
     */
    public static Descriptor stdin() {
        return STDIN;
    }

    /**
     * Returns the descriptor for standard output.
     * @return a descriptor, usually intended for writing
     */
    public static Descriptor stdout() {
        return STDOUT;
    }

    /**
     * Returns the descriptor for standard error.
     * @return a descriptor, usually intended for writing
     */
    public static Descriptor stderr() {
        return STDERR;
    }

    @Override
    public String toString() {
        long fd = state.fd.get();
        if (fd < 0)
            return "Descriptor()";
        else
            return "Descriptor(" + (fd & State.FD_MASK) + ")";
    }

    /**
     * Returns the raw file descriptor.
     *
     * Note that this object retains ownership of the raw descriptor,
     * and it may be closed concurrently.  The caller must not close
     * the returned descriptor because this object will close it eventually
     * (or it is one of the standard descriptors that must not be closed).
     *
     * @return the raw file descriptor, or -1 if the descriptor is closed
     */
    public int raw() {
        long fd = state.fd.get();
        if (fd < 0)
            return -1;
        return (int) fd & State.FD_MASK;
    }

    // Closing based on garbage collection.  The implementation assumes
    // that garbage collection does not happen when the reference count
    // is positive.  Reachability fences must be used to ensure this.
    private static final class State implements Runnable {
        // Encoding of the descriptor and state in the fd value:
        //
        // -1 means the descriptor is closed.  This is the terminal state,
        // no further updates.
        //
        // Bits 0 to 30 encode the actual file descriptor.
        final static int FD_MASK = 0x7fffffff;
        //
        // Bit 31 means that there is a pending close() request.
        // The close request is only acted upon by the thread that observes
        // reference count 1 after the increment in close(), or the
        // thread that observes count 0 after the decrement in unlock().
        // Only one of them performs the raw close.
        final static long CLOSING_MASK = 1L << 31;
        // Bits 32 to 62 are a reference counter of concurrently
        // running operations.  0 means that the descriptor is not busy.
        // This bit field is incremented by lock() and decremented by unlock().
        // Bit 63 is used as an overflow indicator,
        // and as part of the -1 sentinel value.
        final AtomicLong fd;

        private State(int fd) {
            this.fd = new AtomicLong(fd);
        }

        @Override
        public void run() {
            long fd0 = fd.get();
            if (fd0 == -1)
                return;
            // The descriptor only becomes eligible for collection
            // if there are no pending operations.
            if ((fd0 >>> 31) != 0)
                // This exception is not invisible on the cleaner
                // thread, but could be detected with a service agent.
                throw new InternalError("Invalid descriptor state: " + fd0);
            fd.set(-1);
            RawSyscalls.close((int) fd0);
        }
    }

    private Descriptor(int fd) {
        state = new State(fd);
        // The standard descriptors are never closed and therefore do
        // not need cleaner registration.
        if (fd >= 3)
            cleanable = cleaner.register(this, state);
        else
            cleanable = null;
    }

    private int lock() {
        long oldValue;
        long newValue;
        do {
            oldValue = state.fd.getAcquire();
            // Check covers -1 as well.
            if ((oldValue & State.CLOSING_MASK) != 0)
                throw new IllegalStateException("Descriptor is closed");
            newValue = oldValue + (1L << 32);
            if (newValue < 0)
                throw new InternalError(
                    "Invalid descriptor state: " + oldValue);
        } while (!state.fd.compareAndSet(oldValue, newValue));
        return (int) newValue;
    }

    private void unlock() {
        long newValue = state.fd.addAndGet(-1L << 32);
        if (newValue < 0)
            throw new InternalError("Invalid descriptor state: " + newValue);
        if ((newValue >> 31) == 1)
            // Close request, but zero reference counts.  No other
            // thread can perform another system call on this
            // descriptor (see lock() and its CLOSING_MASK check
            // above).  close() will not update the descriptor,
            // either.  So we can directly close it.
            closeAsOwner((int) newValue & State.FD_MASK);
        else
            // Must come last, so that the cleaner thread cannot observe
            // an intermediate value (with non-zero reference count).
            Reference.reachabilityFence(this);
    }

    // Called for a descriptor that can be closed (so not
    // CLOSED/STDIN/STDOUT/STDERR) after the current thread has
    // obtained exclusive ownership, either after increment in close(),
    // or by being the last thread in unlock().
    private void closeAsOwner(int fd) {
        state.fd.set(-1);
        // This does nothing in State.run(), but still deregisters
        // the cleanable.  Not null because we have a closeable descriptor.
        cleanable.clean();
        // Fence before the raw close in case the close throws.  It
        // has to come after the state.fd update, so that the
        // cleaner thread cannot observe an intermediate value.
        Reference.reachabilityFence(this);
        RawSyscalls.close(fd);
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    static private final Descriptor CLOSED = new Descriptor(-1);
    static private final Descriptor STDIN = new Descriptor(0);
    static private final Descriptor STDOUT = new Descriptor(1);
    static private final Descriptor STDERR = new Descriptor(2);
    static private final Cleaner cleaner = Cleaner.create();
}
