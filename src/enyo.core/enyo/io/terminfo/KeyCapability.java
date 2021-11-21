package enyo.io.terminfo;

/**
 * A terminal string capability that is sent from the terminal if a
 * key is pressed.
 *
 * @see StringCapability
 * @see TermFile#get(StringCapability)
 */
public enum KeyCapability implements StringCapability {
    /**
     * Backspace key.
     */
    KEY_BACKSPACE(55, "kbs"),

    /**
     * Clear-All-Tabs key.
     */
    KEY_CATAB(56, "ktbc"),

    /**
     * Clear-Screen key.
     */
    KEY_CLEAR(57, "kclr"),

    /**
     * Clear-Tab key.
     * @see #KEY_STAB
     */
    KEY_CTAB(58, "kctab"),

    /**
     * Delete-Character key.
     */
    KEY_DC(59, "kdch1"),

    /**
     * Delete-Line key.
     */
    KEY_DL(60, "kdl1"),

    /**
     * Cursor-Down key.
     */
    KEY_DOWN(61, "kcud1"),

    /**
     * Virtual key used in insert mode.
     * @see UntypedStringCapability#ENTER_INSERT_MODE
     * @see UntypedStringCapability#EXIT_INSERT_MODE
     */
    KEY_EIC(62, "krmir"),

    /**
     * Clear-To-End-of-Line key.
     */
    KEY_EOL(63, "kel"),

    /**
     * Clear-To-End-of-Screen key.
     */
    KEY_EOS(64, "ked"),

    /**
     * F0 function key.
     */
    KEY_F0(65, "kf0"),

    /**
     * F1 function key.
     */
    KEY_F1(66, "kf1"),

    /**
     * F10 function key.
     */
    KEY_F10(67, "kf10"),

    /**
     * F2 function key.
     */
    KEY_F2(68, "kf2"),

    /**
     * F3 function key.
     */
    KEY_F3(69, "kf3"),

    /**
     * F4 function key.
     */
    KEY_F4(70, "kf4"),

    /**
     * F5 function key.
     */
    KEY_F5(71, "kf5"),

    /**
     * F6 function key.
     */
    KEY_F6(72, "kf6"),

    /**
     * F7 function key.
     */
    KEY_F7(73, "kf7"),

    /**
     * F8 function key.
     */
    KEY_F8(74, "kf8"),

    /**
     * F9 function key.
     */
    KEY_F9(75, "kf9"),

    /**
     * Home key.
     */
    KEY_HOME(76, "khome"),

    /**
     * Insert-Character key.
     */
    KEY_IC(77, "kich1"),

    /**
     * Insert-Line key.
     */
    KEY_IL(78, "kil1"),

    /**
     * Cursor-Left key.
     */
    KEY_LEFT(79, "kcub1"),

    /**
     * Lower-Left key.
     */
    KEY_LL(80, "kll"),

    /**
     * Page-Down key.
     */
    KEY_NPAGE(81, "knp"),

    /**
     * Page-Up key.
     */
    KEY_PPAGE(82, "kpp"),

    /**
     * Cursor-Right key.
     */
    KEY_RIGHT(83, "kcuf1"),

    /**
     * Scroll-Forward key.
     */
    KEY_SF(84, "kind"),

    /**
     * Scroll-Reverse key.
     */
    KEY_SR(85, "kri"),

    /**
     * Set-Tab key.
     * @see #KEY_CTAB
     */
    KEY_STAB(86, "khts"),

    /**
     * Cursor-Up key.
     */
    KEY_UP(87, "kcuu1"),

    /**
     * Top left key on keypad.
     */
    KEY_A1(139, "ka1"),

    /**
     * Top right key on keypad.
     */
    KEY_A3(140, "ka3"),

    /**
     * Center key on keypad (5).
     */
    KEY_B2(141, "kb2"),

    /**
     * Botton left key on keypad.
     */
    KEY_C1(142, "kc1"),

    /**
     * Btoom right key on keypad.
     */
    KEY_C3(143, "kc3"),

    /**
     * Back-Tab key.
     */
    KEY_BTAB(148, "kcbt"),

    /**
     * Begin key.
     */
    KEY_BEG(158, "kbeg"),

    /**
     * Cancel key.
     */
    KEY_CANCEL(159, "kcan"),

    /**
     * Close key.
     */
    KEY_CLOSE(160, "kclo"),

    /**
     * Command key.
     */
    KEY_COMMAND(161, "kcmd"),

    /**
     * Copy key.
     */
    KEY_COPY(162, "kcpy"),

    /**
     * Create key.
     */
    KEY_CREATE(163, "kcrt"),

    /**
     * End key.
     */
    KEY_END(164, "kend"),

    /**
     * Enter key.
     */
    KEY_ENTER(165, "kent"),

