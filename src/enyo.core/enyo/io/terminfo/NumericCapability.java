package enyo.io.terminfo;

/**
 * A numeric terminfo capability.
 *
 * @see TermFile#get(NumericCapability)
 */
public enum NumericCapability {
    // Order follows the definition in <term.h>.

    /**
     */
    COLUMNS,

    /**
     */
    INIT_TABS,

    /**
     */
    LINES,

    /**
     */
    LINES_OF_MEMORY,

    /**
     */
    MAGIC_COOKIE_GLITCH,

    /**
     */
    PADDING_BAUD_RATE,

    /**
     */
    VIRTUAL_TERMINAL,

    /**
     */
    WIDTH_STATUS_LINE,

    /**
     */
    NUM_LABELS,

    /**
     */
    LABEL_HEIGHT,

    /**
     */
    LABEL_WIDTH,

    /**
     */
    MAX_ATTRIBUTES,

    /**
     */
    MAXIMUM_WINDOWS,

    /**
     */
    MAX_COLORS,

    /**
     */
    MAX_PAIRS,

    /**
     */
    NO_COLOR_VIDEO,

    /**
     */
    BUFFER_CAPACITY,

    /**
     */
    DOT_VERT_SPACING,

    /**
     */
    DOT_HORZ_SPACING,

    /**
     */
    MAX_MICRO_ADDRESS,

    /**
     */
    MAX_MICRO_JUMP,

    /**
     */
    MICRO_COL_SIZE,

    /**
     */
    MICRO_LINE_SIZE,

    /**
     */
    NUMBER_OF_PINS,

    /**
     */
    OUTPUT_RES_CHAR,

    /**
     */
    OUTPUT_RES_LINE,

    /**
     */
    OUTPUT_RES_HORZ_INCH,

    /**
     */
    OUTPUT_RES_VERT_INCH,

    /**
     */
    PRINT_RATE,

    /**
     */
    WIDE_CHAR_SIZE,

    /**
     */
    BUTTONS,

    /**
     */
    BIT_IMAGE_ENTWINING,

    /**
     */
    BIT_IMAGE_TYPE,
    ;

    static final int COUNT = values().length;
}
