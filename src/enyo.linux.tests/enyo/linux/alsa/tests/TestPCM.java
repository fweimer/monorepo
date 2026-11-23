package enyo.linux.alsa.tests;

import enyo.linux.Errno;
import enyo.linux.SyscallException;
import enyo.linux.alsa.PCM;

import enyo.testing.Checks;
import enyo.testing.ExitDuringTest;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.nio.ByteBuffer;
import java.util.List;

public final class TestPCM {
    @Test
    @ExitDuringTest
    public static void testDevices(Testing t) {
        for (PCM.Device dev : PCM.devices()) {
            PCM pcm;
            try {
                pcm = new PCM(dev);
            } catch (SyscallException e) {
                if (List.of(
                        Errno.EACCES,
                        Errno.EBUSY,
                        Errno.EPERM)
                    .contains(e.errno()))
                    continue;
                else
                    throw e;
            }
            try {
                PCM.Info info = pcm.info();
                {
                    var e = t.examine(info).map(PCM.Info::stream);
                    if (dev.toString().endsWith("p"))
                        e.is(PCM.Direction.E.PLAYBACK);
                    if (dev.toString().endsWith("c"))
                        e.is(PCM.Direction.E.CAPTURE);
                }
            } finally {
                pcm.close();
            }
        }
    }

    @Test
    @ExitDuringTest
    public static void testDrain(Testing t) {
        for (PCM.Device dev : PCM.devices()) {
            if (dev.direction() != PCM.Direction.E.PLAYBACK)
                continue;

            PCM pcm;
            try {
                pcm = new PCM(dev);
            } catch (SyscallException e) {
                if (List.of(
                        Errno.EACCES,
                        Errno.EBUSY,
                        Errno.EPERM)
                    .contains(e.errno()))
                    continue;
                else
                    throw e;
            }
            PCM.Params params = null;
            try {
                params = new PCM.Params();
                pcm.refine(params);

                params.access().clear();
                params.access().add(PCM.Access.RW_INTERLEAVED);
                pcm.refine(params);

                params.format().clear();
                params.format().add(PCM.Format.S16_LE);
                pcm.refine(params);

                params.subformat().clear();
                params.subformat().add(PCM.Subformat.STD);
                pcm.refine(params);

                params.interval(PCM.IntervalIndex.CHANNELS).set(2);
                pcm.refine(params);

                params.interval(PCM.IntervalIndex.RATE).set(44100);
                pcm.refine(params);

                params.interval(PCM.IntervalIndex.BUFFER_TIME).set(100_000);
                pcm.refine(params);

                params.interval(PCM.IntervalIndex.PERIODS).min(2);
                pcm.refine(params);
                params.interval(PCM.IntervalIndex.PERIODS).set(2);
                pcm.refine(params);

                pcm.set(params);

                t.examine(params.interval(PCM.IntervalIndex.RATE))
                    .map(PCM.Interval::min, e -> e.is(44100L))
                    .map(PCM.Interval::max, e -> e.is(44100L));

                int periods;
                {
                    PCM.Interval ival =
                        params.interval(PCM.IntervalIndex.PERIODS);
                    long periods0 = ival.min();
                    t.examine(periods0)
                        .check(Checks.inRangeClosed(1L, 100_000L));
                    periods = (int) periods0;
                }

                ByteBuffer periodBuffer;
                {
                    PCM.Interval ival =
                        params.interval(PCM.IntervalIndex.PERIOD_BYTES);
                    long periodBytes = ival.min();
                    t.examine(periodBytes)
                        .check(Checks.inRangeClosed(1L, 44100 * 4L));
                    periodBuffer = ByteBuffer.allocateDirect(
                        (int) periodBytes);
                }
                pcm.prepare();

                double periodMillis =
                    1000.0 * periodBuffer.capacity() / (44100 * 4);
                double bufferMillis = periodMillis * periods;
                long totalStartTime = System.nanoTime();

                for (int i = 0; i < periods; ++i) {
                    long startTime = System.nanoTime();
                    periodBuffer.clear();
                    pcm.writeInterleaved(periodBuffer);
                    long endTime = System.nanoTime();
                    double millis = (endTime - startTime) * 1e-6;
                    t.examine(millis)
                        .boxed()
                        .check(Checks.inRange(0.0, periodMillis / 4));
                }

                // The total submission time must be below a period
                // time.
                {
                    long endTime = System.nanoTime();
                    double millis = (endTime - totalStartTime) * 1e-6;
                    t.examine(millis)
                        .boxed()
                        .check(Checks.inRange(0.0, periodMillis / 2));
                }

                // Submitting one more period must result in a delay.
                {
                    periodBuffer.clear();
                    pcm.writeInterleaved(periodBuffer);
                    long endTime = System.nanoTime();
                    double millis = (endTime - totalStartTime) * 1e-6;
                    t.examine(millis)
                        .boxed()
                        .check(Checks.inRange(periodMillis,
                                1.5 * periodMillis));
                }

                // Draining all samples must correspond to the
                // playback delay.
                {
                    pcm.drain();
                    long endTime = System.nanoTime();
                    double millis = (endTime - totalStartTime) * 1e-6;
                    t.examine(millis)
                        .boxed()
                        .check(Checks.inRange((periods + 1) * periodMillis,
                                (periods + 1.5) * periodMillis));
                }
            } catch (SyscallException e) {
                if (params != null)
                    t.failedCheck("PCM failure", e,
                        params.access(), params.format(),
                        params.interval(PCM.IntervalIndex.CHANNELS),
                        params.interval(PCM.IntervalIndex.RATE),
                        params.interval(PCM.IntervalIndex.BUFFER_TIME),
                        params.interval(PCM.IntervalIndex.PERIODS));
                throw e;
            } finally {
                pcm.close();
            }
        }
    }
}
