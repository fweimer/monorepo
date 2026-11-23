import enyo.lang.NeedsNativeAccess;

/**
 * Linux UAPI access.
 */
@NeedsNativeAccess
module enyo.linux {
    exports enyo.linux;
    exports enyo.linux.alsa;

    requires transitive enyo.core;
}
