package enyo.linux.tests;

import enyo.linux.Syscalls;
import enyo.linux.SyscallException;

import enyo.testing.Test;
import enyo.testing.Checks;
import enyo.testing.Testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestSyscalls {
    @Test
    public static void testGetpid(Testing t) {
        String expected;
        try {
            expected = Files.readSymbolicLink(Path.of("/proc/self")).toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        t.examine(Syscalls.getpid())
            .mapToObj(Integer::toString)
            .is(expected);
    }

    @Test
    public static void testSchedYield() {
        Syscalls.sched_yield();
    }
}
