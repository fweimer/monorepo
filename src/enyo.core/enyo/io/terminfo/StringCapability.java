package enyo.io.terminfo;

import java.util.List;

/**
 * A string-value terminfo capability.
 *
 * String capabilities that correspond to terminal keys are collected
 * under {@link KeyCapability}.
 *
 * @see TermFile#get(StringCapability)
 */
public interface StringCapability {
    /**
     * The long name of the string capability.
     * @return an upper-case identifier
     */
    String name();

    /**
     * Returns the index of the capability in the terminal file.
     * @return a non-negative integer
     */
    int index();

    /**
     * Returns the expected number of parameters.
     * @return a non-negative integer
     */
    int parameters();

    /**
     * Returns the name that is used in terminfo definitions.
     * @return a non-empty string
     */
    String terminfoName();

    /**
     */
    static final StringCapability BACK_TAB =
        UntypedStringCapability.BACK_TAB;

    /**
     */
    static final StringCapability BELL =
        UntypedStringCapability.BELL;

    /**
     */
    static final StringCapability CARRIAGE_RETURN =
        UntypedStringCapability.CARRIAGE_RETURN;

    /**
     */
    static final StringCapability CHANGE_SCROLL_REGION =
        UntypedStringCapability.CHANGE_SCROLL_REGION;

    /**
     */
    static final StringCapability CLEAR_ALL_TABS =
        UntypedStringCapability.CLEAR_ALL_TABS;

    /**
     */
    static final StringCapability CLEAR_SCREEN =
        UntypedStringCapability.CLEAR_SCREEN;

    /**
     */
    static final StringCapability CLR_EOL =
        UntypedStringCapability.CLR_EOL;

    /**
     */
    static final StringCapability CLR_EOS =
        UntypedStringCapability.CLR_EOS;

    /**
     */
    static final StringCapability COLUMN_ADDRESS =
        UntypedStringCapability.COLUMN_ADDRESS;

    /**
     */
    static final StringCapability COMMAND_CHARACTER =
        UntypedStringCapability.COMMAND_CHARACTER;

    /**
     */
    static final StringCapability CURSOR_ADDRESS =
        UntypedStringCapability.CURSOR_ADDRESS;

    /**
     */
    static final StringCapability CURSOR_DOWN =
        UntypedStringCapability.CURSOR_DOWN;

    /**
     */
    static final StringCapability CURSOR_HOME =
        UntypedStringCapability.CURSOR_HOME;

    /**
     */
    static final StringCapability CURSOR_INVISIBLE =
        UntypedStringCapability.CURSOR_INVISIBLE;

    /**
     */
    static final StringCapability CURSOR_LEFT =
        UntypedStringCapability.CURSOR_LEFT;

    /**
     */
    static final StringCapability CURSOR_MEM_ADDRESS =
        UntypedStringCapability.CURSOR_MEM_ADDRESS;

    /**
     */
    static final StringCapability CURSOR_NORMAL =
        UntypedStringCapability.CURSOR_NORMAL;

    /**
     */
    static final StringCapability CURSOR_RIGHT =
        UntypedStringCapability.CURSOR_RIGHT;

    /**
     */
    static final StringCapability CURSOR_TO_LL =
        UntypedStringCapability.CURSOR_TO_LL;

    /**
     */
    static final StringCapability CURSOR_UP =
        UntypedStringCapability.CURSOR_UP;

    /**
     */
    static final StringCapability CURSOR_VISIBLE =
        UntypedStringCapability.CURSOR_VISIBLE;

    /**
     */
    static final StringCapability DELETE_CHARACTER =
        UntypedStringCapability.DELETE_CHARACTER;

    /**
     */
    static final StringCapability DELETE_LINE =
        UntypedStringCapability.DELETE_LINE;

    /**
     */
    static final StringCapability DIS_STATUS_LINE =
        UntypedStringCapability.DIS_STATUS_LINE;

    /**
     */
    static final StringCapability DOWN_HALF_LINE =
        UntypedStringCapability.DOWN_HALF_LINE;

    /**
     */
    static final StringCapability ENTER_ALT_CHARSET_MODE =
        UntypedStringCapability.ENTER_ALT_CHARSET_MODE;

