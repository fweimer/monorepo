package enyo.testing.runtime;

import enyo.testing.ThrowingCallable;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

final class ParallelThread extends Thread {
    private final CountDownLatch startSignal;
    private final ThrowingCallable<?> action;
    volatile Throwable exception;
    Object result;

    ParallelThread(CountDownLatch startSignal, ThrowingCallable<?> action) {
        this.startSignal = startSignal;
        this.action = action;
    }

    @Override
    public void run() {
	// Start calling the action after all threads have been
	// launched, to achieve maximum parallel execution.
        try {
	    startSignal.countDown();
	    startSignal.await();
	    result = action.call();
        } catch (Throwable e) {
	    exception = e;
        }
    }
}
