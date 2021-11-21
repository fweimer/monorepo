package enyo.io.terminfo;

/**
 * A numeric terminfo capability.
 *
 * @see TermFile#get(NumericCapability)
 */
public enum NumericCapability {
    // Order follows the definition in <term.h>.

    /**
     * Columns per line.
     */
    COLUMNS,

    /**
     * Distance in spaces between default tabs.
     */
    INIT_TABS,

    /**
     * Rows on the screen.
     */
    LINES,

    /**
     * Rows that can be stored in memory.
     */
    LINES_OF_MEMORY,

    /**
     * Count of blanks produced by
     * {@link UntypedStringCapability#ENTER_STANDOUT_MODE} or
     * {@link UntypedStringCapability#EXIT_STANDOUT_MODE}.
     */
    MAGIC_COOKIE_GLITCH,

    /**
     * Minimum baud rate which requires padding.
     */
    PADDING_BAUD_RATE,

    /**
     * Virtual terminal number.
     */
    VIRTUAL_TERMINAL,

    /**
     * Columns in the status line.
     */
    WIDTH_STATUS_LINE,

    /**
     * Number of labels on the screen.
     */
    NUM_LABELS,

    /**
     * Rows per label.
     */
    LABEL_HEIGHT,

    /**
     * Columns per label.
     */
    LABEL_WIDTH,

    /**
     * Number of supported simultaneous attributes.
     */
    MAX_ATTRIBUTES,

    /**
     * Maximum number of windows that can be created.
     */
    MAXIMUM_WINDOWS,

    /**
     * Maximum number of colors that can be shown on the screen.
     */
    MAX_COLORS,

    /**
     * Maximum number of foreground/background color pairs that can be
     * shown on the screen.
     */
    MAX_PAIRS,

    /**
     * Bit mask of modes that do not work with color.
     * <table>
     * <tr><th>Bit position (0 is LSB)<th>Capability/Attribute</tr>
     * <tr><td>0
     *     <td>{@see UntypedStringCapability#ENTER_STANDOUT_MODE}</td>
     *     </tr>
     * <tr><td>1
     *     <td>{@see UntypedStringCapability#ENTER_UNDERLINE_MODE}</td>
     *     </tr>
     * <tr><td>2
     *     <td>{@see UntypedStringCapability#ENTER_REVERSE_MODE}</td>
     *     </tr>
     * <tr><td>3
     *     <td>{@see UntypedStringCapability#ENTER_BLINK_MODE}</td>
     *     </tr>
     * <tr><td>4
     *     <td>{@see UntypedStringCapability#ENTER_DIM_MODE}</td>
     *     </tr>
     * <tr><td>5
     *     <td>{@see UntypedStringCapability#ENTER_BOLD_MODE}</td>
     *     </tr>
     * <tr><td>6
     *     <td>{@see UntypedStringCapability#ENTER_SECURE_MODE}</td>
     *     </tr>
     * <tr><td>7
     *     <td>{@see UntypedStringCapability#ENTER_PROTECTED_MODE}</td>
     *     </tr>
     * <tr><td>8
     *     <td>{@see UntypedStringCapability#ENTER_ALT_CHARSET_MODE}</td>
     *     </tr>
     * <tr><td>9
     *     <td>{@see UntypedStringCapability#ENTER_HORIZONTAL_HL_MODE}</td>
     *     </tr>
     * <tr><td>10
     *     <td>{@see UntypedStringCapability#ENTER_LEFT_HL_MODE}</td>
     *     </tr>
     * <tr><td>11
     *     <td>{@see UntypedStringCapability#ENTER_LOW_HL_MODE}</td>
     *     </tr>
     * <tr><td>12
     *     <td>{@see UntypedStringCapability#ENTER_RIGHT_HL_MODE}</td>
     *     </tr>
     * <tr><td>13
     *     <td>{@see UntypedStringCapability#ENTER_RIGHT_HL_MODE}</td>
     *     </tr>
     * <tr><td>14
     *     <td>{@see UntypedStringCapability#ENTER_TOP_HL_MODE}</td>
     *     </tr>
     * <tr><td>15
     *     <td>{@see UntypedStringCapability#ENTER_ITALICS_MODE}</td>
     *     </tr>
     * <caption>Meaning of bits in {@code NO_COLOR_VIDEO}</caption>
     * </table>
     * @see UntypedStringCapability#SET_ATTRIBUTES
     */
    NO_COLOR_VIDEO,

    /**
     * Size in bytes of the buffer before printing starts.
     * Used to describe printers in System V Release 4.
     */
    BUFFER_CAPACITY,

    /**
     * Vertical resolution in dots per inch.
     * Used to describe printers in System V Release 4.
     */
    DOT_VERT_SPACING,

    /**
     * Horizontal resolution in dots per inch.
     * Used to describe printers in System V Release 4.
     */
    DOT_HORZ_SPACING,

    /**
     * Maximum value for
     * {@link UntypedStringCapability#MICRO_COLUMN_ADDRESS} and
     * {@link StringCapability.Int#MICRO_ROW_ADDRESS}.
     * Used to describe printers in System V Release 4.
     */
    MAX_MICRO_ADDRESS,

    /**
     * Maximum value for
     * {@link UntypedStringCapability#PARM_DOWN_MICRO},
     * {@link UntypedStringCapability#PARM_LEFT_MICRO},
     * {@link UntypedStringCapability#PARM_RIGHT_MICRO},
     * {@link UntypedStringCapability#PARM_UP_MICRO}.
     * Used to describe printers in System V Release 4.
     */
    MAX_MICRO_JUMP,

    /**
     * Size of one column in micro-steps in micro mode.
     * Used to describe printers in System V Release 4.
     */
    MICRO_COL_SIZE,

    /**
     * Size of one line in micro-steps in micro mode.
     * Used to describe printers in System V Release 4.
     */
    MICRO_LINE_SIZE,

    /**
     * Number of dots in the print matrix.
     * Used to describe printers in System V Release 4.
     */
    NUMBER_OF_PINS,

    /**
     * Horizontal resolution.  Measured in units per line.
     */
    OUTPUT_RES_CHAR,

    /**
     * Vertical resolution.  Measured in units per line.
     */
    OUTPUT_RES_LINE,

    /**
     * Horizontal resolution.  Measured in units per inch.
     */
    OUTPUT_RES_HORZ_INCH,

    /**
     * Vertical resolution.  Measured in units per inch.
     */
    OUTPUT_RES_VERT_INCH,

    /**
     * Printing rate in characters-per-second (cps).
     * Used to describe printers in System V Release 4.
     */
    PRINT_RATE,

    /**
     * Number of columns consumed by characters in double-width mode.
     */
    WIDE_CHAR_SIZE,

    /**
     * Number of mouse buttons.
     */
    BUTTONS,

    /**
     * Passes per image row.
     */
    BIT_IMAGE_ENTWINING,

    /**
     * Type number of the bit image device.
     */
    BIT_IMAGE_TYPE,
    ;

    static final int COUNT = values().length;
}
