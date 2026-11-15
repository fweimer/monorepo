package enyo.linux.alsa;

/**
 * Buffer underflow while the audio subsystem is reading from a buffer.
 *
 * For example, this can indicate that the application cannot supply
 * data fast enough during playback.
 *
 * <p>To recover from an underflow during
 * {@link PCM#writeInterleaved(java.nio.ByteBuffer)}, an application must
 * invoke {@link PCM#prepare()} before submitting more samples.
 */
public final class UnderflowException extends SoundException {
    /**
     * Creates a new underflow exception.
     */
    public UnderflowException() {
    }

    private static final long serialVersionUID = 0xed7e673e50c5dc90L;
}
