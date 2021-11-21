package enyo.io.terminfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * String capabilities without assigned parameter types.
 */
public enum UntypedStringCapability implements StringCapability {
    /**
     * Back tab.
     */
    BACK_TAB(0, "cbt"),

    /**
     * Bell (visual or audible).
     */
    BELL(1, "bel"),

    /**
     * Carriage return.
     */
    CARRIAGE_RETURN(2, "cr"),

    /**
     * Set the scroll region to cover the screen rows between the two
     * arguments.
     */
    CHANGE_SCROLL_REGION(3, "csr", 2),

    /**
     * Reset tab stops to the default.
     */
    CLEAR_ALL_TABS(4, "tbc"),

    /**
     * Remove all characters from the stream.
     * Move the cursor to the top-left position.
     */
    CLEAR_SCREEN(5, "clear"),

    /**
     * Clear characters/columns to the end of the line.
     */
    CLR_EOL(6, "el"),

    /**
     * Clear characters/rows to the end of the screen.
     */
    CLR_EOS(7, "ed"),

    /**
     * Unused, unclear purpose.
     */
    COMMAND_CHARACTER(9, "cmdch"),

    /**
     * Move cursor to indicated row and column.
     */
    CURSOR_ADDRESS(10, "cup", 2),

    /**
     * Move the cursor down to the next row.
     * @see #CURSOR_UP
     */
    CURSOR_DOWN(11, "cud1"),

    /**
     * Move the cursor to the first column of the first row.
     */
    CURSOR_HOME(12, "home"),

    /**
     * Hide the cursor.
     * @see #CURSOR_NORMAL
     * @see #CURSOR_VISIBLE
     */
    CURSOR_INVISIBLE(13, "civis"),

    /**
     * Move the cursor left one space/column.
     */
    CURSOR_LEFT(14, "cub1"),

    /**
     * Memory-relative cursor movement, to row and column.
     * @see #CURSOR_ADDRESS
     */
    CURSOR_MEM_ADDRESS(15, "mrcup", 2),

    /**
     * Switch cursor back to normal.
     * @see #CURSOR_INVISIBLE
     * @see #CURSOR_VISIBLE
     */
    CURSOR_NORMAL(16, "cnorm"),

    /**
     * Move the cursor right one space/column.  Non-overwriting space.
     */
    CURSOR_RIGHT(17, "cuf1"),

    /**
     * Move the cursor to the first column in the last row.
     */
    CURSOR_TO_LL(18, "ll"),

    /**
     * Move the cursor up to the previous row.
     * @see #CURSOR_DOWN
     */
    CURSOR_UP(19, "cuu1"),

    /**
     * More prominent cursor.
     * @see #CURSOR_INVISIBLE
     * @see #CURSOR_NORMAL
     */
    CURSOR_VISIBLE(20, "cvvis"),

    /**
     * Delete the character under the cursor.
     * Equivalent to {@link StringCapability.Int#PARM_DCH}
     * with argument 1.
     */
    DELETE_CHARACTER(21, "dch1"),

    /**
     * Delete the current line.
     * Equivalent to {@link StringCapability.Int#PARM_DELETE_LINE}
     * with argument 1.
     */
    DELETE_LINE(22, "dl1"),

    /**
     * Switch off the status line.
     */
    DIS_STATUS_LINE(23, "dsl"),

    /**
     * Move the cursor down half a line.
     * @see #CURSOR_DOWN
     * @see #UP_HALF_LINE
     */
    DOWN_HALF_LINE(24, "hd"),

    /**
     * Switch to the alternate character set.
     * @see StringCapability.Int#SELECT_CHAR_SET
     * @see #SET_ATTRIBUTES
     * @see #ENA_ACS
     */
    ENTER_ALT_CHARSET_MODE(25, "smacs"),

    /**
     * Switch on blinking for the following characters.
     * @see #SET_ATTRIBUTES
     */
    ENTER_BLINK_MODE(26, "blink"),

    /**
     * Switch on bold type for the following characters.
     * @see #SET_ATTRIBUTES
     */
    ENTER_BOLD_MODE(27, "bold"),

