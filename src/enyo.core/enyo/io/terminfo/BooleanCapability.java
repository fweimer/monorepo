package enyo.io.terminfo;

/**
 * A boolean terminfo capability.
 *
 * @see TermFile#get(BooleanCapability)
 */
public enum BooleanCapability {
    // Order must match the terminal file layout.

    /**
     * {@link StringCapability#CURSOR_LEFT} moves the cursor back from
     * the first to the last column.
     */
    AUTO_LEFT_MARGIN,

    /**
     * Writing to the right-most column automatically advances to the
     * left-mostcolumn.  */
    AUTO_RIGHT_MARGIN,

    /**
     */
    NO_ESC_CTLC,

    /**
     */
    CEOL_STANDOUT_GLITCH,

    /**
     */
    EAT_NEWLINE_GLITCH,

    /**
     */
    ERASE_OVERSTRIKE,

    /**
     */
    GENERIC_TYPE,

    /**
     */
    HARD_COPY,

    /**
     */
    HAS_META_KEY,

    /**
     */
    HAS_STATUS_LINE,

    /**
     */
    INSERT_NULL_GLITCH,

    /**
     */
    MEMORY_ABOVE,

    /**
     */
    MEMORY_BELOW,

    /**
     */
    MOVE_INSERT_MODE,

    /**
     */
    MOVE_STANDOUT_MODE,

    /**
     */
    OVER_STRIKE,

    /**
     */
    STATUS_LINE_ESC_OK,

    /**
     */
    DEST_TABS_MAGIC_SMSO,

    /**
     */
    TILDE_GLITCH,

    /**
     */
    TRANSPARENT_UNDERLINE,

    /**
     */
    XON_XOFF,

    /**
     */
    NEEDS_XON_XOFF,

    /**
     */
    PRTR_SILENT,

    /**
     */
    HARD_CURSOR,

    /**
     */
    NON_REV_RMCUP,

    /**
     */
    NO_PAD_CHAR,

    /**
     */
    NON_DEST_SCROLL_REGION,

    /**
     */
    CAN_CHANGE,

    /**
     */
    BACK_COLOR_ERASE,

    /**
     */
    HUE_LIGHTNESS_SATURATION,

    /**
     */
    COL_ADDR_GLITCH,

    /**
     */
    CR_CANCELS_MICRO_MODE,

    /**
     */
    HAS_PRINT_WHEEL,

    /**
     */
    ROW_ADDR_GLITCH,

    /**
     */
    SEMI_AUTO_RIGHT_MARGIN,

    /**
     */
    CPI_CHANGES_RES,

    /**
     */
    LPI_CHANGES_RES,
    ;

    static final int COUNT = values().length;
}