    /**
     */
    static final StringCapability ENTER_BLINK_MODE =
        UntypedStringCapability.ENTER_BLINK_MODE;

    /**
     */
    static final StringCapability ENTER_BOLD_MODE =
        UntypedStringCapability.ENTER_BOLD_MODE;

    /**
     */
    static final StringCapability ENTER_CA_MODE =
        UntypedStringCapability.ENTER_CA_MODE;

    /**
     */
    static final StringCapability ENTER_DELETE_MODE =
        UntypedStringCapability.ENTER_DELETE_MODE;

    /**
     */
    static final StringCapability ENTER_DIM_MODE =
        UntypedStringCapability.ENTER_DIM_MODE;

    /**
     */
    static final StringCapability ENTER_INSERT_MODE =
        UntypedStringCapability.ENTER_INSERT_MODE;

    /**
     */
    static final StringCapability ENTER_SECURE_MODE =
        UntypedStringCapability.ENTER_SECURE_MODE;

    /**
     */
    static final StringCapability ENTER_PROTECTED_MODE =
        UntypedStringCapability.ENTER_PROTECTED_MODE;

    /**
     */
    static final StringCapability ENTER_REVERSE_MODE =
        UntypedStringCapability.ENTER_REVERSE_MODE;

    /**
     */
    static final StringCapability ENTER_STANDOUT_MODE =
        UntypedStringCapability.ENTER_STANDOUT_MODE;

    /**
     */
    static final StringCapability ENTER_UNDERLINE_MODE =
        UntypedStringCapability.ENTER_UNDERLINE_MODE;

    /**
     */
    static final StringCapability ERASE_CHARS =
        UntypedStringCapability.ERASE_CHARS;

    /**
     */
    static final StringCapability EXIT_ALT_CHARSET_MODE =
        UntypedStringCapability.EXIT_ALT_CHARSET_MODE;

    /**
     */
    static final StringCapability EXIT_ATTRIBUTE_MODE =
        UntypedStringCapability.EXIT_ATTRIBUTE_MODE;

    /**
     */
    static final StringCapability EXIT_CA_MODE =
        UntypedStringCapability.EXIT_CA_MODE;

    /**
     */
    static final StringCapability EXIT_DELETE_MODE =
        UntypedStringCapability.EXIT_DELETE_MODE;

    /**
     */
    static final StringCapability EXIT_INSERT_MODE =
        UntypedStringCapability.EXIT_INSERT_MODE;

    /**
     */
    static final StringCapability EXIT_STANDOUT_MODE =
        UntypedStringCapability.EXIT_STANDOUT_MODE;

    /**
     */
    static final StringCapability EXIT_UNDERLINE_MODE =
        UntypedStringCapability.EXIT_UNDERLINE_MODE;

    /**
     */
    static final StringCapability FLASH_SCREEN =
        UntypedStringCapability.FLASH_SCREEN;

    /**
     */
    static final StringCapability FORM_FEED =
        UntypedStringCapability.FORM_FEED;

    /**
     */
    static final StringCapability FROM_STATUS_LINE =
        UntypedStringCapability.FROM_STATUS_LINE;

    /**
     */
    static final StringCapability INIT_1STRING =
        UntypedStringCapability.INIT_1STRING;

    /**
     */
    static final StringCapability INIT_2STRING =
        UntypedStringCapability.INIT_2STRING;

    /**
     */
    static final StringCapability INIT_3STRING =
        UntypedStringCapability.INIT_3STRING;

    /**
     */
    static final StringCapability INIT_FILE =
        UntypedStringCapability.INIT_FILE;

    /**
     */
    static final StringCapability INSERT_CHARACTER =
        UntypedStringCapability.INSERT_CHARACTER;

    /**
     */
    static final StringCapability INSERT_LINE =
        UntypedStringCapability.INSERT_LINE;

    /**
     */
    static final StringCapability INSERT_PADDING =
        UntypedStringCapability.INSERT_PADDING;

    /**
     */
    static final StringCapability KEYPAD_LOCAL =
        UntypedStringCapability.KEYPAD_LOCAL;

    /**
     */
    static final StringCapability KEYPAD_XMIT =
        UntypedStringCapability.KEYPAD_XMIT;

