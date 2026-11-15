package enyo.sound.pcm;

import enyo.io.BufferInput;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.function.BiConsumer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Description of a PCM data format transformation.
 *
 * @see FrameFormat
 */
public final class PCMTransformation {
    /**
     * Creates a new transformation description.
     * @param source the source data format
     * @param destination the destination data format
     * @param cost an indicator of the transformation cost
     * @param transformer a function that returns a transformed buffer
     *   input, given another buffer input
     * @throws IllegalArgumentException if the cost is not positive
     * @throws NullPointerException if any of the arguments are null
     */
    public PCMTransformation(FrameFormat source, FrameFormat destination,
        int cost, UnaryOperator<BufferInput> transformer) {
        this.source = Objects.requireNonNull(source, "source");
        this.destination = Objects.requireNonNull(destination, "destination");
        if (cost <= 0)
            throw new IllegalArgumentException("cost: " + cost);
        this.cost = cost;
        this.transformer = Objects.requireNonNull(transformer, "transformer");
    }

    /**
     * Returns the source PCM format.
     * @return a PCM format descriptor
     */
    public FrameFormat source() {
        return source;
    }

    /**
     * Returns the destination PCM format.
     * @return a PCM format descriptor
     */
    public FrameFormat destination() {
        return destination;
    }

    /**
     * Returns an indicator of the cost of performing this transformation.
     * @return a positive integer
     */
    public int cost() {
        return cost;
    }

    /**
     * Returns the transformer that can be applied to a {@link BufferInput}
     * to realize the transformation.
     * @return the buffer input transformer
     */
    public UnaryOperator<BufferInput> transformer() {
        return transformer;
    }

    /**
     * Finds a PCM transformation based on specific transformers.
     *
     * <p>The maxium distance from the {@code source} starting point
     * must be bounded.  That is, repeated application of elements of
     * {@code transformers}, when applied in any order, must
     * eventually return {@link Optional#empty()}.
     * @param source the source format
     * @param solution a predicate function that returns true if a
     *   given PCM format is an acceptable solution
     * @param transformers the transformers to try
     * @return the discovered transformation if present. or an empty optional
     *   if no transformation exists
     */
    public static Optional<PCMTransformation> find(FrameFormat source,
        Predicate<? super FrameFormat> solution,
        Collection<Function<FrameFormat, Optional<PCMTransformation>>>
        transformers) {
        // Perform a breadth-first search, discovering more and more
        // candidate transformations.

        var noop = new PCMTransformation(source, source,
            1, UnaryOperator.identity());
        if (solution.test(source))
            return Optional.of(noop);

        // Transformations found so far, with their destination as the key.
        // (These destinations are not in the destinations set.)
        var byDestination = new HashMap<FrameFormat, PCMTransformation>();
        byDestination.put(source, noop);

        // The best transformation to one of the destination formats
        // found so far.
        PCMTransformation candidate = null;

        // These (intermediate) destinations need re-checking.  Two
        // sets are used to avoid modifying the set currently being
        // iterated over.  At the start of the loop, recheck is the
        // set that should be iterated over.  During the loop, it is
        // the set that needs tobe updated.
        var recheck = new HashSet<FrameFormat>();
        recheck.add(source);
        var recheck2 = new HashSet<FrameFormat>();
        do {
            {
                var tmp = recheck;
                recheck = recheck2;
                recheck2 = tmp;
            }
            recheck.clear();
            for (var recheckDest : recheck2) {
                var baseTransform = byDestination.get(recheckDest);

                // Apply each transformer to the previous destination.
                for (var transformer : transformers) {
                    var tr = transformer.apply(
                        baseTransform.destination());
                    if (!tr.isPresent())
                        continue;

                    var newTransform = tr.get();
                    var newDest = newTransform.destination();
                    var oldTransform = byDestination.get(newDest);

                    // Costs are additive.
                    int newCost = baseTransform.cost()
                        + newTransform.cost();

                    if (candidate != null && candidate.cost() <= newCost)
                        // The new transformation is not an improvement.
                        continue;

                    if (oldTransform == null
                        || newCost < oldTransform.cost()) {
                        // Transformations always start at the
                        // original source.  Run the new
                        // transformation after all the previous ones.
                        var newTr = new PCMTransformation(
                            source, newDest, newCost,
                            in -> newTransform.transformer().apply(
                                baseTransform.transformer().apply(in)));
                        byDestination.put(newDest, newTr);

                        // If the new transformation is a solution,
                        // use it if it is an improvement.
                        if (solution.test(
                                newTransform.destination())) {
                            if (candidate == null
                                || candidate.cost() > newCost)
                                candidate = newTr;
                        } else
                            // We found something new.  Record that
                            // this (intermediate) destination needs
                            // to be transformed further.
                            recheck.add(newDest);
                    }
                }
            }
        } while (!recheck.isEmpty());

        return Optional.ofNullable(candidate);
    }

