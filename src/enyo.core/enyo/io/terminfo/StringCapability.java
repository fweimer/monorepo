package enyo.io.terminfo;

/**
 * A string-value terminfo capability.
 *
 * @see TermFile#get(StringCapability)
 */
public enum StringCapability {
    // Order must match the terminal file layout.

    /**
     */
    BACK_TAB,

    /**
     */
    BELL,


    /**
     */
    CARRIAGE_RETURN,

    /**
     */
    CHANGE_SCROLL_REGION(2),

    /**
     */
    CLEAR_ALL_TABS,

    /**
     */
    CLEAR_SCREEN,

    /**
     */
    CLR_EOL,

    /**
     */
    CLR_EOS,

    /**
     */
    COLUMN_ADDRESS(1),

    /**
     */
    COMMAND_CHARACTER,

    /**
     */
    CURSOR_ADDRESS(2),

    /**
     */
    CURSOR_DOWN,

    /**
     */
    CURSOR_HOME,

    /**
     */
    CURSOR_INVISIBLE,

    /**
     */
    CURSOR_LEFT,

    /**
     */
    CURSOR_MEM_ADDRESS(2),

    /**
     */
    CURSOR_NORMAL,

    /**
     */
    CURSOR_RIGHT,

    /**
     */
    CURSOR_TO_LL,

    /**
     */
    CURSOR_UP,

    /**
     */
    CURSOR_VISIBLE,

    /**
     */
    DELETE_CHARACTER,

    /**
     */
    DELETE_LINE(1),

    /**
     */
    DIS_STATUS_LINE,

    /**
     */
    DOWN_HALF_LINE,

    /**
     */
    ENTER_ALT_CHARSET_MODE,

    /**
     */
    ENTER_BLINK_MODE,

    /**
     */
    ENTER_BOLD_MODE,

    /**
     */
    ENTER_CA_MODE(2),

    /**
     */
    ENTER_DELETE_MODE,

    /**
     */
    ENTER_DIM_MODE,

    /**
     */
    ENTER_INSERT_MODE,

    /**
     */
    ENTER_SECURE_MODE,

    /**
     */
    ENTER_PROTECTED_MODE,

    /**
     */
    ENTER_REVERSE_MODE,

    /**
     */
    ENTER_STANDOUT_MODE,

    /**
     */
    ENTER_UNDERLINE_MODE,

    /**
     */
    ERASE_CHARS(1),

    /**
     */
    EXIT_ALT_CHARSET_MODE,

    /**
     */
    EXIT_ATTRIBUTE_MODE,

    /**
     */
    EXIT_CA_MODE(2),

    /**
     */
    EXIT_DELETE_MODE,

    /**
     */
    EXIT_INSERT_MODE,

    /**
     */
    EXIT_STANDOUT_MODE,

    /**
     */
    EXIT_UNDERLINE_MODE,

    /**
     */
    FLASH_SCREEN,

    /**
     */
    FORM_FEED,

    /**
     */
    FROM_STATUS_LINE,

    /**
     */
    INIT_1STRING,

    /**
     */
    INIT_2STRING,

    /**
     */
    INIT_3STRING,

    /**
     */
    INIT_FILE,

    /**
     */
    INSERT_CHARACTER,

    /**
     */
    INSERT_LINE,

    /**
     */
    INSERT_PADDING,

    /**
     */
    KEY_BACKSPACE,

    /**
     */
    KEY_CATAB,

    /**
     */
    KEY_CLEAR,

    /**
     */
    KEY_CTAB,

    /**
     */
    KEY_DC,

    /**
     */
    KEY_DL,

    /**
     */
    KEY_DOWN,

    /**
     */
    KEY_EIC,

    /**
     */
    KEY_EOL,

    /**
     */
    KEY_EOS,

    /**
     */
    KEY_F0,

    /**
     */
    KEY_F1,

    /**
     */
    KEY_F10,

    /**
     */
    KEY_F2,

    /**
     */
    KEY_F3,

    /**
     */
    KEY_F4,

    /**
     */
    KEY_F5,

    /**
     */
    KEY_F6,

