package enyo.build;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * An array of independent locks in a lock array.
 *
 * <p>The class is thread-safe, but multiple lock arrays for the
 * same backing files in the same virtual machine may not work
 * correctly due to file channel lock limitations.
 */
final class LockArray implements Closeable {
    /**
     * Creates a new lock array.  The main lock is initially acquired.
     * The underlying lock file is created, and the file is rewritten
     * as necessary.
     * @param path the path to the lock file
     */
    public LockArray(Path path) throws IOException {
        lockChannel = createLockFile(path);

        try {
            // The marker object is released lock on the entire file.
            marker = lockChannel.lock(0, 0, false);
            marker.release();

            // Obtaining the main lock is special because it is used
            // for error-checking the external synchronization.  If it
            // cannot be acquired, it is an error.
            FileLock lock = tryLock0(Lock.MAIN);
            if (lock == null)
                try {
                    throw new IOException(path + ": Cannot acquire main lock");
                } finally {
                    lockChannel.close();
                }
            locks.put(Lock.MAIN, lock);
        } catch (Throwable e) {
            try {
                throw e;
            } finally {
                lockChannel.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            // This is a bit of a best effort.  Closing the file
            // channel should release all locks.
            FileLock[] locksCopy = locks.values().toArray(new FileLock[0]);
            for (FileLock lock : locksCopy)
                if (lock != marker)
                    lock.release();
        } finally {
            lockChannel.close();
        }
    }

    public enum Lock {
        /**
         * The main lock which ensures that the test link directory
         * is not destructively modified while tests are running.
         */
        MAIN,
        ;
    }

    /**
     * Locks the specified lock, blocking if necessary.
     * @param lock the lock to acquire
     * @throws IOException if the lock could not be acquired, including
     *   when the lock is already locked by the same lock array
     */
    public void lock(Lock lock) throws IOException {
        Objects.requireNonNull(lock);
        FileLock fileLock = locks.put(lock, marker);
        if (fileLock != null)
            throw new IOException("Lock of already-locked lock: " + lock);
        locks.put(lock, lock0(lock));
    }

    /**
     * Attempts to lock the specified lock.
     * If the lock cannot be acquired immediately, this method returns
     * {@code false}, without blocking.
     * @param lock the lock to acquire
     * @return true if the lock could be acquired, false otherwise
     * @throws IOException if the lock could not be acquired, including
     *   when the lock is already locked by the same lock array
     */
    public boolean tryLock(Lock lock) throws IOException {
        Objects.requireNonNull(lock);
        FileLock fileLock = locks.put(lock, marker);
        if (fileLock != null)
            throw new IOException("Lock of already-locked lock: " + lock);
        fileLock = tryLock0(lock);
        if (fileLock == null)
            locks.remove(lock);
        else
            locks.put(lock, fileLock);
        return fileLock != null;
    }

    /**
     * Unlocks the specified lock.
     * @throws IOException if the lock could not be released, including
     *   when the lock has not been locked by this lock array
     */
    public void unlock(Lock lock) throws IOException {
        Objects.requireNonNull(lock);
        FileLock fileLock = locks.remove(lock);
        if (fileLock == null || fileLock == marker)
            throw new IOException("Unlock of non-locked lock: " + lock);
        fileLock.release();
    }

    private final FileChannel lockChannel;

    // This is used as a pending-lock marker object in locks.
    private final FileLock marker;

    private final Map<Lock, FileLock> locks = Collections.synchronizedMap(
        new EnumMap<>(Lock.class));

    private static FileChannel createLockFile(Path path) throws IOException {
        int size = Lock.values().length;

        FileChannel channel = FileChannel.open(path,
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE);
        try {
            // Adjust the file size so that there is one byte per
            // lock.  This helps with the locking operation on obscure
            // file systems.  This sizing operation is not disruptive
            // to incorrect parallel access of the test-link
            // directory, so it can happen without a file lock.
            channel.truncate(size);
            ByteBuffer buffer = ByteBuffer.allocate(size);
            channel.write(buffer);
            if (buffer.hasRemaining())
                throw new IOException(path + ": short write");
            channel.position(0);

            FileChannel result = channel;
            channel = null;
            return result;
        } finally {
            if (channel != null)
                channel.close();
        }
    }

    // Calls FileChannel.lock() to obtain the lock.
    private FileLock lock0(Lock lock) throws IOException {
        return lockChannel.lock(lock.ordinal(), 1, false);
    }

    // Calls FileChannel.tryLock() in an attempt obtain the lock.
    private FileLock tryLock0(Lock lock) throws IOException {
        return lockChannel.tryLock(lock.ordinal(), 1, false);
    }
}
