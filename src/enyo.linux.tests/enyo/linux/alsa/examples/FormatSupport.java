package enyo.linux.alsa.examples;

import enyo.io.BufferInput;
import enyo.linux.SyscallException;
import enyo.linux.alsa.PCM;
import enyo.linux.alsa.SoundException;
import enyo.sound.pcm.FrameFormat;
import enyo.sound.pcm.PCMFormat;
import enyo.sound.pcm.PCMTransformation;
import enyo.util.EnumBitSet;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.UnaryOperator;

final class FormatSupport {
    // Need to duplicate PCMTransformation because the latter does
    // not capture the sample rate.
    record Transformation(PCMFormat source, PCMFormat destination,
        UnaryOperator<BufferInput> transformer) {
    }


    /**
     * Tries to locate a cached transformation.
     */
    synchronized Optional<Optional<Transformation>> findTransformation(
        PCMFormat format) {
        return Optional.ofNullable(supported.get(format));
    }

    synchronized Optional<Transformation> findTransformation(
        PCM pcm, PCMFormat format) {
        {
            var cached = findTransformation(format);
            if (cached.isPresent())
                return cached.get();
        }

        var tf = PCMTransformation.find(format.frame(),
            fmt -> {
                try {
                    prepare(pcm, new PCMFormat(fmt, format.rate()));
                    return true;
                } catch (SoundException e) {
                    return false;
                }
            }).map(tf1 -> new Transformation(
                    format,
                    new PCMFormat(tf1.destination(), format.rate()),
                    tf1.transformer()));
        supported.put(format, tf);
        return tf;
    }

    record PrepareResult(int periodBytes) { }

    static PrepareResult prepare(PCM pcm, PCMFormat format) {
        PCM.Format alsaFormat = alsaFormat(format)
            .orElseGet(() -> {
                    throw new SoundException(
                        "Unsupported format: " + format);
                });

        PCM.Params params = new PCM.Params();
        pcm.refine(params);

        set(pcm, params, params.access(), PCM.Access.RW_INTERLEAVED);
        set(pcm, params, params.format(), alsaFormat);
        set(pcm, params, params.subformat(), PCM.Subformat.STD);
        set(pcm, params, PCM.IntervalIndex.CHANNELS,
            format.frame().channels());
        set(pcm, params, PCM.IntervalIndex.RATE, format.rate());
        set(pcm, params, PCM.IntervalIndex.BUFFER_TIME, 100_000);
        setMin(pcm, params, PCM.IntervalIndex.PERIODS, 2);
        pcm.set(params);
        pcm.prepare();

        int periodBytes;
        {
            PCM.Interval ival =
                params.interval(PCM.IntervalIndex.PERIOD_BYTES);
            long periodBytes0 = ival.min();
            if (periodBytes0 <= 0 || periodBytes0 > Integer.MAX_VALUE)
                throw new IllegalStateException(
                    "Could not determine period size in bytes: " + ival);
            periodBytes = (int) periodBytes0;
        }

        long bufferSize = params.interval(PCM.IntervalIndex.BUFFER_SIZE).min();
        long boundary = Long.highestOneBit(Long.MAX_VALUE / bufferSize)
            * bufferSize;
        PCM.SoftwareParams swParams = new PCM.SoftwareParams()
            .frameLimitForWakeup(
                params.interval(PCM.IntervalIndex.PERIOD_SIZE).min())
            .stopThreshold(bufferSize - 2);
        pcm.set(swParams);

        return new PrepareResult(periodBytes);
    }

    private static Optional<PCM.Format> alsaFormat(PCMFormat format) {
        var frame = format.frame();
        var order = frame.order();
        switch (frame.bitsPerSample()) {
        case 8:
            return Optional.of(PCM.Format.S8);
        case 16:
            if (order == ByteOrder.LITTLE_ENDIAN)
                return Optional.of(PCM.Format.S16_LE);
            if (order == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S16_BE);
            break;
        case 24:
            if (order == ByteOrder.LITTLE_ENDIAN)
                return Optional.of(PCM.Format.S24_LE);
            if (order == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S24_BE);
            break;
        case 32:
            if (order == ByteOrder.LITTLE_ENDIAN)
                return Optional.of(PCM.Format.S32_LE);
            if (order == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S32_BE);
            break;
        }
        return Optional.empty();
    }

    private static <E extends Enum<E>> void set(PCM pcm, PCM.Params params,
        EnumBitSet<E> set, E flag) {
        if (!set.contains(flag))
            throw new SoundException(String.format(
                    "%s unsupported in %s%n", flag, set));
        set.clear();
        set.add(flag);
        pcm.refine(params);
    }

    private static void set(PCM pcm, PCM.Params params,
        PCM.IntervalIndex index, int value) {
        PCM.Interval ival = params.interval(index);
        String original = ival.toString();
        if (!ival.contains(value))
            throw new SoundException(String.format(
                    "%d unsupported for %s: %s%n",
                    value, index, original));
        ival.set(value);
        try {
            pcm.refine(params);
        } catch (SyscallException e) {
            throw new SoundException(String.format(
                    "Applying value %d to %s with interval %s",
                    value, index, original), e);
        }
    }

    private static void setMin(PCM pcm, PCM.Params params,
        PCM.IntervalIndex index, int value) {
        PCM.Interval ival = params.interval(index);
        String original = ival.toString();
        if (!ival.contains(value))
            throw new SoundException(String.format(
                    "%d unsupported for %s: %s%n", value, index, original));

        // First set minimum, then maximum, to force an exact value.
        ival.min(value);
        pcm.refine(params);
        ival.set(ival.min());
        try {
            pcm.refine(params);
        } catch (SoundException e) {
            throw new SoundException(String.format(
                    "Applying minimum %d to %s with interval %s",
                    value, index, original), e);
        }
    }

    private static Optional<PCM.Format> toALSA(PCMFormat format) {
        switch (format.frame().bitsPerSample()) {
        case 8:
            if (format.frame().order() == ByteOrder.LITTLE_ENDIAN
                || format.frame().order() == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S8);
            break;
        case 16:
            if (format.frame().order() == ByteOrder.LITTLE_ENDIAN)
                return Optional.of(PCM.Format.S16_LE);
            if (format.frame().order() == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S16_BE);
            break;
        case 24:
            if (format.frame().order() == ByteOrder.LITTLE_ENDIAN)
                return Optional.of(PCM.Format.S24_LE);
            if (format.frame().order() == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S24_BE);
            break;
        case 32:
            if (format.frame().order() == ByteOrder.LITTLE_ENDIAN)
                return Optional.of(PCM.Format.S32_LE);
            if (format.frame().order() == ByteOrder.BIG_ENDIAN)
                return Optional.of(PCM.Format.S32_BE);
            break;
        }
        return Optional.empty();
    }

    private final HashMap<PCMFormat, Optional<Transformation>> supported =
        new HashMap<>();
}