    /**
     */
    KEY_F7,

    /**
     */
    KEY_F8,

    /**
     */
    KEY_F9,

    /**
     */
    KEY_HOME,

    /**
     */
    KEY_IC,

    /**
     */
    KEY_IL,

    /**
     */
    KEY_LEFT,

    /**
     */
    KEY_LL,

    /**
     */
    KEY_NPAGE,

    /**
     */
    KEY_PPAGE,

    /**
     */
    KEY_RIGHT,

    /**
     */
    KEY_SF,

    /**
     */
    KEY_SR,

    /**
     */
    KEY_STAB,

    /**
     */
    KEY_UP,

    /**
     */
    KEYPAD_LOCAL,

    /**
     */
    KEYPAD_XMIT,

    /**
     */
    LAB_F0,

    /**
     */
    LAB_F1,

    /**
     */
    LAB_F10,

    /**
     */
    LAB_F2,

    /**
     */
    LAB_F3,

    /**
     */
    LAB_F4,

    /**
     */
    LAB_F5,

    /**
     */
    LAB_F6,

    /**
     */
    LAB_F7,

    /**
     */
    LAB_F8,

    /**
     */
    LAB_F9,

    /**
     */
    META_OFF,

    /**
     */
    META_ON,

    /**
     */
    NEWLINE,

    /**
     */
    PAD_CHAR,

    /**
     */
    PARM_DCH(1),

    /**
     */
    PARM_DELETE_LINE(1),

    /**
     */
    PARM_DOWN_CURSOR(1),

    /**
     */
    PARM_ICH(1),

    /**
     */
    PARM_INDEX(1),

    /**
     */
    PARM_INSERT_LINE(1),

    /**
     */
    PARM_LEFT_CURSOR(1),

    /**
     */
    PARM_RIGHT_CURSOR(1),

    /**
     */
    PARM_RINDEX(1),

    /**
     */
    PARM_UP_CURSOR(1),

    /**
     */
    PKEY_KEY(2),

    /**
     */
    PKEY_LOCAL(2),

    /**
     */
    PKEY_XMIT(2),

    /**
     */
    PRINT_SCREEN,

    /**
     */
    PRTR_OFF,

    /**
     */
    PRTR_ON,

    /**
     */
    REPEAT_CHAR(2),

    /**
     */
    RESET_1STRING,

    /**
     */
    RESET_2STRING,

    /**
     */
    RESET_3STRING,

    /**
     */
    RESET_FILE,

    /**
     */
    RESTORE_CURSOR,

    /**
     */
    ROW_ADDRESS(1),

    /**
     */
    SAVE_CURSOR,

    /**
     */
    SCROLL_FORWARD,

    /**
     */
    SCROLL_REVERSE,

    /**
     */
    SET_ATTRIBUTES(9),

    /**
     */
    SET_TAB,

    /**
     */
    SET_WINDOW(4),

    /**
     */
    TAB,

    /**
     */
    TO_STATUS_LINE(1),

    /**
     */
    UNDERLINE_CHAR,

    /**
     */
    UP_HALF_LINE,

    /**
     */
    INIT_PROG,

    /**
     */
    KEY_A1,

    /**
     */
    KEY_A3,

    /**
     */
    KEY_B2,

    /**
     */
    KEY_C1,

    /**
     */
    KEY_C3,

    /**
     */
    PRTR_NON(1),

    /**
     */
    CHAR_PADDING,

    /**
     */
    ACS_CHARS,

    /**
     */
    PLAB_NORM(2),

    /**
     */
    KEY_BTAB,

    /**
     */
    ENTER_XON_MODE,

    /**
     */
    EXIT_XON_MODE,

    /**
     */
    ENTER_AM_MODE,

    /**
     */
    EXIT_AM_MODE,

    /**
     */
    XON_CHARACTER,

    /**
     */
    XOFF_CHARACTER,

    /**
     */
    ENA_ACS,

    /**
     */
    LABEL_ON,

    /**
     */
    LABEL_OFF,

    /**
     */
    KEY_BEG,