    /**
     * Activate {@link UntypedStringCapability#CURSOR_ADDRESS} mode.
     */
    ENTER_CA_MODE(28, "smcup"),

    /**
     * Enter delete mode.  Not used in practice.
     * @see #EXIT_DELETE_MODE
     */
    ENTER_DELETE_MODE(29, "smdc"),

    /**
     * Switch to a mode with dimmer colors.
     * @see #SET_ATTRIBUTES
     */
    ENTER_DIM_MODE(30, "dim"),

    /**
     * Treat characters as insertion.
     * Until a following {@link #EXIT_INSERT_MODE}.
     * This is prefered over {@link #INSERT_CHARACTER} for
     * multiple characters, but {@link StringCapability.Int#PARM_ICH}
     * is even more efficient.
     */
    ENTER_INSERT_MODE(31, "smir"),

    /**
     * Switch to character-invisible (blank) mode.
     * @see #SET_ATTRIBUTES
     */
    ENTER_SECURE_MODE(32, "invis"),

    /**
     * Switch to protected mode.
     * @see #SET_ATTRIBUTES
     */
    ENTER_PROTECTED_MODE(33, "prot"),

    /**
     * Switch to reverse-video mode.
     * @see #SET_ATTRIBUTES
     */
    ENTER_REVERSE_MODE(34, "rev"),

    /**
     * Switch to standout mode.
     * This form of character highlighting can be a combination
     * of the bold and reverse modes, for example.
     * @see #SET_ATTRIBUTES
     */
    ENTER_STANDOUT_MODE(35, "smso"),

    /**
     * Turn on underlining characters.
     * @see #EXIT_UNDERLINE_MODE
     * @see #SET_ATTRIBUTES
     */
    ENTER_UNDERLINE_MODE(36, "smul"),

    /**
     * Switch back to the default character set.
     * @see #SET_ATTRIBUTES
     */
    EXIT_ALT_CHARSET_MODE(38, "rmacs"),

    /**
     * Exit all attribute modes.
     * This turns off bold mode, italics mode etc.
     * @see #SET_ATTRIBUTES
     */
    EXIT_ATTRIBUTE_MODE(39, "sgr0"),

    /**
     * Deactivate {@link UntypedStringCapability#CURSOR_ADDRESS} mode.
     */
    EXIT_CA_MODE(40, "rmcup"),

    /**
     * Exit delete mode.  Not used in practice.
     * @see #ENTER_DELETE_MODE
     */
    EXIT_DELETE_MODE(41, "rmdc"),

    /**
     * Switch back to overwriting mode.
     * This should come after {@link #ENTER_INSERT_MODE}.
     */
    EXIT_INSERT_MODE(42, "rmir"),

    /**
     * Exit standout mode.
     * @see #ENTER_STANDOUT_MODE
     */
    EXIT_STANDOUT_MODE(43, "rmso"),

    /**
     * Stop underlining characters.
     * @see #ENTER_UNDERLINE_MODE
     */
    EXIT_UNDERLINE_MODE(44, "rmul"),

    /**
     * Flash the screen (visual bell).
     */
    FLASH_SCREEN(45, "flash"),

    /**
     * Continue printing on the next page.
     */
    FORM_FEED(46, "ff"),

    /**
     * Switch back from the status line.
     * @see StringCapability.Int#TO_STATUS_LINE
     */
    FROM_STATUS_LINE(47, "fsl"),

    /**
     * First string to send during terminal initialization.
     * @see #RESET_1STRING
     */
    INIT_1STRING(48, "is1"),

    /**
     * Second string to send during terminal initialization.
     * @see #RESET_2STRING
     */
    INIT_2STRING(49, "is2"),

    /**
     * Final string to send during terminal initalization.
     * @see #RESET_3STRING
     */
    INIT_3STRING(50, "is3"),

    /**
     * Path of a local file to be sent as part of terminal initialization.
     * The file contents is expected to be sent after
     * {@link #INIT_2STRING}, but before {@link #INIT_3STRING}.
     *
     * <p><b>Note:</b> When parsing arbitrary terminal definitions,
     * reading local files may constitute a security hazard.
     */
    INIT_FILE(51, "if"),