    /**
     */
    static final StringCapability LAB_F0 =
        UntypedStringCapability.LAB_F0;

    /**
     */
    static final StringCapability LAB_F1 =
        UntypedStringCapability.LAB_F1;

    /**
     */
    static final StringCapability LAB_F10 =
        UntypedStringCapability.LAB_F10;

    /**
     */
    static final StringCapability LAB_F2 =
        UntypedStringCapability.LAB_F2;

    /**
     */
    static final StringCapability LAB_F3 =
        UntypedStringCapability.LAB_F3;

    /**
     */
    static final StringCapability LAB_F4 =
        UntypedStringCapability.LAB_F4;

    /**
     */
    static final StringCapability LAB_F5 =
        UntypedStringCapability.LAB_F5;

    /**
     */
    static final StringCapability LAB_F6 =
        UntypedStringCapability.LAB_F6;

    /**
     */
    static final StringCapability LAB_F7 =
        UntypedStringCapability.LAB_F7;

    /**
     */
    static final StringCapability LAB_F8 =
        UntypedStringCapability.LAB_F8;

    /**
     */
    static final StringCapability LAB_F9 =
        UntypedStringCapability.LAB_F9;

    /**
     */
    static final StringCapability META_OFF =
        UntypedStringCapability.META_OFF;

    /**
     */
    static final StringCapability META_ON =
        UntypedStringCapability.META_ON;

    /**
     */
    static final StringCapability NEWLINE =
        UntypedStringCapability.NEWLINE;

    /**
     */
    static final StringCapability PAD_CHAR =
        UntypedStringCapability.PAD_CHAR;

    /**
     */
    static final StringCapability PARM_DCH =
        UntypedStringCapability.PARM_DCH;

    /**
     */
    static final StringCapability PARM_DELETE_LINE =
        UntypedStringCapability.PARM_DELETE_LINE;

    /**
     */
    static final StringCapability PARM_DOWN_CURSOR =
        UntypedStringCapability.PARM_DOWN_CURSOR;

    /**
     */
    static final StringCapability PARM_ICH =
        UntypedStringCapability.PARM_ICH;

    /**
     */
    static final StringCapability PARM_INDEX =
        UntypedStringCapability.PARM_INDEX;

    /**
     */
    static final StringCapability PARM_INSERT_LINE =
        UntypedStringCapability.PARM_INSERT_LINE;

    /**
     */
    static final StringCapability PARM_LEFT_CURSOR =
        UntypedStringCapability.PARM_LEFT_CURSOR;

    /**
     */
    static final StringCapability PARM_RIGHT_CURSOR =
        UntypedStringCapability.PARM_RIGHT_CURSOR;

    /**
     */
    static final StringCapability PARM_RINDEX =
        UntypedStringCapability.PARM_RINDEX;

    /**
     */
    static final StringCapability PARM_UP_CURSOR =
        UntypedStringCapability.PARM_UP_CURSOR;

    /**
     */
    static final StringCapability PKEY_KEY =
        UntypedStringCapability.PKEY_KEY;

    /**
     */
    static final StringCapability PKEY_LOCAL =
        UntypedStringCapability.PKEY_LOCAL;

    /**
     */
    static final StringCapability PKEY_XMIT =
        UntypedStringCapability.PKEY_XMIT;

    /**
     */
    static final StringCapability PRINT_SCREEN =
        UntypedStringCapability.PRINT_SCREEN;

    /**
     */
    static final StringCapability PRTR_OFF =
        UntypedStringCapability.PRTR_OFF;

    /**
     */
    static final StringCapability PRTR_ON =
        UntypedStringCapability.PRTR_ON;

    /**
     */
    static final StringCapability REPEAT_CHAR =
        UntypedStringCapability.REPEAT_CHAR;

    /**
     */
    static final StringCapability RESET_1STRING =
        UntypedStringCapability.RESET_1STRING;

    /**
     */
    static final StringCapability RESET_2STRING =
        UntypedStringCapability.RESET_2STRING;

    /**
     */
    static final StringCapability RESET_3STRING =
        UntypedStringCapability.RESET_3STRING;

    /**
     */
    static final StringCapability RESET_FILE =
        UntypedStringCapability.RESET_FILE;

    /**
     */
    static final StringCapability RESTORE_CURSOR =
        UntypedStringCapability.RESTORE_CURSOR;

