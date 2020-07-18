package enyo.io;

import enyo.io.function.IOFunction;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Class representing the identity of one file.
 *
 * The identity is supposed to change when the file is modified or
 * replaced with a different file.
 */
public final class FileIdentity  {
    // Ideally, FileIdentity should be Serializable, but
    // sun.nio.fs.UnixFileKey (the underlying implementation of file
    // keys) is not, so this is not possible.

    /**
     * Creates a file identity from file attributes.
     *
     * @param attrs the file attributes
     * @return the file identity
     * @throws IllegalArgumentException if the {@code attrs} describes
     *    neither {@linkplain #hasFileIdentity(BasicFileAttributes) a
     *    regular file nor a directory}.
     * @throws NullPointerException if {@code attrs} is null
     */
    public static FileIdentity of(BasicFileAttributes attrs) {
        if (!hasFileIdentity(attrs)) {
            if (attrs.isSymbolicLink())
                throw new IllegalArgumentException("Symbolic link: " + attrs);
            else
                throw new IllegalArgumentException(
                    "File without identity: " + attrs);
        }
        return new FileIdentity(attrs);
    }

    /**
     * Creates a file identity for the file system entry at the
     * specified path.
     *
     * @param path the path for which attributes are determined
     * @return the file identity
     * @throws java.nio.file.NoSuchFileException if {@code path} does not exist
     * @throws IllegalArgumentException if the {@code attrs} describes
     *    neither {@linkplain #hasFileIdentity(BasicFileAttributes) a
     *    regular file nor a directory}.
     * @throws NullPointerException if {@code attrs} is null
     */
    public static FileIdentity of(Path path) throws IOException {
        BasicFileAttributes attrs =
            Files.readAttributes(path, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (!hasFileIdentity(attrs)) {
            if (attrs.isSymbolicLink())
                throw new IllegalArgumentException("Symbolic link: " + path);
            else
                throw new IllegalArgumentException(
                    "File without identity: " + path);
        }
        return new FileIdentity(attrs);
    }

    /**
     * Indicates whether the file is a directory.
     *
     * @return true if the file identity corresponds to a directory,
     *   false if it is a regular file instead
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Returns the file key for this file idenity.
     *
     * @return the file key, which can be null
     * @see BasicFileAttributes#fileKey()
     */
    public Object fileKey() {
        return fileKey;
    }

    /**
     * Returns the modification time.
     *
     * @return the modification time
     */
    public FileTime lastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Returns size file size.
     *
     * @return the size of the file, in bytes
     */
    public long size() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != FileIdentity.class)
            return false;
        FileIdentity other = (FileIdentity) o;
        return size == other.size
            && directory == other.directory
            && lastModifiedTime.equals(other.lastModifiedTime)
            && Objects.equals(fileKey, other.fileKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fileKey)
            + Boolean.hashCode(directory)
            + lastModifiedTime.hashCode()
            + Long.hashCode(size);
    }

    @Override
    public String toString() {
        return Long.toString(size) + '/' + lastModifiedTime
            + '/' + fileKey;
    }

    /**
     * Returns true if these file attributes should have an identity.
     * Regular files and directories have an identity, symbolic links
     * do not.
     *
     * @param attrs the attributes to check
     * @return true if the attributes should have an identity
     * @throws NullPointerException if {@code attrs} is null
     */
    public static boolean hasFileIdentity(BasicFileAttributes attrs) {
        return attrs.isRegularFile() || attrs.isDirectory();
    }

    /**
     * Returns a collector for producing a map from file system paths
     * to their file identity.
     *
     * The collector may throw the following exceptions:
     *
     * <ul>
     * <li>{@link java.io.UncheckedIOException} - if an I/O error occurs,
     *   particularly if one of the paths does not exist in the file system
     * <li>{@link IllegalArgumentException} - if one of the paths does not have
     *   a {@linkplain #hasFileIdentity(BasicFileAttributes) file identity}
     * </ul>

     * @return a collector that produces an unmodifiable map
     */
    public static Collector<Path, ?, Map<Path, FileIdentity>>
        mapToFileIdentity() {
        return Collectors.toUnmodifiableMap(Function.identity(),
            IOFunction.of(p -> new FileIdentity(
                    Files.readAttributes(p, BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS))));
    }

    private final Object fileKey;
    private final boolean directory;
    private final FileTime lastModifiedTime;
    private final long size;

    private FileIdentity(BasicFileAttributes attrs) {
        fileKey = attrs.fileKey();
        directory = attrs.isDirectory();
        lastModifiedTime = Objects.requireNonNull(
            attrs.lastModifiedTime(), "lastModifiedTime");
        size = attrs.size();
    }
}
