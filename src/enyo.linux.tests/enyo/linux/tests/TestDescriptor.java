package enyo.linux.tests;

import enyo.linux.Descriptor;
import enyo.linux.Errno;
import enyo.linux.OpenFlag;
import enyo.linux.Pipe;
import enyo.linux.RawSyscalls;
import enyo.linux.SyscallException;
import enyo.linux.Termios;

import enyo.io.function.IOFunction;
import enyo.io.TempDirectory;

import enyo.testing.Test;
import enyo.testing.Checks;
import enyo.testing.Testing;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TestDescriptor {
    @Test
    public static void testOfBad(Testing t) {
        t.expect(() -> Descriptor.of(-1),
            IllegalArgumentException.class);
        t.expect(() -> Descriptor.of(-2),
            IllegalArgumentException.class);
        t.expect(() -> Descriptor.of(-100),
            IllegalArgumentException.class);
        t.expect(() -> Descriptor.of(Integer.MIN_VALUE),
            IllegalArgumentException.class);
    }

    @Test
    public static void testOfStd(Testing t) {
        t.examine(Descriptor.of(0)).isSame(Descriptor.stdin());
        t.examine(Descriptor.of(1)).isSame(Descriptor.stdout());
        t.examine(Descriptor.of(2)).isSame(Descriptor.stderr());
        t.examine(Descriptor.stdin().raw()).is(0);
        t.examine(Descriptor.stdout().raw()).is(1);
        t.examine(Descriptor.stderr().raw()).is(2);
    }

    @Test
    public static void testOpen(Testing t) throws IOException {
        try (TempDirectory dir = TempDirectory.withPrefix(
            "enyo-linux-Descriptor-")) {
            testOpen(t, dir); // Separate to check declared exceptions.
        }
    }

    private static void testOpen(Testing t, TempDirectory dir) {
        Path aPath = dir.resolve("a");

        // Without O_CREATE, the file is not created.
        t.expect(() -> Descriptor.open(aPath, OpenFlag.O_RDONLY),
            SyscallException.class)
            .check(Helpers.errno(Errno.ENOENT));
        t.assertFalse(Files.exists(aPath));
        t.expect(() -> Descriptor.open(aPath, OpenFlag.O_WRONLY),
            SyscallException.class)
            .check(Helpers.errno(Errno.ENOENT));
        t.assertFalse(Files.exists(aPath));
        t.expect(() -> Descriptor.open(aPath, OpenFlag.O_RDWR),
            SyscallException.class)
            .check(Helpers.errno(Errno.ENOENT));
        t.assertFalse(Files.exists(aPath));

        // The file has been created.
        try (var fd = Descriptor.open(aPath, OpenFlag.O_WRONLY,
            OpenFlag.O_CREAT(0600))) {
            t.expect(() -> fd.read(new byte[0]),
                SyscallException.class)
                .check(Helpers.errno(Errno.EBADF));
            fd.write("example\n".getBytes());
        }
        t.examine(IOFunction.of(Files::readAllBytes).apply(aPath))
            .check(Checks.byteArrayEquals("example\n".getBytes()));

        try (var fd = Descriptor.open(aPath, OpenFlag.O_RDONLY)) {
            var buf = ByteBuffer.allocate(10);
            buf.position(1);
            t.examine(fd.read(buf)).is(8);
            buf.limit(9);
            t.examine(buf)
                .check(Checks.byteBufferEquals("example\n".getBytes()));
        }

    }

    @Test
    public static void testPipeClose(Testing t) {
        try (var pipe = new Pipe()) {
            pipe.readEnd().close();
            pipe.writeEnd().close();
        }
    }

    @Test
    public static void testPipeWriteRead(Testing t) {
        try (var pipe = new Pipe();
             var arena = Arena.ofConfined()) {
            byte[] input = "example".getBytes();
            t.examine(pipe.writeEnd().write(
                arena.allocateFrom(ValueLayout.JAVA_BYTE, input)))
                .is(input.length);
            t.examine(input)
                .check(Checks.byteArrayEquals("example".getBytes()));

            var output = arena.allocate(
                ValueLayout.JAVA_BYTE, input.length + 1);
            output.set(ValueLayout.JAVA_BYTE, input.length, (byte) '@');
            t.examine(pipe.readEnd().read(output))
                .is(input.length);
            t.examine(output.toArray(ValueLayout.JAVA_BYTE))
                .check(Checks.byteArrayEquals("example@".getBytes()));
        }
    }

    @Test
    public static void testPipeWriteReadByteBuffer(Testing t) {
        for (int shift = 0; shift < 1; ++shift) {
            testPipeWriteReadByteBuffer(t, false, shift);
            testPipeWriteReadByteBuffer(t, true, shift);
        }
    }

    @Test
    public static void testOpenpt(Testing t) {
        try (var fd = Descriptor.openpt()) {
            t.examine(fd).map(Descriptor::isatty).is(true);
            t.examine(fd)
                .map(Descriptor::ptsname)
                .map(Path::toString)
                .check(Checks.startsWith("/dev/pts/"));
            t.examine(fd)
                .map(Descriptor::tcgetwinsize)
                .map(Termios.Winsize::toString)
                .is("Winsize[row=0, col=0, xpixel=0, ypixel=0]");
            fd.tcsetwinsize(
                new Termios.Winsize(1, 2, 3, 4));
            t.examine(fd)
                .map(Descriptor::tcgetwinsize)
                .map(Termios.Winsize::toString)
                .is("Winsize[row=1, col=2, xpixel=3, ypixel=4]");
            fd.tcsetwinsize(
                new Termios.Winsize(0xffff, 0xfffe, 0xfffd, 0xfffc));
            t.examine(fd)
                .map(Descriptor::tcgetwinsize)
                .map(Termios.Winsize::toString)
                .is("Winsize[row=65535, col=65534,"
                    + " xpixel=65533, ypixel=65532]");
            // Stil locked.
            t.expect(() -> Descriptor.open(fd.ptsname(),
                    OpenFlag.O_RDWR, OpenFlag.O_NOCTTY),
                SyscallException.class)
                .map(SyscallException::errno)
                .is(Errno.EIO);
            // Unlock.
            fd.unlockpt();
            try (var fds = Descriptor.open(fd.ptsname(),
                    OpenFlag.O_RDWR, OpenFlag.O_NOCTTY)) {
                t.examine(fds).map(Descriptor::isatty).is(true);
                // Set multiplexer side.
                fd.tcsetwinsize(
                    new Termios.Winsize(1, 2, 3, 4));
                // The service side follows.
                t.examine(fds)
                    .map(Descriptor::tcgetwinsize)
                    .map(Termios.Winsize::toString)
                    .is("Winsize[row=1, col=2, xpixel=3, ypixel=4]");
                // And vice versa.
                fds.tcsetwinsize(
                    new Termios.Winsize(11, 22, 33, 44));
                t.examine(fd)
                    .map(Descriptor::tcgetwinsize)
                    .map(Termios.Winsize::toString)
                    .is("Winsize[row=11, col=22, xpixel=33, ypixel=44]");
                // Not a multiplexer.
                t.expect(() -> fds.unlockpt(),
                    SyscallException.class)
                    .map(SyscallException::errno)
                    .is(Errno.ENOTTY);
            }
        }
    }

    @Test
    public static void testOpenpty(Testing t) {
        try (var fd = Descriptor.openpt()) {
            // Unlocking is required.
            t.expect(() -> fd.openpty(),
                SyscallException.class)
                .map(SyscallException::errno)
                .is(Errno.EIO);
            fd.unlockpt();
            try (var fds = fd.openpty()) {
                t.examine(fds).map(Descriptor::isatty).is(true);
            }
        }
    }

    @Test
    public static void testIsatty(Testing t) {
        try (var fd = Descriptor.open(Path.of("/dev/null"), OpenFlag.O_RDWR)) {
            t.examine(fd).map(Descriptor::isatty).is(false);
        }
    }

    private interface ArrayOp {
        int perform(Descriptor fd, byte[] buf, int off, int len);
    }

    private static final List<ArrayOp> READ_OPS = List.of(
        (fd, buf, off, len) -> fd.read(buf, off, len),
        (fd, buf, off, len) ->
            fd.read(ByteBuffer.wrap(buf).position(off).limit(off + len)),
        // ByteBuffer with a non-zero array offset.
        (fd, buf, off, len) -> {
            int shiftL = off / 2;
            int shiftR = (buf.length - off - len) / 2;
            return fd.read(ByteBuffer.wrap(buf)
                .position(shiftL)
                .limit(buf.length - shiftR)
                .slice()
                .position(off - shiftL)
                .limit(off + len));
        },
        // Heap memory segment.
        (fd, buf, off, len) ->
            Math.toIntExact(fd.read(MemorySegment.ofArray(buf)
                .asSlice(off, len))));


    private static void testPipeWriteReadByteBuffer(Testing t,
        boolean direct, int shift) {
        ByteBuffer input;
        ByteBuffer output;
        if (direct) {
            input = ByteBuffer.allocateDirect(shift + 7);
            output = ByteBuffer.allocateDirect(shift + 8);
        } else {
            input = ByteBuffer.allocate(shift + 7);
            output = ByteBuffer.allocate(shift + 8);
        }
        input.position(shift);
        input.put("example".getBytes());
        input.position(shift);
        output.position(shift);
        output.put(shift + 7, (byte) '@');

        try (var pipe = new Pipe()) {
            t.examine(pipe.writeEnd().write(input)).is(7);
            t.examine(input)
                .check(Checks.byteBufferEquals("example".getBytes()));

            t.examine(pipe.readEnd().read(output)).is(7);
            t.examine(output)
                .check(Checks.byteBufferEquals("example@".getBytes()));
        }
    }
}
