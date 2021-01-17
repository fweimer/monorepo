package enyo.testing.runtime;

import enyo.testing.ThrowingRunnable;

final class ParallelThread extends Thread {
    private final ThrowingRunnable action;
    volatile Throwable exception;

    ParallelThread(ThrowingRunnable action) {
        this.action = action;
    }

    @Override
    public void run() {
        try {
            action.run();
        } catch (Throwable e) {
            exception = e;
        }
    }
}
