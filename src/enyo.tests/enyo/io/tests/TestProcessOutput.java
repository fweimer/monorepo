package enyo.io.tests;

import enyo.io.ProcessOutput;
import enyo.lang.RelaunchVM;
import enyo.testing.Checks;
import enyo.testing.Cost;
import enyo.testing.Resources;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class TestProcessOutput {
    @Test
    public static void testEmptyTake(Testing t) throws InterruptedException {
        testEmpty(t, BlockingQueue::take);
    }

    @Test
    public static void testPollEmpty(Testing t) throws InterruptedException {
        testEmpty(t, mkPollImmediate(t));
    }

    @Test
    public static void testOneDataItem(Testing t) throws InterruptedException {
        testOneDataItem(t, BlockingQueue::take);
        testOneDataItem(t, mkPollImmediate(t));
    }

    @Test
    @Cost(0.5)
    @Resources(threads=3, memory=100)
    public static void testSubprocess(Testing t) throws Exception {
        testSubprocess(t, "unbuffered");
        testSubprocess(t, "buffered");
    }

    @Test
    @Cost(0.5)
    public static void testExceptions(Testing t) throws Exception {
        class Stream extends InputStream {
            final RuntimeException exception = new RuntimeException();
            @Override
            public int read() {
                throw exception;
            }
        }

        Stream[] streams = { new Stream(), new Stream() };
        var queue = new LinkedBlockingQueue<ProcessOutput.Data>();
        var po = new ProcessOutput(streams[0], streams[1], queue);
        try {
            var seen = EnumSet.noneOf(ProcessOutput.Source.class);

            for (int i = 0; i < 2; ++i) {
                var e = queue.take();
                t.examine(seen.add(e.source())).is(true);
                t.examine(e.exception()).isSame(
                    streams[e.source().ordinal()].exception);
                t.examine(e.data()).isNull();
                t.examine(e.isCompleteLine()).is(false);
                t.examine(e.isLastForStream()).is(true);
            }

            t.examine(seen).is(EnumSet.allOf(ProcessOutput.Source.class));
            var e = queue.poll(500, TimeUnit.MILLISECONDS);
            t.examine(e).isNull();

        } finally {
            po.close();
        }
    }

    @Test
    @Cost(0.1)
    public static void testClose(Testing t) throws Exception {
        class Stream extends InputStream {
            volatile boolean interrupted;

            @Override
            public int read() {
                while (true) {
                    t.examine(interrupted).is(false);
                    try {
                        Thread.sleep(60 * 1000);
                        t.failedCheck("sleep completed");
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                    return -1;
                }
            }
        }

        var out = new Stream();
        var err = new Stream();
        var queue = new LinkedBlockingQueue<ProcessOutput.Data>();
        var po = new ProcessOutput(out, err, queue);
        t.examine(out.interrupted).is(false);
        t.examine(err.interrupted).is(false);
        po.close();

        // Wait until the interrupt has propagated to the threads
        // being closed.
        for (int i = 0; i < 100; ++i) {
            if (out.interrupted && err.interrupted)
                break;
            Thread.sleep(100);
        }

        t.examine(out.interrupted).is(true);
        t.examine(err.interrupted).is(true);
    }

    private interface InterruptibleFunction<T, R> {
        R apply(T t) throws InterruptedException;
    }

    private static void testEmpty(Testing t,
        InterruptibleFunction<BlockingQueue<ProcessOutput.Data>,
        ProcessOutput.Data> take)
        throws InterruptedException {
        var queue = new LinkedBlockingQueue<ProcessOutput.Data>();
        var po = new ProcessOutput(
            InputStream.nullInputStream(), InputStream.nullInputStream(),
            queue);
        try {
            var seen = EnumSet.noneOf(ProcessOutput.Source.class);

            var e = take.apply(queue);
            seen.add(e.source());
            t.examine(e.exception()).isNull();
            t.examine(e.data()).isNull();
            t.examine(e.isLastForStream()).is(true);
            t.examine(e.isCompleteLine()).is(false);

            e = take.apply(queue);
            seen.add(e.source());
            t.examine(e.isLastForStream()).is(true);
            t.examine(e.exception()).isNull();
            t.examine(e.data()).isNull();
            t.examine(e.isCompleteLine()).is(false);

            t.examine(seen).is(EnumSet.allOf(ProcessOutput.Source.class));
        } finally {
            po.close();
        }
    }

    private static void testOneDataItem(Testing t,
        InterruptibleFunction<BlockingQueue<ProcessOutput.Data>,
        ProcessOutput.Data> take)
        throws InterruptedException {
        for (var source : ProcessOutput.Source.values()) {
            var output = InputStream.nullInputStream();
            var error = InputStream.nullInputStream();
            var dataStream = new ByteArrayInputStream("abc\n".getBytes());
            if (source == ProcessOutput.Source.OUTPUT)
                output = dataStream;
            else
                error = dataStream;
            var queue = new LinkedBlockingQueue<ProcessOutput.Data>();
            var po = new ProcessOutput(output, error, queue);
            try {
                var seen = EnumSet.noneOf(ProcessOutput.Source.class);

                boolean dataSeen = false;
                for (int i = 0; i < 3; ++i) {
                    var e = take.apply(queue);
                    t.examine(e.exception()).isNull();
                    boolean added = seen.add(e.source());
                    if (e.source() == source && added) {
                        t.examine(e.data()).check(
                            Checks.byteArrayEquals("abc\n".getBytes()));
                        t.examine(e.isLastForStream()).is(false);
                        t.examine(e.isCompleteLine()).is(true);
                        dataSeen = true;
                    } else {
                        t.examine(e.data()).isNull();
                        t.examine(e.isLastForStream()).is(true);
                        t.examine(e.isCompleteLine()).is(false);
                    }
                }

                t.examine(seen).is(EnumSet.allOf(ProcessOutput.Source.class));
                t.examine(dataSeen).is(true);
            } finally {
                po.close();
            }
        }
    }

    // Creates a take-like function that asserts that the poll returns
    // almost immediately.
    private static InterruptibleFunction<BlockingQueue<ProcessOutput.Data>,
        ProcessOutput.Data> mkPollImmediate(Testing t) {
        return q -> {
            long before = System.nanoTime();
            var e = q.poll(10, TimeUnit.SECONDS);
            long after = System.nanoTime();
            t.examine(after - before).check(
                Checks.lessThan(2L * 1000 * 1000 * 1000));
            return e;
        };
    }

    private static void testSubprocess(Testing t, String command)
        throws IOException, InterruptedException {
        var pb = RelaunchVM.processBuilder(
            TestProcessOutput.class, List.of(command));
        var p = pb.start();

        var bytes = new ByteArrayOutputStream[2];
        bytes[0] = new ByteArrayOutputStream();
        bytes[1] = new ByteArrayOutputStream();
        try {
            int terminated = 0;
            var queue = new LinkedBlockingQueue<ProcessOutput.Data>();
            var po = new ProcessOutput(p, queue);
            try {
                while (true) {
                    var e = queue.take();
                    var data = e.data();
                    if (e.exception() != null)
                        t.failedCheck("no exception on " + e.source(),
                            e.exception());
                    if (data != null) {
                        if (!e.isCompleteLine()) {
                            for (int i = 0; i < data.length; ++i)
                                if (data[i] == '\n') {
                                    t.failedCheck("unexpected newline",
                                        i, data);
                                    break;
                                }
                        }
                        bytes[e.source().ordinal()].write(data);
                    }
                    if (e.isLastForStream()) {
                        ++terminated;
                        if (terminated == 2)
                            break;
                    }
                }
            } finally {
                po.close();
            }

            t.examine(p.waitFor()).is(0);
        } finally {
            p.destroyForcibly();
            p.waitFor();
        }


        BiConsumer<ProcessOutput.Source, Character> doCheck = (src, ch) -> {
            var out = new ByteArrayOutputStream();
            for (int i = 0; i < OUTPUT_LINE_COUNT; ++i)
                try {
                    out.write(ch);
                    out.write(Integer.toString(i).getBytes());
                    out.write('\n');
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            var expected = out.toByteArray();
            var actual = bytes[src.ordinal()].toByteArray();
            t.examine(actual).check(Checks.byteArrayEquals(expected));
        };
        doCheck.accept(ProcessOutput.Source.OUTPUT, 'O');
        doCheck.accept(ProcessOutput.Source.ERROR, 'E');
    }

    /**
     * Main program for end-to-end testing.
     */
    public static void main(String[] args) throws Exception {
        switch (args[0]) {
        case "unbuffered":
            parallelWrites(false);
            break;
        case "buffered":
            parallelWrites(true);
            break;
        default:
            throw new IllegalArgumentException(args[0]);
        }
    }

    private static final int OUTPUT_LINE_COUNT = 100000;

    private static void parallelWrites(boolean buffered) throws Exception {
        var latch = new CountDownLatch(2);

        BiConsumer<FileDescriptor, Character> doWrites = (fd, ch) -> {
            try {
                Writer wr = new OutputStreamWriter(new FileOutputStream(fd));
                if (buffered)
                    wr = new BufferedWriter(wr);
                latch.countDown();
                latch.await();
                for (int i = 0; i < OUTPUT_LINE_COUNT; ++i)
                    wr.write(ch + Integer.toString(i) + '\n');
                wr.flush();
            } catch (Throwable e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        };

        var thr = new Thread(() -> doWrites.accept(FileDescriptor.err, 'E'));
        thr.start();
        doWrites.accept(FileDescriptor.out, 'O');
        thr.join();
    }
}
