package enyo.io;

import java.nio.charset.Charset;

/**
 * Extension character sets.
 */
public final class ExtendedCharsets {
    /**
     * Character set for round-tripping decoding and encoding of
     * partially valid UTF-8 sequences.
     *
     * @see <a href="https://www.python.org/dev/peps/pep-0383/">PEP 383</a>
     */
    public static final Charset UTF_8S = new UTF8SCharset();

    // No instances.
    private ExtendedCharsets() { }
}
