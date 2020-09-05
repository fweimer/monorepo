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
    KEY_BTAB(148, "kcbt"),

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