    /**
     */
    static final StringCapability ROW_ADDRESS =
        UntypedStringCapability.ROW_ADDRESS;

    /**
     */
    static final StringCapability SAVE_CURSOR =
        UntypedStringCapability.SAVE_CURSOR;

    /**
     */
    static final StringCapability SCROLL_FORWARD =
        UntypedStringCapability.SCROLL_FORWARD;

    /**
     */
    static final StringCapability SCROLL_REVERSE =
        UntypedStringCapability.SCROLL_REVERSE;

    /**
     */
    static final StringCapability SET_ATTRIBUTES =
        UntypedStringCapability.SET_ATTRIBUTES;

    /**
     */
    static final StringCapability SET_TAB =
        UntypedStringCapability.SET_TAB;

    /**
     */
    static final StringCapability SET_WINDOW =
        UntypedStringCapability.SET_WINDOW;

    /**
     */
    static final StringCapability TAB =
        UntypedStringCapability.TAB;

    /**
     */
    static final StringCapability TO_STATUS_LINE =
        UntypedStringCapability.TO_STATUS_LINE;

    /**
     */
    static final StringCapability UNDERLINE_CHAR =
        UntypedStringCapability.UNDERLINE_CHAR;

    /**
     */
    static final StringCapability UP_HALF_LINE =
        UntypedStringCapability.UP_HALF_LINE;

    /**
     */
    static final StringCapability INIT_PROG =
        UntypedStringCapability.INIT_PROG;

    /**
     */
    static final StringCapability PRTR_NON =
        UntypedStringCapability.PRTR_NON;

    /**
     */
    static final StringCapability CHAR_PADDING =
        UntypedStringCapability.CHAR_PADDING;

    /**
     */
    static final StringCapability ACS_CHARS =
        UntypedStringCapability.ACS_CHARS;

    /**
     */
    static final StringCapability PLAB_NORM =
        UntypedStringCapability.PLAB_NORM;

    /**
     */
    static final StringCapability ENTER_XON_MODE =
        UntypedStringCapability.ENTER_XON_MODE;

    /**
     */
    static final StringCapability EXIT_XON_MODE =
        UntypedStringCapability.EXIT_XON_MODE;

    /**
     */
    static final StringCapability ENTER_AM_MODE =
        UntypedStringCapability.ENTER_AM_MODE;

    /**
     */
    static final StringCapability EXIT_AM_MODE =
        UntypedStringCapability.EXIT_AM_MODE;

    /**
     */
    static final StringCapability XON_CHARACTER =
        UntypedStringCapability.XON_CHARACTER;

    /**
     */
    static final StringCapability XOFF_CHARACTER =
        UntypedStringCapability.XOFF_CHARACTER;

    /**
     */
    static final StringCapability ENA_ACS =
        UntypedStringCapability.ENA_ACS;

    /**
     */
    static final StringCapability LABEL_ON =
        UntypedStringCapability.LABEL_ON;

    /**
     */
    static final StringCapability LABEL_OFF =
        UntypedStringCapability.LABEL_OFF;

    /**
     */
    static final StringCapability REQ_FOR_INPUT =
        UntypedStringCapability.REQ_FOR_INPUT;

    /**
     */
    static final StringCapability CLR_BOL =
        UntypedStringCapability.CLR_BOL;

    /**
     */
    static final StringCapability CLEAR_MARGINS =
        UntypedStringCapability.CLEAR_MARGINS;

    /**
     */
    static final StringCapability SET_LEFT_MARGIN =
        UntypedStringCapability.SET_LEFT_MARGIN;

    /**
     */
    static final StringCapability SET_RIGHT_MARGIN =
        UntypedStringCapability.SET_RIGHT_MARGIN;

    /**
     */
    static final StringCapability LABEL_FORMAT =
        UntypedStringCapability.LABEL_FORMAT;

    /**
     */
    static final StringCapability SET_CLOCK =
        UntypedStringCapability.SET_CLOCK;

    /**
     */
    static final StringCapability DISPLAY_CLOCK =
        UntypedStringCapability.DISPLAY_CLOCK;

    /**
     */
    static final StringCapability REMOVE_CLOCK =
        UntypedStringCapability.REMOVE_CLOCK;

