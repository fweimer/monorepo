package enyo.linux.alsa.tests;

import enyo.linux.alsa.PCM;

public final class RefinePCM {
    public static void main(String[] args) {
        int pos = 0;
        String deviceName = args[pos++];
        PCM.Format format = PCM.Format.valueOf(args[pos++]);
        int channels = Integer.parseInt(args[pos++]);
        int rate = Integer.parseInt(args[pos++]);
        long bufferMs;
        if (pos < args.length)
            bufferMs = Integer.parseInt(args[pos++]);
        else
            bufferMs = 0;
        int periods;
        if (pos < args.length)
            periods = Integer.parseInt(args[pos++]);
        else
            periods = 0;

        PCM.Device dev = new PCM.Device(deviceName);
        try (PCM pcm = new PCM(dev)) {
            PCM.Params params = new PCM.Params();
            pcm.refine(params);

            {
                var access = params.access();
                if (access.contains(PCM.Access.RW_INTERLEAVED)) {
                    access.clear();
                    access.add(PCM.Access.RW_INTERLEAVED);
                    pcm.refine(params);
                } else
                    System.err.format(
                        "warning: RW_INTERLEAVED access mode unsupported: %s%n",
                        access);
            }

            var supportedFormats = params.format();
            if (supportedFormats.contains(format)) {
                supportedFormats.clear();
                supportedFormats.add(format);
                pcm.refine(params);
            } else
               System.err.format(
                   "warning: format %s not supported, available: %s%n",
                   format, supportedFormats);
            PCM.Interval supportedChannels =
                params.interval(PCM.IntervalIndex.CHANNELS);
            if (supportedChannels.contains(channels)) {
                supportedChannels.set(channels);
                pcm.refine(params);
            } else
               System.err.format(
                   "warning: channel count %d not supported, available: %s%n",
                   channels, supportedChannels);
            PCM.Interval supportedRates =
                params.interval(PCM.IntervalIndex.RATE);
            if (supportedRates.contains(rate)) {
                supportedRates.set(rate);
                pcm.refine(params);
            } else
               System.err.format(
                   "warning: rate %d not supported, available: %s%n",
                   rate, supportedRates);
            if (bufferMs > 0) {
                long bufferUs = Math.multiplyExact(bufferMs, 1000);
                PCM.Interval time =
                    params.interval(PCM.IntervalIndex.BUFFER_TIME);
                if (time.contains(bufferUs)) {
                    time.set(bufferUs);
                    pcm.refine(params);
                } else
                    System.err.format(
                        "warning: %d ms (%d us) buffer time not supported,"
                        + " available: %s%n",
                        bufferMs, bufferUs, time);
            }
            if (periods > 0) {
                PCM.Interval ival = params.interval(PCM.IntervalIndex.PERIODS);
                if (ival.contains(periods)) {
                    ival.set(periods);
                    pcm.refine(params);
                } else
                    System.err.format(
                        "warning: %d interrupts per buffer not supported,"
                        + " available: %s%n",
                        periods, ival);
            }

            pcm.set(params);

            System.out.println("Parameters after refinement:");
            ShowCard.dump(params);

            {
                long bufferSize =
                    params.interval(PCM.IntervalIndex.BUFFER_SIZE).min();
                long boundary = Long.highestOneBit(Long.MAX_VALUE / bufferSize)
                    * bufferSize;

                PCM.SoftwareParams swParams = new PCM.SoftwareParams()
                    .frameLimitForWakeup(
                        params.interval(PCM.IntervalIndex.PERIOD_SIZE).min())
                    .stopThreshold(bufferSize - 2);
                pcm.set(swParams);
                System.out.println("Software parameters:");
                ShowCard.dump(swParams);
                pcm.prepare();
            }
        }
    }

    private RefinePCM() { } // No instances.
}
