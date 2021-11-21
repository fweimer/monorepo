package enyo.io.terminfo;

/**
 * A boolean terminfo capability.
 *
 * @see TermFile#get(BooleanCapability)
 */
public enum BooleanCapability {
    // Order must match the terminal file layout.

    /**
     * {@link UntypedStringCapability#CURSOR_LEFT} moves the cursor
     * back from the first to the last column.
     */
    AUTO_LEFT_MARGIN,

    /**
     * Writing to the right-most column automatically advances to the
     * left-mostcolumn.
     * @see UntypedStringCapability#ENTER_AM_MODE
     */
    AUTO_RIGHT_MARGIN,

    /**
     * Terminal does not support Esc and Ctrl-C.
     * Instead F1 and F2 should be used.
     */
    NO_ESC_CTLC,

    /**
     * Standout property is not removed by overwriting characters.
     */
    CEOL_STANDOUT_GLITCH,

    /**
     * Beyond column 80, {@link UntypedStringCapability#NEWLINE} has no
     * effect.
     */
    EAT_NEWLINE_GLITCH,

    /**
     * Overstrike characters can be removed with a blank.
     */
    ERASE_OVERSTRIKE,

    /**
     * Generic line type.
     */
    GENERIC_TYPE,

    /**
     * The terminal is a printer.
     */
    HARD_COPY,

    /**
     * The terminal has a Meta key.
     * The Meta key sets the most-significant bit in the input characters.
     */
    HAS_META_KEY,

    /**
     * The terminal has a status line.
     * @see StringCapability.Int#TO_STATUS_LINE
     */
    HAS_STATUS_LINE,

    /**
     * Null characters count in insert mode.
     */
    INSERT_NULL_GLITCH,

    /**
     * The terminal retains character data before the displayed screen
     * contents.
     */
    MEMORY_ABOVE,

    /**
     * The terminal retains character data after the displayed screen
     * contents.
     */
    MEMORY_BELOW,

    /**
     * The terminal supports cursor movements in insert mode.
     * @see UntypedStringCapability#ENTER_INSERT_MODE
     */
    MOVE_INSERT_MODE,

    /**
     * The terminal supports cursor movements in standout mode.
     * @see UntypedStringCapability#ENTER_STANDOUT_MODE
     */
    MOVE_STANDOUT_MODE,

    /**
     * The terminal can over-strike characters.
     */
    OVER_STRIKE,

    /**
     * The status line supports Escape.
     */
    STATUS_LINE_ESC_OK,

    /**
     * Tabs are destructive.
     * {@link UntypedStringCapability#ENTER_STANDOUT_MODE} is special.
     */
    DEST_TABS_MAGIC_SMSO,

    /**
     * {@code '~'} is not printable.
     */
    TILDE_GLITCH,

    /**
     * {@code '_'} does no erase the underlying character.
     */
    TRANSPARENT_UNDERLINE,

    /**
     * The terminal supports XON/XOFF handshaking.
     * @see #NEEDS_XON_XOFF
     * @see UntypedStringCapability#ENTER_XON_MODE
     * @see UntypedStringCapability#XON_CHARACTER
     * @see UntypedStringCapability#XOFF_CHARACTER
     */
    XON_XOFF,

    /**
     * The terminal requires XON/XOFF handshaking.
     * @see #XON_XOFF
     * @see UntypedStringCapability#ENTER_XON_MODE
     */
    NEEDS_XON_XOFF,

    /**
     * Characters sent to the printer do not show up on the screen.
     */
    PRTR_SILENT,

    /**
     * The terminal has a poorly visible indicator for the cursor.
     */
    HARD_CURSOR,

    /**
     * {@link UntypedStringCapability#ENTER_CA_MODE} does not undo
     * {@link UntypedStringCapability#EXIT_CA_MODE}.
     */
    NON_REV_RMCUP,

    /**
     * No pad char is defined.
     * Otherwise, with {@link UntypedStringCapability#PAD_CHAR}
     * undefined, a null byte would be used.
     */
    NO_PAD_CHAR,

    /**
     * The scroll region is not destructive.
     * @see UntypedStringCapability#CHANGE_SCROLL_REGION
     */
    NON_DEST_SCROLL_REGION,

    /**
     * Pre-defined colors can be redefined.
     */
    CAN_CHANGE,

    /**
     * Erasing the screen uses the background color.
     */
    BACK_COLOR_ERASE,

    /**
     * Terminal uses HLS colors.
     */
    HUE_LIGHTNESS_SATURATION,

    /**
     * {@link StringCapability.Int#COLUMN_ADDRESS} and
     * {@link UntypedStringCapability#MICRO_COLUMN_ADDRESS} support
     * positive arguments only.
     */
    COL_ADDR_GLITCH,

    /**
     * Whether {@link UntypedStringCapability#CARRIAGE_RETURN} exits
     * micro mode.
     * @see UntypedStringCapability#EXIT_MICRO_MODE
     */
    CR_CANCELS_MICRO_MODE,

    /**
     * Character set changes need manual operator intervention.
     */
    HAS_PRINT_WHEEL,

    /**
     * {@link StringCapability.Int#ROW_ADDRESS} and
     * {@link StringCapability.Int#MICRO_ROW_ADDRESS} support
     * positive arguments only.
     */
    ROW_ADDR_GLITCH,

    /**
     * Printing a character to the last column triggers
     * {@linkplain UntypedStringCapability#CARRIAGE_RETURN carriage return}.
     */
    SEMI_AUTO_RIGHT_MARGIN,

    /**
     * {@link StringCapability.Int#CHANGE_CHAR_PITCH} changes resolution.
     */
    CPI_CHANGES_RES,

    /**
     * {@link StringCapability.Int#CHANGE_LINE_PITCH} changes resolution.
     */
    LPI_CHANGES_RES,
    ;

    static final int COUNT = values().length;
}
