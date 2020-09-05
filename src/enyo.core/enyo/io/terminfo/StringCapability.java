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
    BACK_TAB("cbt"),

    /**
     */
    BELL("bel"),


    /**
     */
    CARRIAGE_RETURN("cr"),

    /**
     */
    CHANGE_SCROLL_REGION("csr", 2),

    /**
     */
    CLEAR_ALL_TABS("tbc"),

    /**
     */
    CLEAR_SCREEN("clear"),

    /**
     */
    CLR_EOL("el"),

    /**
     */
    CLR_EOS("ed"),

    /**
     */
    COLUMN_ADDRESS("hpa", 1),

    /**
     */
    COMMAND_CHARACTER("cmdch"),

    /**
     */
    CURSOR_ADDRESS("cup", 2),

    /**
     */
    CURSOR_DOWN("cud1"),

    /**
     */
    CURSOR_HOME("home"),

    /**
     */
    CURSOR_INVISIBLE("civis"),

    /**
     */
    CURSOR_LEFT("cub1"),

    /**
     */
    CURSOR_MEM_ADDRESS("mrcup", 2),

    /**
     */
    CURSOR_NORMAL("cnorm"),

    /**
     */
    CURSOR_RIGHT("cuf1"),

    /**
     */
    CURSOR_TO_LL("ll"),

    /**
     */
    CURSOR_UP("cuu1"),

    /**
     */
    CURSOR_VISIBLE("cvvis"),

    /**
     */
    DELETE_CHARACTER("dch1"),

    /**
     */
    DELETE_LINE("dl1", 1),

    /**
     */
    DIS_STATUS_LINE("dsl"),

    /**
     */
    DOWN_HALF_LINE("hd"),

    /**
     */
    ENTER_ALT_CHARSET_MODE("smacs"),

    /**
     */
    ENTER_BLINK_MODE("blink"),

    /**
     */
    ENTER_BOLD_MODE("bold"),

    /**
     */
    ENTER_CA_MODE("smcup", 2),

    /**
     */
    ENTER_DELETE_MODE("smdc"),

    /**
     */
    ENTER_DIM_MODE("dim"),

    /**
     */
    ENTER_INSERT_MODE("smir"),

    /**
     */
    ENTER_SECURE_MODE("invis"),

    /**
     */
    ENTER_PROTECTED_MODE("prot"),

    /**
     */
    ENTER_REVERSE_MODE("rev"),

    /**
     */
    ENTER_STANDOUT_MODE("smso"),

    /**
     */
    ENTER_UNDERLINE_MODE("smul"),

    /**
     */
    ERASE_CHARS("ech", 1),

    /**
     */
    EXIT_ALT_CHARSET_MODE("rmacs"),

    /**
     */
    EXIT_ATTRIBUTE_MODE("sgr0"),

    /**
     */
    EXIT_CA_MODE("rmcup", 2),

    /**
     */
    EXIT_DELETE_MODE("rmdc"),

    /**
     */
    EXIT_INSERT_MODE("rmir"),

    /**
     */
    EXIT_STANDOUT_MODE("rmso"),

    /**
     */
    EXIT_UNDERLINE_MODE("rmul"),

    /**
     */
    FLASH_SCREEN("flash"),

    /**
     */
    FORM_FEED("ff"),

    /**
     */
    FROM_STATUS_LINE("fsl"),

    /**
     */
    INIT_1STRING("is1"),

    /**
     */
    INIT_2STRING("is2"),

    /**
     */
    INIT_3STRING("is3"),

    /**
     */
    INIT_FILE("if"),

    /**
     */
    INSERT_CHARACTER("ich1"),

    /**
     */
    INSERT_LINE("il1"),

    /**
     */
    INSERT_PADDING("ip"),

    /**
     */
    KEY_BACKSPACE("kbs"),

    /**
     */
    KEY_CATAB("ktbc"),

    /**
     */
    KEY_CLEAR("kclr"),

    /**
     */
    KEY_CTAB("kctab"),

    /**
     */
    KEY_DC("kdch1"),

    /**
     */
    KEY_DL("kdl1"),

    /**
     */
    KEY_DOWN("kcud1"),

    /**
     */
    KEY_EIC("krmir"),

    /**
     */
    KEY_EOL("kel"),

    /**
     */
    KEY_EOS("ked"),

    /**
     */
    KEY_F0("kf0"),

    /**
     */
    KEY_F1("kf1"),

    /**
     */
    KEY_F10("kf10"),

    /**
     */
    KEY_F2("kf2"),

    /**
     */
    KEY_F3("kf3"),

    /**
     */
    KEY_F4("kf4"),

    /**
     */
    KEY_F5("kf5"),

    /**
     */
    KEY_F6("kf6"),

    /**
     */
    KEY_F7("kf7"),

    /**
     */
    KEY_F8("kf8"),

    /**
     */
    KEY_F9("kf9"),

    /**
     */
    KEY_HOME("khome"),

    /**
     */
    KEY_IC("kich1"),

    /**
     */
    KEY_IL("kil1"),

    /**
     */
    KEY_LEFT("kcub1"),

    /**
     */
    KEY_LL("kll"),

    /**
     */
    KEY_NPAGE("knp"),

    /**
     */
    KEY_PPAGE("kpp"),

    /**
     */
    KEY_RIGHT("kcuf1"),

    /**
     */
    KEY_SF("kind"),

    /**
     */
    KEY_SR("kri"),

    /**
     */
    KEY_STAB("khts"),

    /**
     */
    KEY_UP("kcuu1"),

    /**
     */
    KEYPAD_LOCAL("rmkx"),

    /**
     */
    KEYPAD_XMIT("smkx"),

    /**
     */
    LAB_F0("lf0"),

    /**
     */
    LAB_F1("lf1"),

    /**
     */
    LAB_F10("lf10"),

    /**
     */
    LAB_F2("lf2"),

    /**
     */
    LAB_F3("lf3"),

    /**
     */
    LAB_F4("lf4"),

    /**
     */
    LAB_F5("lf5"),

    /**
     */
    LAB_F6("lf6"),

    /**
     */
    LAB_F7("lf7"),

    /**
     */
    LAB_F8("lf8"),

    /**
     */
    LAB_F9("lf9"),

    /**
     */
    META_OFF("rmm"),

    /**
     */
    META_ON("smm"),

    /**
     */
    NEWLINE("nel"),

    /**
     */
    PAD_CHAR("pad"),

    /**
     */
    PARM_DCH("dch", 1),

    /**
     */
    PARM_DELETE_LINE("dl", 1),

    /**
     */
    PARM_DOWN_CURSOR("cud", 1),

    /**
     */
    PARM_ICH("ich", 1),

    /**
     */
    PARM_INDEX("indn", 1),

    /**
     */
    PARM_INSERT_LINE("il", 1),

    /**
     */
    PARM_LEFT_CURSOR("cub", 1),

    /**
     */
    PARM_RIGHT_CURSOR("cuf", 1),

    /**
     */
    PARM_RINDEX("rin", 1),

    /**
     */
    PARM_UP_CURSOR("cuu", 1),

    /**
     */
    PKEY_KEY("pfkey", 2),

    /**
     */
    PKEY_LOCAL("pfloc", 2),

    /**
     */
    PKEY_XMIT("pfx", 2),

    /**
     */
    PRINT_SCREEN("mc0"),

    /**
     */
    PRTR_OFF("mc4"),

    /**
     */
    PRTR_ON("mc5"),

    /**
     */
    REPEAT_CHAR("rep", 2),

    /**
     */
    RESET_1STRING("rs1"),

    /**
     */
    RESET_2STRING("rs2"),

    /**
     */
    RESET_3STRING("rs3"),

    /**
     */
    RESET_FILE("rf"),

    /**
     */
    RESTORE_CURSOR("rc"),

    /**
     */
    ROW_ADDRESS("vpa", 1),

    /**
     */
    SAVE_CURSOR("sc"),

    /**
     */
    SCROLL_FORWARD("ind"),

    /**
     */
    SCROLL_REVERSE("ri"),

    /**
     */
    SET_ATTRIBUTES("sgr", 9),

    /**
     */
    SET_TAB("hts"),

    /**
     */
    SET_WINDOW("wind", 4),

    /**
     */
    TAB("ht"),

    /**
     */
    TO_STATUS_LINE("tsl", 1),

    /**
     */
    UNDERLINE_CHAR("uc"),

    /**
     */
    UP_HALF_LINE("hu"),

    /**
     */
    INIT_PROG("iprog"),

    /**
     */
    KEY_A1("ka1"),

    /**
     */
    KEY_A3("ka3"),

    /**
     */
    KEY_B2("kb2"),

    /**
     */
    KEY_C1("kc1"),

    /**
     */
    KEY_C3("kc3"),

    /**
     */
    PRTR_NON("mc5p", 1),

    /**
     */
    CHAR_PADDING("rmp"),

    /**
     */
    ACS_CHARS("acsc"),

    /**
     */
    PLAB_NORM("pln", 2),

    /**
     */
    KEY_BTAB("kcbt"),

    /**
     */
    ENTER_XON_MODE("smxon"),

    /**
     */
    EXIT_XON_MODE("rmxon"),

    /**
     */
    ENTER_AM_MODE("smam"),

    /**
     */
    EXIT_AM_MODE("rmam"),

    /**
     */
    XON_CHARACTER("xonc"),

    /**
     */
    XOFF_CHARACTER("xoffc"),

    /**
     */
    ENA_ACS("enacs"),

    /**
     */
    LABEL_ON("smln"),

    /**
     */
    LABEL_OFF("rmln"),

    /**
     */
    KEY_BEG("kbeg"),

    /**
     */
    KEY_CANCEL("kcan"),

    /**
     */
    KEY_CLOSE("kclo"),

    /**
     */
    KEY_COMMAND("kcmd"),

    /**
     */
    KEY_COPY("kcpy"),

    /**
     */
    KEY_CREATE("kcrt"),

    /**
     */
    KEY_END("kend"),

    /**
     */
    KEY_ENTER("kent"),

    /**
     */
    KEY_EXIT("kext"),

    /**
     */
    KEY_FIND("kfnd"),

    /**
     */
    KEY_HELP("khlp"),

    /**
     */
    KEY_MARK("kmrk"),

    /**
     */
    KEY_MESSAGE("kmsg"),

    /**
     */
    KEY_MOVE("kmov"),

    /**
     */
    KEY_NEXT("knxt"),

    /**
     */
    KEY_OPEN("kopn"),

    /**
     */
    KEY_OPTIONS("kopt"),

    /**
     */
    KEY_PREVIOUS("kprv"),

    /**
     */
    KEY_PRINT("kprt"),

    /**
     */
    KEY_REDO("krdo"),

    /**
     */
    KEY_REFERENCE("kref"),

    /**
     */
    KEY_REFRESH("krfr"),

    /**
     */
    KEY_REPLACE("krpl"),

    /**
     */
    KEY_RESTART("krst"),

    /**
     */
    KEY_RESUME("kres"),

    /**
     */
    KEY_SAVE("ksav"),

    /**
     */
    KEY_SUSPEND("kspd"),

    /**
     */
    KEY_UNDO("kund"),

    /**
     */
    KEY_SBEG("kBEG"),

    /**
     */
    KEY_SCANCEL("kCAN"),

    /**
     */
    KEY_SCOMMAND("kCMD"),

    /**
     */
    KEY_SCOPY("kCPY"),

    /**
     */
    KEY_SCREATE("kCRT"),

    /**
     */
    KEY_SDC("kDC"),

    /**
     */
    KEY_SDL("kDL"),

    /**
     */
    KEY_SELECT("kslt"),

    /**
     */
    KEY_SEND("kEND"),

    /**
     */
    KEY_SEOL("kEOL"),

    /**
     */
    KEY_SEXIT("kEXT"),

    /**
     */
    KEY_SFIND("kFND"),

    /**
     */
    KEY_SHELP("kHLP"),

    /**
     */
    KEY_SHOME("kHOM"),

    /**
     */
    KEY_SIC("kIC"),

    /**
     */
    KEY_SLEFT("kLFT"),

    /**
     */
    KEY_SMESSAGE("kMSG"),

    /**
     */
    KEY_SMOVE("kMOV"),

    /**
     */
    KEY_SNEXT("kNXT"),

    /**
     */
    KEY_SOPTIONS("kOPT"),

    /**
     */
    KEY_SPREVIOUS("kPRV"),

    /**
     */
    KEY_SPRINT("kPRT"),

    /**
     */
    KEY_SREDO("kRDO"),

    /**
     */
    KEY_SREPLACE("kRPL"),

    /**
     */
    KEY_SRIGHT("kRIT"),

    /**
     */
    KEY_SRSUME("kRES"),

    /**
     */
    KEY_SSAVE("kSAV"),

    /**
     */
    KEY_SSUSPEND("kSPD"),

    /**
     */
    KEY_SUNDO("kUND"),

    /**
     */
    REQ_FOR_INPUT("rfi"),

    /**
     */
    KEY_F11("kf11"),

    /**
     */
    KEY_F12("kf12"),

    /**
     */
    KEY_F13("kf13"),

    /**
     */
    KEY_F14("kf14"),

    /**
     */
    KEY_F15("kf15"),

    /**
     */
    KEY_F16("kf16"),

    /**
     */
    KEY_F17("kf17"),

    /**
     */
    KEY_F18("kf18"),

    /**
     */
    KEY_F19("kf19"),

    /**
     */
    KEY_F20("kf20"),

    /**
     */
    KEY_F21("kf21"),

    /**
     */
    KEY_F22("kf22"),

    /**
     */
    KEY_F23("kf23"),

    /**
     */
    KEY_F24("kf24"),

    /**
     */
    KEY_F25("kf25"),

    /**
     */
    KEY_F26("kf26"),

    /**
     */
    KEY_F27("kf27"),

    /**
     */
    KEY_F28("kf28"),

    /**
     */
    KEY_F29("kf29"),

    /**
     */
    KEY_F30("kf30"),

    /**
     */
    KEY_F31("kf31"),

    /**
     */
    KEY_F32("kf32"),

    /**
     */
    KEY_F33("kf33"),

    /**
     */
    KEY_F34("kf34"),

    /**
     */
    KEY_F35("kf35"),

    /**
     */
    KEY_F36("kf36"),

    /**
     */
    KEY_F37("kf37"),

    /**
     */
    KEY_F38("kf38"),

    /**
     */
    KEY_F39("kf39"),

    /**
     */
    KEY_F40("kf40"),

    /**
     */
    KEY_F41("kf41"),

    /**
     */
    KEY_F42("kf42"),

    /**
     */
    KEY_F43("kf43"),

    /**
     */
    KEY_F44("kf44"),

    /**
     */
    KEY_F45("kf45"),

    /**
     */
    KEY_F46("kf46"),

    /**
     */
    KEY_F47("kf47"),

    /**
     */
    KEY_F48("kf48"),

    /**
     */
    KEY_F49("kf49"),

    /**
     */
    KEY_F50("kf50"),

    /**
     */
    KEY_F51("kf51"),

    /**
     */
    KEY_F52("kf52"),

    /**
     */
    KEY_F53("kf53"),

    /**
     */
    KEY_F54("kf54"),

    /**
     */
    KEY_F55("kf55"),

    /**
     */
    KEY_F56("kf56"),

    /**
     */
    KEY_F57("kf57"),

    /**
     */
    KEY_F58("kf58"),

    /**
     */
    KEY_F59("kf59"),

    /**
     */
    KEY_F60("kf60"),

    /**
     */
    KEY_F61("kf61"),

    /**
     */
    KEY_F62("kf62"),

    /**
     */
    KEY_F63("kf63"),

    /**
     */
    CLR_BOL("el1"),

    /**
     */
    CLEAR_MARGINS("mgc"),

    /**
     */
    SET_LEFT_MARGIN("smgl"),

    /**
     */
    SET_RIGHT_MARGIN("smgr"),

    /**
     */
    LABEL_FORMAT("fln"),

    /**
     */
    SET_CLOCK("sclk", 3),

    /**
     */
    DISPLAY_CLOCK("dclk"),

    /**
     */
    REMOVE_CLOCK("rmclk"),

    /**
     */
    CREATE_WINDOW("cwin", 5),

    /**
     */
    GOTO_WINDOW("wingo", 1),

    /**
     */
    HANGUP("hup"),

    /**
     */
    DIAL_PHONE("dial", 1),

    /**
     */
    QUICK_DIAL("qdial", 1),

    /**
     */
    TONE("tone"),

    /**
     */
    PULSE("pulse"),

    /**
     */
    FLASH_HOOK("hook"),

    /**
     */
    FIXED_PAUSE("pause"),

    /**
     */
    WAIT_TONE("wait"),

    /**
     */
    USER0("u0"),

    /**
     */
    USER1("u1"),

    /**
     */
    USER2("u2"),

    /**
     */
    USER3("u3"),

    /**
     */
    USER4("u4"),

    /**
     */
    USER5("u5"),

    /**
     */
    USER6("u6"),

    /**
     */
    USER7("u7"),

    /**
     */
    USER8("u8"),

    /**
     */
    USER9("u9"),

    /**
     */
    ORIG_PAIR("op"),

    /**
     */
    ORIG_COLORS("oc"),

    /**
     */
    INITIALIZE_COLOR("initc", 4),

    /**
     */
    INITIALIZE_PAIR("initp", 7),

    /**
     */
    SET_COLOR_PAIR("scp", 1),

    /**
     */
    SET_FOREGROUND("setf", 1),

    /**
     */
    SET_BACKGROUND("setb", 1),

    /**
     */
    CHANGE_CHAR_PITCH("cpi", 1),

    /**
     */
    CHANGE_LINE_PITCH("lpi", 1),

    /**
     */
    CHANGE_RES_HORZ("chr", 1),

    /**
     */
    CHANGE_RES_VERT("cvr", 1),

    /**
     */
    DEFINE_CHAR("defc", 3),

    /**
     */
    ENTER_DOUBLEWIDE_MODE("swidm"),

    /**
     */
    ENTER_DRAFT_QUALITY("sdrfq"),

    /**
     */
    ENTER_ITALICS_MODE("sitm"),

    /**
     */
    ENTER_LEFTWARD_MODE("slm"),

    /**
     */
    ENTER_MICRO_MODE("smicm"),

    /**
     */
    ENTER_NEAR_LETTER_QUALITY("snlq"),

    /**
     */
    ENTER_NORMAL_QUALITY("snrmq"),

    /**
     */
    ENTER_SHADOW_MODE("sshm"),

    /**
     */
    ENTER_SUBSCRIPT_MODE("ssubm"),

    /**
     */
    ENTER_SUPERSCRIPT_MODE("ssupm"),

    /**
     */
    ENTER_UPWARD_MODE("sum"),

    /**
     */
    EXIT_DOUBLEWIDE_MODE("rwidm"),

    /**
     */
    EXIT_ITALICS_MODE("ritm"),

    /**
     */
    EXIT_LEFTWARD_MODE("rlm"),

    /**
     */
    EXIT_MICRO_MODE("rmicm"),

    /**
     */
    EXIT_SHADOW_MODE("rshm"),

    /**
     */
    EXIT_SUBSCRIPT_MODE("rsubm"),

    /**
     */
    EXIT_SUPERSCRIPT_MODE("rsupm"),

    /**
     */
    EXIT_UPWARD_MODE("rum"),

    /**
     */
    MICRO_COLUMN_ADDRESS("mhpa"),

    /**
     */
    MICRO_DOWN("mcud1"),

    /**
     */
    MICRO_LEFT("mcub1"),

    /**
     */
    MICRO_RIGHT("mcuf1"),

    /**
     */
    MICRO_ROW_ADDRESS("mvpa", 1),

    /**
     */
    MICRO_UP("mcuu1"),

    /**
     */
    ORDER_OF_PINS("porder"),

    /**
     */
    PARM_DOWN_MICRO("mcud"),

    /**
     */
    PARM_LEFT_MICRO("mcub"),

    /**
     */
    PARM_RIGHT_MICRO("mcuf"),

    /**
     */
    PARM_UP_MICRO("mcuu"),

    /**
     */
    SELECT_CHAR_SET("scs", 1),

    /**
     */
    SET_BOTTOM_MARGIN("smgb"),

    /**
     */
    SET_BOTTOM_MARGIN_PARM("smgbp", 1),

    /**
     */
    SET_LEFT_MARGIN_PARM("smglp", 1),

    /**
     */
    SET_RIGHT_MARGIN_PARM("smgrp", 1),

    /**
     */
    SET_TOP_MARGIN("smgt"),

    /**
     */
    SET_TOP_MARGIN_PARM("smgtp", 1),

    /**
     */
    START_BIT_IMAGE("sbim"),

    /**
     */
    START_CHAR_SET_DEF("scsd", 2),

    /**
     */
    STOP_BIT_IMAGE("rbim"),

    /**
     */
    STOP_CHAR_SET_DEF("rcsd", 1),

    /**
     */
    SUBSCRIPT_CHARACTERS("subcs"),

    /**
     */
    SUPERSCRIPT_CHARACTERS("supcs"),

    /**
     */
    THESE_CAUSE_CR("docr"),

    /**
     */
    ZERO_MOTION("zerom"),

    /**
     */
    CHAR_SET_NAMES("csnm", 1),

    /**
     */
    KEY_MOUSE("kmous"),

    /**
     */
    MOUSE_INFO("minfo"),

    /**
     */
    REQ_MOUSE_POS("reqmp"),

    /**
     */
    GET_MOUSE("getm", 1),

    /**
     */
    SET_A_FOREGROUND("setaf", 1),

    /**
     */
    SET_A_BACKGROUND("setab", 1),

    /**
     */
    PKEY_PLAB("pfxl", 3),

    /**
     */
    DEVICE_TYPE("devt"),

    /**
     */
    CODE_SET_INIT("csin"),

    /**
     */
    SET0_DES_SEQ("s0ds"),

    /**
     */
    SET1_DES_SEQ("s1ds"),

    /**
     */
    SET2_DES_SEQ("s2ds"),

    /**
     */
    SET3_DES_SEQ("s3ds"),

    /**
     */
    SET_LR_MARGIN("smglr", 2),

    /**
     */
    SET_TB_MARGIN("smgtb", 2),

    /**
     */
    BIT_IMAGE_REPEAT("birep", 2),

    /**
     */
    BIT_IMAGE_NEWLINE("binel"),

    /**
     */
    BIT_IMAGE_CARRIAGE_RETURN("bicr"),

    /**
     */
    COLOR_NAMES("colornm", 1),

    /**
     */
    DEFINE_BIT_IMAGE_REGION("defbi"),

    /**
     */
    END_BIT_IMAGE_REGION("endbi"),

    /**
     */
    SET_COLOR_BAND("setcolor", 1),

    /**
     */
    SET_PAGE_LENGTH("slines", 1),

    /**
     */
    DISPLAY_PC_CHAR("dispc", 1),

    /**
     */
    ENTER_PC_CHARSET_MODE("smpch"),

    /**
     */
    EXIT_PC_CHARSET_MODE("rmpch"),

    /**
     */
    ENTER_SCANCODE_MODE("smsc"),

    /**
     */
    EXIT_SCANCODE_MODE("rmsc"),

    /**
     */
    PC_TERM_OPTIONS("pctrm"),

    /**
     */
    SCANCODE_ESCAPE("scesc"),

    /**
     */
    ALT_SCANCODE_ESC("scesa"),

    /**
     */
    ENTER_HORIZONTAL_HL_MODE("ehhlm"),

    /**
     */
    ENTER_LEFT_HL_MODE("elhlm"),

    /**
     */
    ENTER_LOW_HL_MODE("elohlm"),

    /**
     */
    ENTER_RIGHT_HL_MODE("erhlm"),

    /**
     */
    ENTER_TOP_HL_MODE("ethlm"),

    /**
     */
    ENTER_VERTICAL_HL_MODE("evhlm"),

    /**
     */
    SET_A_ATTRIBUTES("sgr1", 6),

    /**
     */
    SET_PGLEN_INCH("slength", 1),
    ;

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

    private final String terminfoName;
    private final int parameters;

    static final int COUNT = values().length;

    private StringCapability(String terminfoName, int parameters) {
        this.terminfoName = terminfoName;
        this.parameters = parameters;
    }

    private StringCapability(String terminfoName) {
        this(terminfoName, 0);
    }
}
