package enyo.io.terminfo.tests;

import enyo.io.terminfo.TermFile;
import enyo.io.terminfo.TerminalScriptException;
import enyo.io.terminfo.TerminalState;
import enyo.io.terminfo.TerminalTarget;
import enyo.io.terminfo.TerminalWriter;
import enyo.testing.Test;
import enyo.testing.Testing;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TestTerminalState {
    @Test
    public static void testStringFormat(Testing t) {
        runScript(t, "%p1%s", "", "");
        runScript(t, "%p1%s", "1", "1");
        runScript(t, "%p1%s", "123", "123");
        runScript(t, "%p1%:-3s", "1", "1  ");
        runScript(t, "%p1%3s", "1", "  1");
    }

    @Test
    public static void testInvalidStringFormat(Testing t) {
        runScript(t, "%#s", TerminalScriptException.class,
            e -> t.examine("%s directive at offset 0 does not support # flag")
            .is(e.getMessage()));
    }

    @Test
    public static void testDecimalFormat(Testing t) {
        runScript(t, "%p1%d", 1, "1");
        runScript(t, "%p1%d", 123, "123");
        runScript(t, "%p1%:-3d", 1, "1  ");
        runScript(t, "%p1%3d", 1, "  1");
        runScript(t, "%p1%3d", -1, " -1");
        runScript(t, "%p1%:-3d", -1, "-1 ");
        runScript(t, "%p1%:+-3d", 1, "+1 ");
        runScript(t, "%p1%:+3d", 1, " +1");
    }

    @Test
    public static void testInvalidDecimalFormat(Testing t) {
        runScript(t, "%#d", TerminalScriptException.class,
            e -> t.examine("%d directive at offset 0 does not support # flag")
            .is(e.getMessage()));
    }

    @Test
    public static void testOctalFormat(Testing t) {
        runScript(t, "%p1%o", 1, "1");
        runScript(t, "%p1%#o", 1, "01");
        runScript(t, "%p1%o", 123, "173");
        runScript(t, "%p1%#o", 123, "0173");
        runScript(t, "%p1%o", -1, "37777777777");
        runScript(t, "%p1%:-3o", 1, "1  ");
        runScript(t, "%p1%3o", 1, "  1");
    }

    @Test
    public static void testHexFormat(Testing t) {
        runScript(t, "%p1%x", 1, "1");
        runScript(t, "%p1%#x", 1, "0x1");
        runScript(t, "%p1%x", 123, "7b");
        runScript(t, "%p1%#x", 123, "0x7b");
        runScript(t, "%p1%x", -2, "fffffffe");
        runScript(t, "%p1%:-3x", 1, "1  ");
        runScript(t, "%p1%3x", 1, "  1");

        runScript(t, "%p1%X", 1, "1");
        runScript(t, "%p1%#X", 1, "0X1");
        runScript(t, "%p1%X", 123, "7B");
        runScript(t, "%p1%#X", 123, "0X7B");
        runScript(t, "%p1%X", -2, "FFFFFFFE");
        runScript(t, "%p1%:-3X", 1, "1  ");
        runScript(t, "%p1%3X", 1, "  1");
    }

    @Test
    public static void testArithmetic(Testing t) {
        runScript(t, "%{1}%{2}%+%d", "3");
        runScript(t, "%{1}%{2}%-%d", "-1");
        runScript(t, "%{2}%{3}%*%d", "6");
        runScript(t, "%{15}%{3}%/%d", "5");
        runScript(t, "%{7}%{5}%m%d", "2");
        runScript(t, "%{7}%{5}%<%d", "0");
        runScript(t, "%{7}%{5}%>%d", "1");
        runScript(t, "%{7}%{5}%=%d", "0");
        runScript(t, "%{7}%{5}%&%d", "5");
        runScript(t, "%{7}%{8}%|%d", "15");
        runScript(t, "%{7}%{5}%^%d", "2");

        runScript(t, "%{0}%!%d", "1");
        runScript(t, "%{1}%!%d", "0");
        runScript(t, "%{0}%{1}%-%!%d", "0");
        runScript(t, "%{1}%~%d", "-2");
    }

    @Test
    public static void testStringLength(Testing t) {
        runScript(t, "%p1%l%d", "", "0");
        runScript(t, "%p1%l%d", "X", "1");
        runScript(t, "%p1%l%d", "XY", "2");
        runScript(t, "%p1%l%d", "XYZ", "3");
    }

    @Test
    public static void testPrintByte(Testing t) {
        runScript(t, "%{64}%{1}%+%c", "A");
    }

    @Test
    public static void testIfThen(Testing t) {
        runScript(t, "%?[%p1%tTHEN%;]", 1, "[THEN]");
        runScript(t, "[%?%p1%tTHEN%;]", 0, "[]");
    }

    @Test
    public static void testIfThenElse(Testing t) {
        runScript(t, "%?[%p1%tTHEN%eELSE%;]", 0, "[ELSE]");
        runScript(t, "[%?%p1%tTHEN%eELSE%;]", 1, "[THEN]");
    }

    @Test
    public static void testIfThenElseChained(Testing t) {
        String script = "[%?%p1%{0}%=%tZERO%e%p1%{1}%=%tONE"
            + "%e%p1%{2}%=%tTWO%eUNKNOWN(%p1%d)%;]";
        runScript(t, script, 0, "[ZERO]");
        runScript(t, script, 1, "[ONE]");
        runScript(t, script, 2, "[TWO]");
        runScript(t, script, 3, "[UNKNOWN(3)]");
        runScript(t, script, -1, "[UNKNOWN(-1)]");
    }

    @Test
    public static void testIfThenElseNested(Testing t) {
        String script = "[%?%p1%t%?%p2%tTT%eTF%;%e%?%p2%tFT%eFF%;%;]";
        runScript(t, script, new int[] {0, 0}, "[FF]");
        runScript(t, script, new int[] {0, 1}, "[FT]");
        runScript(t, script, new int[] {1, 0}, "[TF]");
        runScript(t, script, new int[] {1, 1}, "[TT]");
    }

    public static void runScript(Testing t,
        String script, String argument, String expected) {
        runScript(t, script, expected,
            (state, blob) -> state.runScript(
                blob, argument.getBytes(StandardCharsets.ISO_8859_1)));
    }

    public static void runScript(Testing t,
        String script, int argument, String expected) {
        runScript(t, script, expected,
            (state, blob) -> state.runScript(blob, argument));
    }

    public static void runScript(Testing t,
        String script, int[] argument, String expected) {
        runScript(t, script, expected,
            (state, blob) -> state.runScript(blob, argument));
    }

    public static void runScript(Testing t,
        String script, String expected) {
        runScript(t, script, expected, TerminalState::runScript);
    }

    public static void runScript(Testing t,
        String script, Class<? extends Throwable> exception,
        Consumer<? super Throwable> verifier) {
        runScript(t, script, "",
            (state, blob) -> t.expect(() -> state.runScript(blob),
                exception, verifier));
    }

    public static void runScript(Testing t,
        String script, String expected,
        BiConsumer<TerminalState, byte[]> invokeRunScript) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var terminalTarget = new TerminalTarget() {
                @Override
                public void write(int b) {
                    out.write(b);
                }

                @Override
                public void flush() {
                }

                @Override
                public void wait(int millis) {
                }
            };
        TerminalWriter writer = new TerminalWriter(terminalTarget,
            TermFile.of(TestTermFile.terminalBlob()));
        TerminalState state = new TerminalState(writer);
        invokeRunScript.accept(
            state, script.getBytes(StandardCharsets.ISO_8859_1));
        t.assertByteArrayEquals(
            expected.getBytes(StandardCharsets.ISO_8859_1),
            out.toByteArray());
    }
}
