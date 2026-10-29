package enyo.linux.tests;

import enyo.linux.RawSyscalls;
import enyo.linux.SyscallException;

import enyo.testing.Test;
import enyo.testing.Checks;
import enyo.testing.Testing;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestRawSyscalls {
    @Test
    public static void testCloseBad(Testing t) {
        t.expect(() -> RawSyscalls.close(-1),
            SyscallException.class)
            .map(SyscallException::getMessage,
                e0 -> e0.is("EBADF close [-1]"));
    }

    @Test
    public static void testPipeWriteRead(Testing t) {
        var fds = RawSyscalls.pipe();
        try (var arena = Arena.ofConfined()) {
        byte[] input = "example".getBytes();
        t.examine(RawSyscalls.write(fds[1],
            arena.allocateFrom(ValueLayout.JAVA_BYTE, input)))
            .is(input.length);
        t.examine(input)
            .check(Checks.byteArrayEquals("example".getBytes()));

            var output = arena.allocate(
                ValueLayout.JAVA_BYTE, input.length + 1);
            output.set(ValueLayout.JAVA_BYTE, input.length, (byte) '@');
            t.examine(RawSyscalls.read(fds[0], output))
                .is(input.length);
            t.examine(output.toArray(ValueLayout.JAVA_BYTE))
                .check(Checks.byteArrayEquals("example@".getBytes()));
        } finally {
            try {
                RawSyscalls.close(fds[0]);
            } finally {
                RawSyscalls.close(fds[1]);
            }
        }
    }
}