    /**
     */
    static final StringCapability CREATE_WINDOW =
        UntypedStringCapability.CREATE_WINDOW;

    /**
     */
    static final StringCapability GOTO_WINDOW =
        UntypedStringCapability.GOTO_WINDOW;

    /**
     */
    static final StringCapability HANGUP =
        UntypedStringCapability.HANGUP;

    /**
     */
    static final StringCapability DIAL_PHONE =
        UntypedStringCapability.DIAL_PHONE;

    /**
     */
    static final StringCapability QUICK_DIAL =
        UntypedStringCapability.QUICK_DIAL;

    /**
     */
    static final StringCapability TONE =
        UntypedStringCapability.TONE;

    /**
     */
    static final StringCapability PULSE =
        UntypedStringCapability.PULSE;

    /**
     */
    static final StringCapability FLASH_HOOK =
        UntypedStringCapability.FLASH_HOOK;

    /**
     */
    static final StringCapability FIXED_PAUSE =
        UntypedStringCapability.FIXED_PAUSE;

    /**
     */
    static final StringCapability WAIT_TONE =
        UntypedStringCapability.WAIT_TONE;

    /**
     */
    static final StringCapability USER0 =
        UntypedStringCapability.USER0;

    /**
     */
    static final StringCapability USER1 =
        UntypedStringCapability.USER1;

    /**
     */
    static final StringCapability USER2 =
        UntypedStringCapability.USER2;

    /**
     */
    static final StringCapability USER3 =
        UntypedStringCapability.USER3;

    /**
     */
    static final StringCapability USER4 =
        UntypedStringCapability.USER4;

    /**
     */
    static final StringCapability USER5 =
        UntypedStringCapability.USER5;

    /**
     */
    static final StringCapability USER6 =
        UntypedStringCapability.USER6;

    /**
     */
    static final StringCapability USER7 =
        UntypedStringCapability.USER7;

    /**
     */
    static final StringCapability USER8 =
        UntypedStringCapability.USER8;

    /**
     */
    static final StringCapability USER9 =
        UntypedStringCapability.USER9;

    /**
     */
    static final StringCapability ORIG_PAIR =
        UntypedStringCapability.ORIG_PAIR;

    /**
     */
    static final StringCapability ORIG_COLORS =
        UntypedStringCapability.ORIG_COLORS;

    /**
     */
    static final StringCapability INITIALIZE_COLOR =
        UntypedStringCapability.INITIALIZE_COLOR;

    /**
     */
    static final StringCapability INITIALIZE_PAIR =
        UntypedStringCapability.INITIALIZE_PAIR;

    /**
     */
    static final StringCapability SET_COLOR_PAIR =
        UntypedStringCapability.SET_COLOR_PAIR;

    /**
     */
    static final StringCapability SET_FOREGROUND =
        UntypedStringCapability.SET_FOREGROUND;

    /**
     */
    static final StringCapability SET_BACKGROUND =
        UntypedStringCapability.SET_BACKGROUND;

    /**
     */
    static final StringCapability CHANGE_CHAR_PITCH =
        UntypedStringCapability.CHANGE_CHAR_PITCH;

    /**
     */
    static final StringCapability CHANGE_LINE_PITCH =
        UntypedStringCapability.CHANGE_LINE_PITCH;

    /**
     */
    static final StringCapability CHANGE_RES_HORZ =
        UntypedStringCapability.CHANGE_RES_HORZ;

    /**
     */
    static final StringCapability CHANGE_RES_VERT =
        UntypedStringCapability.CHANGE_RES_VERT;

    /**
     */
    static final StringCapability DEFINE_CHAR =
        UntypedStringCapability.DEFINE_CHAR;

    /**
     */
    static final StringCapability ENTER_DOUBLEWIDE_MODE =
        UntypedStringCapability.ENTER_DOUBLEWIDE_MODE;

    /**
     */
    static final StringCapability ENTER_DRAFT_QUALITY =
        UntypedStringCapability.ENTER_DRAFT_QUALITY;

    /**
     */
    static final StringCapability ENTER_ITALICS_MODE =
        UntypedStringCapability.ENTER_ITALICS_MODE;

