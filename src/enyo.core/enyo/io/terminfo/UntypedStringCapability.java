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
     */
    BACK_TAB(0, "cbt"),

    /**
     */
    BELL(1, "bel"),

    /**
     */
    CARRIAGE_RETURN(2, "cr"),

    /**
     */
    CHANGE_SCROLL_REGION(3, "csr", 2),

    /**
     */
    CLEAR_ALL_TABS(4, "tbc"),

    /**
     */
    CLEAR_SCREEN(5, "clear"),

    /**
     */
    CLR_EOL(6, "el"),

    /**
     */
    CLR_EOS(7, "ed"),

    /**
     */
    COMMAND_CHARACTER(9, "cmdch"),

    /**
     */
    CURSOR_ADDRESS(10, "cup", 2),

    /**
     */
    CURSOR_DOWN(11, "cud1"),

    /**
     */
    CURSOR_HOME(12, "home"),

    /**
     */
    CURSOR_INVISIBLE(13, "civis"),

    /**
     */
    CURSOR_LEFT(14, "cub1"),

    /**
     */
    CURSOR_MEM_ADDRESS(15, "mrcup", 2),

    /**
     */
    CURSOR_NORMAL(16, "cnorm"),

    /**
     */
    CURSOR_RIGHT(17, "cuf1"),

    /**
     */
    CURSOR_TO_LL(18, "ll"),

    /**
     */
    CURSOR_UP(19, "cuu1"),

    /**
     */
    CURSOR_VISIBLE(20, "cvvis"),

    /**
     */
    DELETE_CHARACTER(21, "dch1"),

    /**
     */
    DELETE_LINE(22, "dl1"),

    /**
     */
    DIS_STATUS_LINE(23, "dsl"),

    /**
     */
    DOWN_HALF_LINE(24, "hd"),

    /**
     */
    ENTER_ALT_CHARSET_MODE(25, "smacs"),

    /**
     */
    ENTER_BLINK_MODE(26, "blink"),

    /**
     */
    ENTER_BOLD_MODE(27, "bold"),

    /**
     */
    ENTER_CA_MODE(28, "smcup"),

    /**
     */
    ENTER_DELETE_MODE(29, "smdc"),

    /**
     */
    ENTER_DIM_MODE(30, "dim"),

    /**
     */
    ENTER_INSERT_MODE(31, "smir"),

    /**
     */
    ENTER_SECURE_MODE(32, "invis"),

    /**
     */
    ENTER_PROTECTED_MODE(33, "prot"),

    /**
     */
    ENTER_REVERSE_MODE(34, "rev"),

    /**
     */
    ENTER_STANDOUT_MODE(35, "smso"),

    /**
     */
    ENTER_UNDERLINE_MODE(36, "smul"),

    /**
     */
    EXIT_ALT_CHARSET_MODE(38, "rmacs"),

    /**
     */
    EXIT_ATTRIBUTE_MODE(39, "sgr0"),

    /**
     */
    EXIT_CA_MODE(40, "rmcup"),

    /**
     */
    EXIT_DELETE_MODE(41, "rmdc"),

    /**
     */
    EXIT_INSERT_MODE(42, "rmir"),

    /**
     */
    EXIT_STANDOUT_MODE(43, "rmso"),

    /**
     */
    EXIT_UNDERLINE_MODE(44, "rmul"),

    /**
     */
    FLASH_SCREEN(45, "flash"),

    /**
     */
    FORM_FEED(46, "ff"),

    /**
     */
    FROM_STATUS_LINE(47, "fsl"),

    /**
     */
    INIT_1STRING(48, "is1"),

    /**
     */
    INIT_2STRING(49, "is2"),

    /**
     */
    INIT_3STRING(50, "is3"),

    /**
     */
    INIT_FILE(51, "if"),

    /**
     */
    INSERT_CHARACTER(52, "ich1"),

    /**
     */
    INSERT_LINE(53, "il1"),

    /**
     */
    INSERT_PADDING(54, "ip"),

    /**
     */
    KEYPAD_LOCAL(88, "rmkx"),

    /**
     */
    KEYPAD_XMIT(89, "smkx"),

    /**
     */
    LAB_F0(90, "lf0"),

    /**
     */
    LAB_F1(91, "lf1"),

    /**
     */
    LAB_F10(92, "lf10"),

    /**
     */
    LAB_F2(93, "lf2"),

    /**
     */
    LAB_F3(94, "lf3"),

    /**
     */
    LAB_F4(95, "lf4"),

    /**
     */
    LAB_F5(96, "lf5"),

    /**
     */
    LAB_F6(97, "lf6"),

    /**
     */
    LAB_F7(98, "lf7"),

    /**
     */
    LAB_F8(99, "lf8"),

    /**
     */
    LAB_F9(100, "lf9"),

    /**
     */
    META_OFF(101, "rmm"),

    /**
     */
    META_ON(102, "smm"),

    /**
     */
    NEWLINE(103, "nel"),

    /**
     */
    PAD_CHAR(104, "pad"),

    /**
     */
    PRINT_SCREEN(118, "mc0"),

    /**
     */
    PRTR_OFF(119, "mc4"),

    /**
     */
    PRTR_ON(120, "mc5"),

    /**
     */
    REPEAT_CHAR(121, "rep", 2),

    /**
     */
    RESET_1STRING(122, "rs1"),

    /**
     */
    RESET_2STRING(123, "rs2"),

    /**
     */
    RESET_3STRING(124, "rs3"),

    /**
     */
    RESET_FILE(125, "rf"),

    /**
     */
    RESTORE_CURSOR(126, "rc"),

    /**
     */
    SAVE_CURSOR(128, "sc"),

    /**
     */
    SCROLL_FORWARD(129, "ind"),

    /**
     */
    SCROLL_REVERSE(130, "ri"),

    /**
     */
    SET_ATTRIBUTES(131, "sgr", 9),

    /**
     */
    SET_TAB(132, "hts"),

    /**
     */
    SET_WINDOW(133, "wind", 4),

    /**
     */
    TAB(134, "ht"),

    /**
     */
    UNDERLINE_CHAR(136, "uc"),

    /**
     */
    UP_HALF_LINE(137, "hu"),

    /**
     */
    INIT_PROG(138, "iprog"),

    /**
     */
    CHAR_PADDING(145, "rmp"),

    /**
     */
    ACS_CHARS(146, "acsc"),

    /**
     */
    ENTER_XON_MODE(149, "smxon"),

    /**
     */
    EXIT_XON_MODE(150, "rmxon"),

    /**
     */
    ENTER_AM_MODE(151, "smam"),

    /**
     */
    EXIT_AM_MODE(152, "rmam"),

    /**
     */
    XON_CHARACTER(153, "xonc"),

    /**
     */
    XOFF_CHARACTER(154, "xoffc"),

    /**
     */
    ENA_ACS(155, "enacs"),

    /**
     */
    LABEL_ON(156, "smln"),

    /**
     */
    LABEL_OFF(157, "rmln"),

    /**
     */
    REQ_FOR_INPUT(215, "rfi"),

    /**
     */
    CLR_BOL(269, "el1"),

    /**
     */
    CLEAR_MARGINS(270, "mgc"),

    /**
     */
    SET_LEFT_MARGIN(271, "smgl"),

    /**
     */
    SET_RIGHT_MARGIN(272, "smgr"),

    /**
     */
    LABEL_FORMAT(273, "fln"),

    /**
     */
    SET_CLOCK(274, "sclk", 3),

    /**
     */
    DISPLAY_CLOCK(275, "dclk"),

    /**
     */
    REMOVE_CLOCK(276, "rmclk"),

    /**
     */
    CREATE_WINDOW(277, "cwin", 5),

    /**
     */
    GOTO_WINDOW(278, "wingo", 1),

    /**
     */
    HANGUP(279, "hup"),

    /**
     */
    DIAL_PHONE(280, "dial", 1),

    /**
     */
    QUICK_DIAL(281, "qdial", 1),

    /**
     */
    TONE(282, "tone"),

    /**
     */
    PULSE(283, "pulse"),

    /**
     */
    FLASH_HOOK(284, "hook"),

    /**
     */
    FIXED_PAUSE(285, "pause"),

    /**
     */
    WAIT_TONE(286, "wait"),

    /**
     */
    USER0(287, "u0"),

    /**
     */
    USER1(288, "u1"),

    /**
     */
    USER2(289, "u2"),

    /**
     */
    USER3(290, "u3"),

    /**
     */
    USER4(291, "u4"),

    /**
     */
    USER5(292, "u5"),

    /**
     */
    USER6(293, "u6"),

    /**
     */
    USER7(294, "u7"),

    /**
     */
    USER8(295, "u8"),

    /**
     */
    USER9(296, "u9"),

    /**
     */
    ORIG_PAIR(297, "op"),

    /**
     */
    ORIG_COLORS(298, "oc"),

    /**
     */
    INITIALIZE_COLOR(299, "initc", 4),

    /**
     */
    INITIALIZE_PAIR(300, "initp", 7),

    /**
     */
    CHANGE_RES_HORZ(306, "chr", 1),

    /**
     */
    CHANGE_RES_VERT(307, "cvr", 1),

    /**
     */
    DEFINE_CHAR(308, "defc", 3),

    /**
     */
    ENTER_DOUBLEWIDE_MODE(309, "swidm"),

    /**
     */
    ENTER_DRAFT_QUALITY(310, "sdrfq"),

    /**
     */
    ENTER_ITALICS_MODE(311, "sitm"),

    /**
     */
    ENTER_LEFTWARD_MODE(312, "slm"),

    /**
     */
    ENTER_MICRO_MODE(313, "smicm"),

    /**
     */
    ENTER_NEAR_LETTER_QUALITY(314, "snlq"),

    /**
     */
    ENTER_NORMAL_QUALITY(315, "snrmq"),

    /**
     */
    ENTER_SHADOW_MODE(316, "sshm"),

    /**
     */
    ENTER_SUBSCRIPT_MODE(317, "ssubm"),

    /**
     */
    ENTER_SUPERSCRIPT_MODE(318, "ssupm"),

    /**
     */
    ENTER_UPWARD_MODE(319, "sum"),

    /**
     */
    EXIT_DOUBLEWIDE_MODE(320, "rwidm"),

    /**
     */
    EXIT_ITALICS_MODE(321, "ritm"),

    /**
     */
    EXIT_LEFTWARD_MODE(322, "rlm"),

    /**
     */
    EXIT_MICRO_MODE(323, "rmicm"),

    /**
     */
    EXIT_SHADOW_MODE(324, "rshm"),

    /**
     */
    EXIT_SUBSCRIPT_MODE(325, "rsubm"),

    /**
     */
    EXIT_SUPERSCRIPT_MODE(326, "rsupm"),

    /**
     */
    EXIT_UPWARD_MODE(327, "rum"),

    /**
     */
    MICRO_COLUMN_ADDRESS(328, "mhpa"),

    /**
     */
    MICRO_DOWN(329, "mcud1"),

    /**
     */
    MICRO_LEFT(330, "mcub1"),

    /**
     */
    MICRO_RIGHT(331, "mcuf1"),

    /**
     */
    MICRO_UP(333, "mcuu1"),

    /**
     */
    ORDER_OF_PINS(334, "porder"),

    /**
     */
    PARM_DOWN_MICRO(335, "mcud"),

    /**
     */
    PARM_LEFT_MICRO(336, "mcub"),

    /**
     */
    PARM_RIGHT_MICRO(337, "mcuf"),

    /**
     */
    PARM_UP_MICRO(338, "mcuu"),

    /**
     */
    SET_BOTTOM_MARGIN(340, "smgb"),

    /**
     */
    SET_TOP_MARGIN(344, "smgt"),

    /**
     */
    START_BIT_IMAGE(346, "sbim"),

    /**
     */
    START_CHAR_SET_DEF(347, "scsd", 2),

    /**
     */
    STOP_BIT_IMAGE(348, "rbim"),

    /**
     */
    STOP_CHAR_SET_DEF(349, "rcsd", 1),

    /**
     */
    SUBSCRIPT_CHARACTERS(350, "subcs"),

    /**
     */
    SUPERSCRIPT_CHARACTERS(351, "supcs"),

    /**
     */
    THESE_CAUSE_CR(352, "docr"),

    /**
     */
    ZERO_MOTION(353, "zerom"),

    /**
     * Prefix of a mouse position peport.  Usually {@code "\033[M"} if
     * defined.
     */
    KEY_MOUSE(355, "kmous"),

    /**
     */
    MOUSE_INFO(356, "minfo"),

    /**
     */
    REQ_MOUSE_POS(357, "reqmp"),

    /**
     */
    PKEY_PLAB(361, "pfxl", 3),

    /**
     */
    DEVICE_TYPE(362, "devt"),

    /**
     */
    CODE_SET_INIT(363, "csin"),

    /**
     */
    SET0_DES_SEQ(364, "s0ds"),

    /**
     */
    SET1_DES_SEQ(365, "s1ds"),

    /**
     */
    SET2_DES_SEQ(366, "s2ds"),

    /**
     */
    SET3_DES_SEQ(367, "s3ds"),

    /**
     */
    SET_LR_MARGIN(368, "smglr", 2),

    /**
     */
    SET_TB_MARGIN(369, "smgtb", 2),

    /**
     */
    BIT_IMAGE_REPEAT(370, "birep", 2),

    /**
     */
    BIT_IMAGE_NEWLINE(371, "binel"),

    /**
     */
    BIT_IMAGE_CARRIAGE_RETURN(372, "bicr"),

    /**
     */
    COLOR_NAMES(373, "colornm", 1),

    /**
     */
    DEFINE_BIT_IMAGE_REGION(374, "defbi"),

    /**
     */
    END_BIT_IMAGE_REGION(375, "endbi"),

    /**
     */
    SET_COLOR_BAND(376, "setcolor", 1),

    /**
     */
    SET_PAGE_LENGTH(377, "slines", 1),

    /**
     */
    ENTER_PC_CHARSET_MODE(379, "smpch"),

    /**
     */
    EXIT_PC_CHARSET_MODE(380, "rmpch"),

    /**
     */
    ENTER_SCANCODE_MODE(381, "smsc"),

    /**
     */
    EXIT_SCANCODE_MODE(382, "rmsc"),

    /**
     */
    PC_TERM_OPTIONS(383, "pctrm"),

    /**
     */
    SCANCODE_ESCAPE(384, "scesc"),

    /**
     */
    ALT_SCANCODE_ESC(385, "scesa"),

    /**
     */
    ENTER_HORIZONTAL_HL_MODE(386, "ehhlm"),

    /**
     */
    ENTER_LEFT_HL_MODE(387, "elhlm"),

    /**
     */
    ENTER_LOW_HL_MODE(388, "elohlm"),

    /**
     */
    ENTER_RIGHT_HL_MODE(389, "erhlm"),

    /**
     */
    ENTER_TOP_HL_MODE(390, "ethlm"),

    /**
     */
    ENTER_VERTICAL_HL_MODE(391, "evhlm"),

    /**
     */
    SET_A_ATTRIBUTES(392, "sgr1", 6),

    /**
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
