package enyo.sound.pcm;

import java.util.Objects;

/**
 * Description of the format of a PCM data stream.
 *
 * This combines the {@linkplain FrameFormat frame layout} description
 * with the frame rate.
 *
 * @param frame the frame format
 * @param rate the frame rate, in Hz
 */
public record PCMFormat(FrameFormat frame, int rate) {
    /**
     * Creates a new PCM format description.
     * @throws IllegalArgumentException if {@code rate} is not positive
     * @throws NullPointerException if {@code frame} is null
     */
    public PCMFormat {
        Objects.requireNonNull(frame, "frame");
        if (rate < 1)
            throw new IllegalArgumentException("rate: " + rate);
    }
}
