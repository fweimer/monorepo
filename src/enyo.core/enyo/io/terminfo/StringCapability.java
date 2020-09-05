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
         */
        COLUMN_ADDRESS(8, "hpa"),

        /**
         */
        ERASE_CHARS(37, "ech"),

        /**
         */
        PARM_DCH(105, "dch"),

        /**
         */
        PARM_DELETE_LINE(106, "dl"),

        /**
         */
        PARM_DOWN_CURSOR(107, "cud"),

        /**
         */
        PARM_ICH(108, "ich"),

        /**
         */
        PARM_INDEX(109, "indn"),

        /**
         */
        PARM_INSERT_LINE(110, "il"),

        /**
         */
        PARM_LEFT_CURSOR(111, "cub"),

        /**
         */
        PARM_RIGHT_CURSOR(112, "cuf"),

        /**
         */
        PARM_RINDEX(113, "rin"),

        /**
         */
        PARM_UP_CURSOR(114, "cuu"),

        /**
         */
        ROW_ADDRESS(127, "vpa"),

        /**
         */
        TO_STATUS_LINE(135, "tsl"),

        /**
         */
        PRTR_NON(144, "mc5p"),

        /**
         */
        SET_COLOR_PAIR(301, "scp"),

        /**
         */
        SET_FOREGROUND(302, "setf"),

        /**
         */
        SET_BACKGROUND(303, "setb"),

        /**
         */
        CHANGE_CHAR_PITCH(304, "cpi"),

        /**
         */
        CHANGE_LINE_PITCH(305, "lpi"),

        /**
         */
        MICRO_ROW_ADDRESS(332, "mvpa"),

        /**
         */
        SELECT_CHAR_SET(339, "scs"),

        /**
         */
        SET_BOTTOM_MARGIN_PARM(341, "smgbp"),

        /**
         */
        SET_LEFT_MARGIN_PARM(342, "smglp"),

        /**
         */
        SET_RIGHT_MARGIN_PARM(343, "smgrp"),

        /**
         */
        SET_TOP_MARGIN_PARM(345, "smgtp"),

        /**
         */
        CHAR_SET_NAMES(354, "csnm"),

        /**
         */
        GET_MOUSE(358, "getm"),

        /**
         */
        SET_A_FOREGROUND(359, "setaf"),

        /**
         */
        SET_A_BACKGROUND(360, "setab"),

        /**
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
         */
        PKEY_KEY(115, "pfkey"),

        /**
         */
        PKEY_LOCAL(116, "pfloc"),

        /**
         */
        PKEY_XMIT(117, "pfx"),

        /**
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
