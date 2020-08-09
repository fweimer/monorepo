package enyo.build;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.AsynchronousCloseException;

/**
 * Thread that waits until the {@link LockArray.Lock.TEST_STOP} lock
 * can be acquired.
 */
final class ExitMonitorThread extends Thread {
    ExitMonitorThread(GlobalTestingState global, LockArray lockArray) {
        super("Waiting for testing stop request");
        setDaemon(true);
        this.global = global;
        this.lockArray = lockArray;
    }

    @Override
    public void run() {
        try {
            lockArray.lock(LockArray.Lock.TEST_STOP);
        } catch (AsynchronousCloseException e) {
            // Nothing left to do: the virtual machine is about to
            // exit anyway.
            return;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Probe only once.  Future completion of the remaining
        // exit-inhibiting tests will eventually lead to termination
        // of the virtual machine.
        global.probeForExit();
    }

    private final GlobalTestingState global;
    private final LockArray lockArray;
}