    /**
     * Insert the next character on the current line.
     * This is equivalent to {@link StringCapability.Int#PARM_ICH}
     * with an argument of 1.
     * If {@code PARM_ICH} is not available,
     * {@link #ENTER_INSERT_MODE} is prefered.
     */
    INSERT_CHARACTER(52, "ich1"),

    /**
     * Insert one line.  Can be used for scrolling.
     * Equivalent to {@link StringCapability.Int#PARM_INSERT_LINE}
     * with argument 1.
     */
    INSERT_LINE(53, "il1"),

    /**
     * Padding to be used with insertion.
     * The padding is expected to be emitted after each character
     * in {@link #ENTER_INSERT_MODE insert mode}, and after each
     * {@link #INSERT_CHARACTER}/character sequence.
     * Padding is automatically handled by
     * {@link StringCapability.Int#PARM_ICH}.
     */
    INSERT_PADDING(54, "ip"),

    /**
     * Enter the mode which handles the keypad keys locally, in the
     * terminal.
     * @see #KEYPAD_XMIT
     */
    KEYPAD_LOCAL(88, "rmkx"),

    /**
     * Enter the mode which transmits keypad keys.
     * @see #KEYPAD_LOCAL
     */
    KEYPAD_XMIT(89, "smkx"),

    /**
     * Label on F0 key.
     */
    LAB_F0(90, "lf0"),

    /**
     * Label on F1 key.
     */
    LAB_F1(91, "lf1"),

    /**
     * Label on F10 key.
     */
    LAB_F10(92, "lf10"),

    /**
     * Label on F2 key.
     */
    LAB_F2(93, "lf2"),

    /**
     * Label on F3 key.
     */
    LAB_F3(94, "lf3"),

    /**
     * Label on F4 key.
     */
    LAB_F4(95, "lf4"),

    /**
     * Label on F5 key.
     */
    LAB_F5(96, "lf5"),

    /**
     * Label on F6 key.
     */
    LAB_F6(97, "lf6"),

    /**
     * Label on F7 key.
     */
    LAB_F7(98, "lf7"),

    /**
     * Label on F8 key.
     */
    LAB_F8(99, "lf8"),

    /**
     * Label on F9 key.
     */
    LAB_F9(100, "lf9"),

    /**
     * Switch meta mode off.
     * @see #META_ON
     */
    META_OFF(101, "rmm"),

    /**
     * Switch meta mode on.
     * @see #META_OFF
     */
    META_ON(102, "smm"),

    /**
     * New line.  {@link #CARRIAGE_RETURN} followed by
     * {@code '\n'}.
     */
    NEWLINE(103, "nel"),

    /**
     * The padding character.
     * If not defined, and if not {@link BooleanCapability#NO_PAD_CHAR},
     * the null byte is used.
     */
    PAD_CHAR(104, "pad"),

    /**
     * Create a hard copy of the screen.
     */
    PRINT_SCREEN(118, "mc0"),

    /**
     * Switch off printing.
     * @see #PRTR_ON
     */
    PRTR_OFF(119, "mc4"),

    /**
     * Switch on printing of the output.
     * @see #PRTR_OFF
     */
    PRTR_ON(120, "mc5"),

    /**
     * Repeat a character multiple times.  The character is in the
     * first argument (as a number), the repeat count in the second.
     */
    REPEAT_CHAR(121, "rep", 2),

    /**
     * First string to send during terminal reset.
     * If this capability is not available, {@link #INIT_1STRING}
     * can be used instead.
     */
    RESET_1STRING(122, "rs1"),

    /**
     * Second string to send during terminal reset.
     * If this capability is not available, {@link #INIT_2STRING}
     * can be used instead.
     */
    RESET_2STRING(123, "rs2"),

    /**
     * Final string to send during terminal reset.
     * If this capability is not available, {@link #INIT_3STRING}
     * can be used instead.
     */
    RESET_3STRING(124, "rs3"),

    /**
     * Path of a local file to be sent as part of a terminal reset.
     * The file contents is expected to be sent after
     * {@link #RESET_2STRING}, but before {@link #RESET_3STRING}.
     * If this capability is not available, {@link #INIT_FILE}
     * can be used instead.
     *
     * <p><b>Note:</b> When parsing arbitrary terminal definitions,
     * reading local files may constitute a security hazard.
     */
    RESET_FILE(125, "rf"),

