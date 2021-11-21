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
            return Int.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        try {
            return IntStr.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        try {
            return KeyCapability.valueOf(name);
        } catch (IllegalArgumentException e) {
        }
        throw new IllegalArgumentException(name);
    }

    /**
     * String capabilities which accept an integer parameter.
     */
    enum Int implements StringCapability {
        /**
         * Move the cursor to the indicated column.
         * The column is specified relative to the start of the line
         * (absolute).
         * @see #ROW_ADDRESS
         */
        COLUMN_ADDRESS(8, "hpa"),

        /**
         * Erase the specified number of characters.
         */
        ERASE_CHARS(37, "ech"),

        /**
         * Delete the specified number of characters.
         * @see UntypedStringCapability#DELETE_CHARACTER
         */
        PARM_DCH(105, "dch"),

        /**
         * Delete the specified number of lines.
         * @see UntypedStringCapability#DELETE_LINE
         */
        PARM_DELETE_LINE(106, "dl"),

        /**
         * Move cursor downwards by the specified number of lines.
         * @see UntypedStringCapability#CURSOR_DOWN
         */
        PARM_DOWN_CURSOR(107, "cud"),

        /**
         * Treat the following characters (of the specified count) as
         * an insertion.  This the preferred method of handling
         * insertions.
         * @see UntypedStringCapability#INSERT_CHARACTER
         */
        PARM_ICH(108, "ich"),

        /**
         * Scroll forward by the specified number of rows.
         */
        PARM_INDEX(109, "indn"),

        /**
         * Insert the specified number of rows.
         */
        PARM_INSERT_LINE(110, "il"),

        /**
         * Move the cursor left by the specified number of columns.
         * @see UntypedStringCapability#CURSOR_LEFT
         */
        PARM_LEFT_CURSOR(111, "cub"),

        /**
         * Move the cursor right by the specified number of columns.
         * @see UntypedStringCapability#CURSOR_RIGHT
         */
        PARM_RIGHT_CURSOR(112, "cuf"),

        /**
         * Scroll back by the specified number of rows.
         */
        PARM_RINDEX(113, "rin"),

        /**
         * Move cursor upwards by the specified number of lines.
         * @see UntypedStringCapability#CURSOR_UP
         */
        PARM_UP_CURSOR(114, "cuu"),

        /**
         * Move the cursor to the specified line.
         * The line is relative to the start of the screen/window.
         * @see BooleanCapability#ROW_ADDR_GLITCH
         * @see #COLUMN_ADDRESS
         */
        ROW_ADDRESS(127, "vpa"),

        /**
         * Move the cursor to the status line, at the indicated column.
         * @see BooleanCapability#HAS_STATUS_LINE
         * @see UntypedStringCapability#FROM_STATUS_LINE
         */
        TO_STATUS_LINE(135, "tsl"),

        /**
         * Print the following counted bytes with the printer.
         * @see UntypedStringCapability#PRTR_ON
         * @see UntypedStringCapability#PRTR_OFF
         */
        PRTR_NON(144, "mc5p"),

        /**
         * Set the current color pair number.
         */
        SET_COLOR_PAIR(301, "scp"),

        /**
         * Set the foreground color to the indicated color number.
         */
        SET_FOREGROUND(302, "setf"),

        /**
         * Set the background color to the indicated color number.
         */
        SET_BACKGROUND(303, "setb"),

        /**
         * Set the character size, expressed incharacters per inch
         * (cpi).
         * @see BooleanCapability#CPI_CHANGES_RES
         */
        CHANGE_CHAR_PITCH(304, "cpi"),

        /**
         * Set the number of lines per inch.
         * @see BooleanCapability#LPI_CHANGES_RES
         */
        CHANGE_LINE_PITCH(305, "lpi"),

        /**
         * Move the cursor to the given absolute vertifical position
         * To be used in micro mode.
         * @see BooleanCapability#ROW_ADDR_GLITCH
         * @see UntypedStringCapability#ENTER_MICRO_MODE
         * @see #ROW_ADDRESS
         */
        MICRO_ROW_ADDRESS(332, "mvpa"),

        /**
         * Select the specified character set.
         * @see UntypedStringCapability#ENTER_ALT_CHARSET_MODE
         */
        SELECT_CHAR_SET(339, "scs"),

        /**
         * Use the argument to set the bottom margin.
         * @see UntypedStringCapability#SET_BOTTOM_MARGIN
         */
        SET_BOTTOM_MARGIN_PARM(341, "smgbp"),

        /**
         * Use the argument to set the left margin.
         * @see UntypedStringCapability#SET_LEFT_MARGIN
         */
        SET_LEFT_MARGIN_PARM(342, "smglp"),

        /**
         * Use the argument to set the right margin.
         * @see UntypedStringCapability#SET_RIGHT_MARGIN
         */
        SET_RIGHT_MARGIN_PARM(343, "smgrp"),

        /**
         * Use the argument to set the top margin.
         * @see UntypedStringCapability#SET_TOP_MARGIN
         */
        SET_TOP_MARGIN_PARM(345, "smgtp"),

        /**
         * Expands to the name of the specified character set.
         * @see #SELECT_CHAR_SET
         */
        CHAR_SET_NAMES(354, "csnm"),

        /**
         * Mouse-related terminal handshake.
         * Unused in practice.
         */
        GET_MOUSE(358, "getm"),

        /**
         * Set the specified ANSI color number as the foreground color.
         */
        SET_A_FOREGROUND(359, "setaf"),

        /**
         * Set the specified ANSI color number as the background color.
         */
        SET_A_BACKGROUND(360, "setab"),

        /**
         * Display the PC character with the specified number.
         * @see UntypedStringCapability#ENTER_PC_CHARSET_MODE
         */
        DISPLAY_PC_CHAR(378, "dispc"),

        ;

        @Override
        public int index() {
            return index;
        }

        @Override
        public int parameters() {
            return 1;
        }

        @Override
        public String terminfoName() {
            return terminfoName;
        }

        private final int index;
        private final String terminfoName;

        private Int(int index, String terminfoName) {
            this.index = index;
            this.terminfoName = terminfoName;
        }
    }

    /**
     * String capabilities which accept an integer and a string
     * parameter.
     */
    enum IntStr implements StringCapability {
        /**
         * Change the specified function key to input the specified string.
         */
        PKEY_KEY(115, "pfkey"),

        /**
         * Change the specified function key to execute the specified string.
         */
        PKEY_LOCAL(116, "pfloc"),

        /**
         * Change the specified function key to transmit the specified string.
         */
        PKEY_XMIT(117, "pfx"),

        /**
         * Change the specified function key to display the specified string.
         */
        PLAB_NORM(147, "pln"),
        ;

        @Override
        public int index() {
            return index;
        }

        @Override
        public int parameters() {
            return 2;
        }

        @Override
        public String terminfoName() {
            return terminfoName;
        }

        private final int index;
        private final String terminfoName;

        private IntStr(int index, String terminfoName) {
            this.index = index;
            this.terminfoName = terminfoName;
        }
    }
}