    /**
     */
    KEY_CANCEL,

    /**
     */
    KEY_CLOSE,

    /**
     */
    KEY_COMMAND,

    /**
     */
    KEY_COPY,

    /**
     */
    KEY_CREATE,

    /**
     */
    KEY_END,

    /**
     */
    KEY_ENTER,

    /**
     */
    KEY_EXIT,

    /**
     */
    KEY_FIND,

    /**
     */
    KEY_HELP,

    /**
     */
    KEY_MARK,

    /**
     */
    KEY_MESSAGE,

    /**
     */
    KEY_MOVE,

    /**
     */
    KEY_NEXT,

    /**
     */
    KEY_OPEN,

    /**
     */
    KEY_OPTIONS,

    /**
     */
    KEY_PREVIOUS,

    /**
     */
    KEY_PRINT,

    /**
     */
    KEY_REDO,

    /**
     */
    KEY_REFERENCE,

    /**
     */
    KEY_REFRESH,

    /**
     */
    KEY_REPLACE,

    /**
     */
    KEY_RESTART,

    /**
     */
    KEY_RESUME,

    /**
     */
    KEY_SAVE,

    /**
     */
    KEY_SUSPEND,

    /**
     */
    KEY_UNDO,

    /**
     */
    KEY_SBEG,

    /**
     */
    KEY_SCANCEL,

    /**
     */
    KEY_SCOMMAND,

    /**
     */
    KEY_SCOPY,

    /**
     */
    KEY_SCREATE,

    /**
     */
    KEY_SDC,

    /**
     */
    KEY_SDL,

    /**
     */
    KEY_SELECT,

    /**
     */
    KEY_SEND,

    /**
     */
    KEY_SEOL,

    /**
     */
    KEY_SEXIT,

    /**
     */
    KEY_SFIND,

    /**
     */
    KEY_SHELP,

    /**
     */
    KEY_SHOME,

    /**
     */
    KEY_SIC,

    /**
     */
    KEY_SLEFT,

    /**
     */
    KEY_SMESSAGE,

    /**
     */
    KEY_SMOVE,

    /**
     */
    KEY_SNEXT,

    /**
     */
    KEY_SOPTIONS,

    /**
     */
    KEY_SPREVIOUS,

    /**
     */
    KEY_SPRINT,

    /**
     */
    KEY_SREDO,

    /**
     */
    KEY_SREPLACE,

    /**
     */
    KEY_SRIGHT,

    /**
     */
    KEY_SRSUME,

    /**
     */
    KEY_SSAVE,

    /**
     */
    KEY_SSUSPEND,

    /**
     */
    KEY_SUNDO,

    /**
     */
    REQ_FOR_INPUT,

    /**
     */
    KEY_F11,

    /**
     */
    KEY_F12,

    /**
     */
    KEY_F13,

    /**
     */
    KEY_F14,

    /**
     */
    KEY_F15,

    /**
     */
    KEY_F16,

    /**
     */
    KEY_F17,

    /**
     */
    KEY_F18,

    /**
     */
    KEY_F19,

    /**
     */
    KEY_F20,

    /**
     */
    KEY_F21,

    /**
     */
    KEY_F22,

    /**
     */
    KEY_F23,

    /**
     */
    KEY_F24,

    /**
     */
    KEY_F25,

    /**
     */
    KEY_F26,

    /**
     */
    KEY_F27,

    /**
     */
    KEY_F28,

    /**
     */
    KEY_F29,

    /**
     */
    KEY_F30,

    /**
     */
    KEY_F31,

    /**
     */
    KEY_F32,

    /**
     */
    KEY_F33,

    /**
     */
    KEY_F34,

    /**
     */
    KEY_F35,

    /**
     */
    KEY_F36,

    /**
     */
    KEY_F37,

    /**
     */
    KEY_F38,

    /**
     */
    KEY_F39,

    /**
     */
    KEY_F40,

    /**
     */
    KEY_F41,

    /**
     */
    KEY_F42,

    /**
     */
    KEY_F43,

    /**
     */
    KEY_F44,

    /**
     */
    KEY_F45,

