package enyo.testing.runtime;

import enyo.testing.ThrowingCallable;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

final class ParallelThread extends Thread {
    private final CountDownLatch startSignal;
    private final CountDownLatch endSignal;
    private final ThrowingCallable<?> action;

    // Set to true before interrupt() is called in response
    // to an exception-throwing thread.
    volatile boolean inInterruptPhase;

    volatile Throwable exception;
    volatile Object result;

    ParallelThread(CountDownLatch startSignal, CountDownLatch endSignal,
        ThrowingCallable<?> action) {
        this.startSignal = startSignal;
        this.endSignal = endSignal;
        this.action = action;
    }

    @Override
    public void run() {
        // Start calling the action after all threads have been
        // launched, to achieve maximum parallel execution.
        try {
            startSignal.countDown();
            try {
                startSignal.await();
            } catch (InterruptedException e) {
                // This can happen after another thread threw an
                // exception, triggering the round of interrupt()
                // calles in Testing.parallelValues().  For
                // consistency, we still want to invoke the callback,
                // so catch the exception here and set the interrupt
                // flag.
                Thread.currentThread().interrupt();
            }
            result = action.call();
            endSignal.countDown();
        } catch (Throwable e) {
            exception = e;
            // Signal premature exit.  It's likely that the other
            // threads are stuck because this thread failed.
            do
                endSignal.countDown();
            while (endSignal.getCount() > 0);
        }
    }
}