    /**
     * Go back to the last saved cursor position.
     * @see #SAVE_CURSOR
     */
    RESTORE_CURSOR(126, "rc"),

    /**
     * Save the current cursor position for a future return.
     * @see #RESTORE_CURSOR
     */
    SAVE_CURSOR(128, "sc"),

    /**
     * Scroll up.
     * @see #SCROLL_REVERSE
     */
    SCROLL_FORWARD(129, "ind"),

    /**
     * Scroll down.
     * @see #SCROLL_FORWARD
     */
    SCROLL_REVERSE(130, "ri"),

    /**
     * Set a multitude of terminal attributes.
     * The arguments have the following meanings (0 is off, 1 is on):
     * <ol>
     * <li>{@linkplain #ENTER_STANDOUT_MODE standout mode}
     * <li>{@linkplain #ENTER_UNDERLINE_MODE underline mode}
     * <li>{@linkplain #ENTER_REVERSE_MODE reverse mode}
     * <li>{@linkplain #ENTER_BLINK_MODE blink mode}
     * <li>{@linkplain #ENTER_DIM_MODE dim mode}
     * <li>{@linkplain #ENTER_BOLD_MODE bold mode}
     * <li>{@linkplain #ENTER_SECURE_MODE secure mode}
     * <li>{@linkplain #ENTER_PROTECTED_MODE protected mode}
     * <li>{@linkplain #ENTER_ALT_CHARSET_MODE alternate character set mode}
     * </ol>
     * Color is not set.
     * @see NumericCapability#NO_COLOR_VIDEO
     */
    SET_ATTRIBUTES(131, "sgr", 9),

    /**
     * Create a tab stop at the current column.
     * This applies to all rows.
     */
    SET_TAB(132, "hts"),

    /**
     * Set the current window.
     * The first two arguments define the first and last row of the window,
     * the last two arguments the first and last column.
     */
    SET_WINDOW(133, "wind", 4),

    /**
     * Move cursor forward to the next tab stop.
     */
    TAB(134, "ht"),

    /**
     * Adds an underline to the character at the current cursor position.
     * The cursor advances afterwards.
     */
    UNDERLINE_CHAR(136, "uc"),

    /**
     * Move the cursor up half a line.
     * @see #CURSOR_UP
     * @see #DOWN_HALF_LINE
     */
    UP_HALF_LINE(137, "hu"),

    /**
     * Shell command to be invoked for terminal initialization.
     *
     * (This is not used among current terminal definitions.)
     *
     * <p><b>Note:</b> When parsing arbitrary terminal definitions,
     * executing commands locally may constitute a security hazard.
     */
    INIT_PROG(138, "iprog"),

    /**
     * Unused.
     */
    CHAR_PADDING(145, "rmp"),

    /**
     * Line-drawing character map.
     * This string contains a list of byte pairs, the first byte
     * describing the type of line-drawing character, and the
     * second byte the line-drawing character itself.
     * (Not compatible with multi-byte character sets such as Unicode.)
     * @see #ENTER_PC_CHARSET_MODE
     */
    ACS_CHARS(146, "acsc"),

    /**
     * Activate XON/XOFF mode.
     * @see BooleanCapability#XON_XOFF
     */
    ENTER_XON_MODE(149, "smxon"),

    /**
     * Deactivate XON/XOFF mode.
     */
    EXIT_XON_MODE(150, "rmxon"),

    /**
     * Switch on automatic margins.
     * @see #EXIT_AM_MODE
     * @see BooleanCapability#AUTO_RIGHT_MARGIN
     */
    ENTER_AM_MODE(151, "smam"),

    /**
     * Switch off automatic margins.
     * @see #ENTER_AM_MODE
     */
    EXIT_AM_MODE(152, "rmam"),

    /**
     * XON character
     * Used after {@link UntypedStringCapability#ENTER_XON_MODE}
     * or with {@link BooleanCapability#XON_XOFF} terminals.
     */
    XON_CHARACTER(153, "xonc"),

