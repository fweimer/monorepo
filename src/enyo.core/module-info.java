/**
 * Enyo core libraries.
 */
module enyo.core {
    exports enyo.crypto;
    exports enyo.hash;
    exports enyo.lang;
    exports enyo.util;
    exports enyo.util.argparse;
    exports enyo.io;
    exports enyo.io.function;
    exports enyo.io.terminfo;

    provides java.nio.charset.spi.CharsetProvider
        with enyo.provider.ExtendedCharsetProvider;
}
