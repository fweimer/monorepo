/**
 * Enyo core libraries.
 */
module enyo.core {
    exports enyo.crypto;
    exports enyo.lang;
    exports enyo.lang.classfile;
    exports enyo.util;
    exports enyo.io;
    exports enyo.io.function;

    provides java.nio.charset.spi.CharsetProvider
        with enyo.provider.ExtendedCharsetProvider;
}