    /**
     */
    static final StringCapability ENTER_LEFTWARD_MODE =
        UntypedStringCapability.ENTER_LEFTWARD_MODE;

    /**
     */
    static final StringCapability ENTER_MICRO_MODE =
        UntypedStringCapability.ENTER_MICRO_MODE;

    /**
     */
    static final StringCapability ENTER_NEAR_LETTER_QUALITY =
        UntypedStringCapability.ENTER_NEAR_LETTER_QUALITY;

    /**
     */
    static final StringCapability ENTER_NORMAL_QUALITY =
        UntypedStringCapability.ENTER_NORMAL_QUALITY;

    /**
     */
    static final StringCapability ENTER_SHADOW_MODE =
        UntypedStringCapability.ENTER_SHADOW_MODE;

    /**
     */
    static final StringCapability ENTER_SUBSCRIPT_MODE =
        UntypedStringCapability.ENTER_SUBSCRIPT_MODE;

    /**
     */
    static final StringCapability ENTER_SUPERSCRIPT_MODE =
        UntypedStringCapability.ENTER_SUPERSCRIPT_MODE;

    /**
     */
    static final StringCapability ENTER_UPWARD_MODE =
        UntypedStringCapability.ENTER_UPWARD_MODE;

    /**
     */
    static final StringCapability EXIT_DOUBLEWIDE_MODE =
        UntypedStringCapability.EXIT_DOUBLEWIDE_MODE;

    /**
     */
    static final StringCapability EXIT_ITALICS_MODE =
        UntypedStringCapability.EXIT_ITALICS_MODE;

    /**
     */
    static final StringCapability EXIT_LEFTWARD_MODE =
        UntypedStringCapability.EXIT_LEFTWARD_MODE;

    /**
     */
    static final StringCapability EXIT_MICRO_MODE =
        UntypedStringCapability.EXIT_MICRO_MODE;

    /**
     */
    static final StringCapability EXIT_SHADOW_MODE =
        UntypedStringCapability.EXIT_SHADOW_MODE;

    /**
     */
    static final StringCapability EXIT_SUBSCRIPT_MODE =
        UntypedStringCapability.EXIT_SUBSCRIPT_MODE;

    /**
     */
    static final StringCapability EXIT_SUPERSCRIPT_MODE =
        UntypedStringCapability.EXIT_SUPERSCRIPT_MODE;

    /**
     */
    static final StringCapability EXIT_UPWARD_MODE =
        UntypedStringCapability.EXIT_UPWARD_MODE;

    /**
     */
    static final StringCapability MICRO_COLUMN_ADDRESS =
        UntypedStringCapability.MICRO_COLUMN_ADDRESS;

    /**
     */
    static final StringCapability MICRO_DOWN =
        UntypedStringCapability.MICRO_DOWN;

    /**
     */
    static final StringCapability MICRO_LEFT =
        UntypedStringCapability.MICRO_LEFT;

    /**
     */
    static final StringCapability MICRO_RIGHT =
        UntypedStringCapability.MICRO_RIGHT;

    /**
     */
    static final StringCapability MICRO_ROW_ADDRESS =
        UntypedStringCapability.MICRO_ROW_ADDRESS;

    /**
     */
    static final StringCapability MICRO_UP =
        UntypedStringCapability.MICRO_UP;

    /**
     */
    static final StringCapability ORDER_OF_PINS =
        UntypedStringCapability.ORDER_OF_PINS;

    /**
     */
    static final StringCapability PARM_DOWN_MICRO =
        UntypedStringCapability.PARM_DOWN_MICRO;

    /**
     */
    static final StringCapability PARM_LEFT_MICRO =
        UntypedStringCapability.PARM_LEFT_MICRO;

    /**
     */
    static final StringCapability PARM_RIGHT_MICRO =
        UntypedStringCapability.PARM_RIGHT_MICRO;

    /**
     */
    static final StringCapability PARM_UP_MICRO =
        UntypedStringCapability.PARM_UP_MICRO;

    /**
     */
    static final StringCapability SELECT_CHAR_SET =
        UntypedStringCapability.SELECT_CHAR_SET;

    /**
     */
    static final StringCapability SET_BOTTOM_MARGIN =
        UntypedStringCapability.SET_BOTTOM_MARGIN;

