package enyo.linux.tests;

import enyo.linux.Descriptor;
import enyo.linux.RawSyscalls;
import enyo.linux.SyscallException;
import enyo.linux.OpenFlag;
import enyo.linux.Errno;

import enyo.io.TempDirectory;

import enyo.testing.Test;
import enyo.testing.Checks;
import enyo.testing.Testing;

import java.io.IOException;

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
        // Without O_CREATE, the file is not created.
        t.expect(() -> Descriptor.open(dir.resolve("a"), OpenFlag.O_RDONLY),
            SyscallException.class)
            .map(SyscallException::errno, e0 -> e0.is(Errno.ENOENT));
        t.expect(() -> Descriptor.open(dir.resolve("a"), OpenFlag.O_WRONLY),
            SyscallException.class)
            .map(SyscallException::errno, e0 -> e0.is(Errno.ENOENT));
        t.expect(() -> Descriptor.open(dir.resolve("a"), OpenFlag.O_RDWR),
            SyscallException.class)
            .map(SyscallException::errno, e0 -> e0.is(Errno.ENOENT));

        // This should succed.
        try (var fd = Descriptor.open(dir.resolve("a"), OpenFlag.O_WRONLY,
            OpenFlag.O_CREAT(0600))) {
            fd.write("example".getBytes());
        }
    }
}