    /**
     */
    KEY_F46,

    /**
     */
    KEY_F47,

    /**
     */
    KEY_F48,

    /**
     */
    KEY_F49,

    /**
     */
    KEY_F50,

    /**
     */
    KEY_F51,

    /**
     */
    KEY_F52,

    /**
     */
    KEY_F53,

    /**
     */
    KEY_F54,

    /**
     */
    KEY_F55,

    /**
     */
    KEY_F56,

    /**
     */
    KEY_F57,

    /**
     */
    KEY_F58,

    /**
     */
    KEY_F59,

    /**
     */
    KEY_F60,

    /**
     */
    KEY_F61,

    /**
     */
    KEY_F62,

    /**
     */
    KEY_F63,

    /**
     */
    CLR_BOL,

    /**
     */
    CLEAR_MARGINS,

    /**
     */
    SET_LEFT_MARGIN,

    /**
     */
    SET_RIGHT_MARGIN,

    /**
     */
    LABEL_FORMAT,

    /**
     */
    SET_CLOCK(3),

    /**
     */
    DISPLAY_CLOCK,

    /**
     */
    REMOVE_CLOCK,

    /**
     */
    CREATE_WINDOW(5),

    /**
     */
    GOTO_WINDOW(1),

    /**
     */
    HANGUP,

    /**
     */
    DIAL_PHONE(1),

    /**
     */
    QUICK_DIAL(1),

    /**
     */
    TONE,

    /**
     */
    PULSE,

    /**
     */
    FLASH_HOOK,

    /**
     */
    FIXED_PAUSE,

    /**
     */
    WAIT_TONE,

    /**
     */
    USER0,

    /**
     */
    USER1,

    /**
     */
    USER2,

    /**
     */
    USER3,

    /**
     */
    USER4,

    /**
     */
    USER5,

    /**
     */
    USER6,

    /**
     */
    USER7,

    /**
     */
    USER8,

    /**
     */
    USER9,

    /**
     */
    ORIG_PAIR,

    /**
     */
    ORIG_COLORS,

    /**
     */
    INITIALIZE_COLOR(4),

    /**
     */
    INITIALIZE_PAIR(7),

    /**
     */
    SET_COLOR_PAIR(1),

    /**
     */
    SET_FOREGROUND(1),

    /**
     */
    SET_BACKGROUND(1),

    /**
     */
    CHANGE_CHAR_PITCH(1),

    /**
     */
    CHANGE_LINE_PITCH(1),

    /**
     */
    CHANGE_RES_HORZ(1),

    /**
     */
    CHANGE_RES_VERT(1),

    /**
     */
    DEFINE_CHAR(3),

    /**
     */
    ENTER_DOUBLEWIDE_MODE,

    /**
     */
    ENTER_DRAFT_QUALITY,

    /**
     */
    ENTER_ITALICS_MODE,

    /**
     */
    ENTER_LEFTWARD_MODE,

    /**
     */
    ENTER_MICRO_MODE,

    /**
     */
    ENTER_NEAR_LETTER_QUALITY,

    /**
     */
    ENTER_NORMAL_QUALITY,

    /**
     */
    ENTER_SHADOW_MODE,

    /**
     */
    ENTER_SUBSCRIPT_MODE,

    /**
     */
    ENTER_SUPERSCRIPT_MODE,

    /**
     */
    ENTER_UPWARD_MODE,

    /**
     */
    EXIT_DOUBLEWIDE_MODE,

    /**
     */
    EXIT_ITALICS_MODE,

    /**
     */
    EXIT_LEFTWARD_MODE,

    /**
     */
    EXIT_MICRO_MODE,

    /**
     */
    EXIT_SHADOW_MODE,

    /**
     */
    EXIT_SUBSCRIPT_MODE,

    /**
     */
    EXIT_SUPERSCRIPT_MODE,

    /**
     */
    EXIT_UPWARD_MODE,

    /**
     */
    MICRO_COLUMN_ADDRESS,

    /**
     */
    MICRO_DOWN,

    /**
     */
    MICRO_LEFT,