    /**
     * Finds a PCM transformation using built-in transformers.
     * Padding, endianness conversion and mono-to-stero conversion
     * are used.
     * @param source the source format
     * @param solution a predicate function that returns true if a
     *   given PCM format is an acceptable solution
     * @return the discovered transformation if present. or an empty optional
     *   if no transformation exists
     */
    public static Optional<PCMTransformation> find(FrameFormat source,
        Predicate<? super FrameFormat> solution) {

        return find(source, solution, List.of(
                PCMTransformation::trfMonoToStereo,
                PCMTransformation::trfPadByte,
                PCMTransformation::trfSwapBytes));
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("PCMTransformation(");
        toStringDiff(sb, "bitsPerSample",
            source.bitsPerSample(), destination.bitsPerSample());
        toStringDiff(sb, "channels",
            source.channels(), destination.channels());
        toStringDiff(sb, "order", source.order(), destination.order());
        toStringEqual(sb, "bitsPerSample",
            source.bitsPerSample(), destination.bitsPerSample());
        toStringEqual(sb, "channels",
            source.channels(), destination.channels());
        toStringEqual(sb, "order", source.order(), destination.order());
        sb.append("cost=");
        sb.append(cost);
        sb.append(')');
        return sb.toString();
    }

    private final FrameFormat source;
    private final FrameFormat destination;
    private final int cost;
    private final UnaryOperator<BufferInput> transformer;

    private static void toStringDiff(StringBuilder sb,
        String field, Object source, Object destination) {
        if (!source.equals(destination)) {
            sb.append(field);
            sb.append('=');
            sb.append(source);
            sb.append("=>");
            sb.append(destination);
            sb.append(", ");
        }
    }

    private static void toStringEqual(StringBuilder sb,
        String field, Object source, Object destination) {
        if (source.equals(destination)) {
            sb.append(field);
            sb.append('=');
            sb.append(source);
            sb.append(", ");
        }
    }

    // Assumes byte-sized frames.
    private static int frameSizeInBytes(FrameFormat format) {
        return format.channels() * format.bitsPerSample() / 8;
    }

