package enyo.sound.flac.tests;

import enyo.io.BufferInput;
import enyo.sound.flac.FlacInput;
import enyo.sound.flac.Streaminfo;

import enyo.sound.tests.WaveFormat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;

public final class CompareWAV {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.format("usage: %s FLAC-FILE WAV-FILE%n",
                CompareWAV.class);
            System.exit(1);
        }

        try (var flac0 = FileChannel.open(Path.of(args[0]));
            var wav0 = FileChannel.open(Path.of(args[1]))) {

            var flac = new FlacInput(BufferInput.wrap(flac0));
            var wav = BufferInput.wrap(wav0);

            {
                Streaminfo flacFormat = flac.streaminfo();
                WaveFormat waveFormat = WaveFormat.readHeader(
                    Channels.newInputStream(wav0));
                int errors = 0;
                errors += checkEqual("number of channels",
                    flacFormat.channels(), waveFormat.channels());
                errors += checkEqual("sample rate",
                    flacFormat.sampleRate(), waveFormat.sampleRate());
                errors += checkEqual("bits per sample",
                    flacFormat.bitsPerSample(), waveFormat.bitsPerSample());
                if (errors > 0)
                    System.exit(1);
            }

            long bytesRead = 0;
            while (true) {
                var flacBuffer = flac.fillBuffer(0);
                var wavBuffer = wav.fillBuffer(0);
                if (!flacBuffer.hasRemaining() && !wavBuffer.hasRemaining())
                    break;
                if (!flacBuffer.hasRemaining()) {
                    System.out.format(
                        "error: FLAC ends before WAV after %d bytes",
                        bytesRead);
                    System.exit(1);
                }
                if (!wavBuffer.hasRemaining()) {
                    System.out.format(
                        "error: WAV ends before FLAC after %d bytes",
                        bytesRead);
                    System.exit(1);
                }

                int mismatch = flacBuffer.mismatch(wavBuffer);
                if (mismatch > 0) {
                    // One buffer may be shorter than the other.  Try
                    // again.  If there is premature EOF on one side,
                    // the initial loop checks detect it.  Otherwise,
                    // the mismatch will be at the first byte during
                    // the next iteration.
                    flacBuffer.position(flacBuffer.position() + mismatch);
                    wavBuffer.position(wavBuffer.position() + mismatch);
                    bytesRead += mismatch;
                } else if (mismatch == 0) {
                    System.out.format(
                        "error: difference after %d sample bytes"
                        + " in frame %d%n",
                        bytesRead, flac.frameCounter() - 1);
                    System.exit(1);
                } else {
                    // Both buffers have the same size.
                    bytesRead += flacBuffer.remaining();
                    flacBuffer.position(flacBuffer.limit());
                    wavBuffer.position(wavBuffer.limit());
                }
            }
        }
    }

    private static int checkEqual(String description,
        Object left, Object right) {
        if (!left.equals(right)) {
            System.out.format("error: %s: %s != %s%n",
                description, left, right);
            return 1;
        } else
            return 0;
    }
}
