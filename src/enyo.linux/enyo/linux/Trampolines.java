package enyo.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

final class Trampolines {
    final static MethodHandle L;
    final static MethodHandle LL;
    final static MethodHandle LLL;
    final static MethodHandle LLLLLL;
    final static MethodHandle LLM;
    final static MethodHandle LLMLLL;
    final static MethodHandle LML;
    final static MethodHandle LMLL;
    final static MethodHandle LMLLLL;
    final static MethodHandle LMM;
    final static MethodHandle LMMLLL;
    final static MethodHandle ML;
    final static MethodHandle MLLLLL;
    final static MethodHandle NONE;

    static {
        if (!"Linux".equals(System.getProperty("os.name")))
            throw new UnsupportedOperationException(
                "Unsupported os.name:" + System.getProperty("os.name"));

        // The generic system call trampoline.  Six long arguments,
        // followed by the system call number.
        MemorySegment trampoline;

        switch (System.getProperty("os.arch")) {
            case "amd64":
                // endbr64
                // mov 8(%rsp), %eax
                // mov %rcx, %r10
                // syscall
                // ret
                // ud2
                trampoline = syscallTrampoline(
                    0xfa1e0ff3, 0x0824448b, 0x0fca8949, 0x0b0fc305);
                break;
            default:
                throw new UnsupportedOperationException(
                    "Unsupported os.arch:" + System.getProperty("os.arch"));
        }

        LLLLLL = syscallWrapper(trampoline);
        LLMLLL = syscallWrapper(trampoline, 2);
        LMLLLL = syscallWrapper(trampoline, 1);
        LMMLLL = syscallWrapper(trampoline, 1, 2);
        MLLLLL = syscallWrapper(trampoline, 0);

        L = withArguments(LLLLLL, 1);
        LL = withArguments(LLLLLL, 2);
        LLL = withArguments(LLLLLL, 3);
        LLM = withArguments(LLMLLL, 3);
        LML = withArguments(LMLLLL, 3);
        LMLL = withArguments(LMLLLL, 4);
        LMM = withArguments(LMMLLL, 3);
        ML = withArguments(MLLLLL, 2);
        NONE = withArguments(LLLLLL, 0);
    }

    @SuppressWarnings("restricted")
    private static MemorySegment syscallTrampoline(int... code) {
        var linker = Linker.nativeLinker();
        try {
            var libc = linker.defaultLookup();
            var ctypes = linker.canonicalLayouts();
            var size_t = ctypes.get("size_t");
            long size = code.length * 4;
            var mmap = linker.downcallHandle(
                libc.find("mmap64").get(), FunctionDescriptor.of(
                    ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS,
                    size_t,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_LONG)); // mmap64 always uses 64-bit off_t
            var mprotect = linker.downcallHandle(
                libc.find("mprotect").get(), FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS,
                    size_t,
                    ValueLayout.JAVA_INT));

            // Allocate the page.
            var codeAddress = (MemorySegment) mmap.invokeExact(
                MemorySegment.NULL,
                // FIXME: long will not match size_t on 32 bit.
                size,
                Mman.PROT_READ | Mman.PROT_WRITE,
                Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS,
                -1,
                0L);
            if (codeAddress.address() == -1)
                throw new InternalError(
                    "Could not mmap system call trampoline");

            // Update the extent to match what was allocated.
            codeAddress = codeAddress.reinterpret(size);

            // Copy the machine code.
            MemorySegment.copy(code, 0,
                codeAddress, ValueLayout.JAVA_INT, 0,
                code.length);

            // Make the page executable.
            int ret = (int) mprotect.invokeExact(codeAddress, size,
                Mman.PROT_READ | Mman.PROT_EXEC);
            if (ret != 0)
                throw new InternalError(
                    "Could not mprotect system call trampoline");
            return codeAddress;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    // Produces a six-argument wrapper for the trampoline.
    // The indicated positions have MemorySegment arguments instead
    // of long arguments.
    @SuppressWarnings("restricted")
    private static MethodHandle syscallWrapper(
        MemorySegment trampoline, int... memorySegmentArgs) {
        var args = new MemoryLayout[7];
        Arrays.fill(args, ValueLayout.JAVA_LONG);
        for (int idx : memorySegmentArgs)
            args[idx] = ValueLayout.ADDRESS;
        args[6] = ValueLayout.JAVA_INT;

        return Linker.nativeLinker()
            .downcallHandle(trampoline, FunctionDescriptor.of(
                ValueLayout.JAVA_LONG, args));
    }

    private static MethodHandle withArguments(MethodHandle wrapper, int count) {
        if (wrapper.type().parameterCount() != 7
            || count < 0 || count >= 6)
            throw new IllegalArgumentException();
        Long[] inserted = new Long[6 - count];
        Arrays.fill(inserted, 0L);
        return MethodHandles.insertArguments(
            wrapper, count, (Object[]) inserted);
    }
}