    /**
     * XOFF character.
     * Used after {@link UntypedStringCapability#ENTER_XON_MODE}
     * or with {@link BooleanCapability#XON_XOFF} terminals.
     */
    XOFF_CHARACTER(154, "xoffc"),

    /**
     * Enable the processing of the alternate character set.
     * @see #ENTER_ALT_CHARSET_MODE
     */
    ENA_ACS(155, "enacs"),

    /**
     * Switch on labels.
     */
    LABEL_ON(156, "smln"),

    /**
     * Switch off labels.
     */
    LABEL_OFF(157, "rmln"),

    /**
     * Request input from a pseudo-terminal.
     * Not actually used in practice.
     */
    REQ_FOR_INPUT(215, "rfi"),

    /**
     * Clear to the start of the current line.
     */
    CLR_BOL(269, "el1"),

    /**
     * Clear the terminal margins.
     * This is part of typical terminal reset sequences.
     */
    CLEAR_MARGINS(270, "mgc"),

    /**
     * Use the current column to set the left margin.
     * @see StringCapability.Int#SET_LEFT_MARGIN_PARM
     */
    SET_LEFT_MARGIN(271, "smgl"),

    /**
     * Use the current column to set the right margin.
     * @see StringCapability.Int#SET_RIGHT_MARGIN_PARM
     */
    SET_RIGHT_MARGIN(272, "smgr"),

    /**
     * Unused in practice.
     */
    LABEL_FORMAT(273, "fln"),

    /**
     * Set the clock display to the specified hour/minute/second.
     * Unused, parameter types unknown.
     */
    SET_CLOCK(274, "sclk", 3),

    /**
     * Show the clock.
     * @see #REMOVE_CLOCK
     * @see #SET_CLOCK
     */
    DISPLAY_CLOCK(275, "dclk"),

    /**
     * Hide the clock.
     * @see #DISPLAY_CLOCK
     * @see #SET_CLOCK
     */
    REMOVE_CLOCK(276, "rmclk"),

    /**
     * Unused.  Parameter types unknown.
     */
    CREATE_WINDOW(277, "cwin", 5),

    /**
     * Unused.  Parameter type unknown.
     */
    GOTO_WINDOW(278, "wingo", 1),

    /**
     * Hang up the phone.
     */
    HANGUP(279, "hup"),

    /**
     * Unused.  The parameter has type string.
     */
    DIAL_PHONE(280, "dial", 1),

    /**
     * Unused.  Parameter type unknown.
     */
    QUICK_DIAL(281, "qdial", 1),

    /**
     * Switch to tone dialing.
     * @see #DIAL_PHONE
     * Unused.
     */
    TONE(282, "tone"),

    /**
     * Switch to pulse dialing.
     * @see #DIAL_PHONE
     */
    PULSE(283, "pulse"),

    /**
     * Flash phone switch hook.
     * Unused.
     */
    FLASH_HOOK(284, "hook"),

    /**
     * Unused.
     */
    FIXED_PAUSE(285, "pause"),

    /**
     * Wait for dial tone.
     * Unused.
     * @see #DIAL_PHONE
     */
    WAIT_TONE(286, "wait"),

    /**
     * User string 0.
     */
    USER0(287, "u0"),

    /**
     * User string 1.
     */
    USER1(288, "u1"),

    /**
     * User string 2.
     */
    USER2(289, "u2"),

    /**
     * User string 3.
     */
    USER3(290, "u3"),

    /**
     * User string 4.
     */
    USER4(291, "u4"),

    /**
     * User string 5.
     */
    USER5(292, "u5"),

    /**
     * User string 6.
     */
    USER6(293, "u6"),

    /**
     * User string 7.
     */
    USER7(294, "u7"),

    /**
     * User string 8.
     */
    USER8(295, "u8"),

    /**
     * User string 9.
     */
    USER9(296, "u9"),

    /**
     * Reset the current color pair to the default.
     * @see #INITIALIZE_PAIR
     * @see StringCapability.Int#SET_COLOR_PAIR
     */
    ORIG_PAIR(297, "op"),

    /**
     * Reset color pairs to their defaults.
     * @see #INITIALIZE_PAIR
     */
    ORIG_COLORS(298, "oc"),

