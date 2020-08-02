package enyo.provider;

import enyo.io.ExtendedCharsets;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.List;
import java.util.Iterator;

/**
 * Charset provider for extension character sets.
 *
 * <b>Note:</b> This is an internal class, not intended for direct
 * instantiation.
 *
 * @see Charset
 * @see ExtendedCharsets
 */
public final class ExtendedCharsetProvider extends CharsetProvider {
    @Override
    public Charset charsetForName(String charsetName) {
        switch (charsetName) {
        case "UTF-8S":
        case "UTF_8S":
        case "UTF-8b":
            return ExtendedCharsets.UTF_8S;
        default:
            return null;
        }
    }

    @Override
    public Iterator<Charset> charsets() {
        return List.of(ExtendedCharsets.UTF_8S).iterator();
    }
}