    /**
     */
    static final StringCapability SET_BOTTOM_MARGIN_PARM =
        UntypedStringCapability.SET_BOTTOM_MARGIN_PARM;

    /**
     */
    static final StringCapability SET_LEFT_MARGIN_PARM =
        UntypedStringCapability.SET_LEFT_MARGIN_PARM;

    /**
     */
    static final StringCapability SET_RIGHT_MARGIN_PARM =
        UntypedStringCapability.SET_RIGHT_MARGIN_PARM;

    /**
     */
    static final StringCapability SET_TOP_MARGIN =
        UntypedStringCapability.SET_TOP_MARGIN;

    /**
     */
    static final StringCapability SET_TOP_MARGIN_PARM =
        UntypedStringCapability.SET_TOP_MARGIN_PARM;

    /**
     */
    static final StringCapability START_BIT_IMAGE =
        UntypedStringCapability.START_BIT_IMAGE;

    /**
     */
    static final StringCapability START_CHAR_SET_DEF =
        UntypedStringCapability.START_CHAR_SET_DEF;

    /**
     */
    static final StringCapability STOP_BIT_IMAGE =
        UntypedStringCapability.STOP_BIT_IMAGE;

    /**
     */
    static final StringCapability STOP_CHAR_SET_DEF =
        UntypedStringCapability.STOP_CHAR_SET_DEF;

    /**
     */
    static final StringCapability SUBSCRIPT_CHARACTERS =
        UntypedStringCapability.SUBSCRIPT_CHARACTERS;

    /**
     */
    static final StringCapability SUPERSCRIPT_CHARACTERS =
        UntypedStringCapability.SUPERSCRIPT_CHARACTERS;

    /**
     */
    static final StringCapability THESE_CAUSE_CR =
        UntypedStringCapability.THESE_CAUSE_CR;

    /**
     */
    static final StringCapability ZERO_MOTION =
        UntypedStringCapability.ZERO_MOTION;

    /**
     */
    static final StringCapability CHAR_SET_NAMES =
        UntypedStringCapability.CHAR_SET_NAMES;

    /**
     * Prefix of a mouse position peport.  Usually {@code "\033[M"} if
     * defined.
     */
    static final StringCapability KEY_MOUSE =
        UntypedStringCapability.KEY_MOUSE;

    /**
     */
    static final StringCapability MOUSE_INFO =
        UntypedStringCapability.MOUSE_INFO;

    /**
     */
    static final StringCapability REQ_MOUSE_POS =
        UntypedStringCapability.REQ_MOUSE_POS;

    /**
     */
    static final StringCapability GET_MOUSE =
        UntypedStringCapability.GET_MOUSE;

    /**
     */
    static final StringCapability SET_A_FOREGROUND =
        UntypedStringCapability.SET_A_FOREGROUND;

    /**
     */
    static final StringCapability SET_A_BACKGROUND =
        UntypedStringCapability.SET_A_BACKGROUND;

    /**
     */
    static final StringCapability PKEY_PLAB =
        UntypedStringCapability.PKEY_PLAB;

    /**
     */
    static final StringCapability DEVICE_TYPE =
        UntypedStringCapability.DEVICE_TYPE;

    /**
     */
    static final StringCapability CODE_SET_INIT =
        UntypedStringCapability.CODE_SET_INIT;

    /**
     */
    static final StringCapability SET0_DES_SEQ =
        UntypedStringCapability.SET0_DES_SEQ;

    /**
     */
    static final StringCapability SET1_DES_SEQ =
        UntypedStringCapability.SET1_DES_SEQ;

    /**
     */
    static final StringCapability SET2_DES_SEQ =
        UntypedStringCapability.SET2_DES_SEQ;

    /**
     */
    static final StringCapability SET3_DES_SEQ =
        UntypedStringCapability.SET3_DES_SEQ;

    /**
     */
    static final StringCapability SET_LR_MARGIN =
        UntypedStringCapability.SET_LR_MARGIN;

    /**
     */
    static final StringCapability SET_TB_MARGIN =
        UntypedStringCapability.SET_TB_MARGIN;

    /**
     */
    static final StringCapability BIT_IMAGE_REPEAT =
        UntypedStringCapability.BIT_IMAGE_REPEAT;

