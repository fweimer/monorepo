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
    COLUMN_ADDRESS(8, "hpa", 1),

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
    DELETE_LINE(22, "dl1", 1),

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
    ENTER_CA_MODE(28, "smcup", 2),

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
    ERASE_CHARS(37, "ech", 1),

    /**
     */
    EXIT_ALT_CHARSET_MODE(38, "rmacs"),

    /**
     */
    EXIT_ATTRIBUTE_MODE(39, "sgr0"),

    /**
     */
    EXIT_CA_MODE(40, "rmcup", 2),

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
    KEY_BACKSPACE(55, "kbs"),

    /**
     */
    KEY_CATAB(56, "ktbc"),

    /**
     */
    KEY_CLEAR(57, "kclr"),

    /**
     */
    KEY_CTAB(58, "kctab"),

    /**
     */
    KEY_DC(59, "kdch1"),

    /**
     */
    KEY_DL(60, "kdl1"),

    /**
     */
    KEY_DOWN(61, "kcud1"),

    /**
     */
    KEY_EIC(62, "krmir"),

    /**
     */
    KEY_EOL(63, "kel"),

    /**
     */
    KEY_EOS(64, "ked"),

    /**
     */
    KEY_F0(65, "kf0"),

    /**
     */
    KEY_F1(66, "kf1"),

    /**
     */
    KEY_F10(67, "kf10"),

    /**
     */
    KEY_F2(68, "kf2"),

    /**
     */
    KEY_F3(69, "kf3"),

    /**
     */
    KEY_F4(70, "kf4"),

    /**
     */
    KEY_F5(71, "kf5"),

    /**
     */
    KEY_F6(72, "kf6"),

    /**
     */
    KEY_F7(73, "kf7"),

    /**
     */
    KEY_F8(74, "kf8"),

    /**
     */
    KEY_F9(75, "kf9"),

    /**
     */
    KEY_HOME(76, "khome"),

    /**
     */
    KEY_IC(77, "kich1"),

    /**
     */
    KEY_IL(78, "kil1"),

    /**
     */
    KEY_LEFT(79, "kcub1"),

    /**
     */
    KEY_LL(80, "kll"),

    /**
     */
    KEY_NPAGE(81, "knp"),

    /**
     */
    KEY_PPAGE(82, "kpp"),

    /**
     */
    KEY_RIGHT(83, "kcuf1"),

    /**
     */
    KEY_SF(84, "kind"),

    /**
     */
    KEY_SR(85, "kri"),

    /**
     */
    KEY_STAB(86, "khts"),

    /**
     */
    KEY_UP(87, "kcuu1"),

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
    PARM_DCH(105, "dch", 1),

    /**
     */
    PARM_DELETE_LINE(106, "dl", 1),

    /**
     */
    PARM_DOWN_CURSOR(107, "cud", 1),

    /**
     */
    PARM_ICH(108, "ich", 1),

    /**
     */
    PARM_INDEX(109, "indn", 1),

    /**
     */
    PARM_INSERT_LINE(110, "il", 1),

    /**
     */
    PARM_LEFT_CURSOR(111, "cub", 1),

    /**
     */
    PARM_RIGHT_CURSOR(112, "cuf", 1),

    /**
     */
    PARM_RINDEX(113, "rin", 1),

    /**
     */
    PARM_UP_CURSOR(114, "cuu", 1),

    /**
     */
    PKEY_KEY(115, "pfkey", 2),

    /**
     */
    PKEY_LOCAL(116, "pfloc", 2),

    /**
     */
    PKEY_XMIT(117, "pfx", 2),

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
    ROW_ADDRESS(127, "vpa", 1),

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
    TO_STATUS_LINE(135, "tsl", 1),

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
    KEY_A1(139, "ka1"),

    /**
     */
    KEY_A3(140, "ka3"),

    /**
     */
    KEY_B2(141, "kb2"),

    /**
     */
    KEY_C1(142, "kc1"),

    /**
     */
    KEY_C3(143, "kc3"),

    /**
     */
    PRTR_NON(144, "mc5p", 1),

    /**
     */
    CHAR_PADDING(145, "rmp"),

    /**
     */
    ACS_CHARS(146, "acsc"),

    /**
     */
    PLAB_NORM(147, "pln", 2),

    /**
     */
    KEY_BTAB(148, "kcbt"),

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
    KEY_BEG(158, "kbeg"),

    /**
     */
    KEY_CANCEL(159, "kcan"),

    /**
     */
    KEY_CLOSE(160, "kclo"),

    /**
     */
    KEY_COMMAND(161, "kcmd"),

    /**
     */
    KEY_COPY(162, "kcpy"),

    /**
     */
    KEY_CREATE(163, "kcrt"),

    /**
     */
    KEY_END(164, "kend"),

    /**
     */
    KEY_ENTER(165, "kent"),

    /**
     */
    KEY_EXIT(166, "kext"),

    /**
     */
    KEY_FIND(167, "kfnd"),

    /**
     */
    KEY_HELP(168, "khlp"),

    /**
     */
    KEY_MARK(169, "kmrk"),

    /**
     */
    KEY_MESSAGE(170, "kmsg"),

    /**
     */
    KEY_MOVE(171, "kmov"),

    /**
     */
    KEY_NEXT(172, "knxt"),

    /**
     */
    KEY_OPEN(173, "kopn"),

    /**
     */
    KEY_OPTIONS(174, "kopt"),

    /**
     */
    KEY_PREVIOUS(175, "kprv"),

    /**
     */
    KEY_PRINT(176, "kprt"),

    /**
     */
    KEY_REDO(177, "krdo"),

    /**
     */
    KEY_REFERENCE(178, "kref"),

    /**
     */
    KEY_REFRESH(179, "krfr"),

    /**
     */
    KEY_REPLACE(180, "krpl"),

    /**
     */
    KEY_RESTART(181, "krst"),

    /**
     */
    KEY_RESUME(182, "kres"),

    /**
     */
    KEY_SAVE(183, "ksav"),

    /**
     */
    KEY_SUSPEND(184, "kspd"),

    /**
     */
    KEY_UNDO(185, "kund"),

    /**
     */
    KEY_SBEG(186, "kBEG"),

    /**
     */
    KEY_SCANCEL(187, "kCAN"),

    /**
     */
    KEY_SCOMMAND(188, "kCMD"),

    /**
     */
    KEY_SCOPY(189, "kCPY"),

    /**
     */
    KEY_SCREATE(190, "kCRT"),

    /**
     */
    KEY_SDC(191, "kDC"),

    /**
     */
    KEY_SDL(192, "kDL"),

    /**
     */
    KEY_SELECT(193, "kslt"),

    /**
     */
    KEY_SEND(194, "kEND"),

    /**
     */
    KEY_SEOL(195, "kEOL"),

    /**
     */
    KEY_SEXIT(196, "kEXT"),

    /**
     */
    KEY_SFIND(197, "kFND"),

    /**
     */
    KEY_SHELP(198, "kHLP"),

    /**
     */
    KEY_SHOME(199, "kHOM"),

    /**
     */
    KEY_SIC(200, "kIC"),

    /**
     */
    KEY_SLEFT(201, "kLFT"),

    /**
     */
    KEY_SMESSAGE(202, "kMSG"),

    /**
     */
    KEY_SMOVE(203, "kMOV"),

    /**
     */
    KEY_SNEXT(204, "kNXT"),

    /**
     */
    KEY_SOPTIONS(205, "kOPT"),

    /**
     */
    KEY_SPREVIOUS(206, "kPRV"),

    /**
     */
    KEY_SPRINT(207, "kPRT"),

    /**
     */
    KEY_SREDO(208, "kRDO"),

    /**
     */
    KEY_SREPLACE(209, "kRPL"),

    /**
     */
    KEY_SRIGHT(210, "kRIT"),

    /**
     */
    KEY_SRSUME(211, "kRES"),

    /**
     */
    KEY_SSAVE(212, "kSAV"),

    /**
     */
    KEY_SSUSPEND(213, "kSPD"),

    /**
     */
    KEY_SUNDO(214, "kUND"),

    /**
     */
    REQ_FOR_INPUT(215, "rfi"),

    /**
     */
    KEY_F11(216, "kf11"),

    /**
     */
    KEY_F12(217, "kf12"),

    /**
     */
    KEY_F13(218, "kf13"),

    /**
     */
    KEY_F14(219, "kf14"),

    /**
     */
    KEY_F15(220, "kf15"),

    /**
     */
    KEY_F16(221, "kf16"),

    /**
     */
    KEY_F17(222, "kf17"),

    /**
     */
    KEY_F18(223, "kf18"),

    /**
     */
    KEY_F19(224, "kf19"),

    /**
     */
    KEY_F20(225, "kf20"),

    /**
     */
    KEY_F21(226, "kf21"),

    /**
     */
    KEY_F22(227, "kf22"),

    /**
     */
    KEY_F23(228, "kf23"),

    /**
     */
    KEY_F24(229, "kf24"),

    /**
     */
    KEY_F25(230, "kf25"),

    /**
     */
    KEY_F26(231, "kf26"),

    /**
     */
    KEY_F27(232, "kf27"),

    /**
     */
    KEY_F28(233, "kf28"),

    /**
     */
    KEY_F29(234, "kf29"),

    /**
     */
    KEY_F30(235, "kf30"),

    /**
     */
    KEY_F31(236, "kf31"),

    /**
     */
    KEY_F32(237, "kf32"),

    /**
     */
    KEY_F33(238, "kf33"),

    /**
     */
    KEY_F34(239, "kf34"),

    /**
     */
    KEY_F35(240, "kf35"),

    /**
     */
    KEY_F36(241, "kf36"),

    /**
     */
    KEY_F37(242, "kf37"),

    /**
     */
    KEY_F38(243, "kf38"),

    /**
     */
    KEY_F39(244, "kf39"),

    /**
     */
    KEY_F40(245, "kf40"),

    /**
     */
    KEY_F41(246, "kf41"),

    /**
     */
    KEY_F42(247, "kf42"),

    /**
     */
    KEY_F43(248, "kf43"),

    /**
     */
    KEY_F44(249, "kf44"),

    /**
     */
    KEY_F45(250, "kf45"),

    /**
     */
    KEY_F46(251, "kf46"),

    /**
     */
    KEY_F47(252, "kf47"),

    /**
     */
    KEY_F48(253, "kf48"),

    /**
     */
    KEY_F49(254, "kf49"),

    /**
     */
    KEY_F50(255, "kf50"),

    /**
     */
    KEY_F51(256, "kf51"),

    /**
     */
    KEY_F52(257, "kf52"),

    /**
     */
    KEY_F53(258, "kf53"),

    /**
     */
    KEY_F54(259, "kf54"),

    /**
     */
    KEY_F55(260, "kf55"),

    /**
     */
    KEY_F56(261, "kf56"),

    /**
     */
    KEY_F57(262, "kf57"),

    /**
     */
    KEY_F58(263, "kf58"),

    /**
     */
    KEY_F59(264, "kf59"),

    /**
     */
    KEY_F60(265, "kf60"),

    /**
     */
    KEY_F61(266, "kf61"),

    /**
     */
    KEY_F62(267, "kf62"),

    /**
     */
    KEY_F63(268, "kf63"),

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
    SET_COLOR_PAIR(301, "scp", 1),

    /**
     */
    SET_FOREGROUND(302, "setf", 1),

    /**
     */
    SET_BACKGROUND(303, "setb", 1),

    /**
     */
    CHANGE_CHAR_PITCH(304, "cpi", 1),

    /**
     */
    CHANGE_LINE_PITCH(305, "lpi", 1),

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
    MICRO_ROW_ADDRESS(332, "mvpa", 1),

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
    SELECT_CHAR_SET(339, "scs", 1),

    /**
     */
    SET_BOTTOM_MARGIN(340, "smgb"),

    /**
     */
    SET_BOTTOM_MARGIN_PARM(341, "smgbp", 1),

    /**
     */
    SET_LEFT_MARGIN_PARM(342, "smglp", 1),

    /**
     */
    SET_RIGHT_MARGIN_PARM(343, "smgrp", 1),

    /**
     */
    SET_TOP_MARGIN(344, "smgt"),

    /**
     */
    SET_TOP_MARGIN_PARM(345, "smgtp", 1),

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
     */
    CHAR_SET_NAMES(354, "csnm", 1),

    /**
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
    GET_MOUSE(358, "getm", 1),

    /**
     */
    SET_A_FOREGROUND(359, "setaf", 1),

    /**
     */
    SET_A_BACKGROUND(360, "setab", 1),

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
    DISPLAY_PC_CHAR(378, "dispc", 1),

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

    /**
     * Returns the index of the capability in the terminal file.
     * @return a non-negative integer
     */
    public int index() {
        return index;
    }

    /**
     * Returns the expected number of parameters.
     * @return a non-negative integer
     */
    public int parameters() {
        return parameters;
    }

    /**
     * Returns the name that is used in terminfo definitions.
     * @return a non-empty string
     */
    public String terminfoName() {
        return terminfoName;
    }

    private final int index;
    private final String terminfoName;
    private final int parameters;

    // Maximum across all defined string capabilities, plus one.
    static final int COUNT = 394;

    private StringCapability(int index, String terminfoName, int parameters) {
        this.index = index;
        this.terminfoName = terminfoName;
        this.parameters = parameters;
    }

    private StringCapability(int index, String terminfoName) {
        this(index, terminfoName, 0);
    }
}
