package enyo.testing.runtime;

import enyo.testing.ThrowingRunnable;

import java.util.concurrent.CountDownLatch;

final class ParallelThread extends Thread {
    private final CountDownLatch startSignal;
    private final ThrowingRunnable action;
    volatile Throwable exception;

    ParallelThread(CountDownLatch startSignal, ThrowingRunnable action) {
        this.startSignal = startSignal;
        this.action = action;
    }

    @Override
    public void run() {
        try {
            // Start calling the action after all threads have been
            // launched, to achieve maximum parallel execution.
            startSignal.countDown();
            startSignal.await();
            action.run();
        } catch (Throwable e) {
            exception = e;
        }
    }
}
