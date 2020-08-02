package enyo.build;

import enyo.testing.introspect.ScheduledTest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Thread class for processing test queues.
 */
final class QueueProcessor extends Thread {
    QueueProcessor(GlobalTestingState global, QueueReader queue) {
        this.global = global;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            run0();
        } catch (Throwable e) {
            threadException = e;
        }
    }

    Throwable threadException;
    volatile ScheduledTest currentTest;
    int errors;

    private final GlobalTestingState global;
    private final QueueReader queue;

    private void run0() throws IOException, InterruptedException {
        while (true) {
            if (global.stopRequested())
                break;
            Optional<ScheduledTest> test = queue.read();
            if (test.isPresent())
                process(test.orElseThrow());
            else
                break;
        }
    }

    private void process(ScheduledTest test)
        throws IOException, InterruptedException {
        global.resources().withResources(
            test.resources(), () -> process0(test));
    }

    private void process0(ScheduledTest test) throws IOException {
        // TODO: Inhibit VM exit if requested by test here.

        boolean exitRequested = false;

        // Invoke the tests.  TODO: Only one test per scheduling group
        // at present.
        {
            if (global.stopRequested())
                return;

            currentTest = test;

            if (!RunTests.runTest(global.testLayer(), test.locator()))
                ++errors;

            currentTest = null;

            global.oneTestCompleted();
        }


        // TODO: Remove VM exit inhibitor here (see above).
    }
}