    /**
     * Set the specified color number.
     * The color number is the first argument, subsequent arguments
     * are the red, green, blue values
     * (unless {@link BooleanCapability#HUE_LIGHTNESS_SATURATION} is set).
     */
    INITIALIZE_COLOR(299, "initc", 4),

    /**
     * Set foreground and background color for a numbered color pair.
     * Arguments are the color pair number, and red, green, blue values
     * (unless {@link BooleanCapability#HUE_LIGHTNESS_SATURATION} is set),
     * first for the foreground color, then for the background color.
     */
    INITIALIZE_PAIR(300, "initp", 7),

    /**
     * Change horizontal resolution.
     * De-facto unused.  (Only use does not have parameters, but
     * {@code tic} in ncurses expects one.)
     */
    CHANGE_RES_HORZ(306, "chr", 1),

    /**
     * Change vertical resolution.
     * De-facto unused.  (Only use does not have parameters, but
     * {@code tic} in ncurses expects one.)
     */
    CHANGE_RES_VERT(307, "cvr", 1),

    /**
     * Define a character, with height, width, and descender.
     * (Unused, parameter types uncertain.)
     */
    DEFINE_CHAR(308, "defc", 3),

    /**
     * Enter double-wide mode.
     */
    ENTER_DOUBLEWIDE_MODE(309, "swidm"),

    /**
     * Unused.
     */
    ENTER_DRAFT_QUALITY(310, "sdrfq"),

    /**
     * Switch to italics mode.
     * @see #EXIT_ITALICS_MODE
     */
    ENTER_ITALICS_MODE(311, "sitm"),

    /**
     * Unused.
     */
    ENTER_LEFTWARD_MODE(312, "slm"),

    /**
     * Switch on micro mode.
     * Micro mode allows finer-grained cursor movement.
     * @see #EXIT_MICRO_MODE
     */
    ENTER_MICRO_MODE(313, "smicm"),

    /**
     * Increase printing quality.
     * @see #ENTER_NORMAL_QUALITY
     */
    ENTER_NEAR_LETTER_QUALITY(314, "snlq"),

    /**
     * Use standard printing quality.
     * (Unused.)
     * @see #ENTER_NEAR_LETTER_QUALITY
     */
    ENTER_NORMAL_QUALITY(315, "snrmq"),

    /**
     * Switch to shadow-print mode.
     * @see #EXIT_SHADOW_MODE
     */
    ENTER_SHADOW_MODE(316, "sshm"),

    /**
     * Unused.
     */
    ENTER_SUBSCRIPT_MODE(317, "ssubm"),

    /**
     * Unused.
     */
    ENTER_SUPERSCRIPT_MODE(318, "ssupm"),

    /**
     * Unused.
     */
    ENTER_UPWARD_MODE(319, "sum"),

    /**
     * Unused.
     */
    EXIT_DOUBLEWIDE_MODE(320, "rwidm"),

    /**
     * Switch off italics mode.
     * @see #ENTER_ITALICS_MODE
     */
    EXIT_ITALICS_MODE(321, "ritm"),

    /**
     * Unused.
     */
    EXIT_LEFTWARD_MODE(322, "rlm"),

    /**
     * Exit micro mode.
     * @see #ENTER_MICRO_MODE
     * @see BooleanCapability#CR_CANCELS_MICRO_MODE
     */
    EXIT_MICRO_MODE(323, "rmicm"),

    /**
     * Disable shadow-print mode.
     * @see #ENTER_SHADOW_MODE
     */
    EXIT_SHADOW_MODE(324, "rshm"),

    /**
     * Unused.
     */
    EXIT_SUBSCRIPT_MODE(325, "rsubm"),

    /**
     * Unused.
     */
    EXIT_SUPERSCRIPT_MODE(326, "rsupm"),

    /**
     * Unused.
     */
    EXIT_UPWARD_MODE(327, "rum"),

    /**
     * Unused.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     */
    MICRO_COLUMN_ADDRESS(328, "mhpa"),

    /**
     * Move the cursor one unit downwards.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see #CURSOR_DOWN
     */
    MICRO_DOWN(329, "mcud1"),

