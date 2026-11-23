package enyo.linux.alsa.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Access to directories in the {@code .config} subdirectory of the
 * home directory.
 */
final class Config {
    public Config(String name) throws IOException {
        String homeDirString = System.getProperty("user.home");
        if (homeDirString == null)
            throw new IOException("user.home not set, cannot locate .config");
        Path configPath = Path.of(homeDirString).resolve(".config");

        FileAttribute<Set<PosixFilePermission>> userOnlyPermissions =
            PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rwx------"));

        if (!Files.isDirectory(configPath))
            Files.createDirectory(configPath, userOnlyPermissions);

        path = configPath.resolve(name);
        if (!Files.isDirectory(path))
            Files.createDirectory(path, userOnlyPermissions);
    }

    public Config(Class<?> cls) throws IOException {
        this(cls.getName());
    }

    public Path path() {
        return path;
    }

    public Path resolve(String name) {
        return path.resolve(name);
    }

    @Override
    public String toString() {
        return path.toString();
    }

    private final Path path;
}
