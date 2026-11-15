package enyo.sound.tests;

import enyo.crypto.DigestAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * Computes the digest of the sample data in a WAVE file in the same
 * way the FLAC reference implementation does.
 */
public final class FlacDigest {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.format("usage: %s WAVE-FILE%n", FlacDigest.class);
            System.exit(1);
        }

        Path path = Path.of(args[0]);
        try (FileChannel ch = FileChannel.open(path)) {
            InputStream in = Channels.newInputStream(ch);

            WaveFormat format = WaveFormat.readHeader(in);
            if (format.channels() != 2
                || format.bitsPerSample() != 16)
                throw new IOException(path + ": invalid format: " + format);
            if (ch.size() != format.expectedFileSize())
                throw new IOException(path + ": expected file size "
                    + format.expectedFileSize()
                    + ", got " + ch.size());

            long totalBytes = 0;
            MessageDigest md = DigestAlgorithm.MD5.newDigest();
            byte[] buffer = new byte[8192];
            while (true) {
                int ret = in.read(buffer);
                if (ret <= 0)
                    break;
                md.update(buffer, 0, ret);
                totalBytes += ret;
            }
            if (totalBytes != format.sampleDataSize())
                throw new IOException(String.format(
                        "%d bytes of sample data found, expected %d",
                        totalBytes, format.sampleDataSize()));

            for (byte b : md.digest())
                System.out.format("%02x", Byte.toUnsignedInt(b));
            System.out.println();
        }
    }

    private FlacDigest() { } // No instances.
}
