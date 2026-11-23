package enyo.linux;

/**
 * System call wrappers.
 */
public final class Syscalls {
    /**
     * Returns the process of the current process.
     * @return the PID, a positive integer
     */
    public static int getpid() {
        try {
            return (int) (long) Trampolines.NONE.invokeExact(
                SyscallConstants.getpid);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    /**
     * Relinquishes the CPU.
     * This pauses the execution of the current thread.
     * @see Thread#yield()
     */
    public static void sched_yield() {
        try {
            long _ = (long) Trampolines.NONE.invokeExact(
                SyscallConstants.sched_yield);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    private Syscalls() {}
}