    /**
     * Exit key.
     */
    KEY_EXIT(166, "kext"),

    /**
     * Find key.
     */
    KEY_FIND(167, "kfnd"),

    /**
     * Help key.
     */
    KEY_HELP(168, "khlp"),

    /**
     * Mark key.
     */
    KEY_MARK(169, "kmrk"),

    /**
     * Message key.
     */
    KEY_MESSAGE(170, "kmsg"),

    /**
     * Move key.
     */
    KEY_MOVE(171, "kmov"),

    /**
     * Next key.
     */
    KEY_NEXT(172, "knxt"),

    /**
     * Open key.
     */
    KEY_OPEN(173, "kopn"),

    /**
     * Options key.
     */
    KEY_OPTIONS(174, "kopt"),

    /**
     * Previous key.
     */
    KEY_PREVIOUS(175, "kprv"),

    /**
     * Print key.
     */
    KEY_PRINT(176, "kprt"),

    /**
     * Redo key.
     */
    KEY_REDO(177, "krdo"),

    /**
     * Reference key.
     */
    KEY_REFERENCE(178, "kref"),

    /**
     * Refresh key.
     */
    KEY_REFRESH(179, "krfr"),

    /**
     * Replace key
     */
    KEY_REPLACE(180, "krpl"),

    /**
     * Restart key.
     */
    KEY_RESTART(181, "krst"),

    /**
     * Resume key.
     */
    KEY_RESUME(182, "kres"),

    /**
     * Save key.
     */
    KEY_SAVE(183, "ksav"),

    /**
     * Suspend key.
     */
    KEY_SUSPEND(184, "kspd"),

    /**
     * Undo key.
     */
    KEY_UNDO(185, "kund"),

    /**
     * Shift/Begin key combination.
     */
    KEY_SBEG(186, "kBEG"),

    /**
     * Shift/Cancel key combination.
     */
    KEY_SCANCEL(187, "kCAN"),

    /**
     * Shift/Command key combination.
     */
    KEY_SCOMMAND(188, "kCMD"),

    /**
     * Shift/Copy key combination.
     */
    KEY_SCOPY(189, "kCPY"),

    /**
     * Shift/Create key combination.
     */
    KEY_SCREATE(190, "kCRT"),

    /**
     * Shift/Delete-Character key combination.
     */
    KEY_SDC(191, "kDC"),

    /**
     * Shift/Delete-Line key combination.
     */
    KEY_SDL(192, "kDL"),

    /**
     * Select key.
     */
    KEY_SELECT(193, "kslt"),

    /**
     * Shift/End key combiantion.
     */
    KEY_SEND(194, "kEND"),

    /**
     * Shift/Clear-To-Line-End key combination.
     */
    KEY_SEOL(195, "kEOL"),

    /**
     * Shift/Exit key combination.
     */
    KEY_SEXIT(196, "kEXT"),

    /**
     * Shift/Find key combination.
     */
    KEY_SFIND(197, "kFND"),

    /**
     * Shift/Help key combination.
     */
    KEY_SHELP(198, "kHLP"),

    /**
     * Shift/Home key combination.
     */
    KEY_SHOME(199, "kHOM"),

    /**
     * Shift/Insert-Character key combination.
     */
    KEY_SIC(200, "kIC"),

    /**
     * Shift/Left key combination.
     */
    KEY_SLEFT(201, "kLFT"),

    /**
     * Shift/Message key combination.
     */
    KEY_SMESSAGE(202, "kMSG"),

    /**
     * Shift/Move key combination.
     */
    KEY_SMOVE(203, "kMOV"),

    /**
     * Shift/Next key combination.
     */
    KEY_SNEXT(204, "kNXT"),

    /**
     * Shift/Options key combination.
     */
    KEY_SOPTIONS(205, "kOPT"),

    /**
     * Shift/Previous key combination.
     */
    KEY_SPREVIOUS(206, "kPRV"),

    /**
     * Shift/Print key combination.
     */
    KEY_SPRINT(207, "kPRT"),

    /**
     * Shift/Redo key combination.
     */
    KEY_SREDO(208, "kRDO"),

    /**
     * Shift/Replace key comination.
     */
    KEY_SREPLACE(209, "kRPL"),

    /**
     * Shift/Right key combination.
     */
    KEY_SRIGHT(210, "kRIT"),

    /**
     * Shift/Resume key combination.
     */
    KEY_SRSUME(211, "kRES"),

    /**
     * Shift/Save key combination.
     */
    KEY_SSAVE(212, "kSAV"),

    /**
     * Shift/Suspend key combination.
     */
    KEY_SSUSPEND(213, "kSPD"),

    /**
     * Shift/Undo key combination.
     */
    KEY_SUNDO(214, "kUND"),

