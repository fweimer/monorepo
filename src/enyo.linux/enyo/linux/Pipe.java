package enyo.linux;

/**
 * A pipe with a read end and a write end.
 *
 * @param readEnd the read end of the pipe (which can be closed)
 * @param writeEnd the write end of the pipe (which can be closed)
 */
public record Pipe(
    Descriptor readEnd,
    Descriptor writeEnd)
    implements AutoCloseable {

    /**
     * Creates a new pipe.
     * The {@code O_CLOEXEC} flag is implicit.
     */
    public Pipe() {
        this(0);
    }

    /**
     * Creates a new pipe.
     * The {@code O_CLOEXEC} flag is implicit.
     * @param flags the open flags to apply
     */
    public Pipe(int flags) {
        int fds[] = RawSyscalls.pipe2(flags);
        Descriptor readEnd;
        try {
            readEnd = Descriptor.of(fds[0]);
        } catch (Throwable e) {
            RawSyscalls.close(fds[0]);
            RawSyscalls.close(fds[1]);
            throw e;
        }
        Descriptor writeEnd;
        try {
            writeEnd = Descriptor.of(fds[1]);
        } catch (Throwable e) {
            try {
                readEnd.close();
            } finally {
                RawSyscalls.close(fds[1]);
            }
            throw e;
        }
        this(readEnd, writeEnd);
    }

    /**
     * Closes both ends of the pipe.
     *
     * The ends can be closed individually as well.  Either operation
     * is sufficient to free resources.
     */
    @Override
    public void close() {
        try {
            readEnd.close();
        } finally {
            writeEnd.close();
        }
    }
}
