package enyo.linux;

/**
 * A file open flag that permits creation.
 * @see Descriptor#open(Path,OpenFlag,OpenOrCreateFlag[])
 * @see Descriptor#openat(Path,OpenFlag,OpenOrCreateFlag[])
 */
public sealed interface OpenOrCreateFlag
    permits OpenFlag, OpenFlag.CreateFlag {
    /**
     * The flag value.
     * @return a power of two, or 0 for the mode constant.
     */
    int value();
    /**
     * The mode value.
     * @return a mode mask (in the range from 0 to 65535 inclusive),
     *   or -1 for an open flag
     */
    int mode();
}