    /**
     */
    MICRO_RIGHT,

    /**
     */
    MICRO_ROW_ADDRESS(1),

    /**
     */
    MICRO_UP,

    /**
     */
    ORDER_OF_PINS,

    /**
     */
    PARM_DOWN_MICRO,

    /**
     */
    PARM_LEFT_MICRO,

    /**
     */
    PARM_RIGHT_MICRO,

    /**
     */
    PARM_UP_MICRO,

    /**
     */
    SELECT_CHAR_SET(1),

    /**
     */
    SET_BOTTOM_MARGIN,

    /**
     */
    SET_BOTTOM_MARGIN_PARM(1),

    /**
     */
    SET_LEFT_MARGIN_PARM(1),

    /**
     */
    SET_RIGHT_MARGIN_PARM(1),

    /**
     */
    SET_TOP_MARGIN,

    /**
     */
    SET_TOP_MARGIN_PARM(1),

    /**
     */
    START_BIT_IMAGE,

    /**
     */
    START_CHAR_SET_DEF(2),

    /**
     */
    STOP_BIT_IMAGE,

    /**
     */
    STOP_CHAR_SET_DEF(1),

    /**
     */
    SUBSCRIPT_CHARACTERS,

    /**
     */
    SUPERSCRIPT_CHARACTERS,

    /**
     */
    THESE_CAUSE_CR,

    /**
     */
    ZERO_MOTION,

    /**
     */
    CHAR_SET_NAMES(1),

    /**
     */
    KEY_MOUSE,

    /**
     */
    MOUSE_INFO,

    /**
     */
    REQ_MOUSE_POS,

    /**
     */
    GET_MOUSE(1),

    /**
     */
    SET_A_FOREGROUND(1),

    /**
     */
    SET_A_BACKGROUND(1),

    /**
     */
    PKEY_PLAB(3),

    /**
     */
    DEVICE_TYPE,

    /**
     */
    CODE_SET_INIT,

    /**
     */
    SET0_DES_SEQ,

    /**
     */
    SET1_DES_SEQ,

    /**
     */
    SET2_DES_SEQ,

    /**
     */
    SET3_DES_SEQ,

    /**
     */
    SET_LR_MARGIN(2),

    /**
     */
    SET_TB_MARGIN(2),

    /**
     */
    BIT_IMAGE_REPEAT(2),

    /**
     */
    BIT_IMAGE_NEWLINE,

    /**
     */
    BIT_IMAGE_CARRIAGE_RETURN,

    /**
     */
    COLOR_NAMES(1),

    /**
     */
    DEFINE_BIT_IMAGE_REGION,

    /**
     */
    END_BIT_IMAGE_REGION,

    /**
     */
    SET_COLOR_BAND(1),

    /**
     */
    SET_PAGE_LENGTH(1),

    /**
     */
    DISPLAY_PC_CHAR(1),

    /**
     */
    ENTER_PC_CHARSET_MODE,

    /**
     */
    EXIT_PC_CHARSET_MODE,

    /**
     */
    ENTER_SCANCODE_MODE,

    /**
     */
    EXIT_SCANCODE_MODE,

    /**
     */
    PC_TERM_OPTIONS,

    /**
     */
    SCANCODE_ESCAPE,

    /**
     */
    ALT_SCANCODE_ESC,

    /**
     */
    ENTER_HORIZONTAL_HL_MODE,

    /**
     */
    ENTER_LEFT_HL_MODE,

    /**
     */
    ENTER_LOW_HL_MODE,

    /**
     */
    ENTER_RIGHT_HL_MODE,

    /**
     */
    ENTER_TOP_HL_MODE,

    /**
     */
    ENTER_VERTICAL_HL_MODE,

    /**
     */
    SET_A_ATTRIBUTES(6),
    ;

    /**
     * Returns the expected number of parameters.
     * @return a non-negative integer
     */
    public int parameters() {
        return parameters;
    }

    private final int parameters;

    static final int COUNT = values().length;

    private StringCapability(int parameters) {
        this.parameters = parameters;
    }

    private StringCapability() {
        this(0);
    }
}
