package enyo.linux;

/**
 * An open flag.
 * @see Descriptor#open(Path,OpenFlag,OpenOrCreateFlag[])
 * @see Descriptor#openat(Path,OpenFlag,OpenOrCreateFlag[])
 */
public enum OpenFlag implements OpenOrCreateFlag {
    /**
     * Open the file for appending.
     * Writes occur always at the end of the file, disregarding
     * the file pointer.
     */
    O_APPEND(Fcntl.O_APPEND),
    /**
     * Enable a special transfer mode with special buffer alignment
     * requirements.  {@link java.lang.foreign.MemorySegment}-based
     * interfaces must be used.
     * @see Descriptor#read(MemorySegment)
     * @see Descriptor#write(MemorySegment)
     */
    O_DIRECT(Fcntl.O_DIRECT),
    /**
     * Fail the open operation if the path is not a directory.
     */
    O_DIRECTORY(Fcntl.O_DIRECTORY),
    /**
     * Always perform synchronous updates of the underlying storage device,
     * but only for data, not necessarily metadata.
     */
    O_DSYNC(Fcntl.O_DSYNC),
    /**
     * Permit the use of an empty path string.
     */
    O_EMPTYPATH(Fcntl.O_EMPTYPATH),
    /**
     * Together with {@link #O_CREAT(int)}, fail creation if the
     * file already exists.
     */
    O_EXCL(Fcntl.O_EXCL),
    /**
     * Do not update the acces time when reading the file.
     */
    O_NOATIME(Fcntl.O_NOATIME),
    /**
     * Do not gain a controlling terminal when opening a terminal.
     */
    O_NOCTTY(Fcntl.O_NOCTTY),
    /**
     * Do not follow symbolic links when following the final path component.
     */
    O_NOFOLLOW(Fcntl.O_NOFOLLOW),
    /**
     * Turn on non-blocking mode.  Not all files support non-blocking mode;
     * if unsupported, operations may block even if the descriptor is reported
     * as read for reading or writing.
     */
    O_NONBLOCK(Fcntl.O_NONBLOCK),
    /**
     * Only open the path, not the file content.  Provides access to a
     * limit set of operations only.
     */
    O_PATH(Fcntl.O_PATH),
    /**
     * Open for reading.
     */
    O_RDONLY(Fcntl.O_RDONLY),
    /**
     * Open for reading and writing.
     */
    O_RDWR(Fcntl.O_RDWR),
    /**
     * Always perform fully synchronous updates of the underlying
     * storage device.
     */
    O_SYNC(Fcntl.O_SYNC),
    /**
     * Open a hidden file in the specified directory.
     */
    O_TMPFILE(Fcntl.O_TMPFILE),
    /**
     * When writing, truncate the file length to zero.
     */
    O_TRUNC(Fcntl.O_TRUNC),
    /**
     * Open for writing.
     */
    O_WRONLY(Fcntl.O_WRONLY),
    ;

    private final int value;

    OpenFlag(int value) {
        this.value = value;
    }

    /**
     * Returns the special create mode.
     * It must be associated with a file mode.
     * @param mode, a non-negative integer less than 65536
     * @return a new open flag of kind {@code O_CREAT} (file creation),
     *   with the associated mode
     * @throws IllegalArgumentException the {@code mode} is not within range
     */
    public static OpenOrCreateFlag O_CREAT(int mode) {
        return new CreateFlag(mode);
    }

    record CreateFlag(int mode) implements OpenOrCreateFlag {
        CreateFlag {
            if (mode < 0 || mode > 65535)
                throw new IllegalArgumentException("mode: " + mode);
        }

        @Override
        public int value() {
            return Fcntl.O_CREAT;
        }
    }

    @Override
    public int value() {
        return value;
    }

    @Override
    public int mode() {
        return -1;
    }


}
