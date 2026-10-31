package enyo.linux.tests;

import enyo.linux.Pipe;

import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;
import enyo.testing.ThrowingRunnable;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public final class TestDescriptorThread {
    final static int THREADS = 5;
    final static int TRIES = 100;
    final static int TRIES_INNER = 100;

    @Test
    @Resources(threads=5)
    public static void testClose(Testing t) {
        var pipe = new AtomicReference<Pipe>();
        var barrier = new CyclicBarrier(THREADS);

            t.parallel(IntStream.range(0, THREADS)
                .mapToObj(_ -> (ThrowingRunnable)
                    () -> {
                        for (int i = 0; i < TRIES; ++i) {
                            // Pick one thread to create the new
                            // pipe and to read from it.
                            int id = barrier.await();
                            if (id == 0)
                                pipe.set(new Pipe());
                            barrier.await();
                            var pipe0 = pipe.get();

                            if (id == 0) {
                                int ret = 0;
                                try {
                                    ret = pipe0.readEnd().read(new byte[1]);
                                } catch (IllegalStateException e) {
                                    t.examine(e)
                                        .map(Exception::getMessage)
                                        .is("Descriptor is closed");
                                }
                                t.examine(ret).is(0);
                            } else {
                                for (int j = 0; j < TRIES_INNER; ++j)
                                    pipe0.readEnd().close();
                                // This unblocks the thread with id == 0.
                                pipe0.writeEnd().close();
                            }
                        }
                    })
                .toList());
    }
}
