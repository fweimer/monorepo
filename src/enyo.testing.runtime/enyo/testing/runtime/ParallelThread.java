package enyo.testing.runtime;

import enyo.testing.ThrowingCallable;
import enyo.testing.ThrowingRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

final class ParallelThread extends Thread {
    private final CountDownLatch startSignal;
    private final Object action;
    volatile Throwable exception;
    Object result;

    ParallelThread(CountDownLatch startSignal, Object action) {
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
	    if (action instanceof ThrowingRunnable tr)
		tr.run();
	    else if (action instanceof ThrowingCallable<?> cb)
	        result = cb.call();
	    else
		throw new AssertionError(action.getClass().toString());
        } catch (Throwable e) {
	    exception = e;
        }
    }
}
