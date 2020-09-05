package enyo.io.terminfo.tests;

import enyo.io.function.IOConsumer;
import enyo.io.terminfo.ScriptParser;
import enyo.io.terminfo.ScriptVisitor;
import enyo.io.terminfo.StringCapability;
import enyo.io.terminfo.TermFile;
import enyo.io.terminfo.TerminalScriptException;

import enyo.testing.Checks;
import enyo.testing.Test;
import enyo.testing.TestFailure;
import enyo.testing.Testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TestScriptVisitor {
    @Test
    public void testEmpty(Testing t) {
        new ScriptParser()
            .accept(new FailingVisitor(t) { }, new byte[0]);
    }

    @Test
    public static void testLiteral(Testing t) {
        var visitor = new FailingVisitor(t) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();

                @Override
                public void visit(byte b) {
                    out.write(b);
                }
            };
        new ScriptParser().accept(visitor, "Hello, world!".getBytes());
        t.examine(visitor.out.toByteArray())
            .check(Checks.byteArrayEquals("Hello, world!".getBytes()));

        visitor.out.reset();
        new ScriptParser().accept(visitor,
            "Hello %% world!".getBytes());
        t.examine(visitor.out.toByteArray())
            .check(Checks.byteArrayEquals("Hello % world!".getBytes()));
    }

    @Test
    public static void testPrintChar(Testing t) {
        var p = new ScriptParser();

        var visitor = new FailingVisitor(t) {
                byte[] script;
                final String marker = "<marker>";
                ArrayList<Object> callbacks = new ArrayList<>();

                @Override
                public void visit(byte value) {
                    t.examine(script[p.lastOffset()]).is(value);
                    callbacks.add(value);
                }

                @Override
                public void visit(Format op) {
                    t.examine(op.op()).is(FormatOp.CHAR);
                    t.examine(script[p.lastOffset()]).is('%');
                    t.examine(script[p.lastOffset() + 1]).is('c');
                    callbacks.add(marker);
                }
            };

        visitor.script = "%c".getBytes();
        p.accept(visitor, visitor.script);
        t.<List<?>>examine(visitor.callbacks).is(List.of(visitor.marker));

        visitor.callbacks.clear();
        visitor.script = "Hello %c world!".getBytes();
        p.accept(visitor, visitor.script);
        t.<List<?>>examine(visitor.callbacks).is(List.of(
                (byte) 'H', (byte) 'e', (byte) 'l', (byte) 'l', (byte) 'o',
                (byte) ' ', visitor.marker, (byte) ' ', (byte) 'w', (byte) 'o',
                (byte) 'r', (byte) 'l', (byte) 'd', (byte) '!'));
    }

    @Test
    public static void testFormat1(Testing t) {
        var p = new ScriptParser();

        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(p.lastOffset()).is(0);;
                        t.examine(op.flags()).is(Set.of());
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(p.lastOffset()).is(0);;
                        t.examine(op.flags()).is(Set.of());
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%:" + (char) ch).getBytes()));
        "oxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(p.lastOffset()).is(0);;
                        t.examine(op.flags()).is(Set.of(FormatFlag.SHARP));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%#" + (char) ch).getBytes()));
        "oxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(p.lastOffset()).is(0);;
                        t.examine(op.flags()).is(Set.of(FormatFlag.SHARP));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%:#" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(p.lastOffset()).is(0);;
                        t.examine(op.flags()).is(Set.of(FormatFlag.PLUS));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%:+" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(p.lastOffset()).is(0);;
                        t.examine(op.flags()).is(Set.of(FormatFlag.MINUS));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%:-" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of(FormatFlag.SPACE));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("% " + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of(FormatFlag.ZERO));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%0" + (char) ch).getBytes()));
        "oxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of(FormatFlag.SHARP,
                                FormatFlag.PLUS,
                                FormatFlag.MINUS,
                                FormatFlag.ZERO,
                                FormatFlag.SPACE));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%#+-0 " + (char) ch).getBytes()));
        "oxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of(
                                FormatFlag.SHARP,
                                FormatFlag.PLUS,
                                FormatFlag.MINUS,
                                FormatFlag.ZERO,
                                FormatFlag.SPACE));
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%:+-# 0" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of());
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(0);
                    }
                }, ("%." + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of());
                        t.examine(op.width()).is(-1);
                        t.examine(op.precision()).is(123);
                    }
                }, ("%.123" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of());
                        t.examine(op.width()).is(123);
                        t.examine(op.precision()).is(-1);
                    }
                }, ("%123" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of());
                        t.examine(op.width()).is(123);
                        t.examine(op.precision()).is(456);
                    }
                }, ("%123.456" + (char) ch).getBytes()));
        "dosxX".chars().forEach(ch ->
            p.accept(new FailingVisitor(t) {
                    @Override
                    public void visit(Format op) {
                        t.examine(p.lastOffset()).is(0);
                        t.examine(op.op().directive()).is((byte) ch);
                        t.examine(op.flags()).is(Set.of(FormatFlag.ZERO));
                        t.examine(op.width()).is(123);
                        t.examine(op.precision()).is(456);
                    }
                }, ("%0123.456" + (char) ch).getBytes()));
    }

    @Test
    public void testComplexCapabilities(Testing t) throws IOException {
        TestTermFile.terminfoFiles(t).forEach(IOConsumer.of(path -> {
                    byte[] blob = Files.readAllBytes(path);
                    try {
                        TermFile tf = TermFile.of(blob);
                        for (StringCapability cap : INTERESTING_CAPS)
                            try {
                                byte[] cap1 = tf.get(cap);
                                if (cap1 != null) {
                                    new ScriptParser()
                                        .accept(new RenderingVisitor(), cap1);
                                }
                            } catch (Throwable e) {
                                throw new TestFailure(cap.toString(), e);
                            }
                    } catch (Throwable e) {
                        throw new TestFailure(path.toString(), e);
                    }
                }));
    }

    @Test
    public static void testConsume(Testing t) {
        checkScript(t, "%c", new FailingVisitor(t) {
                @Override
                public void visit(Format op) {
                    t.examine(op.op()).is(FormatOp.CHAR);
                }
            });
    }

    @Test
    public static void testFormat2(Testing t) {
        checkScript(t, "%d");
        checkScript(t, "%o");
        checkScript(t, "%x");
        checkScript(t, "%X");
        checkScript(t, "%s");
        checkScript(t, "%:d", "%d");
        checkScript(t, "%:o", "%o");
        checkScript(t, "%:x", "%x");
        checkScript(t, "%:X", "%X");
        checkScript(t, "%:s", "%s");
        checkScript(t, "%:-2d");
    }

    @Test
    public static void testUnary(Testing t) {
        checkScript(t, "%l");
        checkScript(t, "%!");
        checkScript(t, "%~");
    }

    @Test
    public static void testBinary(Testing t) {
        checkScript(t, "%+");
        checkScript(t, "%-");
        checkScript(t, "%*");
        checkScript(t, "%/");
        checkScript(t, "%m");
        checkScript(t, "%&");
        checkScript(t, "%|");
        checkScript(t, "%^");
        checkScript(t, "%=");
        checkScript(t, "%>");
        checkScript(t, "%<");
        checkScript(t, "%A");
        checkScript(t, "%O");
    }

    @Test
    public static void testPushParameter(Testing t) {
        for (int i = 1; i <= 9; ++i)
            checkScript(t, "%p" + i);
    }

    @Test
    public static void testIncrement(Testing t) {
        var v = new FailingVisitor(t) {
                @Override
                public void visitIncrement() {
                }
            };
        checkScript(t, "%i", v);
    }

    @Test
    public static void testPushVariable(Testing t) {
        var v = new FailingVisitor(t) {
                @Override
                public void visitPushVariable(int var) {
                }
            };
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            checkScript(t, "%g" + ch, v);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            checkScript(t, "%g" + ch, v);
    }

    @Test
    public static void testSetVariable(Testing t) {
        var v = new FailingVisitor(t) {
                @Override
                public void visitSetVariable(int var) {
                }
            };
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            checkScript(t, "%P" + ch, v);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            checkScript(t, "%P" + ch, v);
    }

    @Test
    public static void testPushConstant(Testing t) {
        var v = new FailingVisitor(t) {
                @Override
                public void visitPushConstant(int var) {
                }
            };
        checkScript(t, "%'\000'", "%{0}", v);
        checkScript(t, "%'''", "%{39}", v);
        checkScript(t, "%'\377'", "%{255}", v);
        checkScript(t, "%'@'", "%{64}", v);
        checkScript(t, "%'A'", "%{65}", v);
        for (int i = 0; i <= 1001; ++i) {
            checkScript(t, "%{" + i + '}', v);
            checkScript(t, "%{" + (Integer.MAX_VALUE - i) + '}', v);
        }
    }

    @Test
    public static void testNestedIf(Testing t) {
        // Example in terminfo(5).
        checkScript(
            t, "%? c1 %t b1 %e c2 %t b2 %e c3 %t b3 %e c4 %t b4 %e %;");
    }

    @Test
    public static void testMalformed(Testing t) {
        checkMalformed(t, "\033[32%{");
    }

    // Checks that the script renders back to itself.
    private static void checkScript(Testing t, String script) {
        checkScript(t, script, script);
    }

    // Checks that the script renders back to the expected string.
    private static void checkScript(Testing t,
        String script, String expected) {
        RenderingVisitor visitor = new RenderingVisitor();
        new ScriptParser().accept(
            visitor, script.getBytes(StandardCharsets.ISO_8859_1));
        t.assertByteArrayEquals(
            expected.getBytes(StandardCharsets.ISO_8859_1),
            visitor.out.toByteArray());
    }

    // Test like checkScript(t, script) and also with the specified
    // visitor.
    private static void checkScript(Testing t, String script,
        ScriptVisitor visitor) {
        checkScript(t, script, script);
        new ScriptParser().accept(
            visitor, script.getBytes(StandardCharsets.ISO_8859_1));
    }

    // Test like checkScript(t, script, expected) and also with the
    // specified visitor.
    private static void checkScript(Testing t, String script,
        String expected, ScriptVisitor visitor) {
        checkScript(t, script, expected);
        new ScriptParser().accept(
            visitor, script.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static void checkMalformed(Testing t, String script) {
        ScriptVisitor visitor = new RenderingVisitor();
        byte[] bytes = script.getBytes(StandardCharsets.ISO_8859_1);
        t.expect(() -> new ScriptParser().accept(visitor, bytes),
            TerminalScriptException.class);
    }

    static final class RenderingVisitor implements ScriptVisitor {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void visit(UnaryOp op) {
            append(op);
        }

        @Override
        public void visit(BinaryOp op) {
            append(op);
        }

        @Override
        public void visit(Format op) {
            write(op.toString());
        }

        @Override
        public void visitIncrement() {
            out.write('%');
            out.write('i');
        }

        @Override
        public void visitPushParameter(int var) {
            out.write('%');
            out.write('p');
            out.write((char) ('0' + var));
        }

        @Override
        public void visitPushVariable(int var) {
            out.write('%');
            out.write('g');
            out.write((char) var);
        }

        @Override
        public void visitSetVariable(int var) {
            out.write('%');
            out.write('P');
            out.write((char) var);
        }

        @Override
        public void visitPushConstant(int var) {
            write("%{" + var + '}');
        }

        @Override
        public void visit(byte value) {
            out.write(value);
        }

        @Override
        public IfVisitor visitIf() {
            return new IfVisitor() {
                @Override
                public ScriptVisitor visitIf() {
                    out.write('%');
                    out.write('?');
                    return RenderingVisitor.this;
                }

                @Override
                public ScriptVisitor visitThen() {
                    out.write('%');
                    out.write('t');
                    return RenderingVisitor.this;
                }

                @Override
                public ScriptVisitor visitElse() {
                    out.write('%');
                    out.write('e');
                    return RenderingVisitor.this;
                }

                @Override
                public void visitEnd() {
                    out.write('%');
                    out.write(';');
                }
            };
        }

        private void append(Directive op) {
            out.write('%');
            out.write(op.directive());
        }

        private void write(String str) {
            byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
            out.write(bytes, 0, bytes.length); // Avoid IOException.
        }
    }

    // These string capabilities are supposed to run through the
    // script parser, based on analysis of several terminal
    // definitions.
    static final Set<StringCapability> INTERESTING_CAPS = List.of(
        "BIT_IMAGE_REPEAT",
        "CHANGE_CHAR_PITCH",
        "CHANGE_LINE_PITCH",
        "CHANGE_RES_HORZ",
        "CHANGE_RES_VERT",
        "CHANGE_SCROLL_REGION",
        "CHAR_SET_NAMES",
        "COLOR_NAMES",
        "COLUMN_ADDRESS",
        "CREATE_WINDOW",
        "CURSOR_ADDRESS",
        "CURSOR_MEM_ADDRESS",
        "DEFINE_CHAR",
        "DIAL_PHONE",
        "DISPLAY_PC_CHAR",
        "ENTER_BLINK_MODE",
        "ENTER_BOLD_MODE",
        "ENTER_REVERSE_MODE",
        "ERASE_CHARS",
        "EXIT_ALT_CHARSET_MODE",
        "EXIT_ATTRIBUTE_MODE",
        "EXIT_STANDOUT_MODE",
        "EXIT_UNDERLINE_MODE",
        "GET_MOUSE",
        "GOTO_WINDOW",
        "INITIALIZE_COLOR",
        "INITIALIZE_PAIR",
        "MICRO_ROW_ADDRESS",
        "ORIG_PAIR",
        "PARM_DCH",
        "PARM_DELETE_LINE",
        "PARM_DOWN_CURSOR",
        "PARM_ICH",
        "PARM_INDEX",
        "PARM_INSERT_LINE",
        "PARM_LEFT_CURSOR",
        "PARM_RIGHT_CURSOR",
        "PARM_RINDEX",
        "PARM_UP_CURSOR",
        "PKEY_KEY",
        "PKEY_LOCAL",
        "PKEY_PLAB",
        "PKEY_XMIT",
        "PLAB_NORM",
        "PRTR_NON",
        "QUICK_DIAL",
        "REPEAT_CHAR",
        "ROW_ADDRESS",
        "SELECT_CHAR_SET",
        "SET_ATTRIBUTES",
        "SET_A_ATTRIBUTES",
        "SET_A_BACKGROUND",
        "SET_A_FOREGROUND",
        "SET_BACKGROUND",
        "SET_BOTTOM_MARGIN_PARM",
        "SET_CLOCK",
        "SET_COLOR_BAND",
        "SET_COLOR_PAIR",
        "SET_FOREGROUND",
        "SET_LEFT_MARGIN_PARM",
        "SET_LR_MARGIN",
        "SET_PAGE_LENGTH",
        "SET_PGLEN_INCH",
        "SET_RIGHT_MARGIN_PARM",
        "SET_TB_MARGIN",
        "SET_TOP_MARGIN_PARM",
        "SET_WINDOW",
        "START_CHAR_SET_DEF",
        "STOP_CHAR_SET_DEF",
        "TO_STATUS_LINE")
        .stream()
        .map(StringCapability::of)
        .collect(Collectors.toUnmodifiableSet());

    @Test
    public void testInterestingCaps(Testing t) {
        // Check that all string capabilities with parameters are also
        // listed in INTERESTING_CAPS.
        for (StringCapability cap : StringCapability.values())
            if (cap.parameters() > 0 && !INTERESTING_CAPS.contains(cap))
                t.failedCheck("INTERESTING_CAPS coverage",
                    cap.toString(), cap.terminfoName(), cap.parameters());
    }
}