    /**
     */
    static final StringCapability BIT_IMAGE_NEWLINE =
        UntypedStringCapability.BIT_IMAGE_NEWLINE;

    /**
     */
    static final StringCapability BIT_IMAGE_CARRIAGE_RETURN =
        UntypedStringCapability.BIT_IMAGE_CARRIAGE_RETURN;

    /**
     */
    static final StringCapability COLOR_NAMES =
        UntypedStringCapability.COLOR_NAMES;

    /**
     */
    static final StringCapability DEFINE_BIT_IMAGE_REGION =
        UntypedStringCapability.DEFINE_BIT_IMAGE_REGION;

    /**
     */
    static final StringCapability END_BIT_IMAGE_REGION =
        UntypedStringCapability.END_BIT_IMAGE_REGION;

    /**
     */
    static final StringCapability SET_COLOR_BAND =
        UntypedStringCapability.SET_COLOR_BAND;

    /**
     */
    static final StringCapability SET_PAGE_LENGTH =
        UntypedStringCapability.SET_PAGE_LENGTH;

    /**
     */
    static final StringCapability DISPLAY_PC_CHAR =
        UntypedStringCapability.DISPLAY_PC_CHAR;

    /**
     */
    static final StringCapability ENTER_PC_CHARSET_MODE =
        UntypedStringCapability.ENTER_PC_CHARSET_MODE;

    /**
     */
    static final StringCapability EXIT_PC_CHARSET_MODE =
        UntypedStringCapability.EXIT_PC_CHARSET_MODE;

    /**
     */
    static final StringCapability ENTER_SCANCODE_MODE =
        UntypedStringCapability.ENTER_SCANCODE_MODE;

    /**
     */
    static final StringCapability EXIT_SCANCODE_MODE =
        UntypedStringCapability.EXIT_SCANCODE_MODE;

    /**
     */
    static final StringCapability PC_TERM_OPTIONS =
        UntypedStringCapability.PC_TERM_OPTIONS;

    /**
     */
    static final StringCapability SCANCODE_ESCAPE =
        UntypedStringCapability.SCANCODE_ESCAPE;

    /**
     */
    static final StringCapability ALT_SCANCODE_ESC =
        UntypedStringCapability.ALT_SCANCODE_ESC;

    /**
     */
    static final StringCapability ENTER_HORIZONTAL_HL_MODE =
        UntypedStringCapability.ENTER_HORIZONTAL_HL_MODE;

    /**
     */
    static final StringCapability ENTER_LEFT_HL_MODE =
        UntypedStringCapability.ENTER_LEFT_HL_MODE;

    /**
     */
    static final StringCapability ENTER_LOW_HL_MODE =
        UntypedStringCapability.ENTER_LOW_HL_MODE;

    /**
     */
    static final StringCapability ENTER_RIGHT_HL_MODE =
        UntypedStringCapability.ENTER_RIGHT_HL_MODE;

    /**
     */
    static final StringCapability ENTER_TOP_HL_MODE =
        UntypedStringCapability.ENTER_TOP_HL_MODE;

    /**
     */
    static final StringCapability ENTER_VERTICAL_HL_MODE =
        UntypedStringCapability.ENTER_VERTICAL_HL_MODE;

    /**
     */
    static final StringCapability SET_A_ATTRIBUTES =
        UntypedStringCapability.SET_A_ATTRIBUTES;

    /**
     */
    static final StringCapability SET_PGLEN_INCH =
        UntypedStringCapability.SET_PGLEN_INCH;

    /**
     * Returns a list of the defined string capabilities.
     * {@linkplain KeyCapability Key capabilities} are not included.
     * @return an immutable list
     */
    static List<StringCapability> values() {
        return UntypedStringCapability.VALUES;
    }

    /**
     * Returns the string capability for the specified name.
     * The name must be the {@linkplain #name() upper-case long name}.
     * @param name the name of the capability
     * @return the string capability
     * @throws IllegalArgumentException if the argument is not a recognized
     *   string capability identifier
     * @throws NullPointerException if the argument is null
     */
    static StringCapability of(String name) {
        try {
            return UntypedStringCapability.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        try {
            return KeyCapability.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        throw new IllegalArgumentException(name);
    }
}
