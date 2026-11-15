package enyo.linux.alsa.examples;

import enyo.crypto.DigestAlgorithm;
import enyo.io.ObjectSequences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Unprocessed playlist, for storing under {@code ~/.config}.
 */
final class Playlist {
    /**
     * Initializes the playlist file to start at zero.
     */
    Playlist(List<URL> files) throws IOException {
        list = List.copyOf(files);
        buffer = writePlayList(config(), files);
    }

    /**
     * Reads the existing playlist.
     */
    Playlist() throws IOException {
        Path playlist = config().resolve("playlist");
        try (FileChannel ch = FileChannel.open(playlist,
                Set.of(StandardOpenOption.READ,
                    StandardOpenOption.WRITE))) {
            InputStream in = Channels.newInputStream(ch);

            // Check the file magic.
            {
                byte[] magic = in.readNBytes(PLAYLIST_MARKER.length());
                if (!Arrays.equals(magic,
                        PLAYLIST_MARKER.getBytes(StandardCharsets.UTF_8)))
                    throw new IOException(playlist + ": wrong file magic");
            }

            // Skip the playlist offset.
            in.skipNBytes(4);

            // Read the payload and check the digest.
            byte[] blob;
            {
                byte[] digest = in.readNBytes(
                    DigestAlgorithm.SHA256.lengthInBytes());
                blob = in.readAllBytes();
                if (!Arrays.equals(
                        DigestAlgorithm.SHA256.newDigest().digest(blob),
                        digest))
                    throw new IOException(playlist + ": wrong digest");
            }

            list = List.copyOf(ObjectSequences.readList(
                    new DataInputStream(new ByteArrayInputStream(blob)),
                    in0 -> {
                        try {
                            return new URI(ObjectSequences.readString(in0)).toURL();
                        } catch (URISyntaxException e) {
                            throw new UncheckedIOException(new IOException (e));
                        }
                    }));
            buffer = mapOffset(ch);
        }
    }

    List<URL> files() {
        return list;
    }

    int offsetInSeconds() {
        return (int) VARHANDLE.getVolatile(buffer, 0);
    }

    void offsetInSeconds(int offset) {
        VARHANDLE.setVolatile(buffer, 0, offset);
    }

    private static Config config() throws IOException {
        // TODO: Switch to final class name once moved.
        return new Config("enyo.sound.tools.Play");
    }

    // Magic string for ~/.config/enyo.sound.tools.Play/playlist.
    // Length must be a multiple of four, so that the subsequent int
    // is aligned.
    private final static String PLAYLIST_MARKER =
        "enyo.sound.tools.Play playlist file\n";

    private static ByteBuffer writePlayList(Config config, List<URL> files)
        throws IOException {
        // Construct the serialized version of the playlist.
        byte[] blob;
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(out);
            ObjectSequences.writeSequence(data, files,
                (out0, url) ->
                ObjectSequences.writeString(out0, url.toString()));
            blob = out.toByteArray();
        }

        Path playlist = config.resolve("playlist");
        try (FileChannel ch = FileChannel.open(playlist,
                Set.of(StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING))) {
            OutputStream out = Channels.newOutputStream(ch);

            // File magic.
            out.write(PLAYLIST_MARKER.getBytes(StandardCharsets.UTF_8));

            // Placeholder for playlist offset (in seconds).
            out.write(new byte[4]);

            // Detector for corruption of the following data.
            out.write(DigestAlgorithm.SHA256.newDigest().digest(blob));

            // The serialized data.
            out.write(blob);
            out.flush();

            return mapOffset(ch);
        }
    }

    // Returns a ByteBuffer for the part of the file that contains the
    // play offset.
    private static ByteBuffer mapOffset(FileChannel ch) throws IOException {
        return ch.map(FileChannel.MapMode.READ_WRITE,
            PLAYLIST_MARKER.length(), 4);
    }

    private final List<URL> list;
    private final ByteBuffer buffer;

    // Used to update the play offset in the buffer.
    private static final VarHandle VARHANDLE =
        MethodHandles.byteBufferViewVarHandle(
            int[].class, ByteOrder.nativeOrder());
}
