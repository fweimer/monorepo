package enyo.build;

import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

final class TeeSystemStreams implements AutoCloseable {
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private final FileOutputStream file;

    TeeSystemStreams(Path duplicate) throws IOException {
        originalOut = System.out;
        originalErr = System.err;
        file = new FileOutputStream(duplicate.toFile(), true);
        System.setOut(new PrintStream(
                new TeeOutputStream(originalOut, file), true));
        System.setErr(new PrintStream(
                new TeeOutputStream(originalErr, file), true));
    }


    @Override
    public void close() throws IOException {
        try {
            System.setOut(originalOut);
        } finally {
            try {
                System.setErr(originalErr);
            } finally {
                file.close();
            }
        }
    }

    void rewind() throws IOException {
        file.getChannel().truncate(0);
    }

    private static class TeeOutputStream extends OutputStream {
        TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void close() throws IOException {
            try {
                out1.close();
            } finally {
                out2.close();
            }
        }

        @Override
        public void write(int b) throws IOException {
            try {
                out1.write(b);
            } finally {
                out2.write(b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                out1.write(b, off, len);
            } finally {
                out2.write(b, off, len);
            }
        }

        private final OutputStream out1;
        private final OutputStream out2;
    }
}