    /**
     * F11 function key.
     */
    KEY_F11(216, "kf11"),

    /**
     * F12 function key.
     */
    KEY_F12(217, "kf12"),

    /**
     * F13 function key.
     */
    KEY_F13(218, "kf13"),

    /**
     * F14 function key.
     */
    KEY_F14(219, "kf14"),

    /**
     * F15 function key.
     */
    KEY_F15(220, "kf15"),

    /**
     * F16 function key.
     */
    KEY_F16(221, "kf16"),

    /**
     * F17 function key.
     */
    KEY_F17(222, "kf17"),

    /**
     * F18 function key.
     */
    KEY_F18(223, "kf18"),

    /**
     * F19 function key.
     */
    KEY_F19(224, "kf19"),

    /**
     * F20 function key.
     */
    KEY_F20(225, "kf20"),

    /**
     * F21 function key.
     */
    KEY_F21(226, "kf21"),

    /**
     * F22 function key.
     */
    KEY_F22(227, "kf22"),

    /**
     * F23 function key.
     */
    KEY_F23(228, "kf23"),

    /**
     * F24 function key.
     */
    KEY_F24(229, "kf24"),

    /**
     * F25 function key.
     */
    KEY_F25(230, "kf25"),

    /**
     * F26 function key.
     */
    KEY_F26(231, "kf26"),

    /**
     * F27 function key.
     */
    KEY_F27(232, "kf27"),

    /**
     * F28 function key.
     */
    KEY_F28(233, "kf28"),

    /**
     * F29 function key.
     */
    KEY_F29(234, "kf29"),

    /**
     * F30 function key.
     */
    KEY_F30(235, "kf30"),

    /**
     * F31 function key.
     */
    KEY_F31(236, "kf31"),

    /**
     * F32 function key.
     */
    KEY_F32(237, "kf32"),

    /**
     * F33 function key.
     */
    KEY_F33(238, "kf33"),

    /**
     * F34 function key.
     */
    KEY_F34(239, "kf34"),

    /**
     * F35 function key.
     */
    KEY_F35(240, "kf35"),

    /**
     * F36 function key.
     */
    KEY_F36(241, "kf36"),

    /**
     * F37 function key.
     */
    KEY_F37(242, "kf37"),

    /**
     * F38 function key.
     */
    KEY_F38(243, "kf38"),

    /**
     * F39 function key.
     */
    KEY_F39(244, "kf39"),

    /**
     * F40 function key.
     */
    KEY_F40(245, "kf40"),

    /**
     * F41 function key.
     */
    KEY_F41(246, "kf41"),

    /**
     * F42 function key.
     */
    KEY_F42(247, "kf42"),

    /**
     * F43 function key.
     */
    KEY_F43(248, "kf43"),

    /**
     * F44 function key.
     */
    KEY_F44(249, "kf44"),

    /**
     * F45 function key.
     */
    KEY_F45(250, "kf45"),

    /**
     * F46 function key.
     */
    KEY_F46(251, "kf46"),

    /**
     * F47 function key.
     */
    KEY_F47(252, "kf47"),

    /**
     * F48 function key.
     */
    KEY_F48(253, "kf48"),

    /**
     * F49 function key.
     */
    KEY_F49(254, "kf49"),

    /**
     * F50 function key.
     */
    KEY_F50(255, "kf50"),

    /**
     * F51 function key.
     */
    KEY_F51(256, "kf51"),

    /**
     * F52 function key.
     */
    KEY_F52(257, "kf52"),

    /**
     * F53 function key.
     */
    KEY_F53(258, "kf53"),

    /**
     * F54 function key.
     */
    KEY_F54(259, "kf54"),

    /**
     * F55 function key.
     */
    KEY_F55(260, "kf55"),

    /**
     * F56 function key.
     */
    KEY_F56(261, "kf56"),

    /**
     * F57 function key.
     */
    KEY_F57(262, "kf57"),

    /**
     * F58 function key.
     */
    KEY_F58(263, "kf58"),

    /**
     * F59 function key.
     */
    KEY_F59(264, "kf59"),

    /**
     * F60 function key.
     */
    KEY_F60(265, "kf60"),

    /**
     * F61 function key.
     */
    KEY_F61(266, "kf61"),

    /**
     * F62 function key.
     */
    KEY_F62(267, "kf62"),

    /**
     * F63 function key.
     */
    KEY_F63(268, "kf63"),
    ;

    @Override
    public int index() {
        return index;
    }

    @Override
    public String terminfoName() {
        return terminfoName;
    }

    @Override
    public int parameters() {
        return 0;
    }

    private final int index;
    private final String terminfoName;

    private KeyCapability(int index, String terminfoName) {
        this.index = index;
        this.terminfoName = terminfoName;
    }
}