    /**
     * Move the cursor one unit to the left.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see #CURSOR_LEFT
     */
    MICRO_LEFT(330, "mcub1"),

    /**
     * Move the cursor one unit to the right.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see #CURSOR_RIGHT
     */
    MICRO_RIGHT(331, "mcuf1"),

    /**
     * Move the cursor one unit upwards.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see #CURSOR_UP
     */
    MICRO_UP(333, "mcuu1"),

    /**
     * Unused.
     */
    ORDER_OF_PINS(334, "porder"),

    /**
     * Move the cursor downwards by the specified number of units.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see StringCapability.Int#PARM_DOWN_CURSOR
     */
    PARM_DOWN_MICRO(335, "mcud"),

    /**
     * Move the cursor left by the specified number of units.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see StringCapability.Int#PARM_LEFT_CURSOR
     */
    PARM_LEFT_MICRO(336, "mcub"),

    /**
     * Move the cursor right by the specified number of units.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see StringCapability.Int#PARM_RIGHT_CURSOR
     */
    PARM_RIGHT_MICRO(337, "mcuf"),

    /**
     * Move the cursor upwards by the specified number of units.
     * For use in micro mode.
     * @see UntypedStringCapability#ENTER_MICRO_MODE
     * @see StringCapability.Int#PARM_UP_CURSOR
     */
    PARM_UP_MICRO(338, "mcuu"),

    /**
     * Use the current line to set the bottom margin.
     * @see StringCapability.Int#SET_BOTTOM_MARGIN_PARM
     */
    SET_BOTTOM_MARGIN(340, "smgb"),

    /**
     * Use the current line to set the top margin.
     * @see StringCapability.Int#SET_TOP_MARGIN_PARM
     */
    SET_TOP_MARGIN(344, "smgt"),

    /**
     * Unused.
     */
    START_BIT_IMAGE(346, "sbim"),

    /**
     * Unused.  Parameter types unknown.
     */
    START_CHAR_SET_DEF(347, "scsd", 2),

    /**
     * Unused.
     */
    STOP_BIT_IMAGE(348, "rbim"),

    /**
     * Unused, parameter type unknown.
     */
    STOP_CHAR_SET_DEF(349, "rcsd", 1),

    /**
     * Unused.
     */
    SUBSCRIPT_CHARACTERS(350, "subcs"),

    /**
     * Unused.
     */
    SUPERSCRIPT_CHARACTERS(351, "supcs"),

    /**
     * List of characters which, when printed, trigger
     * {@link #CARRIAGE_RETURN}.
     * Unused.
     */
    THESE_CAUSE_CR(352, "docr"),

    /**
     * Unused.
     */
    ZERO_MOTION(353, "zerom"),

    /**
     * Prefix of a mouse position peport.  Usually {@code "\033[M"} if
     * defined.
     */
    KEY_MOUSE(355, "kmous"),

    /**
     * Unused.
     */
    MOUSE_INFO(356, "minfo"),

    /**
     * Unused in practice.
     */
    REQ_MOUSE_POS(357, "reqmp"),

    /**
     * Program a function key to send a string and label it.
     * The arguments are key number, the string to send, and the string
     * to show as the label.
     */
    PKEY_PLAB(361, "pfxl", 3),

    /**
     * Unused.
     */
    DEVICE_TYPE(362, "devt"),

    /**
     * Unused.
     */
    CODE_SET_INIT(363, "csin"),

    /**
     * Switch to code set 0.
     * Typically ASCII.  Used with shifted character sets.
     */
    SET0_DES_SEQ(364, "s0ds"),

    /**
     * Switch to code set 1.
     * Used with shifted character sets.
     */
    SET1_DES_SEQ(365, "s1ds"),

    /**
     * Switch to code set 2.
     * Used with shifted character sets.
     */
    SET2_DES_SEQ(366, "s2ds"),

    /**
     * Switch to code set 3.
     * Used with shifted character sets.
     */
    SET3_DES_SEQ(367, "s3ds"),

    /**
     * Unused.  Parameter types unknown.
     */
    SET_LR_MARGIN(368, "smglr", 2),