    private static Optional<PCMTransformation> trfMonoToStereo(
        FrameFormat source) {
        int bitsPerSample = source.bitsPerSample();
        if (source.channels() != 1 || (bitsPerSample % 8) != 0)
            return Optional.empty();

        int bytesPerFrame = bitsPerSample / 8;
        int cost;
        UnaryOperator<BufferInput> trf;
        switch (bytesPerFrame) {
        case 1:
            cost = 2;
            trf = in -> new Transformator(in, frameSizeInBytes(source)) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        while (in.hasRemaining() && out.remaining() >= 2) {
                            byte sample = in.get();
                            out.put(sample);
                            out.put(sample);
                        }
                    }
                };
            break;
        case 2:
            cost = 2;
            trf = in -> new Transformator(in, frameSizeInBytes(source)) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        while (in.remaining() >= 2 && out.remaining() >= 4) {
                            short sample = in.getShort();
                            out.putShort(sample);
                            out.putShort(sample);
                        }
                    }
                };
            break;
        case 4:
            cost = 2;
            trf = in -> new Transformator(in, frameSizeInBytes(source)) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        while (in.remaining() >= 4 && out.remaining() >= 8) {
                            int sample = in.getInt();
                            out.putInt(sample);
                            out.putInt(sample);
                        }
                    }
                };
            break;
        default:
            cost = 2 * bytesPerFrame;
            trf = in -> new Transformator(in, frameSizeInBytes(source)) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        while (in.remaining() >= bytesPerFrame
                            && out.remaining() >= 2 * bytesPerFrame) {
                            for (int pass = 0; pass < 2; ++pass)
                                for (int i = 0; i < bytesPerFrame; ++i)
                                    out.put(in.get(in.position() + i));
                            in.position(in.position() + bytesPerFrame);
                        }
                    }
                };
        }
        return Optional.of(new PCMTransformation(source,
                new FrameFormat(source.bitsPerSample(), 2, source.order()),
                cost, trf));
        }

    private static Optional<PCMTransformation> trfPadByte(FrameFormat source) {
        // We cannot perform padding at non-byte boundaries.  Limit
        // the growth of the padding, otherwise find() will run
        // indefinitely.
        int bitsPerSample = source.bitsPerSample();
        if ((bitsPerSample % 8) != 0 || bitsPerSample == 64)
            return Optional.empty();
        int bytesPerSample = bitsPerSample / 8;
        int channels = source.channels();
        int cost = (bytesPerSample + 1) * channels;
        int inputSize = frameSizeInBytes(source);
        int outputSize = inputSize + channels;
        UnaryOperator<BufferInput> trf;
        if (source.order() == ByteOrder.BIG_ENDIAN)
            trf = in -> new Transformator(in, inputSize) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        while (in.remaining() >= inputSize
                            && out.remaining() >= outputSize)
                            for (int channel = 0; channel < channels; ++channel) {
                                for (int i = 0; i < bytesPerSample; ++i)
                                    out.put(in.get());
                                out.put((byte) 0);
                            }
                    }
                };
        else
            trf = in -> new Transformator(in, inputSize) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        while (in.remaining() >= inputSize
                            && out.remaining() >= outputSize)
                            for (int channel = 0; channel < channels; ++channel) {
                                out.put((byte) 0);
                                for (int i = 0; i < bytesPerSample; ++i)
                                    out.put(in.get());
                            }
                    }
                };
        return Optional.of(new PCMTransformation(source,
                new FrameFormat(bitsPerSample + 8, channels, source.order()),
                cost, trf));
    }

    private static Optional<PCMTransformation> trfSwapBytes(FrameFormat source) {
        int bitsPerSample = source.bitsPerSample();
        if ((bitsPerSample % 8) != 0)
            return Optional.empty();
        ByteOrder swapped;
        if (source.order() == ByteOrder.BIG_ENDIAN)
            swapped = ByteOrder.LITTLE_ENDIAN;
        else
            swapped = ByteOrder.BIG_ENDIAN;

        int channels = source.channels();
        int cost;
        UnaryOperator<BufferInput> trf;
        if (bitsPerSample == 8) {
            // If samples are just one byte, byte order does not matter.
            cost = 1;
            trf = UnaryOperator.identity();
        } else {
            int bytesPerSample = bitsPerSample / 8;
            cost = channels * bytesPerSample;

            trf = in -> new Transformator(in, frameSizeInBytes(source)) {
                    @Override
                    void transform(ByteBuffer in, ByteBuffer out) {
                        for (int channel = 0; channel < channels; ++channel) {
                            int position = in.position();
                            for (int i = position + bytesPerSample - 1;
                                 i >= position; --i)
                                out.put(in.get(i));
                            in.position(position + bytesPerSample);
                        }
                    }
                };
        }

        return Optional.of(new PCMTransformation(source,
                new FrameFormat(source.bitsPerSample(), channels, swapped),
                cost, trf));
    }

    private static abstract class Transformator implements BufferInput {
        private final BufferInput source;
        private final int inputFrameSizeInBytes;
        private ByteBuffer buffer;

        Transformator(BufferInput source, int inputFrameSizeInBytes) {
            this.source = Objects.requireNonNull(source);
            this.inputFrameSizeInBytes = inputFrameSizeInBytes;
        }

        abstract void transform(ByteBuffer in, ByteBuffer out);

        @Override
        public ByteBuffer fillBuffer(int minRemaining) throws IOException {
            if (minRemaining < 0)
                throw new IllegalArgumentException(
                    "minRemaining: " + minRemaining);
            if (buffer == null) {
                var originalBuffer = source.fillBuffer(0);
                // None of the current transformations reduce the
                // input size, so the input capacity seems like a good
                // indicator.  Provide a minimum buffer size as the
                // starting point of the exponential resizing below.
                buffer = ByteBuffer.allocate(
                    Math.max(originalBuffer.capacity(), 16));
                buffer.limit(0);
            }

            // Make sure that the requested number of bytes is
            // available in the buffer.
            if (minRemaining > 0 && buffer.remaining() >= minRemaining
                || (minRemaining == 0 && buffer.hasRemaining()))
                return buffer;

            // Move the existing data to the start of the buffer.
            buffer.compact();

            do {
                // Make sure that if data is available, it matches at
                // least the frame size.  This ensures forward
                // progress on the input side.  This could raise
                // EOFException slightly prematurely, but the stream
                // is necessarily truncated/corrupted afterwards
                // anyway.
                var src = source.fillBuffer(0);
                if (!src.hasRemaining() && minRemaining == 0)
                    break;
                if (src.remaining() > 0
                    || src.remaining() < inputFrameSizeInBytes)
                    src = source.fillBuffer(inputFrameSizeInBytes);

                do {
                    int oldPosition = buffer.position();
                    transform(src, buffer);
                    if (oldPosition == buffer.position()) {
                        // The transformation did not make progress.
                        // Since we offered at least one completed frame
                        // in the put, the output is not large enough.
                        // Increase the buffer size.
                        int oldCapacity = buffer.capacity();
                        var newBuffer = ByteBuffer.allocate(
                            oldCapacity + oldCapacity / 2);
                        newBuffer.put(buffer.flip());
                        buffer = newBuffer;
                    } else
                        break;
                } while (src.remaining() >= inputFrameSizeInBytes);
            } while (buffer.position() < minRemaining);
            return buffer.flip();
        }

        @Override
        public void close() throws IOException {
            source.close();
        }
    }
}