    /**
     * Unused.  Parameter types unknown.
     */
    SET_TB_MARGIN(369, "smgtb", 2),

    /**
     * Unused, parameter types unknown.
     */
    BIT_IMAGE_REPEAT(370, "birep", 2),

    /**
     * Unused.
     */
    BIT_IMAGE_NEWLINE(371, "binel"),

    /**
     * Unused.
     */
    BIT_IMAGE_CARRIAGE_RETURN(372, "bicr"),

    /**
     * Unused, parameter type unknown.
     */
    COLOR_NAMES(373, "colornm", 1),

    /**
     * Unused.
     */
    DEFINE_BIT_IMAGE_REGION(374, "defbi"),

    /**
     * Unused.
     */
    END_BIT_IMAGE_REGION(375, "endbi"),

    /**
     * Unused, parameter type unknown.
     */
    SET_COLOR_BAND(376, "setcolor", 1),

    /**
     * Unused, parameter type unknown.
     */
    SET_PAGE_LENGTH(377, "slines", 1),

    /**
     * Switch to PC character set mode.
     * @see #ACS_CHARS
     * @see StringCapability.Int#DISPLAY_PC_CHAR
     * @see #EXIT_PC_CHARSET_MODE
     */
    ENTER_PC_CHARSET_MODE(379, "smpch"),

    /**
     * Switch to the default character set mode.
     * @see #ENTER_PC_CHARSET_MODE
     */
    EXIT_PC_CHARSET_MODE(380, "rmpch"),

    /**
     * Tell the terminal to transmit key scan codes, not translated
     * characters.
     * Unused in practice.
     * @see #EXIT_SCANCODE_MODE
     */
    ENTER_SCANCODE_MODE(381, "smsc"),

    /**
     * Return to translated character mode.
     * @see #ENTER_SCANCODE_MODE
     * Unused in practice.
     */
    EXIT_SCANCODE_MODE(382, "rmsc"),

    /**
     * PC terminal options.
     * Unused in practice.
     */
    PC_TERM_OPTIONS(383, "pctrm"),

    /**
     * Unused.
     */
    SCANCODE_ESCAPE(384, "scesc"),

    /**
     * Unused.
     */
    ALT_SCANCODE_ESC(385, "scesa"),

    /**
     * Unused.
     */
    ENTER_HORIZONTAL_HL_MODE(386, "ehhlm"),

    /**
     * Unused.
     */
    ENTER_LEFT_HL_MODE(387, "elhlm"),

    /**
     * Unused.
     */
    ENTER_LOW_HL_MODE(388, "elohlm"),

    /**
     * Unused.
     */
    ENTER_RIGHT_HL_MODE(389, "erhlm"),

    /**
     * Unused.
     */
    ENTER_TOP_HL_MODE(390, "ethlm"),

    /**
     * Unused.
     */
    ENTER_VERTICAL_HL_MODE(391, "evhlm"),

    /**
     * Unused.  Parameter types unknown.
     */
    SET_A_ATTRIBUTES(392, "sgr1", 6),

    /**
     * Unused, parameter type unknown.
     */
    SET_PGLEN_INCH(393, "slength", 1),
    ;

    @Override
    public int index() {
        return index;
    }

    @Override
    public int parameters() {
        return parameters;
    }

    @Override
    public String terminfoName() {
        return terminfoName;
    }

    private final int index;
    private final String terminfoName;
    private final int parameters;

    // Maximum across all defined string capabilities, plus one.
    static final int COUNT = 394;

    // List of all string capabilities (excluding key capabilities).
    static final List<StringCapability> VALUES;
    static {
        ArrayList<StringCapability> list = new ArrayList<>();
        list.addAll(Arrays.asList(values()));
        list.addAll(Arrays.asList(Int.values()));
        list.addAll(Arrays.asList(IntStr.values()));
        list.sort(Comparator.comparing(StringCapability::index));
        VALUES = List.copyOf(list);
    }

    private UntypedStringCapability(int index, String terminfoName,
        int parameters) {
        this.index = index;
        this.terminfoName = terminfoName;
        this.parameters = parameters;
    }

    private UntypedStringCapability(int index, String terminfoName) {
        this(index, terminfoName, 0);
    }
}
